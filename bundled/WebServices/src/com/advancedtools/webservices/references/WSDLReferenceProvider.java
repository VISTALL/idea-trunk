package com.advancedtools.webservices.references;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.DeployUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.util.XmlUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @by maxim
 */
public class WSDLReferenceProvider extends MyReferenceProvider {
  private static final @NonNls String TYPE_ATTR_NAME = "type";
  private static final @NonNls String LOCATION_ATTR_NAME = "location";
  private static final @NonNls String ELEMENT_ATTR_NAME = "element";
  public static final @NonNls String NAME_ATTR_NAME = "name";
  private static final @NonNls String BINDING_ATTR_NAME = "binding";

  private static final @NonNls String MESSAGE_ATTR_NAME = "message";
  private static final @NonNls String TARGET_NAMESPACE_ATTR_NAME = "targetNamespace";
  private static final @NonNls String BINDING_TAG_NAME = "binding";

  public static final @NonNls String PORT_TYPE_TAG_NAME = "portType";
  private static final @NonNls String PORT_TAG_NAME = "port";
  private static final @NonNls String INPUT_TAG_NAME = "input";
  private static final @NonNls String OUTPUT_TAG_NAME = "output";
  private static final @NonNls String FAULT_TAG_NAME = "fault";

  private static final @NonNls String MESSAGE_TAG_NAME = "message";
  public static final @NonNls String OPERATION_TAG_NAME = "operation";
  public static final @NonNls String PART_TAG_NAME = "part";
  public static final @NonNls String COMPLEX_TYPE_TAG_NAME = "complexType";
  public static final @NonNls String ELEMENT_TAG_NAME = "element";
  private static final @NonNls String IMPORT_TAG_NAME = "import";
  @NonNls
  private static final String TYPES_TAG_NAME = "types";

  private static final TagCollector schemaTagCollector = new TagCollector() {
    @NotNull
    public XmlTag[] findNeededTags(@NotNull XmlTag t, String param) {
      return t.findSubTags(ELEMENT_TAG_NAME, t.getNamespace());
    }

    @NotNull
    public XmlTag[] findImportedTags(@NotNull XmlTag t) {
      return t.findSubTags(IMPORT_TAG_NAME, t.getNamespace());
    }

    @NotNull
    public String getImportedFileLocationAttrName() {
      return "schemaLocation";
    }
  };
  private static final TagCollector wsdlTagsCollector = new TagCollector() {
    @NotNull
    public XmlTag[] findNeededTags(@NotNull XmlTag t, String param) {
      return t.findSubTags(param, t.getNamespace());
    }

    @NotNull
    public XmlTag[] findImportedTags(@NotNull XmlTag t) {
      return t.findSubTags("import", t.getNamespace());
    }

    @NotNull
    public String getImportedFileLocationAttrName() {
      return "location";
    }
  };
  private final MyPathReferenceProvider myPathProvider;

  public WSDLReferenceProvider(Project project) {
    myPathProvider = EnvironmentFacade.getInstance().acquirePathReferenceProvider(project, false);
  }

  public ElementFilter getFilter() {
    return null;
  }

