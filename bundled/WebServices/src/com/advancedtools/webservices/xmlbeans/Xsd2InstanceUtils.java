package com.advancedtools.webservices.xmlbeans;

import com.advancedtools.webservices.utils.XmlRecursiveElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import com.intellij.xml.impl.schema.XmlNSDescriptorImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Maxim
 */
public class Xsd2InstanceUtils {
  public static XmlElementDescriptor getDescriptor(XmlTag tag, String elementName) {
    final PsiMetaData metaData = tag.getMetaData();

    if (metaData instanceof XmlNSDescriptorImpl) {
      final XmlNSDescriptorImpl nsDescriptor = (XmlNSDescriptorImpl) metaData;
      return nsDescriptor.getElementDescriptor(elementName, nsDescriptor.getDefaultNamespace());
    }

    return null;
  }
  
  public static List<String> addVariantsFromRootTag(XmlTag rootTag) {
    PsiMetaData metaData = rootTag.getMetaData();
    if (metaData instanceof XmlNSDescriptorImpl) {
      XmlNSDescriptorImpl nsDescriptor = (XmlNSDescriptorImpl) metaData;

      List<String> elementDescriptors = new ArrayList<String>();
      XmlElementDescriptor[] rootElementsDescriptors = nsDescriptor.getRootElementsDescriptors(PsiTreeUtil.getParentOfType(rootTag, XmlDocument.class));
      for(XmlElementDescriptor e:rootElementsDescriptors) {
        elementDescriptors.add(e.getName());
      }

      return elementDescriptors;
    }
    return Collections.emptyList();
  }

  public static String processAndSaveAllSchemas(@NotNull XmlFile file, @NotNull final Map<String, String> scannedToFileName,
                                          final @NotNull SchemaReferenceProcessor schemaReferenceProcessor) {
    final String fileName = file.getName();

    String previous = scannedToFileName.get(fileName);

    if (previous != null) return previous;

    scannedToFileName.put(fileName, fileName);

    final StringBuilder result = new StringBuilder();

    file.acceptChildren(new XmlRecursiveElementVisitor() {
      @Override public void visitElement(PsiElement psiElement) {
        super.visitElement(psiElement);
        if (psiElement instanceof LeafPsiElement) {
          final String text = psiElement.getText();
          result.append(text);
        }
      }

      @Override public void visitXmlAttribute(XmlAttribute xmlAttribute) {
        boolean replaced = false;

        if (xmlAttribute.isNamespaceDeclaration()) {
          replaced = true;
          final String value = xmlAttribute.getValue();
          result.append(xmlAttribute.getText()).append(" ");

          if (!scannedToFileName.containsKey(value)) {
            final XmlNSDescriptor nsDescriptor = xmlAttribute.getParent().getNSDescriptor(value, true);

            if (nsDescriptor != null) {
              processAndSaveAllSchemas(nsDescriptor.getDescriptorFile(), scannedToFileName, schemaReferenceProcessor);
            }
          }
        } else if ("schemaLocation".equals(xmlAttribute.getName())) {
          final PsiReference[] references = xmlAttribute.getValueElement().getReferences();

          if (references.length > 0) {
            PsiElement psiElement = references[0].resolve();

            if (psiElement instanceof XmlFile) {
              final String s = processAndSaveAllSchemas(((XmlFile) psiElement), scannedToFileName, schemaReferenceProcessor);
              if (s != null) {
                result.append(xmlAttribute.getName()).append("='").append(s).append('\'');
                replaced = true;
              }
            }
          }
        }
        if (!replaced) result.append(xmlAttribute.getText());
      }
    });

    schemaReferenceProcessor.processSchema(fileName, result.toString());
    return fileName;
  }

  public interface SchemaReferenceProcessor {
    void processSchema(String schemaFileName, String schemaContent);
  }
}
