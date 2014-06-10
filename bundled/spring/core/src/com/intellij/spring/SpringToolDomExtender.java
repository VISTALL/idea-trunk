/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
       */

package com.intellij.spring;

import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.beans.MetadataPropertyValueConverter;
import com.intellij.spring.model.xml.beans.MetadataRefValue;
import com.intellij.spring.model.xml.beans.MetadataValue;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.util.xml.*;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtension;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.impl.schema.XmlAttributeDescriptorImpl;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * @author peter
  */
public class SpringToolDomExtender extends DomExtender<CustomBeanWrapper> {

   @Nullable
   public static XmlTag getToolAnnotationTag(@Nullable PsiElement declaration, boolean allowRecursion) {
     if (declaration instanceof XmlTag) {
       final XmlTag xmlTag = (XmlTag)declaration;
       final XmlTag[] tags = xmlTag.findSubTags("annotation", XmlUtil.XML_SCHEMA_URI);
       if (tags.length > 0) {
         final XmlTag[] tags1 = tags[0].findSubTags("appinfo", XmlUtil.XML_SCHEMA_URI);
         if (tags1.length > 0) {
           final XmlTag[] tags2 = tags1[0].findSubTags("annotation", SpringConstants.TOOL_NAMESPACE);
           if (tags2.length > 0) {
             return tags2[0];
           }
         }
       }
       final XmlAttribute attribute = xmlTag.getAttribute("type");
       if (allowRecursion && attribute != null) {
         final XmlAttributeValue value = attribute.getValueElement();
         if (value != null) {
           for (final PsiReference reference : value.getReferences()) {
             final PsiElement element = reference.resolve();
             if (element instanceof XmlTag) {
               final XmlTag annotationTag = getToolAnnotationTag(element, false);
               if (annotationTag != null) {
                 return annotationTag;
               }
             }
           }
         }
       }
     }
     return null;
   }

   public void registerExtensions(@NotNull final CustomBeanWrapper element, @NotNull final DomExtensionsRegistrar registrar) {
     final XmlTag tag = element.getXmlTag();
     assert tag != null;
     for (final XmlAttribute attribute : tag.getAttributes()) {
       final XmlAttributeDescriptor descriptor = attribute.getDescriptor();
       if (descriptor instanceof XmlAttributeDescriptorImpl) {
         final XmlTag annotationTag = getToolAnnotationTag(descriptor.getDeclaration(), true);
         if (annotationTag != null) {
           boolean ref = "ref".equals(annotationTag.getAttributeValue("kind"));
           final PsiClass expectedTypeClass = getExpectedTypeClass(element, annotationTag);
           if (expectedTypeClass != null) {
             final PsiClassType expectedType = JavaPsiFacade.getInstance(expectedTypeClass.getProject()).getElementFactory().createType(expectedTypeClass);
             final XmlName xmlName = new XmlName(attribute.getName());
             if (ref) {
               registrar.registerAttributeChildExtension(xmlName, MetadataRefValue.class).setConverter(new SpringBeanResolveConverter(){
                 @Nullable
                 public List<PsiClassType> getRequiredClasses(final ConvertContext context) {
                   return Arrays.asList(expectedType);
                 }
               });
             } else {
               if (CommonClassNames.JAVA_LANG_CLASS.equals(expectedType.getCanonicalText())) {
                 final DomExtension extension = registrar.registerAttributeChildExtension(xmlName, ParameterizedTypeImpl.make(
                   GenericAttributeValue.class, new Type[]{PsiClass.class}, null));
                 final XmlTag[] tags1 = annotationTag.findSubTags("assignable-to", SpringConstants.TOOL_NAMESPACE);
                 if (tags1.length > 0) {
                   final String assignableFrom = tags1[0].getAttributeValue("type");
                   if (assignableFrom != null) {
                     extension.addCustomAnnotation(new ExtendClassImpl(){
                       public String value() {
                         return assignableFrom;
                       }
                     });
                   }
                 }
               } else {
                 registrar.registerAttributeChildExtension(xmlName, MetadataValue.class).setConverter(new MetadataPropertyValueConverter(expectedType));
               }
             }
           }
         }
       }
     }
   }

   private static PsiClass getExpectedTypeClass(final CustomBeanWrapper element, final XmlTag annotationTag) {
     final XmlFile file = DomUtil.getFile(element);
     final XmlTag[] expectedTypeTags = annotationTag.findSubTags("expected-type", SpringConstants.TOOL_NAMESPACE);
     final String value = expectedTypeTags.length > 0 ? expectedTypeTags[0].getAttributeValue("type") : null;
     return DomJavaUtil.findClass(value != null ? value : CommonClassNames.JAVA_LANG_OBJECT, file, element.getModule(), null);
   }


}