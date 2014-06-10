package com.intellij.spring.model.highlighting;

import com.intellij.ProjectTopics;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.facet.FacetManager;
import com.intellij.ide.DataManager;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringManager;
import com.intellij.spring.facet.FileSetEditor;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.facet.SpringFacetType;
import com.intellij.spring.facet.SpringFileSet;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomElementAnnotationsManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
public class SpringFacetInspection extends SpringBeanInspectionBase {

  private JPanel myOptionsPanel;
  private JCheckBox myCheckBox;

  public boolean checkTestFiles = false;

  public SpringFacetInspection() {
    myCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        checkTestFiles = myCheckBox.isSelected();
      }
    });
  }

  public void checkFileElement(final DomFileElement<Beans> domFileElement, final DomElementAnnotationHolder holder) {
    final Module module = domFileElement.getModule();
    if (module == null) {
      return;
    }
    final VirtualFile virtualFile = domFileElement.getFile().getVirtualFile();
    if (virtualFile == null) {
      return;
    }
    final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(module.getProject()).getFileIndex();
    if (!projectFileIndex.isInSourceContent(virtualFile) ||
        (!checkTestFiles && projectFileIndex.isInTestSourceContent(virtualFile))) {
      return;
    }
    final Ref<SpringFacet> facetRef = new Ref<SpringFacet>();
    final boolean notFound = ModuleUtil.visitMeAndDependentModules(module, new ModuleUtil.ModuleVisitor() {
      public boolean visit(final Module module) {
        final SpringFacet facet = SpringFacet.getInstance(module);
        if (facet != null) {
          facetRef.set(facet);
          final Set<SpringFileSet> sets = SpringManager.getInstance(module.getProject()).getAllSets(facet);
          for (SpringFileSet fileSet : sets) {
            if (fileSet.hasFile(virtualFile)) {
              return false;
            }
          }

        }
        return true;
      }
    });
    if (!notFound) {
      return;
    }
    final SpringFacet springFacet = facetRef.get();
    if (springFacet == null) {
      holder.createProblem(domFileElement, HighlightSeverity.WARNING,
                           SpringBundle.message("spring.facet.not.configured.for.module", module.getName()),
                           new AddFacetFix(module, domFileElement.getFile()));
    } else {
      holder.createProblem(domFileElement, HighlightSeverity.WARNING,
                           SpringBundle.message("file.set.not.configured.for.file"),
                           new ConfigureFileSetFix(springFacet.getModule(), domFileElement.getFile()));

    }
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("spring.facet.inspection");
  }

  @Override
  public JComponent createOptionsPanel() {
    myCheckBox.setSelected(checkTestFiles);
    return myOptionsPanel;
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "SpringFacetInspection";
  }

  private static class ConfigureFileSetFix extends AddFacetFix {

    protected ConfigureFileSetFix(@NotNull Module module, PsiFile file) {
      super(module, file);
    }

    @NotNull
    public String getName() {
      return SpringBundle.message("configure.file.set.for.file");
    }

    @Override
    protected void doFix(final Project project) {
      final SpringFacet facet = SpringFacet.getInstance(myModule);
      if (facet != null) {
        final Set<SpringFileSet> sets = facet.getConfiguration().getFileSets();
        if (sets.size() == 0) {
          addNewSet(facet, sets);
        }
        else {
          final ArrayList<SpringFileSet> list = new ArrayList<SpringFileSet>(sets);
          final SpringFileSet newSet = new SpringFileSet(SpringFileSet.getUniqueId(sets),
                                                         SpringBundle.message("fileset.new"), facet.getConfiguration()) {
            public boolean isNew() {
              return true;
            }
          };
          list.add(newSet);
          final BaseListPopupStep<SpringFileSet> step =
            new BaseListPopupStep<SpringFileSet>(SpringBundle.message("choose.file.set"), list) {
              public PopupStep onChosen(final SpringFileSet selectedValue, final boolean finalChoice) {
                if (selectedValue == newSet) {
                  final String name = SpringFileSet.getUniqueName(SpringBundle.message("default.fileset.name"), sets);
                  newSet.setName(name);
                  editSet(facet, sets, newSet);
                }
                else {
                  selectedValue.addFile(myVirtualFile);
                  ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                      ProjectRootManagerEx.getInstanceEx(project).beforeRootsChange(false);
                      ProjectRootManagerEx.getInstanceEx(project).rootsChanged(false);
                    }
                  });
                }
                return super.onChosen(selectedValue, finalChoice);
              }
            };
          JBPopupFactory.getInstance().createListPopup(step).showInBestPositionFor(DataManager.getInstance().getDataContext());
        }
      }
    }
  }

  private static class AddFacetFix implements LocalQuickFix, IntentionAction {
    protected final Module myModule;
    protected final VirtualFile myVirtualFile;

    protected AddFacetFix(@NotNull Module module, PsiFile file) {
      myModule = module;
      myVirtualFile = file.getVirtualFile();
    }

    @NotNull
    public String getName() {
      return SpringBundle.message("add.spring.facet", myModule.getName());
    }

    @NotNull
    public String getText() {
      return getName();
    }

    @NotNull
    public String getFamilyName() {
      return SpringBundle.message("model.bean.quickfix.family");
    }

    public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
      return true;
    }

    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      doFix(project);
      DomElementAnnotationsManager.getInstance(project).dropAnnotationsCache();
      DaemonCodeAnalyzer.getInstance(project).restart();
    }

    public boolean startInWriteAction() {
      return false;
    }

    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      doFix(project);
      DomElementAnnotationsManager.getInstance(project).dropAnnotationsCache();
      DaemonCodeAnalyzer.getInstance(project).restart();
    }

    protected void doFix(final Project project) {
      final SpringFacet facet = new WriteCommandAction<SpringFacet>(project, getName()) {
        protected void run(final Result<SpringFacet> springFacetResult) throws Throwable {
          final SpringFacet facet =
            FacetManager.getInstance(myModule).addFacet(SpringFacetType.INSTANCE, SpringFacetType.INSTANCE.getPresentableName(), null);
          springFacetResult.setResult(facet);

        }
      }.execute().getResultObject();
      final Set<SpringFileSet> sets = SpringManager.getInstance(project).getAllSets(facet);
      for (SpringFileSet fileSet : sets) {
        if (fileSet.hasFile(myVirtualFile)) {
          return;
        }
      }
      addNewSet(facet, sets);
    }

    protected void addNewSet(final SpringFacet facet, final Set<SpringFileSet> sets) {
      final SpringFileSet set =
        new SpringFileSet(SpringFileSet.getUniqueId(sets),
                          SpringFileSet.getUniqueName(SpringBundle.message("default.fileset.name"), sets),
                          facet.getConfiguration()) {
          public boolean isNew() {
            return true;
          }
        };
      editSet(facet, sets, set);
    }

    protected void editSet(final SpringFacet facet, final Set<SpringFileSet> sets, final SpringFileSet set) {
      set.addFile(myVirtualFile);
      final FileSetEditor editor = new FileSetEditor(myModule, set, sets);
      editor.show();
      if (editor.isOK()) {
        facet.getConfiguration().getFileSets().add(editor.getEditedFileSet());
        myModule.getProject().getMessageBus().syncPublisher(ProjectTopics.LOGICAL_ROOTS).logicalRootsChanged();
      }
    }
  }
}
