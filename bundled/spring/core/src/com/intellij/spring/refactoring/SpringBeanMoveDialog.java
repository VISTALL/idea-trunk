package com.intellij.spring.refactoring;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.xml.XmlFile;
import com.intellij.refactoring.ui.RefactoringDialog;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.SpringModelVisitor;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.ui.EditorComboBox;
import com.intellij.ui.RecentsManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class SpringBeanMoveDialog extends RefactoringDialog {

  private final SpringBean mySpringBean;
  private JPanel myPanel;
  private ComboboxWithBrowseButton myFileCombo;
  private JLabel myMessage;

  @NonNls private static final String SPRING_CONFIG_FILE_RECENTS = "spring.config.file.recents";
  public static final Logger LOG = Logger.getInstance("#com.intellij.spring.refactoring.SpringBeanMoveDialog");

  public SpringBeanMoveDialog(@NotNull final Project project, SpringBean springBean) {
    super(project, true);
    setTitle(SpringBundle.message("move.bean"));
    myMessage.setText(SpringBundle.message("move.bean.name", springBean.getBeanName()));
    mySpringBean = springBean;

    init();

    final PsiFile psiFile = springBean.getContainingFile();
    final List<String> list = RecentsManager.getInstance(project).getRecentEntries(SPRING_CONFIG_FILE_RECENTS);
    if (list != null) {
      final List<String> recentEntries = new ArrayList<String>(list);
      if (psiFile != null) {
        final VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile != null) {
          recentEntries.remove(virtualFile.getPath());
        }
      }
      ((EditorComboBox)myFileCombo.getComboBox()).setHistory(ArrayUtil.toStringArray(recentEntries));
    }
    myFileCombo.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final ConfigFileChooser chooser = new ConfigFileChooser(project, psiFile);
        chooser.show();
        final XmlFile selectedFile = chooser.getSelectedFile();
        if (selectedFile != null) {
          final VirtualFile virtualFile = selectedFile.getVirtualFile();
          assert virtualFile != null;
          final String path = virtualFile.getPath();
          final DefaultComboBoxModel model = (DefaultComboBoxModel)myFileCombo.getComboBox().getModel();
          if (model.getIndexOf(path) < 0) {
            model.addElement(path);
          }
          model.setSelectedItem(path);
          pack();
        }
      }
    });
  }

  @Nullable
  private XmlFile getTargetFile() {
    final String path = (String)myFileCombo.getComboBox().getSelectedItem();
    if (path != null) {
      final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtil.pathToUrl(path.trim()));
      if (virtualFile != null) {
        final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile);
        return psiFile instanceof XmlFile ? (XmlFile)psiFile : null;
      }
    }
    return null;
  }

  protected boolean areButtonsValid() {
    return getTargetFile() != null;
  }

  protected Action[] createActions() {
    return new Action[]{getRefactorAction(), getCancelAction()};
  }

  protected void doAction() {
    final XmlFile file = getTargetFile();
    if (file != null) {
      doMove(file, mySpringBean, myProject);
      final VirtualFile virtualFile = file.getVirtualFile();
      assert virtualFile != null;
      RecentsManager.getInstance(myProject).registerRecentEntry(SPRING_CONFIG_FILE_RECENTS, virtualFile.getPath());
    }
    close(OK_EXIT_CODE);
  }

  public static void doMove(final XmlFile file, final SpringBean springBean, final Project project) {
    final PsiFile psiFile = springBean.getXmlTag().getContainingFile();
    new WriteCommandAction.Simple(project, SpringBundle.message("move.bean"), psiFile, file) {
      protected void run() throws Throwable {

        final DomFileElement<Beans> fileElement = DomManager.getDomManager(project).getFileElement(file, Beans.class);
        assert fileElement != null;
        final Beans beans = fileElement.getRootElement();
        final SpringBean bean = beans.addBean();

        SpringModelVisitor.visitBean(new SpringModelVisitor() {
          protected boolean visitRef(final SpringRef ref) {
            visitRefBase(ref);
            return super.visitRef(ref);
          }

          protected boolean visitIdref(final Idref idref) {
            visitRefBase(idref);
            return super.visitIdref(idref);
          }

          private void visitRefBase(RefBase refBase) {
            final String local = refBase.getLocal().getStringValue();
            if (local != null) {
              refBase.getBean().setStringValue(local);
              refBase.getLocal().undefine();
            }
          }
        }, springBean);

        bean.copyFrom(springBean);

        for (PsiReference psiReference : ReferencesSearch.search(springBean.getXmlTag())) {
          try {
            psiReference.bindToElement(bean.getXmlTag());
          }
          catch (IncorrectOperationException e) {
            LOG.error("Can't bind " + psiReference + " to " + bean, e);
          }
        }

        springBean.undefine();
      }
    }.execute();
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }

  private void createUIComponents() {
    myFileCombo = new ComboboxWithBrowseButton(new EditorComboBox(""));
  }
}
