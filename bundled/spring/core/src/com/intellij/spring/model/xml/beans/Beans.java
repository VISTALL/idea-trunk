// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/beans:beansElemType interface.
 */
@NameStrategyForAttributes(HyphenNameStrategy.class)
@Namespace(SpringConstants.BEANS_NAMESPACE_KEY)
public interface Beans extends DomElement {

  /**
   * Returns the value of the default-lazy-init child.
   * <pre>
   * <h3>Attribute null:default-lazy-init documentation</h3>
   * 	The default 'lazy-init' value; see the documentation for the
   * 	'lazy-init' attribute of the '<bean>/' element.
   * <p/>
   * </pre>
   *
   * @return the value of the default-lazy-init child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getDefaultLazyInit();


  /**
   * Returns the value of the default-merge child.
   * <pre>
   * <h3>Attribute null:default-merge documentation</h3>
   * 	The default 'merge' value; see the documentation for the
   * 	'merge' attribute of the various collection elements.
   * <p/>
   * </pre>
   *
   * @return the value of the default-merge child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getDefaultMerge();


  /**
   * Returns the value of the default-dependency-check child.
   * <pre>
   * <h3>Attribute null:default-dependency-check documentation</h3>
   * 	The default 'dependency-check' value; see the documentation for the
   * 	'dependency-check' attribute of the '<bean>/' element.
   * <p/>
   * </pre>
   *
   * @return the value of the default-dependency-check child.
   */
  @NotNull
  GenericAttributeValue<DefaultDependencyCheck> getDefaultDependencyCheck();


  /**
   * Returns the value of the default-autowire child.
   * <pre>
   * <h3>Attribute null:default-autowire documentation</h3>
   * 	The default 'autowire' value; see the documentation for the
   * 	'autowire' attribute of the '<bean>/' element.
   * <p/>
   * </pre>
   *
   * @return the value of the default-autowire child.
   */
  @NotNull
  GenericAttributeValue<DefaultAutowire> getDefaultAutowire();


  /**
   * Returns the value of the default-init-method child.
   * <pre>
   * <h3>Attribute null:default-init-method documentation</h3>
   * 	The default 'init-method' value; see the documentation for the
   * 	'init-method' attribute of the '<bean>/' element.
   * <p/>
   * </pre>
   *
   * @return the value of the default-init-method child.
   */
  @NotNull
  GenericAttributeValue<String> getDefaultInitMethod();


  /**
   * Returns the value of the default-destroy-method child.
   * <pre>
   * <h3>Attribute null:default-destroy-method documentation</h3>
   * 	The default 'destroy-method' value; see the documentation for the
   * 	'destroy-method' attribute of the '<bean>/' element.
   * <p/>
   * </pre>
   *
   * @return the value of the default-destroy-method child.
   */
  @NotNull
  GenericAttributeValue<String> getDefaultDestroyMethod();


  /**
   * Returns the value of the description child.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:description documentation</h3>
   * 	Contains informative text describing the purpose of the enclosing
   * 	element.
   * 	Used primarily for user documentation of XML bean definition documents.
   * <p/>
   * </pre>
   *
   * @return the value of the description child.
   */
  @NotNull
  GenericDomValue<String> getDescription();


  /**
   * Returns the list of import children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:import documentation</h3>
   * 	Specifies an XML bean definition resource to import.
   * <p/>
   * </pre>
   *
   * @return the list of import children.
   */
  @NotNull
  List<SpringImport> getImports();

  /**
   * Adds new child to the list of import children.
   *
   * @return created child
   */
  SpringImport addImport();


  /**
   * Returns the list of alias children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:alias documentation</h3>
   * 	Defines an alias for a bean (which can reside in a different definition
   * 	resource).
   * <p/>
   * </pre>
   *
   * @return the list of alias children.
   */
  @NotNull
  @SubTagList(value = "alias")
  List<Alias> getAliases();

  /**
   * Adds new child to the list of alias children.
   *
   * @return created child
   */
  Alias addAlias();

  /**
   * Returns the list of bean children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:bean documentation</h3>
   * 	Defines a single (usually named) bean.
   * 	A bean definition may contain nested tags for constructor arguments,
   * 	property values, lookup methods, and replaced methods. Mixing constructor
   * 	injection and setter injection on the same bean is explicitly supported.
   * <p/>
   * </pre>
   *
   * @return the list of bean children.
   */
  @NotNull
  List<SpringBean> getBeans();

  /**
   * Adds new child to the list of bean children.
   *
   * @return created child
   */
  SpringBean addBean();

  @CustomChildren
  List<CustomBeanWrapper> getCustomBeans();

}
