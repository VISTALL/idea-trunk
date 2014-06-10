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

package org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.impl.source.jsp.JspManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.containers.CollectionFactory;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.parsing.GspElementTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirective;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspXmlRootTag;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.GspXmlTagBaseImpl;
import org.jetbrains.plugins.grails.lang.gsp.resolve.taglib.GspTagLibUtil;

import java.util.List;
import java.util.Map;

/**
 * @author ilyas
 */
public class GspXmlRootTagImpl extends GspXmlTagBaseImpl implements GspXmlRootTag {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.GspXmlRootTagImpl");

  @NonNls
  private static final String GSP_ROOT_TAG = "gsp:root";
  @NonNls
  private static final String GSP_ROOT_TAG_LOCAL = "root";

  private final AtomicNotNullLazyValue<CachedValue<NamespaceData>> myNsData = new AtomicNotNullLazyValue<CachedValue<NamespaceData>>() {
    @NotNull
    @Override
    protected CachedValue<NamespaceData> compute() {
      return createCachedValue(new Computable<NamespaceData>() {
        public NamespaceData compute() {
          return new NamespaceData(GspXmlRootTagImpl.this);
        }
      });
    }
  };

  public GspXmlRootTagImpl() {
    super(GspElementTypes.GSP_ROOT_TAG);
  }

  public String toString() {
    return "Gsp root tag";
  }

  @NotNull
  public String getName() {
    return GSP_ROOT_TAG;
  }

  @NotNull
  public String getLocalName() {
    return GSP_ROOT_TAG_LOCAL;
  }

  @NotNull
  @Override
  public String getNamespace() {
    return GspTagLibUtil.DEFAULT_TAGLIB_PREFIX;
  }

  @Override
  public String getPrefixByNamespace(String namespace) {
    String ns = getNamespaceData().getPrefixByNamespace(namespace);
    return ns != null ? ns : super.getPrefixByNamespace(namespace);
  }

  @NotNull
  @Override
  public String getNamespaceByPrefix(String prefix) {
    LOG.assertTrue(isValid());
    String ns = getNamespaceData().getNamespaceByPrefix(prefix);
    return ns == null ? super.getNamespaceByPrefix(prefix) : ns;
  }

  private NamespaceData getNamespaceData() {
    return myNsData.getValue().getValue();
  }

  @Override
  public String[] knownNamespaces() {
    return getNamespaceData().knownNamespaces();
  }

  @Override
  public XmlNSDescriptor getNSDescriptor(final String namespace, final boolean strict) {
    return getNamespaceData().getNSDescriptor(namespace);
  }

  @Override
  public XmlElementDescriptor getDescriptor() {
    return new GspRootElementDescriptor(this, GspNamespaceDescriptor.getDefaultNsDescriptor(this));
  }

  private static class GspRootElementDescriptor extends GspElementDescriptorBase {
    public GspRootElementDescriptor(final GspXmlRootTagImpl tag, final GspNamespaceDescriptor nsDescriptor) {
      super(nsDescriptor, tag, "gsp root");
    }

    public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
      return XmlAttributeDescriptor.EMPTY;
    }

    public XmlAttributeDescriptor getAttributeDescriptor(@NonNls String attributeName, @Nullable XmlTag context) {
      return null;
    }

  }

  private static class NamespaceData {
    private final BidirectionalMap<String, String> myPrefix2Namespace = new BidirectionalMap<String, String>();
    private final Map<String, CachedValue<XmlNSDescriptor>> myUri2Descriptor = CollectionFactory.newTroveMap();

    public NamespaceData(final GspXmlRootTagImpl tag) {
      final String[] grailsPrefixes = GspTagLibUtil.getKnownPrefixes(tag, true);
      final Project project = tag.getProject();
      for (final String prefix : grailsPrefixes) {
        myPrefix2Namespace.put(prefix, prefix);
        myUri2Descriptor.put(prefix, tag.createCachedValue(new NullableComputable<XmlNSDescriptor>() {
          public XmlNSDescriptor compute() {
            List<PsiClass> taglibClasses = GspTagLibUtil.getTagLibClasses(prefix, tag);
            if (!taglibClasses.isEmpty()) {
              return new GspNamespaceDescriptor(prefix, taglibClasses);
            }
            if (GspTagLibUtil.DEFAULT_TAGLIB_PREFIX.equals(prefix)) {
              return GspTagLibUtil.getGrailsTldDescriptor(project);
            }
            return null;
          }
        }));
      }

      final Module module = ModuleUtil.findModuleForPsiElement(tag);
      final JspManager jspManager = JspManager.getInstance(project);

      final PsiRecursiveElementWalkingVisitor visitor = new PsiRecursiveElementWalkingVisitor() {
        @Override
        public void visitElement(PsiElement element) {
          if (element instanceof GspDirective) {
            final GspDirective directive = (GspDirective)element;
            if (directive.isTaglibDirective()) {
              final String prefix = directive.getPrefixAttributeValue();
              final String uri = directive.getUriAttributeValue();
              if (StringUtil.isNotEmpty(prefix) && StringUtil.isNotEmpty(uri)) {
                myPrefix2Namespace.put(prefix, uri);
                myUri2Descriptor.put(uri, tag.createCachedValue(new NullableComputable<XmlNSDescriptor>() {
                    public XmlNSDescriptor compute() {
                      return jspManager == null ? null : GspTagLibUtil.getTldDescriptor(jspManager.getTldFileByUri(uri, module, null));
                    }
                  }));
              }
            }
            return;
          }
          super.visitElement(element);
        }
      };
      tag.accept(visitor);
    }

    @Nullable
    public XmlNSDescriptor getNSDescriptor(@NotNull String uri) {
      final CachedValue<XmlNSDescriptor> value = myUri2Descriptor.get(uri);
      return value == null ? null : value.getValue();
    }

    @Nullable
    public String getNamespaceByPrefix(@NotNull String prefix) {
      return myPrefix2Namespace.get(prefix);
    }

    @Nullable
    public String getPrefixByNamespace(@NotNull String namespace) {
      final List<String> value = myPrefix2Namespace.getKeysByValue(namespace);
      return value == null || value.isEmpty() ? null : value.get(0);
    }

    public String[] knownNamespaces() {
      return myPrefix2Namespace.values().toArray(new String[myPrefix2Namespace.size()]);
    }
  }

  private <T> CachedValue<T> createCachedValue(final Computable<T> computable) {
    return getManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<T>() {
      public Result<T> compute() {
        return Result.create(computable.compute(), PsiModificationTracker.MODIFICATION_COUNT, ProjectRootManager.getInstance(getProject()));
      }
    }, false);
  }


}
