/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.engine.HibernateEngine;
import com.intellij.hibernate.model.xml.mapping.HbmSqlQuery;
import com.intellij.javaee.JamMessages;
import com.intellij.javaee.model.common.persistence.mapping.NamedNativeQuery;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.jpa.QueryReferencesUtil;
import com.intellij.jpa.util.JpaUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.facet.PersistenceFacetBase;
import com.intellij.persistence.facet.PersistenceFacetConfiguration;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.persistence.model.PersistenceQuery;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ql.psi.QlFile;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public abstract class QueryIntentionActionBase extends PsiElementBaseIntentionAction {
  @NotNull
  public String getText() {
    return getFamilyName();
  }

  public boolean isAvailable(@NotNull final Project project, final Editor editor, @Nullable final PsiElement element) {
    if (element == null || element instanceof PsiWhiteSpace) return false;
    PsiFile file = element.getContainingFile();
    if (!(JamCommonUtil.isPlainJavaFile(file) || JamCommonUtil.isPlainXmlFile(file))) return false;
    final Module module = ModuleUtil.findModuleForPsiElement(file);
    if (module == null || PersistenceCommonUtil.getAllPersistenceFacetsWithDependencies(module).isEmpty()) return false;
    if (!HibernateEngine.isAvailable(module)) return false;
    return findQuery(editor, file, element) != null;
  }

  @Nullable
  private static String findQuery(final Editor editor, final PsiFile file, final PsiElement element) {
    final String result;
    if (JamCommonUtil.isPlainJavaFile(file)) {
      final String query;
      final PsiReference reference = file.findReferenceAt(editor.getCaretModel().getOffset());
      final QueryReferencesUtil.QueryReference queryReference =
        reference == null ? null : JpaUtil.findReferenceOfType(new PsiReference[]{reference}, QueryReferencesUtil.QueryReference.class);
      if (queryReference != null) {
        final PersistenceQuery persistenceQuery = queryReference.resolveQuery();
        query = persistenceQuery == null ? null : persistenceQuery.getQuery().getStringValue();
      }
      else {
        final PsiLiteralExpression expression = PsiTreeUtil.getParentOfType(element, PsiLiteralExpression.class);
        final QueryReferencesUtil.FileInjectionInfo injectionInfo;
        if (expression instanceof PsiLanguageInjectionHost && (injectionInfo = QueryReferencesUtil.getFileInjectionInfo(file))!= null &&
            injectionInfo.containsQuery(expression)) {
          final Ref<PsiFile> qlFileRef = new Ref<PsiFile>();
          ((PsiLanguageInjectionHost)expression).processInjectedPsi(new PsiLanguageInjectionHost.InjectedPsiVisitor() {
            public void visit(@NotNull final PsiFile injectedPsi, @NotNull final List<PsiLanguageInjectionHost.Shred> places) {
              if (injectedPsi instanceof QlFile) {
                qlFileRef.set(injectedPsi);
              }
            }
          });
          query = qlFileRef.isNull()? null : qlFileRef.get().getText();
        }
        else {
          query = null;
        }
      }
      result = query == null? null : StringUtil.unescapeStringCharacters(query);
    }
    else {
      final String query;
      final XmlTag xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
      final DomElement domElement = xmlTag == null? null : DomManager.getDomManager(file.getProject()).getDomElement(xmlTag);
      final PersistenceQuery domQuery = DomUtil.getParentOfType(domElement, PersistenceQuery.class, false);
      if (!(domQuery instanceof NamedNativeQuery || domQuery instanceof HbmSqlQuery)) {
        query = domQuery == null? null : domQuery.getQuery().getStringValue();
      }
      else {
        query = null;
      }
      result = query == null? null : StringUtil.unescapeXml(query);
    }
    return result;
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    final Module module = ModuleUtil.findModuleForPsiElement(file);
    final List<Pair<PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>, PersistencePackage>> pairs =
      new ArrayList<Pair<PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>, PersistencePackage>>();
    final List<PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>> facets =
      PersistenceCommonUtil.getAllPersistenceFacetsWithDependencies(module);

    for (PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> facet : facets) {
      if (facet.getQlLanguage() == null) continue;
      for (PersistencePackage unit : facet.getPersistenceUnits()) {
        pairs.add(Pair.create(facet, unit));
      }
    }
    if (pairs.isEmpty()) return;
    final String query = findQuery(editor, file, file.findElementAt(editor.getCaretModel().getOffset()));
    if (pairs.size() == 1) {
      invokeInner(project, editor, pairs.get(0).getFirst(), pairs.get(0).getSecond(), query);
    }
    else {
      Collections.sort(pairs, new Comparator<Pair<PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>, PersistencePackage>>() {
        public int compare(final Pair<PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>, PersistencePackage> o1,
                           final Pair<PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>, PersistencePackage> o2) {
          final int modules = Comparing.compare(o1.getFirst().getModule().getName(), o2.getFirst().getModule().getName());
          if (modules != 0) return modules;
          return Comparing.compare(o1.getSecond().getName().getValue(), o2.getSecond().getName().getValue());
        }
      });
      final JList list = new JList(pairs.toArray());
      list.setCellRenderer(new ColoredListCellRenderer() {
        protected void customizeCellRenderer(final JList list,
                                             final Object value, final int index, final boolean selected, final boolean hasFocus) {
          final Pair<PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>, PersistencePackage> pair =
            (Pair<PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>, PersistencePackage>)value;
          setIcon(ElementPresentationManager.getIcon(pair.getSecond()));
          final String unitName = pair.getSecond().getName().getValue();
          final String text = pair.getFirst().getModule().getName() + "/" +
                               (StringUtil.isNotEmpty(unitName) ? unitName : JamMessages.message("unnamed.element.presentable.name"));
          append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
      });
      new PopupChooserBuilder(list).
        setTitle(HibernateMessages.message("title.choose.session.factory")).
        setItemChoosenCallback(new Runnable() {
          public void run() {
            final Object value = list.getSelectedValue();
            final Pair<PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>, PersistencePackage> pair =
              (Pair<PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>, PersistencePackage>)value;
            if (pair != null) {
              invokeInner(project, editor, pair.getFirst(), pair.getSecond(), query);
            }
          }
        }).
        createPopup().showInBestPositionFor(editor);
    }
  }

  protected abstract void invokeInner(final Project project, final Editor editor, final PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> curFacet,
                                      final PersistencePackage curUnit,
                                      final String query);


  public boolean startInWriteAction() {
    return false;
  }

}