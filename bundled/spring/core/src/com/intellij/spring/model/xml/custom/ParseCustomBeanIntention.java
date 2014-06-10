package com.intellij.spring.model.xml.custom;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.CustomBeanInfo;
import com.intellij.spring.CustomBeanRegistry;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.unscramble.UnscrambleDialog;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author peter
 */
public class ParseCustomBeanIntention implements IntentionAction {

  @NotNull
  public String getText() {
    return SpringBundle.message("parse.custom.bean.intention");
  }

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    return DomUtil.findDomElement(file.findElementAt(editor.getCaretModel().getOffset()), DomSpringBean.class) instanceof CustomBeanWrapper;
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    final CustomBeanWrapper wrapper =
        DomUtil.findDomElement(file.findElementAt(editor.getCaretModel().getOffset()), CustomBeanWrapper.class);
    assert wrapper != null;
    invokeCustomBeanParsers(project, Arrays.asList(wrapper.getXmlTag()));
  }

  public static void invokeCustomBeanParsers(final Project project, final Collection<XmlTag> tags) {
    final Ref<CustomBeanRegistry.ParseResult> ref = Ref.create(null);
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true);
        ApplicationManager.getApplication().runReadAction(new Runnable() {
          public void run() {
            ref.set(CustomBeanRegistry.getInstance(project).parseBeans(tags));
          }
        });
      }
    }, SpringBundle.message("parsing.custom.bean"), false, project);

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        ((PsiModificationTrackerImpl)PsiManager.getInstance(project).getModificationTracker()).incCounter();
      }
    });
    DaemonCodeAnalyzer.getInstance(project).restart();

    if (ApplicationManager.getApplication().isUnitTestMode()) return; //the rest deals only with pretty feedback to user 

    final CustomBeanRegistry.ParseResult result = ref.get();

    final String message = result.getErrorMessage();
    if (message != null) {
      Messages.showErrorDialog(project, message, SpringBundle.message("parse.custom.bean.error"));
      return;
    }

    final String trace = result.getStackTrace();
    if (trace != null) {
      String shortTrace = trace;
      int i = 0;
      for (int j = 0; j < 10; j++) {
        if (i >= 0) {
          i = trace.indexOf('\n', i + 1);
        }
      }
      if (i >= 0) {
        shortTrace = trace.substring(0, i) + "\n\t...";
      }

      final int exitCode = Messages.showDialog(project, shortTrace, SpringBundle.message("parse.custom.bean.error"),
                                               new String[]{CommonBundle.getOkButtonText(),
                                                   ActionsBundle.message("action.Unscramble.text")}, 0, Messages.getErrorIcon());
      if (exitCode == 1) {
        final UnscrambleDialog dialog = new UnscrambleDialog(project);
        dialog.setText(trace);
        dialog.show();
      }
      return;
    }

    final List<CustomBeanInfo> infos = result.getBeans();
    assert infos != null;
    final String beansText = StringUtil.join(infos, new Function<CustomBeanInfo, String>() {
      @NonNls
      public String fun(final CustomBeanInfo customBeanInfo) {
        return "  id = " + customBeanInfo.beanName + "; class = " + customBeanInfo.beanClassName;
      }
    }, "\n");

    if (infos.size() == 1 && tags.size() == 1) {
      final String idAttr = infos.get(0).idAttribute;
      if (idAttr != null) {
        final XmlTag tag = tags.iterator().next();
        final String ns = tag.getNamespace();
        final String localName = tag.getLocalName();
        final String inductMessage = SpringBundle.message("parse.these.beans.induct", beansText, ns, localName, idAttr);
        if (Messages.showDialog(project, inductMessage, SpringBundle.message("parse.custom.bean.success"),
                                new String[]{CommonBundle.getYesButtonText(), SpringBundle.message("parse.these.beans.induct.only.this")}, 1,
                                Messages.getInformationIcon()) == 0) {
          final CustomBeanInfo beanInfo = new CustomBeanInfo(infos.get(0));
          beanInfo.beanName = null;
          CustomBeanRegistry.getInstance(project).addBeanPolicy(ns, localName, beanInfo);
        }
        return;
      }
    }

    final String parsedMessage = !infos.isEmpty()
                                 ? SpringBundle.message("parse.these.beans", beansText)
                                 : result.hasInfrastructureBeans()
                                   ? SpringBundle.message("parse.only.infrastructure.beans")
                                   : SpringBundle.message("parse.no.custom.beans");
    Messages.showInfoMessage(project, parsedMessage, SpringBundle.message("parse.custom.bean.success"));
  }

  public boolean startInWriteAction() {
    return false;
  }
}