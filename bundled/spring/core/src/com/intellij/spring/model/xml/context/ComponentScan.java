// Generated on Wed Oct 17 15:28:10 MSD 2007
// DTD/Schema  :    http://www.springframework.org/schema/context

package com.intellij.spring.model.xml.context;

import com.intellij.psi.PsiPackage;
import com.intellij.spring.model.converters.PackageListConverter;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * http://www.springframework.org/schema/context:component-scanElemType interface.
 */
public interface ComponentScan extends DomSpringBean, SpringContextElement {

	/**
	 * Returns the value of the base-package child.
	 * @return the value of the base-package child.
	 */
	@NotNull
	@Required
        @Convert(PackageListConverter.class)
        GenericAttributeValue<Collection<PsiPackage>> getBasePackage();


	/**
	 * Returns the value of the resource-pattern child.
	 * @return the value of the resource-pattern child.
	 */
	@NotNull
	GenericAttributeValue<String> getResourcePattern();


	/**
	 * Returns the value of the use-default-filters child.
	 * @return the value of the use-default-filters child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getUseDefaultFilters();


	/**
	 * Returns the value of the annotation-config child.
	 * @return the value of the annotation-config child.
	 */
	@NotNull
	GenericAttributeValue<Boolean> getAnnotationConfig();


	/**
	 * Returns the value of the name-generator child.
	 * @return the value of the name-generator child.
	 */
	@NotNull
	GenericAttributeValue<String> getNameGenerator();


	/**
	 * Returns the value of the scope-resolver child.
	 * @return the value of the scope-resolver child.
	 */
	@NotNull
	GenericAttributeValue<String> getScopeResolver();


	/**
	 * Returns the value of the scoped-proxy child.
	 * @return the value of the scoped-proxy child.
	 */
	@NotNull
	GenericAttributeValue<ScopedProxy> getScopedProxy();


	/**
	 * Returns the list of include-filter children.
	 * @return the list of include-filter children.
	 */
	@NotNull
	List<Filter> getIncludeFilters();
	/**
	 * Adds new child to the list of include-filter children.
	 * @return created child
	 */
	Filter addIncludeFilter();


	/**
	 * Returns the list of exclude-filter children.
	 * @return the list of exclude-filter children.
	 */
	@NotNull
	List<Filter> getExcludeFilters();
	/**
	 * Adds new child to the list of exclude-filter children.
	 * @return created child
	 */
	Filter addExcludeFilter();


}
