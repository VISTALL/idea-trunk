/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.StdLanguages;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.addins.js.JavaScriptIntegrationUtil;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.GspDirectiveKind;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.GspPsiElementFactory;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspScriptletTag;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirective;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspXmlRootTag;
import org.jetbrains.plugins.grails.lang.gsp.psi.html.impl.GspHtmlFileImpl;
import org.jetbrains.plugins.grails.lang.gsp.util.GspDirectiveUtil;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;

import java.util.*;

/**
 * @author ilyas
 */
public class GspFileImpl extends PsiFileImpl implements GspFile {

  private Map<GspDirectiveKind, GspDirective[]> myDirectives = null;

  public GspFileImpl(FileViewProvider viewProvider) {
    super(GspElementTypes.GSP_FILE, GspElementTypes.GSP_FILE, viewProvider);
  }

  public String toString() {
    return "Groovy Server Pages file";
  }

  public Lexer createLexer() {
    return null;
  }

  public GroovyFileBase getGroovyLanguageRoot() {
    PsiFile psiFile = getViewProvider().getPsi(GroovyFileType.GROOVY_FILE_TYPE.getLanguage());
    assert psiFile instanceof GroovyFileBase;
    return ((GroovyFileBase) psiFile);
  }

  @NotNull
  public String[] getImportedClassesNames() {
    ArrayList<String> importedClasses = new ArrayList<String>();
    for (String st : getImportStrings()) {
      String candidate = st.trim();
      if (candidate.matches("(\\w+\\.)*\\w+") ||
          candidate.matches("(\\w+\\.)*\\w+\\s+as\\s+\\w+")) {
        importedClasses.add(candidate);
      }
    }
    return importedClasses.toArray(new String[importedClasses.size()]);
  }

  @NotNull
  public String[] getImportedPackagesNames() {
    ArrayList<String> importedPackages = new ArrayList<String>();
    for (String st : getImportStrings()) {
      String candidate = st.replaceAll(" ", "").replaceAll("\n", "");
      if (candidate.matches("(\\w+\\.)+\\*")) {
        if (candidate.contains(".")) {
          candidate = candidate.substring(0, candidate.lastIndexOf("."));
        }
        importedPackages.add(candidate);
      }
    }
    return importedPackages.toArray(new String[importedPackages.size()]);
  }

  private String[] getImportStrings() {
    ArrayList<String> values = new ArrayList<String>();
    GspDirective[] directives = getDirectiveTags(GspDirectiveKind.PAGE, true);
    for (GspDirective directive : directives) {
      XmlAttribute attribute = directive.getAttribute("import");
      if (attribute != null) {
        String value = attribute.getValue();
        if (value != null) values.addAll(Arrays.asList(value.split(";")));
      }
    }
    return values.toArray(new String[values.size()]);
  }

  public void clearCaches() {
    super.clearCaches();
    myDirectives = null;
  }


  public void accept(@NotNull PsiElementVisitor visitor) {
    visitor.visitFile(this);
  }

  public GspDirective[] getDirectiveTags(final GspDirectiveKind directiveKind, final boolean searchInIncludes) {
    if (searchInIncludes) {
      //todo implement me!
    }
    if (myDirectives != null) {
      final GspDirective[] directives = myDirectives.get(directiveKind);
      return directives == null ? GspDirective.EMPTY_ARRAY : directives;
    }
    final Map<GspDirectiveKind, List<GspDirective>> directivesMap = new HashMap<GspDirectiveKind, List<GspDirective>>();
    XmlUtil.processXmlElements(getRootTag(), new PsiElementProcessor() {
      public boolean execute(final PsiElement element) {
        if (element instanceof GspDirective) {
          final GspDirective directive = (GspDirective) element;
          final GspDirectiveKind directiveKindByTag = GspDirectiveUtil.getDirectiveKindByTag(directive);
          if (directiveKindByTag != null) {
            List<GspDirective> directives = directivesMap.get(directiveKindByTag);
            if (directives == null)
              directivesMap.put(directiveKindByTag, directives = new ArrayList<GspDirective>());
            directives.add(directive);
          }
        }
        return true;
      }
    }, true);
    final Map<GspDirectiveKind, GspDirective[]> directives = new HashMap<GspDirectiveKind, GspDirective[]>();
    for (Map.Entry<GspDirectiveKind, List<GspDirective>> entry : directivesMap.entrySet()) {
      directives.put(entry.getKey(), entry.getValue().toArray(new GspDirective[entry.getValue().size()]));
    }
    myDirectives = directives;
    return getDirectiveTags(directiveKind, false);
  }

