package com.advancedtools.webservices.index;

import com.advancedtools.webservices.WebServicesPluginSettings;
import static com.advancedtools.webservices.index.FileBasedWSIndex.*;
import com.advancedtools.webservices.references.ClassReferenceThatReferencesAnyMethod;
import com.advancedtools.webservices.references.MemberReferenceThatKnowsAboutParentClassName;
import com.advancedtools.webservices.references.WSDDReferenceProvider;
import com.advancedtools.webservices.references.WSDLReferenceProvider;
import com.advancedtools.webservices.utils.XmlRecursiveElementVisitor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.xml.NanoXmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

/**
 * @by Konstantin Bulenkov
 */
public class WSIndexEntry {
  public static final WSIndexEntry[] EMPTY = new WSIndexEntry[0];
  String myFileUrl;
  String myLastClassQName;
  private boolean resolved = false;

  Set<String> myCandidates = new HashSet<String>();
  Set<String> mySymbols = new HashSet<String>(5);
  Map<String, String> myLinkTypes = new HashMap<String, String>(5);
  Map<String, WSTextRange> myLinks = new HashMap<String, WSTextRange>(5);
  int hashCode;

  private VirtualFile file = null;
  private long myJavaModCounter = -1;

  WSIndexEntry(final FileContent content) throws IOException {
    hashCode = content.getContentAsText().toString().hashCode();
    myFileUrl = content.getFile().getUrl();
    NanoXmlUtil.parse(new ByteArrayInputStream(content.getContent()), new NanoXmlUtil.BaseXmlBuilder() {
      @Override
      public void addAttribute(final String key, final String nsPrefix, final String nsURI, final String value, final String type)
          throws Exception {
        if (isFQNLike(value)) {
          myCandidates.add(getClassName(value));
        }
      }

      final char[] pcdata = new char[96]; // skip FQNs longer than 96 symbols
      @Override
      public void addPCData(final Reader reader, final String systemID, final int lineNr) throws Exception {
        int read = reader.read(pcdata);
        String candidate = new StringBuilder().append(pcdata, 0, read).toString();
        if (isFQNLike(candidate)) myCandidates.add(getClassName(candidate));
      }
    });
  }

  private static String getClassName(final @NotNull String value) {
    List<String> tokens = StringUtil.split(value, ".");
    return tokens.get(tokens.size() - 1);
  }
 
  private static boolean isFQNLike(final String value) {
    if (value == null ||
        value.length() == 0 ||
        value.charAt(0) == '.' ||
        value.charAt(value.length() - 1) == '.'
       ) {
      return false;
    }

    for (String clazz : StringUtil.split(value, ".")) {
      if (! isClassName(clazz)) return false;
    }

    return true;
  }

  private static boolean isClassName(final String name) {
    if(name == null || name.length() == 0) return false;
    if (! Character.isJavaIdentifierStart(name.charAt(0))) return false;
    for (int i = 1; i < name.length(); i++) {
      if (! Character.isJavaIdentifierPart(name.charAt(i))) return false;
    }
    return true;
  }

  public boolean isResolved(final Project project) {
    final long count = PsiManager.getInstance(project).getModificationTracker().getJavaStructureModificationCount();
    return myJavaModCounter == count;
  }