  static @NonNls String[] SCHEMA_URIS = new String[] {
    XmlUtil.XML_SCHEMA_URI,
    XmlUtil.XML_SCHEMA_URI2,
    XmlUtil.XML_SCHEMA_URI3
  };
  static {
    Arrays.sort(SCHEMA_URIS);
  }

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement) {
    final PsiElement parent = psiElement.getParent();

    if (parent instanceof XmlAttribute) {
      final XmlAttribute xmlAttribute = ((XmlAttribute) parent);
      final XmlTag parentTag = xmlAttribute.getParent();
      final String tagNS = parentTag.getNamespace();
      final String attrName = xmlAttribute.getName();
      final String tagLocalName = parentTag.getLocalName();

      if (!WebServicesPluginSettings.HTTP_SCHEMAS_XMLSOAP_ORG_WSDL.equals(tagNS) &&
          !WebServicesPluginSettings.HTTP_WWW_W3_ORG_2003_03_WSDL.equals(tagNS)
         ) {
        if (Arrays.binarySearch(SCHEMA_URIS,tagNS) >= 0) {
          if (NAME_ATTR_NAME.equals(attrName)) {
            PsiFile psiFile = psiElement.getContainingFile();
            VirtualFile file = psiFile != null ? psiFile.getOriginalFile().getVirtualFile():null;

            if (file == null ||
              !WebServicesPluginSettings.WSDL_FILE_EXTENSION.equals(file.getExtension())) {
              return PsiReference.EMPTY_ARRAY;
            }

            if (COMPLEX_TYPE_TAG_NAME.equals(tagLocalName)) {
              return new PsiReference[] {
                new WsdlClassReference(psiElement, 1, psiElement.getTextLength() - 1)
              };
            } else if (ELEMENT_TAG_NAME.equals(tagLocalName)) {
              return new PsiReference[] {
                new WsdlPropertyReference(psiElement)
              };
            }
          }
        }
        return PsiReference.EMPTY_ARRAY;
      }

      final String text = StringUtil.stripQuotesAroundValue(psiElement.getText());
      final int i = text.indexOf(':');

      if (BINDING_ATTR_NAME.equals(attrName)) {
        if (PORT_TAG_NAME.equals(tagLocalName)) {
          return collectQReferences(psiElement, text, i, BINDING_TAG_NAME);
        }
      } else if (MESSAGE_ATTR_NAME.equals(attrName)) {
        if (INPUT_TAG_NAME.equals(tagLocalName) ||
            OUTPUT_TAG_NAME.equals(tagLocalName) ||
            FAULT_TAG_NAME.equals(tagLocalName)
           ) {
          return collectQReferences(psiElement, text, i, MESSAGE_TAG_NAME);
        }
      } else if (TYPE_ATTR_NAME.equals(attrName)) {
        final boolean isBindingTagName = BINDING_TAG_NAME.equals(tagLocalName);
        if (isBindingTagName ||
            PART_TAG_NAME.equals(tagLocalName)) {

          PsiReference[] refs = isBindingTagName ? collectQReferences(psiElement, text, i, PORT_TYPE_TAG_NAME): PsiReference.EMPTY_ARRAY;
          refs = ArrayUtil.append(refs, new WsdlClassReference(psiElement, i + 2, text.length() + 1));
          return refs;
        }
      } else if (NAME_ATTR_NAME.equals(attrName)) {
        if (PORT_TYPE_TAG_NAME.equals(tagLocalName)) {
          return new PsiReference[] {
            new WsdlClassReference(psiElement, 1, psiElement.getTextLength() - 1)
          };
        } else if (OPERATION_TAG_NAME.equals(tagLocalName)) {
          final XmlTag grandParentTag = parentTag.getParentTag();
          if (grandParentTag != null) {
            final String grandParentLocalName = grandParentTag.getLocalName();

            if( PORT_TYPE_TAG_NAME.equals(grandParentLocalName) || BINDING_TAG_NAME.equals(grandParentLocalName)) {
              return new PsiReference[] {
                new WsdlMethodReference(psiElement)
              };
            }
          }
        }
      } else if (LOCATION_ATTR_NAME.equals(attrName)) {
        if (IMPORT_TAG_NAME.equals(tagLocalName)) {
          if (text.indexOf("://") == -1) {
            return myPathProvider.getReferencesByString(text, psiElement, 1);
          }
        }
      } else if (ELEMENT_ATTR_NAME.equals(attrName)) {
        if (PART_TAG_NAME.equals(tagLocalName)) {
          NsReference nsRef = null;

          if (i != -1) {
            nsRef = new NsReference(psiElement, 1, i + 1);
          }

          final WsdlEntityReference wsdlEntityReference = new WsdlEntityReference(psiElement, i + 2, text.length() + 1, null) {
            @Override
            protected void processTags(PsiElementProcessor<XmlTag> processor) {
              XmlTag rootTag = ((XmlFile) getElement().getContainingFile()).getDocument().getRootTag();
              if (rootTag == null) return;
              for (XmlTag t : getWsdlTags(rootTag, TYPES_TAG_NAME)) {
                for (XmlTag t2 : t.getSubTags()) {
                  if (processSchemaTags(processor, t2)) return;
                }
              }
              
              for (XmlTag t : getWsdlTags(rootTag, IMPORT_TAG_NAME)) {
                XmlAttribute locationAttr = t.getAttribute(LOCATION_ATTR_NAME);
                if (locationAttr == null) continue;
                XmlAttributeValue value = locationAttr.getValueElement();
                if (value == null) continue;
                PsiReference[] references = value.getReferences();
                if (references.length == 0) continue;

                PsiElement _xmlFile = references[references.length - 1].resolve();
                if (!(_xmlFile instanceof XmlFile)) continue;
                rootTag = ((XmlFile) _xmlFile).getDocument().getRootTag();

                if (rootTag  != null && processSchemaTags(processor, rootTag)) return;
              }
            }
          };

          if (nsRef == null) {
            return new PsiReference[] { wsdlEntityReference };
          } else {
            return new PsiReference[] { nsRef, wsdlEntityReference };
          }
        }
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }

  private static boolean processSchemaTags(PsiElementProcessor<XmlTag> processor, XmlTag t2) {
    for(XmlTag t:collectTags(t2, null, schemaTagCollector, null)) {
      if (!processor.execute(t)) return false;
    }
    return true;
  }

  private static PsiReference[] collectQReferences(PsiElement psiElement, String text, int i, String tagName) {
    NsReference nsreference = null;
    final PsiReference bindingReference = new WsdlEntityReference(psiElement, i + 2, text.length() + 1, tagName);

    if (i != -1) {
      nsreference = new NsReference(psiElement, 1, i + 1);
    }

    if (nsreference == null) {
      return new PsiReference[] {
        bindingReference
      };
    } else {
      return new PsiReference[] {
        nsreference,
        bindingReference
      };
    }
  }

  public static class WsdlMethodReference extends BaseRangedReference {
    public WsdlMethodReference(PsiElement psiElement, int start, int end) {
      super(psiElement, start, end);
    }
    public WsdlMethodReference(PsiElement psiElement) {
      super(psiElement, 1, psiElement.getTextLength() - 1);
    }

    @Nullable
    public PsiElement resolve() {
      final PsiElement psiElement = resolveClass();

      if (psiElement instanceof PsiClass) {
        final PsiMethod[] methodsByName = ((PsiClass) psiElement).findMethodsByName(getCanonicalText(), false);
          return methodsByName.length > 0 ? methodsByName[0]:null;
      }

      return null;
    }

    protected PsiElement resolveClass() {
      final XmlTag grandParentTag = (XmlTag) getElement().getParent().getParent().getParent();
      String attrNameContainingClassReference;

      if (BINDING_TAG_NAME.equals(grandParentTag.getLocalName())) {
        attrNameContainingClassReference = TYPE_ATTR_NAME;
      } else {
        attrNameContainingClassReference = NAME_ATTR_NAME;
      }

      final XmlAttribute attribute = grandParentTag.getAttribute(attrNameContainingClassReference, null);
      final XmlAttributeValue xmlAttributeValue = attribute != null ? attribute.getValueElement():null;

      if (xmlAttributeValue != null) {
        final PsiReference[] references = xmlAttributeValue.getReferences();
        final PsiReference psiReference = references.length > 0 ? references[references.length - 1]:null;
        return psiReference != null ? psiReference.resolve():null;
      }
      return null;
    }

    public Object[] getVariants() {
      final PsiElement psiElement = resolveClass();
      if (psiElement instanceof PsiClass) {
        PsiClass clazz = (PsiClass) psiElement;
        List<String> acceptableMethods = new ArrayList<String>(4);

        for(PsiMethod m:clazz.getMethods()) {
          if (DeployUtils.isAcceptableMethod(m)) {
            acceptableMethods.add(m.getName());
          }
        }

        return ArrayUtil.toObjectArray(acceptableMethods);
      }

      return new Object[0];
    }

    public boolean isSoft() {
      return true;
    }
  }

  static class WsdlEntityReference extends BaseRangedReference {
    private final String entityName;
    public WsdlEntityReference(PsiElement psiElement, int index, int endIndex, String _entityName) {
      super(psiElement, index, endIndex);
      entityName = _entityName;
    }

    @Nullable
    public PsiElement resolve() {
      final PsiElement[] result = new PsiElement[1];

      processTags(new PsiElementProcessor<XmlTag>() {
        final String canonicalText = getCanonicalText();
        public boolean execute(XmlTag element) {
          if (canonicalText.equals(element.getAttributeValue(NAME_ATTR_NAME))) {
            result[0] = element;
            return false;
          }
          return true;
        }
      });

      return result[0];
    }

    public Object[] getVariants() {
      final List<String> result = new ArrayList<String>(1);
      processTags(new PsiElementProcessor<XmlTag>() {
        public boolean execute(XmlTag element) {
          result.add(element.getAttributeValue(NAME_ATTR_NAME));
          return true;
        }
      });
      return ArrayUtil.toObjectArray(result);
    }

    protected void processTags(PsiElementProcessor<XmlTag> processor) {
      XmlTag rootTag = ((XmlFile) getElement().getContainingFile()).getDocument().getRootTag();
      if (rootTag == null) return;

      final XmlTag[] subTags = getWsdlTags(rootTag, this.entityName);

      for(XmlTag t:subTags) {
        if (!processor.execute(t)) return;
      }
    }

    public boolean isSoft() {
      return false;
    }
  }

//  private static final Key<CachedValue<Map<String,XmlTag[]>>> ourWsdlTagsKey = Key.create("wsdl.tags");

  public static XmlTag[] getWsdlTags(XmlTag rootTag, String entityName) {
//    CachedValue<Map<String, XmlTag[]>> mapCachedValue = rootTag.getUserData(ourWsdlTagsKey);
//    if (mapCachedValue == null) {
//      mapCachedValue = rootTag.getManager().getCachedValuesManager().createCachedValue(
//        new CachedValueProvider<Map<String, XmlTag[]>>() {
//          public Result<Map<String, XmlTag[]>> compute() {
//            return null;
//          }
//        },
//        false
//      );
//    }
    return collectTags(rootTag, null, wsdlTagsCollector, entityName);
  }

  private static XmlFile getXmlFileFromAttr(XmlTag t, String attrName) {
    if (t.getAttributeValue(attrName) != null) {
      final XmlAttribute attribute = t.getAttribute(attrName, null);

      if (attribute != null) {
        final PsiReference[] references = attribute.getValueElement().getReferences();
        final PsiElement psiElement = references.length > 0 ? references[0].resolve():null;

        if (psiElement instanceof XmlFile) {
          return (XmlFile) psiElement;
        }
      }
    }
    return null;
  }

  interface TagCollector {
    @NotNull XmlTag[] findNeededTags(@NotNull XmlTag t, String param);
    @NotNull XmlTag[] findImportedTags(@NotNull XmlTag t);
    @NotNull @NonNls String getImportedFileLocationAttrName();
  }

  private static XmlTag[] collectTags(XmlTag rootTag, Set<PsiFile> visitedFiles, TagCollector collector, String param) {
    if (visitedFiles != null) visitedFiles.add(rootTag.getContainingFile());
    XmlTag[] subTags = collector.findNeededTags(rootTag, param);

    for(XmlTag importTag:collector.findImportedTags(rootTag)) {
      final XmlFile imported = getXmlFileFromAttr(importTag, collector.getImportedFileLocationAttrName());
      if (imported == null) continue;

      if (visitedFiles == null) {
        visitedFiles = new THashSet<PsiFile>();
        visitedFiles.add(rootTag.getContainingFile());
      }

      final XmlDocument document = imported.getDocument();
      final XmlTag includedDocRootTag = visitedFiles.contains(imported) || document == null ? null: document.getRootTag();

      if (includedDocRootTag != null) {
        subTags = ArrayUtil.mergeArrays(subTags, collectTags(includedDocRootTag, visitedFiles, collector, param), XmlTag.class);
      }
    }

    return subTags;
  }

  static class NsReference extends BaseRangedReference {
    public NsReference(PsiElement psiElement, int index, int length) {
      super(psiElement, index, length);
    }

    @Nullable
    public PsiElement resolve() {
      final XmlTag tag = PsiTreeUtil.getParentOfType(getElement(), XmlTag.class);
      final String namespace = tag.getNamespaceByPrefix(getCanonicalText());

      PsiElement nsDecl = findTagAttributeByValue(tag, namespace);
      if (nsDecl == null) {
        final XmlTag rootTag = ((XmlFile) getElement().getContainingFile()).getDocument().getRootTag();
        if (rootTag != tag) nsDecl = findTagAttributeByValue(rootTag, namespace);
      }
      return nsDecl;
    }

    private static PsiElement findTagAttributeByValue(XmlTag rootTag, String namespace) {
      for(XmlAttribute attr:rootTag.getAttributes()) {
        if (attr.isNamespaceDeclaration()) {
          final XmlAttributeValue valueElement = attr.getValueElement();

          if (valueElement != null &&
              StringUtil.stripQuotesAroundValue(valueElement.getText()).equals(namespace)) {
            return valueElement;
          }
        }
      }

      return null;
    }

    public Object[] getVariants() {
      final XmlTag rootTag = PsiTreeUtil.getParentOfType(getElement(), XmlTag.class);
      final String[] strings = rootTag.knownNamespaces();

      for(int i = 0; i < strings.length; ++i) {
        strings[i] = rootTag.getPrefixByNamespace(strings[i]);
      }

      return strings;
    }

    public boolean isSoft() {
      return false;
    }
  }

  public static class WsdlPropertyReference extends BaseRangedReference {
    public WsdlPropertyReference(PsiElement psiElement, int start, int end) {
      super(psiElement, start, end);
    }

    public WsdlPropertyReference(PsiElement psiElement) {
      super(psiElement, 1, psiElement.getTextLength() - 1);
    }

    public PsiElement handleElementRename(String string) throws IncorrectOperationException {
      if (resolve() instanceof PsiMethod) {
        String newName = PropertyUtil.getPropertyName(string);
        if (newName != null) string = newName;
      }
      return super.handleElementRename(string);
    }

    private void process(final PsiElementProcessor<PsiMember> processor) {
      PsiClass clazz = resolveClass();

      if (clazz != null) {
        EnvironmentFacade.getInstance().processProperties(clazz, processor);

        for(PsiField f:clazz.getFields()) {
          if (f.hasModifierProperty(PsiModifier.PUBLIC)) {
            if (!processor.execute(f)) return;
          }
        }
      }
    }

    @Nullable
    public PsiElement resolve() {
      final PsiElement[] result = new PsiElement[1];

      process(new PsiElementProcessor<PsiMember>() {
        public boolean execute(PsiMember psiMember) {
          String text = getCanonicalText();
          String name;

          if (psiMember instanceof PsiMethod) {
            name = PropertyUtil.getPropertyName((PsiMethod) psiMember);
          } else {
            name = psiMember.getName();
          }

          if (text.length() > 1 &&
              Character.isLowerCase(text.charAt(0)) &&
              Character.isUpperCase(text.charAt(1))
             ) { // workaround for properties with all capital letters
            text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
          }

          if (text.length() > 1 && Character.isUpperCase(text.charAt(0)) && Character.isLowerCase(text.charAt(1))) {
            text = StringUtil.decapitalize(text);
          }

          if(text.equals(name)) {
            result[0] = psiMember;
            return false;
          }

          return true;
        }
      });

      return result[0];
    }

    protected PsiClass resolveClass() {
      PsiClass clazz = null;

      final PsiElement parentTag = getElement().getParent().getParent().getParent().getParent();
      if (parentTag instanceof XmlTag) {
        final XmlTag xmlTag = (XmlTag) parentTag;

        if (COMPLEX_TYPE_TAG_NAME.equals(xmlTag.getLocalName())) {
          if (xmlTag.getAttributeValue(NAME_ATTR_NAME) != null) {
            final PsiReference[] references = xmlTag.getAttribute(NAME_ATTR_NAME, null).getValueElement().getReferences();
            final PsiElement psiElement = references[references.length - 1].resolve();

            if (psiElement instanceof PsiClass) {
              clazz = (PsiClass) psiElement;
            }
          }
        }
      }
      return clazz;
    }

    public Object[] getVariants() {
      final List<Object> variants = new ArrayList<Object>(3);
      process(
        new PsiElementProcessor<PsiMember>() {
          public boolean execute(PsiMember psiMember) {
            if (psiMember instanceof PsiMethod) {
              variants.add( PropertyUtil.getPropertyName((PsiMethod) psiMember) );
            } else {
              variants.add(psiMember.getName());
            }
            return true;
          }
        }
      );

      return ArrayUtil.toObjectArray(variants);
    }

    public boolean isSoft() {
      return true;
    }
  }

  public static class WsdlClassReference extends BaseRangedReference implements PsiPolyVariantReference {
    private static final String HTTP_PROTOCAL_PREFIX = "http://";
    private static final String JAVA_PREFIX = "java:";

    public WsdlClassReference(PsiElement psiElement, int index, int length) {
      super(psiElement, index, length);
    }

    protected String getPackageCandidateValueFromNs() {
      final TextRange rangeInElement = getRangeInElement();
      if (rangeInElement.getStartOffset() > 2) {
        final PsiReference referenceAt = getElement().findReferenceAt(rangeInElement.getStartOffset() - 2);

        if (referenceAt instanceof NsReference) {
          final PsiElement psiElement = referenceAt.resolve();

          if (psiElement != null) {
            String s = StringUtil.stripQuotesAroundValue(psiElement.getText());

            if (s != null) return s;
          }
        }
      } else {
        final XmlTag rootTag = ((XmlFile) getElement().getContainingFile()).getDocument().getRootTag();
        String s = rootTag.getAttributeValue(TARGET_NAMESPACE_ATTR_NAME);
        if (s != null) return s;
      }

      return null;
    }

    @Nullable
    public PsiElement resolve() {
      ResolveResult[] resolveResults = multiResolve(false);
      if (resolveResults.length == 1) return resolveResults[0].getElement();
      return null;
    }

    public Object[] getVariants() {
      final Object[][] result = new Object[1][];
      processCandidatePackages(new PsiElementProcessor<PsiPackage>() {
        public boolean execute(PsiPackage psiPackage) {
          result[0] = psiPackage.getClasses();
          return false;
        }
      });

      if (result[0] != null) return result[0];

      return EnvironmentFacade.getInstance().getShortNamesCache(getElement().getProject()).getAllClassNames();
    }

    public boolean isSoft() {
      return true;
    }

    private void processCandidatePackages(PsiElementProcessor<PsiPackage> packageProcessor) {
      final PsiManager manager = getElement().getManager();

      String packageCandidateValueFromNs = getPackageCandidateValueFromNs();
      PsiPackage aPackage;

      if (packageCandidateValueFromNs != null) {
        packageCandidateValueFromNs = removeNsDecorations(packageCandidateValueFromNs);

        String axisPackageCandidateValueFromNs = revertAxisDecoration(packageCandidateValueFromNs);
        aPackage = EnvironmentFacade.getInstance().findPackage(axisPackageCandidateValueFromNs, manager.getProject());
        if (aPackage != null && !packageProcessor.execute(aPackage)) return;

        aPackage = EnvironmentFacade.getInstance().findPackage(packageCandidateValueFromNs, manager.getProject());
        if (aPackage != null && !packageProcessor.execute(aPackage)) return;
      }

      final PsiDirectory psiDirectory = EnvironmentFacade.getInstance().getDirectoryFromFile(getElement().getContainingFile());
      if (psiDirectory != null) {
        aPackage = EnvironmentFacade.getInstance().getPackageFor(psiDirectory);
        if (aPackage != null && !packageProcessor.execute(aPackage)) return;
      }
    }

    private String revertAxisDecoration(String s) {
      StringTokenizer tokenizer = new StringTokenizer(s,".");
      StringBuffer buffer = new StringBuffer();

      while(tokenizer.hasMoreTokens()) {
        if (buffer.length() > 0) buffer.insert(0,'.');
        buffer.insert(0,tokenizer.nextToken());
      }
      return buffer.toString();
    }

    private String removeNsDecorations(String s) {
      if (s.startsWith(HTTP_PROTOCAL_PREFIX)) {
        s = s.substring(HTTP_PROTOCAL_PREFIX.length());
      }
      if (s.endsWith("/")) s = s.substring(0,s.length() - 1);
      if (s.startsWith(JAVA_PREFIX)) {
        s = s.substring(JAVA_PREFIX.length());
      }
      return s;
    }

    @NotNull
    public ResolveResult[] multiResolve(boolean b) {
      final ResolveResult[][] result = new ResolveResult[1][];
      Project project = getElement().getProject();
      final String canonicalText = getCanonicalText();

      final String canonicalText1 = canonicalText;
      processCandidatePackages(new PsiElementProcessor<PsiPackage>() {
        private final XmlTag parentTag = (XmlTag) getElement().getParent().getParent();
        private String canonicalText2;
        private String canonicalText3;

        {
          if (parentTag.getLocalName().equals(BINDING_TAG_NAME)) {
            canonicalText2 = canonicalText1 + "Stub";
            canonicalText3 = canonicalText1 + "_BindingStub";
          } else if (parentTag.getLocalName().equals(PORT_TYPE_TAG_NAME)) {
            canonicalText2 = canonicalText1 + "_PortType";
            canonicalText3 = canonicalText1 + "_BindingImpl";
          }
        }

        public boolean execute(PsiPackage psiPackage) {
          PsiClass myPossibleResult = null;

          for(PsiClass clazz:psiPackage.getClasses()) {
            if (canonicalText1.equals(clazz.getName()) ||
                canonicalText2 != null && canonicalText2.equals(clazz.getName()) ||
                canonicalText3 != null && canonicalText3.equals(clazz.getName())
               ) {
              if (clazz.isInterface()) {
                myPossibleResult = clazz;
                continue;
              }
              result[0] = new ResolveResult[] { new MyResolveResult(clazz)};
              return false;
            }
          }

          if (myPossibleResult != null) {
            result[0] = new ResolveResult[] { new MyResolveResult(myPossibleResult)};
            return false;
          }

          return true;
        }
      });

      if (result[0] != null) return result[0];

      final PsiClass[] classes = EnvironmentFacade.getInstance().getShortNamesCache(project).getClassesByName(
        canonicalText,
        GlobalSearchScope.allScope(project)
      );
      final ResolveResult results[] = new ResolveResult[classes.length];

      for(int i = 0; i < classes.length; ++i) {
        results[i] = new MyResolveResult(classes[i]);
      }

      return results;
    }
  }

  public String[] getAttributeNames() {
    return new String[] {
      TYPE_ATTR_NAME,
      NAME_ATTR_NAME,
      BINDING_ATTR_NAME,
      MESSAGE_ATTR_NAME,
      LOCATION_ATTR_NAME,
      ELEMENT_ATTR_NAME
    };
  }

}