  public void addImportForClass(PsiClass aClass) throws IncorrectOperationException {
    addImport(aClass.getQualifiedName());
  }

  public void addImportStatement(GrImportStatement statement) {
    //todo get import string
  }

  public PsiElement createGroovyScriptletFromText(String text) throws IncorrectOperationException {
    GspPsiElementFactory factory = GspPsiElementFactory.getInstance(getProject());
    GspScriptletTag script = factory.createScriptletTagFromText(text);
    GspXmlRootTag rootTag = getRootTag();
    assert rootTag != null;
    PsiElement firstChild = rootTag.getFirstChild();
    if (firstChild != null) {
      rootTag.addBefore(script, firstChild);
    } else {
      rootTag.add(script);
    }
    return script;
  }

  public GspXmlRootTag getRootTag() {
    XmlDocument document = getDocument();
    assert document != null;
    PsiElement child = document.getFirstChild();
    assert child != null;
    return ((GspXmlRootTag) child.getNextSibling());
  }

  @NotNull
  public FileViewProvider getViewProvider() {
    return super.getViewProvider();
  }

  private void addImport(String importString) throws IncorrectOperationException {
    GspDirective directive = calculatePositionForImport();
    GspPsiElementFactory factory = GspPsiElementFactory.getInstance(getProject());
    if (directive != null) {
      XmlAttribute importAttribute = directive.getAttribute("import");
      if (importAttribute != null) {
        String oldValue = importAttribute.getValue();
        String newValue = importString + "; " + oldValue;
        directive.addOrReplaceAttribute(factory.createDirectiveAttribute("import", newValue));
      } else {
        directive.addOrReplaceAttribute(factory.createDirectiveAttribute("import", importString));
      }
    } else {
      GspDirective newDirective = factory.createDirectiveByKind(GspDirectiveKind.PAGE);
      newDirective.addOrReplaceAttribute(factory.createDirectiveAttribute("import", importString));
      GspXmlRootTag rootTag = getRootTag();
      assert rootTag != null;
      PsiElement firstChild = rootTag.getFirstChild();
      if (firstChild != null) {
        rootTag.addBefore(newDirective, firstChild);
      } else {
        rootTag.add(newDirective);
      }
    }
  }

  private GspDirective calculatePositionForImport() {
    GspDirective[] directives = getDirectiveTags(GspDirectiveKind.PAGE, false);
    if (directives.length == 0) return null;
    for (GspDirective directive : directives) {
      if (directive.getAttribute("import") != null) return directive;
    }
    return directives[0];
  }

  @NotNull
  public FileType getFileType() {
    return GspFileType.GSP_FILE_TYPE;
  }

  @Nullable
  public XmlDocument getDocument() {
    CompositeElement treeElement = calcTreeElement();

    ASTNode[] astNodes = treeElement.getChildren(null);
    if (astNodes.length > 0) {
      final PsiElement asPsiElement = astNodes[0].getPsi();
      if (asPsiElement instanceof XmlDocument) {
        return (XmlDocument) asPsiElement;
      }
    }
    return null;
  }

  public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
    if (!super.processDeclarations(processor, state, lastParent, place)) return false;

    // JavaScript support
    if (JavaScriptIntegrationUtil.isJSElement(place)) {
      GspHtmlFileImpl htmlFile = getHtmlLanguageRoot();
      return htmlFile != null && htmlFile.processDeclarations(processor, state, lastParent, place);
    }

    return true;
  }

  public boolean processElements(PsiElementProcessor processor, PsiElement place) {
    final XmlDocument document = getDocument();
    return document == null || document.processElements(processor, place);
  }

  private GspHtmlFileImpl getHtmlLanguageRoot() {
    PsiFile psiFile = getViewProvider().getPsi(StdLanguages.HTML);
    assert psiFile instanceof GspHtmlFileImpl;
    return ((GspHtmlFileImpl) psiFile);
  }

  public GlobalSearchScope getFileResolveScope() {
    Module module = ModuleUtil.findModuleForPsiElement(this);
    VirtualFile file = getVirtualFile();
    if (file != null && getOriginalFile() != null) {
      file = getOriginalFile().getVirtualFile();
    }
    if (module != null && file != null) {
      ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(getProject()).getFileIndex();
      boolean includeTests = projectFileIndex.isInTestSourceContent(file) ||
                             !projectFileIndex.isContentJavaSourceFile(file);
      return GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, includeTests);
    }

    return ProjectScope.getAllScope(getProject());
  }
}