  public void resolve(Project project) {
    VirtualFile vf = getFile();
    if (vf == null) return;
    final PsiFile psifile = PsiManager.getInstance(project).findFile(vf);
    if (! (psifile instanceof XmlFile)) return;
    file = psifile.getVirtualFile();
    final XmlFile xml = (XmlFile)psifile;
    final XmlDocument doc = xml.getDocument();
    if (doc == null) return;
    XmlTag rootTag = doc.getRootTag();
    if (rootTag == null) return;

    final VirtualFile virtualFile = file;
    final boolean wsddFile = WebServicesPluginSettings.WSDD_FILE_EXTENSION.equals(virtualFile.getExtension());

    final boolean servicesXmlFile = isXFireWs(virtualFile);
    final boolean sunJaxWsXmlFile = isSunJaxWs(virtualFile);
    final boolean jaxRpcXmlFile = isJaxRpc(virtualFile);
    final boolean jaxRpcXmlFile2 = isJaxRpc2(virtualFile);
    final boolean cxf = isCxf(virtualFile);
    final boolean wsdl = WebServicesPluginSettings.WSDL_FILE_EXTENSION.equals(virtualFile.getExtension());

    rootTag.acceptChildren(
      new XmlRecursiveElementVisitor() {
        @Override
        public void visitXmlAttributeValue(XmlAttributeValue value) {
          final PsiReference[] references = value.getReferences();
          for(PsiReference r:references) {
            if (r instanceof WSDLReferenceProvider.WsdlClassReference) {
              addClass(r, xml);
            } else if (r instanceof WSDLReferenceProvider.WsdlMethodReference ||
                       r instanceof WSDLReferenceProvider.WsdlPropertyReference ||
                       r instanceof WSDDReferenceProvider.WSMethodReference
                      ) {
              addMethodOrField(r, value, xml);
            } else if (wsddFile || sunJaxWsXmlFile || jaxRpcXmlFile2 || cxf) {
              addClass(r, xml);
            } else if (servicesXmlFile) {
              addMethodOrField(r, value, xml);
            }
          }
        }

        @Override
        public void visitXmlTag(XmlTag xmlTag) {
          if (servicesXmlFile || jaxRpcXmlFile) {
            PsiReference[] references = xmlTag.getReferences();
            for(int i = 2; i < references.length; ++i) {
              addClass(references[i], xml);
            }
            super.visitXmlTag(xmlTag);
          } else if (sunJaxWsXmlFile || jaxRpcXmlFile2 || wsdl || cxf || wsddFile) {
            super.visitXmlTag(xmlTag);
          }
        }
      }
    );
    setJavaModificationCounter(project);
    resolved = true;
  }

  private void setJavaModificationCounter(Project project) {
    myJavaModCounter = PsiManager.getInstance(project).getModificationTracker().getJavaStructureModificationCount();
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
      addMemberLink(className + "." + ANY_NAME, value, r, file);
      return;
    }

    if (!(psiElement instanceof PsiMember)) return;
    if (psiElement instanceof PsiClass) return;

