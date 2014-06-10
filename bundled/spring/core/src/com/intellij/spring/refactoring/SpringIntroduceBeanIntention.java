package com.intellij.spring.refactoring;

import com.intellij.codeInsight.completion.simple.SimpleLookupItem;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.template.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringManager;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringValueHolder;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry Avdeev
 */
public class SpringIntroduceBeanIntention implements IntentionAction {

  private final static Logger LOG = Logger.getInstance("#com.intellij.spring.refactoring.SpringIntroduceBeanIntention");

  @NotNull
  public String getText() {
    return SpringBundle.message("introduce.bean.intention");
  }

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    if(!(file instanceof XmlFile) || !SpringManager.getInstance(project).isSpringBeans((XmlFile)file)) return false;
    
    final SpringBean springBean = SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file);
    return springBean != null && springBean.getParent() instanceof SpringValueHolder;
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    final SpringBean springBean = SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file);
    moveToTheTopLevel(project, editor, springBean);
  }

  public static void moveToTheTopLevel(final Project project, final Editor editor, final SpringBean springBean) {
    if (springBean == null) return;
    final SpringBean topLevelBean = SpringUtils.getTopLevelBean(springBean);

    final SpringBean newBean = DomUtil.addElementAfter(topLevelBean);
    newBean.copyFrom(springBean);

    final String id = newBean.getId().getValue();
    if (id == null) {
      try {
        final XmlAttribute attribute = XmlElementFactory.getInstance(project).createXmlAttribute("id", "");
        final XmlTag tag = newBean.getXmlTag();
        final XmlAttribute[] attributes = tag.getAttributes();
        if (attributes.length > 0) {
          tag.addBefore(attribute, attributes[0]);
        } else {
          tag.add(attribute);
        }        
      }
      catch (IncorrectOperationException e) {
        LOG.error(e);
      }
    }

    final SpringValueHolder holder = (SpringValueHolder)springBean.getParent();
    assert holder != null;
    holder.getRefAttr().setStringValue(id == null ? "" : id);

    springBean.undefine();

    final XmlTag tag = holder.getXmlTag();
    tag.collapseIfEmpty();

    if (id != null) {
      return;
    }

    final SpringBean topLevelBeanCopy = topLevelBean.createStableCopy();
    final SpringBean newBeanCopy = newBean.createStableCopy();
    final SpringValueHolder holderCopy = holder.createStableCopy();

    final Document document = editor.getDocument();
    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);

    final int start = topLevelBeanCopy.getXmlTag().getTextOffset();
    final int end = newBeanCopy.getXmlTag().getTextRange().getEndOffset();

    final TemplateManager templateManager = TemplateManager.getInstance(project);
    final Template template = templateManager.createTemplate("", "");
    template.setToReformat(true);

    final String text = document.getText();
    final int refOffset = holderCopy.getRefAttr().getXmlAttributeValue().getTextOffset();
    final int idOffset = newBeanCopy.getId().getXmlAttributeValue().getTextOffset();

    template.addTextSegment(text.substring(start, refOffset));

    final String[] names = SpringUtils.suggestBeanNames(newBean);
    final Expression node = new Expression() {
      public Result calculateResult(final ExpressionContext context) {
        return null;
      }

      public Result calculateQuickResult(final ExpressionContext context) {
        return null;
      }

      public LookupElement[] calculateLookupItems(final ExpressionContext context) {
        return ContainerUtil.map2Array(names, LookupItem.class, new Function<String, LookupItem>() {
          public LookupItem fun(final String s) {
            return new SimpleLookupItem<String>(s);
          }
        });
      }
    };
    template.addVariable("id", node, node, true);
    template.addTextSegment(text.substring(refOffset, idOffset));
    template.addVariableSegment("id");
    template.addTextSegment(text.substring(idOffset, end));

    document.deleteString(start, end);

    templateManager.startTemplate(editor, template);
  }

  public boolean startInWriteAction() {
    return true;
  }
}
