package com.intellij.hibernate.util;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.hibernate.model.HibernateConstants;
import com.intellij.hibernate.model.HibernatePropertiesConstants;
import com.intellij.hibernate.model.xml.config.Mapping;
import com.intellij.hibernate.model.xml.config.Property;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.javaee.JavaeeUtil;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.GenericValue;
import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Gregory.Shrago
 */
public class HibernateUtil {

  private HibernateUtil() {
  }
  
  public static String getFullPropertyName(final String hibernateProperty) {
    return hibernateProperty.startsWith(HibernatePropertiesConstants.HIBERNATE_PREFIX) ?
           hibernateProperty : HibernatePropertiesConstants.HIBERNATE_PREFIX + hibernateProperty;
  }

  public static boolean isHibernateConfig(final XmlFile file, final Module module) {
    return JavaeeUtil.checkXmlType(file, HibernateConstants.CFG_XML_ROOT_TAG, HibernateConstants.HIBERNATE_CONFIGURATION_3_0);
  }

  public static boolean isHibernateMapping(final XmlFile file, final Module module) {
    return JavaeeUtil.checkXmlType(file, HibernateConstants.HBM_XML_ROOT_TAG, HibernateConstants.HIBERNATE_MAPPING_3_0);
  }

  public static <V, T extends Collection<V>> T getDomMappings(final SessionFactory sessionFactory, final Class<V> mappingClass, final T result) {
    for (Mapping mapping : sessionFactory.getMappings()) {
      PsiFile psiFile;
      if ((psiFile = mapping.getResource().getValue()) != null
        || (psiFile = mapping.getFile().getValue()) != null) {
        ContainerUtil.addIfNotNull(JamCommonUtil.getRootElement(psiFile, mappingClass, sessionFactory.getModule()), result);
      }
      else if ((psiFile = mapping.getJar().getValue()) != null) {
        final PsiManager psiManager = psiFile.getManager();
        final Module module = sessionFactory.getModule();
        processFilesInJar(psiFile.getVirtualFile(), new Processor<VirtualFile>() {
          public boolean process(VirtualFile virtualFile) {
            final PsiFile aFile = psiManager.findFile(virtualFile);
            ContainerUtil.addIfNotNull(JamCommonUtil.getRootElement(aFile, mappingClass, module), result);
            return true;
          }
        });
      }
    }
    return result;
  }

  private static void processFilesInJar(final VirtualFile virtualFile, final Processor<VirtualFile> processor) {
    final VirtualFile jarFile = JarFileSystem.getInstance().findFileByPath(virtualFile.getPath() + JarFileSystem.JAR_SEPARATOR);
    class Visitor {

      boolean accept(final VirtualFile file) {
        if (file.isDirectory()) {
          for (VirtualFile child : file.getChildren()) {
            if (!accept(child)) {
              return false;
            }
          }
        }
        else {
          return processor.process(file);
        }
        return true;
      }

    }
    if (jarFile != null) {
      new Visitor().accept(jarFile);
    }
  }

  public static void consumePersistentObjects(final HbmHibernateMapping mappings,
                                              final Consumer<? super HbmClassBase> classConsumer,
                                              final Consumer<? super HbmEmbeddedAttributeBase> componentConsumer) {
    final HbmAttributeVisitorAdapter componentVisitor = new HbmAttributeVisitorAdapter() {

      @Override public void visitEmbeddedAttributeBase(final HbmEmbeddedAttributeBase embeddedAttributeBase) {
        componentConsumer.consume(embeddedAttributeBase);
        embeddedAttributeBase.visitAttributes(this);
      }

    };
    final Consumer<HbmClassBase> consumer = new Consumer<HbmClassBase>() {
      public void consume(final HbmClassBase hbmClassBase) {
        if (classConsumer != null) {
          classConsumer.consume(hbmClassBase);
        }
        if (componentConsumer != null) {
          hbmClassBase.visitAttributes(componentVisitor);
        }
      }
    };
    final Processor<HbmSubclass> subclassProcessor = new Processor<HbmSubclass>() {
      public boolean process(final HbmSubclass hbmSubclass) {
        consumer.consume(hbmSubclass);
        ContainerUtil.process(hbmSubclass.getSubclasses(), this);
        return true;
      }
    };
    final Processor<HbmUnionSubclass> unionSubclassProcessor = new Processor<HbmUnionSubclass>() {
      public boolean process(final HbmUnionSubclass hbmSubclass) {
        consumer.consume(hbmSubclass);
        ContainerUtil.process(hbmSubclass.getUnionSubclasses(), this);
        return true;
      }
    };
    final Processor<HbmJoinedSubclass> joinedSubclassProcessor = new Processor<HbmJoinedSubclass>() {
      public boolean process(final HbmJoinedSubclass hbmSubclass) {
        consumer.consume(hbmSubclass);
        ContainerUtil.process(hbmSubclass.getJoinedSubclasses(), this);
        return true;
      }
    };
    final Processor<HbmClass> classProcessor = new Processor<HbmClass>() {
      public boolean process(final HbmClass hbmClass) {
        consumer.consume(hbmClass);
        ContainerUtil.process(hbmClass.getSubclasses(), subclassProcessor);
        ContainerUtil.process(hbmClass.getJoinedSubclasses(), joinedSubclassProcessor);
        ContainerUtil.process(hbmClass.getUnionSubclasses(), unionSubclassProcessor);
        return true;
      }
    };
    ContainerUtil.process(mappings.getClasses(), classProcessor);
    ContainerUtil.process(mappings.getJoinedSubclasses(), joinedSubclassProcessor);
    ContainerUtil.process(mappings.getSubclasses(), subclassProcessor);
    ContainerUtil.process(mappings.getUnionSubclasses(), unionSubclassProcessor);
  }