    PsiMember psiMember = (PsiMember) psiElement;
    addMemberLink(getKey(psiMember), value, r, file);
  }

  private void addMemberLink(String name, PsiElement value, PsiReference r, PsiFile file) {
    myLinks.put(name, new WSTextRange(value.getTextRange()));
    mySymbols.add(name);
    myLinkTypes.put(
      name,
      r instanceof WSDLReferenceProvider.WsdlPropertyReference ?
        (isWsdlFile(file) ? WS_PARAMETER_PROPERTY_TYPE : JAXB_PROPERTY_TYPE) :
        WS_METHOD
    );
  }

  private void addClass(PsiReference r, PsiFile file) {
    PsiElement psiElement = r.resolve();

    myLastClassQName = "";

    if (!(psiElement instanceof PsiClass)) return;

    PsiClass psiMember = (PsiClass) psiElement;
    String qualifiedName = psiMember.getQualifiedName();
    myLastClassQName = qualifiedName;
    final PsiElement element = r.getElement();
    myLinks.put(qualifiedName, new WSTextRange(element.getTextRange()));
    mySymbols.add(psiMember.getQualifiedName());

    myLinkTypes.put(
      qualifiedName,
      getLinkType(element, file)
    );

    if (r instanceof ClassReferenceThatReferencesAnyMethod) {
      addMemberLink(qualifiedName + "."+ANY_NAME, element, r, file);
    }
  }

  private static String getLinkType(PsiElement value, PsiFile file) {
    final XmlTag xmlTag = PsiTreeUtil.getParentOfType(value, XmlTag.class, false);
    if (xmlTag == null) return "";
    String localName = xmlTag.getLocalName();

    if (localName.equals(WSDDReferenceProvider.PARAMETER_TAG_NAME)) {
      if (WSDDReferenceProvider.NAME_ATTR_VALUE2.equals(xmlTag.getAttributeValue(WSDDReferenceProvider.NAME_ATTR_NAME))) {
        return WS_TYPE;
      }
    } else if (localName.equals(WSDDReferenceProvider.BEANMAPPING_TAG_NAME)) {
      return WS_PARAMETER_TYPE;
    }

    return WSDLReferenceProvider.COMPLEX_TYPE_TAG_NAME.equals( localName ) ||
      WSDLReferenceProvider.PART_TAG_NAME.equals( localName )?
      isWsdlFile(file) ? WS_PARAMETER_TYPE : JAXB_TYPE :
      WS_TYPE;
  }

  private static boolean isWsdlFile(PsiFile containingFile) {
    VirtualFile file = containingFile != null ? containingFile.getOriginalFile().getVirtualFile():null;

    return file != null && WebServicesPluginSettings.WSDL_FILE_EXTENSION.equals(file.getExtension());
  }

  @Nullable
  public String getWsStatus(PsiMember c) {
    String qname = getKey(c);
    String s = myLinkTypes.get(qname);

    if (s == null) {
      qname = getAnyKey(c);

      if (qname != null) {
        s = myLinkTypes.get(qname);
      }
    }
    return s;
  }

  public boolean hasNonImplicitRef(PsiMember c) {
    return myLinkTypes.get(getKey(c)) != null;    
  }

  @Nullable
  public VirtualFile getFile() {
    if (file != null) return file;
    try {
      return VfsUtil.findRelativeFile(myFileUrl, null);
    } catch (Exception e) {
      return null;
    }
  }

  @Nullable
  public TextRange getWsRange(PsiMember c) {
    String qname = getKey(c);
    TextRange textRange = (myLinks.get(qname) != null) ? myLinks.get(qname).getTextRange() : null;

    if (textRange == null) {
      qname = getAnyKey(c);

      if (qname != null) {
        textRange = myLinks.get(qname) != null ? myLinks.get(qname).getTextRange() : null;
      }
    }
    return textRange;
  }

  WSIndexEntry(final DataInput in) throws IOException {
    myFileUrl = readString(in);
    myLastClassQName = readString(in);
    resolved = in.readBoolean();
    hashCode = in.readInt();

    readStringSet(myCandidates, in);
    readStringSet(mySymbols, in);
    readStringMap(myLinkTypes, in);
    readTextRangeMap(myLinks, in);
  }

  void write(final DataOutput out) throws IOException {
    writeString(myFileUrl, out);
    writeString(myLastClassQName, out);
    out.writeBoolean(resolved);
    out.writeInt(hashCode);

    writeStringSet(myCandidates, out);
    writeStringSet(mySymbols, out);
    writeStringMap(myLinkTypes, out);
    writeTextRangeMap(myLinks, out);
  }

  private static void readStringSet(Set<String> set, final DataInput in) throws IOException {
    int length = in.readInt();
    while (length-- > 0) {
      set.add(readString(in));
    }
  }

  private static void writeStringSet(final Set<String> set, final DataOutput out) throws IOException {
    out.writeInt(set.size());
    for (String s : set) {
      writeString(s, out);
    }
  }

  private static void readStringMap(Map<String, String> map, final DataInput in) throws IOException {
    int length = in.readInt();
    while (length-- > 0) {
      map.put(in.readUTF(), in.readUTF());
    }
  }

  private static void writeStringMap(final Map<String, String> map, final DataOutput out) throws IOException {
    out.writeInt(map.size());
    for (String key : map.keySet()) {
      out.writeUTF(key); //key is not null
      out.writeUTF(map.get(key));
    }
  }

  private static void readTextRangeMap(Map<String, WSTextRange> map, final DataInput in) throws IOException {
    int length = in.readInt();
    while (length-- > 0) {
      map.put(in.readUTF(), new WSTextRange(in.readInt(), in.readInt()));
    }
  }

  private static void writeTextRangeMap(final Map<String, WSTextRange> map, final DataOutput out) throws IOException {
    out.writeInt(map.size());
    for (String key : map.keySet()) {
      out.writeUTF(key); // key is not null
      WSTextRange range = map.get(key);
      out.writeInt(range.getStartOffset());
      out.writeInt(range.getEndOffset());
    }
  }

  @Nullable
  private static String readString(final DataInput in) throws IOException {
    return in.readBoolean() ? in.readUTF() : null;
  }

  private static void writeString(final String s, final DataOutput out) throws IOException {
    if (s == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(s);
    }
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof WSIndexEntry && hashCode == obj.hashCode() && equals(myFileUrl, ((WSIndexEntry)obj).myFileUrl);
  }

  private static boolean equals(String s1, String s2) {
    if (s1 == null && s2 != null) return false;
    return s1.equals(s2);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}
