package com.advancedtools.webservices.index;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.references.ClassReferenceThatReferencesAnyMethod;
import com.advancedtools.webservices.references.MemberReferenceThatKnowsAboutParentClassName;
import com.advancedtools.webservices.references.WSDDReferenceProvider;
import com.advancedtools.webservices.references.WSDLReferenceProvider;
import com.advancedtools.webservices.utils.XmlRecursiveElementVisitor;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.util.Map;
import java.util.Set;

/**
 * @author Maxim
*/
public class IndexEntry {
  static IndexEntry[] EMPTY = new IndexEntry[0];
  final VirtualFile file;
  final long modificationStamp;

  final Map<String,Object> myLinks = new THashMap<String, Object>(5);
  final Map<String,Object> myLinkTypes = new THashMap<String, Object>(5);
  final Set<String> mySymbols = new THashSet<String>(5);
  private String myLastClassQName;

  IndexEntry(final XmlFile _file) {
    file = _file.getVirtualFile();
    modificationStamp = _file.getModificationStamp();
    XmlTag rootTag = _file.getDocument().getRootTag();
    if (rootTag == null) return;

    final VirtualFile virtualFile = file;
    final boolean wsddFile = WebServicesPluginSettings.WSDD_FILE_EXTENSION.equals(virtualFile.getExtension());

    final boolean servicesXmlFile = WSIndex.isXFireWs(virtualFile);
    final boolean sunJaxWsXmlFile = WSIndex.isSunJaxWs(virtualFile);
    final boolean jaxRpcXmlFile = WSIndex.isJaxRpc(virtualFile);
    final boolean jaxRpcXmlFile2 = WSIndex.isJaxRpc2(virtualFile);
    final boolean cxf = WSIndex.isCxf(virtualFile);
    final boolean wsdl = WebServicesPluginSettings.WSDL_FILE_EXTENSION.equals(virtualFile.getExtension());

    rootTag.acceptChildren(
      new XmlRecursiveElementVisitor() {
        @Override public void visitXmlAttributeValue(XmlAttributeValue value) {
          final PsiReference[] references = value.getReferences();
          for(PsiReference r:references) {
            if (r instanceof WSDLReferenceProvider.WsdlClassReference) {
              addClass(r, _file);
            } else if (r instanceof WSDLReferenceProvider.WsdlMethodReference ||
                       r instanceof WSDLReferenceProvider.WsdlPropertyReference ||
                       r instanceof WSDDReferenceProvider.WSMethodReference
                      ) {
              addMethodOrField(r, value, _file);
            } else if (wsddFile || sunJaxWsXmlFile || jaxRpcXmlFile2 || cxf) {
              addClass(r, _file);
            } else if (servicesXmlFile) {
              addMethodOrField(r, value, _file);
            }
          }
        }

        @Override public void visitXmlTag(XmlTag xmlTag) {
          if (servicesXmlFile || jaxRpcXmlFile) {
            PsiReference[] references = xmlTag.getReferences();
            for(int i = 2; i < references.length; ++i) {
              addClass(references[i], _file);
            }
            super.visitXmlTag(xmlTag);
          } else if (sunJaxWsXmlFile || jaxRpcXmlFile2 || wsdl || cxf || wsddFile) {
            super.visitXmlTag(xmlTag);
          }
        }
      }
    );
  }

  private void addMethodOrField(PsiReference r, XmlAttributeValue value, PsiFile file) {
    PsiElement psiElement = r.resolve();

    if (psiElement == r.getElement()) {
      String className;
      if (r instanceof MemberReferenceThatKnowsAboutParentClassName) {
        className = ((MemberReferenceThatKnowsAboutParentClassName)r).getParentClassName();
      } else {
        className = myLastClassQName;
      }
      addMemberLink(className + "."+WSIndex.ANY_NAME, value, r, file);
      return;
    }

    if (!(psiElement instanceof PsiMember)) return;
    if (psiElement instanceof PsiClass) return;

    PsiMember psiMember = (PsiMember) psiElement;
    addMemberLink(WSIndex.getKey(psiMember), value, r, file);
  }

  private void addMemberLink(String name, PsiElement value, PsiReference r, PsiFile file) {
    myLinks.put(name,value.getTextRange());
    mySymbols.add(name);
    myLinkTypes.put(
      name,
      r instanceof WSDLReferenceProvider.WsdlPropertyReference ?
        (isWsdlFile(file) ? WSIndex.WS_PARAMETER_PROPERTY_TYPE : WSIndex.JAXB_PROPERTY_TYPE) :
        WSIndex.WS_METHOD
    );
  }

  private void addClass(PsiReference r, PsiFile file) {
    PsiElement psiElement = r.resolve();

    myLastClassQName = null;
    
    if (!(psiElement instanceof PsiClass)) return;

    PsiClass psiMember = (PsiClass) psiElement;
    String qualifiedName = psiMember.getQualifiedName();
    myLastClassQName = qualifiedName;
    final PsiElement element = r.getElement();
    myLinks.put(qualifiedName, element.getTextRange());
    mySymbols.add(psiMember.getQualifiedName());

    myLinkTypes.put(
      qualifiedName,
      getLinkType(element, file)
    );

    if (r instanceof ClassReferenceThatReferencesAnyMethod) {
      addMemberLink(qualifiedName + "."+WSIndex.ANY_NAME, element, r, file);
    }
  }

  private String getLinkType(PsiElement value, PsiFile file) {
    final XmlTag xmlTag = PsiTreeUtil.getParentOfType(value, XmlTag.class, false);
    String localName = xmlTag.getLocalName();

    if (localName.equals(WSDDReferenceProvider.PARAMETER_TAG_NAME)) {
      if (WSDDReferenceProvider.NAME_ATTR_VALUE2.equals(xmlTag.getAttributeValue(WSDDReferenceProvider.NAME_ATTR_NAME))) {
        return WSIndex.WS_TYPE;
      }
    } else if (localName.equals(WSDDReferenceProvider.BEANMAPPING_TAG_NAME)) {
      return WSIndex.WS_PARAMETER_TYPE;
    }

    return WSDLReferenceProvider.COMPLEX_TYPE_TAG_NAME.equals( localName ) ||
      WSDLReferenceProvider.PART_TAG_NAME.equals( localName )?
      isWsdlFile(file) ? WSIndex.WS_PARAMETER_TYPE : WSIndex.JAXB_TYPE :
      WSIndex.WS_TYPE;
  }

  private boolean isWsdlFile(PsiFile containingFile) {
    VirtualFile file = containingFile != null ? containingFile.getOriginalFile().getVirtualFile():null;

    return file != null && WebServicesPluginSettings.WSDL_FILE_EXTENSION.equals(file.getExtension());
  }

  public String getWsStatus(PsiMember c) {
    String qname = WSIndex.getKey(c);
    String s = (String) myLinkTypes.get(qname);

    if (s == null) {
      qname = WSIndex.getAnyKey(c);

      if (qname != null) {
        s = (String) myLinkTypes.get(qname);
      }
    }
    return s;
  }

  public VirtualFile getFile() {
    return file;
  }

  public TextRange getWsRange(PsiMember c) {
    String qname = WSIndex.getKey(c);
    TextRange textRange = (TextRange) myLinks.get(qname);

    if (textRange == null) {
      qname = WSIndex.getAnyKey(c);

      if (qname != null) {
        textRange = (TextRange) myLinks.get(qname);
      }
    }
    return textRange;
  }
}
