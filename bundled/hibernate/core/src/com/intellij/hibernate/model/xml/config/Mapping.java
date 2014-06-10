// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-configuration-3.0.dtd

package com.intellij.hibernate.model.xml.config;

import com.intellij.hibernate.model.converters.MappingClassResolveConverter;
import com.intellij.hibernate.model.converters.MappingFileConverter;
import com.intellij.hibernate.model.converters.MappingJarConverter;
import com.intellij.hibernate.model.converters.MappingResourceConverter;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.javaee.model.xml.converters.PackageNameConverter;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * hibernate-configuration-3.0.dtd:mapping interface.
 */
public interface Mapping extends CommonDomModelElement {

	/**
	 * Returns the value of the package child.
	 * Attribute package
	 * @return the value of the package child.
	 */
	@NotNull
        @Convert(PackageNameConverter.class)
        GenericAttributeValue<PsiPackage> getPackage();


	/**
	 * Returns the value of the file child.
	 * Attribute file
	 * @return the value of the file child.
	 */
	@NotNull
        @Convert(MappingFileConverter.class)
        GenericAttributeValue<PsiFile> getFile();


	/**
	 * Returns the value of the class child.
	 * Attribute class
	 * @return the value of the class child.
	 */
	@NotNull
	@com.intellij.util.xml.Attribute ("class")
        @Convert(MappingClassResolveConverter.class)
        GenericAttributeValue<PsiClass> getClazz();


	/**
	 * Returns the value of the jar child.
	 * Attribute jar
	 * @return the value of the jar child.
	 */
	@NotNull
        @Convert(MappingJarConverter.class)
        GenericAttributeValue<PsiFile> getJar();


	/**
	 * Returns the value of the resource child.
	 * Attribute resource
	 * @return the value of the resource child.
	 */
	@NotNull
        @Convert(MappingResourceConverter.class)  
        GenericAttributeValue<PsiFile> getResource();


}