  public static <T, S extends GenericValue<T>, U extends Collection<T>, V extends Collection<S>> U getGenericValues(final V values, final U result) {
    for (S value : values) {
      ContainerUtil.addIfNotNull(value.getValue(), result);
    }
    return result;
  }

  public static <S extends GenericValue, U extends Collection<String>, V extends Collection<S>> U getStringValues(final V values, final U result) {
    for (S value : values) {
      ContainerUtil.addIfNotNull(value.getStringValue(), result);
    }
    return result;
  }

  public static boolean isEmbedded(final HbmCompositeId compositeId) {
    return StringUtil.isNotEmpty(compositeId.getClazz().getStringValue()) && !Boolean.TRUE.equals(compositeId.getMapped().getValue());
  }

  public static Map<String, String> getSessionFactoryProperties(final SessionFactory factory) {
    final THashMap<String, String> result = new THashMap<String, String>();
    for (Property<Object> property : factory.getProperties()) {
      final String name = property.getName().getValue();
      final String value = property.getStringValue();
      final String shortName = getPropertyShortName(name);
      if (StringUtil.isNotEmpty(shortName) && StringUtil.isNotEmpty(value)) {
        result.put(shortName, value);
      }
    }
    return result;
  }

  @Nullable
  public static String getPropertyShortName(final String name) {
    return name != null && name.startsWith(HibernatePropertiesConstants.HIBERNATE_PREFIX) ?
           name.substring(HibernatePropertiesConstants.HIBERNATE_PREFIX.length()) : name;
  }

  @Nullable
  public static Property<Object> findSessionFactoryProperty(final SessionFactory factory, final String propertyName) {
    final String propertyShortName = getPropertyShortName(propertyName);
    for (Property<Object> property : factory.getProperties()) {
      final String name = property.getName().getValue();
      final String shortName = getPropertyShortName(name);
      if (StringUtil.isNotEmpty(shortName) && Comparing.strEqual(propertyShortName, shortName)) {
        return property;
      }
    }
    return null;
  }

  public static Property<Object> setSessionFactoryProperty(final SessionFactory factory, final String propertyName, final String value) {
    final Property<Object> existingProperty = findSessionFactoryProperty(factory, propertyName);
    final Property<Object> property = existingProperty == null ? factory.addProperty() : existingProperty;
    if (existingProperty == null) property.getName().setStringValue(propertyName);
    property.setStringValue(value);
    return property;
  }

  public static String getDefaultDialectValue(final PsiElement element, final String jdbcUrl) {
    final StringTokenizer st = new StringTokenizer(jdbcUrl, ":");
    if (st.hasMoreTokens()) st.nextToken(); else  return "";
    if (!st.hasMoreTokens()) return "";
    final String token = st.nextToken().toLowerCase();
    final ArrayList<String> dialects = new ArrayList<String>();
    for (PsiReference reference : element.getReferences()) {
      final Object[] variants = reference.getVariants();
      for (Object variant : variants) {
        final Object obj = variant instanceof LookupElement ? ((LookupElement)variant).getObject() : null;
        final String variantStr = obj instanceof PsiClass? ((PsiClass)obj).getQualifiedName() : null;
        if (variantStr != null && variantStr.toLowerCase().contains(token)) {
          dialects.add(variantStr);
        }
      }
    }
    if (dialects.isEmpty()) return "";
    else {
      Collections.sort(dialects);
      return dialects.get(0);
    }
  }
}
