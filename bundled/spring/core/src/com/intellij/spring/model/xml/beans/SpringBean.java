// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.spring.model.ResolvedConstructorArgs;
import com.intellij.spring.model.converters.*;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * http://www.springframework.org/schema/beans:beanElemType interface.
 */
@Namespace(SpringConstants.BEANS_NAMESPACE_KEY)
public interface SpringBean extends DomSpringBean, ScopedElement {

   SpringDomQualifier getQualifier();

  /**
   * Returns the list of all bean properties
   * defined in current bean and parent beans("parent" attribute).
   *
   * @return the list of all properties.
   */
  List<SpringPropertyDefinition> getAllProperties();

  /**
   * Returns the list of all constructor args
   * defined in current bean and parent beans("parent" attribute).
   *
   * @return the list of all constructor args.
   */
  Set<ConstructorArg> getAllConstructorArgs();

  @NotNull
  ResolvedConstructorArgs getResolvedConstructorArgs();

  boolean isAbstract();

  Autowire getBeanAutowire();
  /**
   * Returns the value of the name child.
   * <pre>
   * <h3>Attribute null:name documentation</h3>
   * 	Can be used to create one or more aliases illegal in an (XML) id.
   * 	Multiple aliases can be separated by any number of spaces, commas,
   * 	or semi-colons (or indeed any mixture of the three).
   * <p/>
   * </pre>
   *
   * @return the value of the name child.
   */
  @NotNull
  @Convert(value = SpringBeanNamesConverter.class)
  GenericAttributeValue<List<String>> getName();


  /**
   * Returns the value of the class child.
   * <pre>
   * <h3>Attribute null:class documentation</h3>
   * 	The fully qualified name of the bean's class, except if it pure serves as parent for child bean definitions.
   * <p/>
   * </pre>
   *
   * @return the value of the class child.
   */
  @NotNull
  @Attribute("class")
  @Required(value = false, nonEmpty = true)
  @ExtendClass(allowInterface = false, instantiatable = false)
  GenericAttributeValue<PsiClass> getClazz();


  /**
   * Returns the value of the parent child.
   * <pre>
   * <h3>Attribute null:parent documentation</h3>
   * 	The name of the parent bean definition.
   * 	Will use the bean class of the parent if none is specified, but can
   * 	also override it. In the latter case, the child bean class must be
   * 	compatible with the parent, i.e. accept the parent's property values
   * 	and constructor argument values, if any.
   * 	A child bean definition will inherit constructor argument values,
   * 	property values and method overrides from the parent, with the option
   * 	to add new values. If init method, destroy method, factory bean and/or
   * 	factory method are specified, they will override the corresponding
   * 	parent settings.
   * 	The remaining settings will always be taken from the child definition:
   * 	depends on, autowire mode, dependency check, scope, lazy init.
   * <p/>
   * </pre>
   *
   * @return the value of the parent child.
   */
  @NotNull
  @Attribute("parent")
  @Convert(value = SpringBeanResolveConverter.Parent.class)
  GenericAttributeValue<SpringBeanPointer> getParentBean();

  /**
   * Returns the value of the abstract child.
   * <pre>
   * <h3>Attribute null:abstract documentation</h3>
   * 	Is this bean "abstract", that is, not meant to be instantiated itself
   * 	but rather just serving as parent for concrete child bean definitions?
   * 	The default is "false". Specify "true" to tell the bean factory to not
   * 	try to instantiate that particular bean in any case.
   * 	Note: This attribute will not be inherited by child bean definitions.
   * 	Hence, it needs to be specified per abstract bean definition.
   * <p/>
   * </pre>
   *
   * @return the value of the abstract child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getAbstract();

  GenericAttributeValue<Boolean> getSingleton();
  
  /**
   * Returns the value of the lazy-init child.
   * <pre>
   * <h3>Attribute null:lazy-init documentation</h3>
   * 	Indicates whether or not this bean is to be lazily initialized.
   * 	If false, it will be instantiated on startup by bean factories
   * 	that perform eager initialization of singletons. The default is
   * 	"false".
   * 	Note: This attribute will not be inherited by child bean definitions.
   * 	Hence, it needs to be specified per concrete bean definition.
   * <p/>
   * </pre>
   *
   * @return the value of the lazy-init child.
   */
  @NotNull
  GenericAttributeValue<DefaultableBoolean> getLazyInit();


  /**
   * Returns the value of the autowire child.
   * <pre>
   * <h3>Attribute null:autowire documentation</h3>
   * 	Controls whether bean properties are "autowired".
   * 	This is an automagical process in which bean references don't need
   * 	to be coded explicitly in the XML bean definition file, but rather the
   * 	Spring container works out dependencies.
   * 	There are 5 modes:
   * 	1. "no"
   * 	The traditional Spring default. No automagical wiring. Bean references
   * 	must be defined in the XML file via the <ref/> element (or "ref"
   * 	attribute). We recommend this in most cases as it makes documentation
   * 	more explicit.
   * 	2. "byName"
   * 	Autowiring by property name. If a bean of class Cat exposes a dog
   * 	property, Spring will try to set this to the value of the bean "dog"
   * 	in the current container. If there is no matching bean by name, nothing
   * 	special happens; use dependency-check="objects" to raise an error in
   * 	that case.
   * 	3. "byType"
   * 	Autowiring if there is exactly one bean of the property type in the
   * 	container. If there is more than one, a fatal error is raised, and
   * 	you cannot use byType autowiring for that bean. If there is none,
   * 	nothing special happens; use dependency-check="objects" to raise an
   * 	error in that case.
   * 	4. "constructor"
   * 	Analogous to "byType" for constructor arguments. If there is not exactly
   * 	one bean of the constructor argument type in the bean factory, a fatal
   * 	error is raised.
   * 	5. "autodetect"
   * 	Chooses "constructor" or "byType" through introspection of the bean
   * 	class. If a default constructor is found, "byType" gets applied.
   * 	Note that explicit dependencies, i.e. "property" and "constructor-arg"
   * 	elements, always override autowiring. Autowire behavior can be combined
   * 	with dependency checking, which will be performed after all autowiring
   * 	has been completed.
   * 	Note: This attribute will not be inherited by child bean definitions.
   * 	Hence, it needs to be specified per concrete bean definition.
   * <p/>
   * </pre>
   *
   * @return the value of the autowire child.
   */
  @NotNull
  GenericAttributeValue<Autowire> getAutowire();


  /**
   * Returns the value of the dependency-check child.
   * <pre>
   * <h3>Attribute null:dependency-check documentation</h3>
   * 	Controls whether or not to check whether all of this
   * 	beans dependencies, expressed in its properties, are satisfied.
   * 	The default is to perform no dependency checking.
   * 	"simple" type dependency checking includes primitives and String
   * 	"object" includes collaborators (other beans in the factory)
   * 	"all" includes both types of dependency checking
   * 	Note: This attribute will not be inherited by child bean definitions.
   * 	Hence, it needs to be specified per concrete bean definition.
   * <p/>
   * </pre>
   *
   * @return the value of the dependency-check child.
   */
  @NotNull
  GenericAttributeValue<DependencyCheck> getDependencyCheck();


  /**
   * Returns the value of the depends-on child.
   * <pre>
   * <h3>Attribute null:depends-on documentation</h3>
   * 	The names of the beans that this bean depends on being initialized.
   * 	The bean factory will guarantee that these beans get initialized
   * 	before this bean.
   * 	Note that dependencies are normally expressed through bean properties
   * 	or constructor arguments. This property should just be necessary for
   * 	other kinds of dependencies like statics (*ugh*) or database preparation
   * 	on startup.
   * 	Note: This attribute will not be inherited by child bean definitions.
   * 	Hence, it needs to be specified per concrete bean definition.
   * <p/>
   * </pre>
   *
   * @return the value of the depends-on child.
   */
  @NotNull
  @Convert(value = SpringBeanListConverter.class)
  GenericAttributeValue<List<SpringBean>> getDependsOn();


  /**
   * Returns the value of the init-method child.
   * <pre>
   * <h3>Attribute null:init-method documentation</h3>
   * 	The name of the custom initialization method to invoke after setting
   * 	bean properties. The method must have no arguments, but may throw any
   * 	exception.
   * <p/>
   * </pre>
   *
   * @return the value of the init-method child.
   */
  @NotNull
  @Convert(value = SpringBeanInitMethodConverter.class)
  GenericAttributeValue<PsiMethod> getInitMethod();


  /**
   * Returns the value of the destroy-method child.
   * <pre>
   * <h3>Attribute null:destroy-method documentation</h3>
   * 	The name of the custom destroy method to invoke on bean factory
   * 	shutdown. The method must have no arguments, but may throw any
   * 	exception.
   * 	Note: Only invoked on beans whose lifecycle is under the full
   * 	control of the factory - which is always the case for singletons,
   * 	but not guaranteed for any other scope.
   * <p/>
   * </pre>
   *
   * @return the value of the destroy-method child.
   */
  @NotNull
  @Convert(value = SpringBeanDestroyMethodConverter.class)
  GenericAttributeValue<PsiMethod> getDestroyMethod();


  /**
   * Returns the value of the factory-method child.
   * <pre>
   * <h3>Attribute null:factory-method documentation</h3>
   * 	The name of a factory method to use to create this object. Use
   * 	constructor-arg elements to specify arguments to the factory method,
   * 	if it takes arguments. Autowiring does not apply to factory methods.
   * 	If the "class" attribute is present, the factory method will be a static
   * 	method on the class specified by the "class" attribute on this bean
   * 	definition. Often this will be the same class as that of the constructed
   * 	object - for example, when the factory method is used as an alternative
   * 	to a constructor. However, it may be on a different class. In that case,
   * 	the created object will *not* be of the class specified in the "class"
   * 	attribute. This is analogous to FactoryBean behavior.
   * 	If the "factory-bean" attribute is present, the "class" attribute is not
   * 	used, and the factory method will be an instance method on the object
   * 	returned from a getBean call with the specified bean name. The factory
   * 	bean may be defined as a singleton or a prototype.
   * 	The factory method can have any number of arguments. Autowiring is not
   * 	supported. Use indexed constructor-arg elements in conjunction with the
   * 	factory-method attribute.
   * 	Setter Injection can be used in conjunction with a factory method.
   * 	Method Injection cannot, as the factory method returns an instance,
   * 	which will be used when the container creates the bean.
   * <p/>
   * </pre>
   *
   * @return the value of the factory-method child.
   */
  @NotNull
  @Convert(value = SpringBeanFactoryMethodConverter.class)
  GenericAttributeValue<PsiMethod> getFactoryMethod();


  /**
   * Returns the value of the factory-bean child.
   * <pre>
   * <h3>Attribute null:factory-bean documentation</h3>
   * 	Alternative to class attribute for factory-method usage.
   * 	If this is specified, no class attribute should be used.
   * 	This must be set to the name of a bean in the current or
   * 	ancestor factories that contains the relevant factory method.
   * 	This allows the factory itself to be configured using Dependency
   * 	Injection, and an instance (rather than static) method to be used.
   * <p/>
   * </pre>
   *
   * @return the value of the factory-bean child.
   */
  @NotNull
  @Convert(value = SpringBeanResolveConverter.class)
  GenericAttributeValue<SpringBeanPointer> getFactoryBean();


  /**
   * Returns the value of the autowire-candidate child.
   * <pre>
   * <h3>Attribute null:autowire-candidate documentation</h3>
   * 	Indicates whether or not this bean should be considered when looking
   * 	for candidates to satisfy another beans autowiring requirements.
   * <p/>
   * </pre>
   *
   * @return the value of the autowire-candidate child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getAutowireCandidate();


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
   * Returns the list of meta children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:meta documentation</h3>
   * 	Arbitrary metadata attached to a bean definition.
   * <p/>
   * </pre>
   *
   * @return the list of meta children.
   */
  @NotNull
  List<Meta> getMetas();

  /**
   * Adds new child to the list of meta children.
   *
   * @return created child
   */
  Meta addMeta();


  /**
   * Returns the list of constructor-arg children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:constructor-arg documentation</h3>
   * 	Bean definitions can specify zero or more constructor arguments.
   * 	This is an alternative to "autowire constructor".
   * 	Arguments correspond to either a specific index of the constructor
   * 	argument list or are supposed to be matched generically by type.
   * 	Note: A single generic argument value will just be used once, rather
   * 	than potentially matched multiple times (as of Spring 1.1).
   * 	constructor-arg elements are also used in conjunction with the
   * 	factory-method element to construct beans using static or instance
   * 	factory methods.
   * <p/>
   * </pre>
   *
   * @return the list of constructor-arg children.
   */
  @NotNull
  List<ConstructorArg> getConstructorArgs();

  /**
   * Adds new child to the list of constructor-arg children.
   *
   * @return created child
   */
  ConstructorArg addConstructorArg();


  /**
   * Returns the list of property children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:property documentation</h3>
   * 	Bean definitions can have zero or more properties.
   * 	Property elements correspond to JavaBean setter methods exposed
   * 	by the bean classes. Spring supports primitives, references to other
   * 	beans in the same or related factories, lists, maps and properties.
   * <p/>
   * </pre>
   *
   * @return the list of property children.
   */
  @NotNull
  List<SpringProperty> getProperties();

  /**
   * Adds new child to the list of property children.
   *
   * @return created child
   */
  SpringProperty addProperty();


  /**
   * Returns the list of lookup-method children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:lookup-method documentation</h3>
   * 	A lookup method causes the IoC container to override the given method
   * 	and return the bean with the name given in the bean attribute. This is
   * 	a form of Method Injection. It is particularly useful as an alternative
   * 	to implementing the BeanFactoryAware interface, in order to be able to
   * 	make getBean() calls for non-singleton instances at runtime. In this
   * 	case, Method Injection is a less invasive alternative.
   * <p/>
   * </pre>
   *
   * @return the list of lookup-method children.
   */
  @NotNull
  List<LookupMethod> getLookupMethods();

  /**
   * Adds new child to the list of lookup-method children.
   *
   * @return created child
   */
  LookupMethod addLookupMethod();


  /**
   * Returns the list of replaced-method children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:replaced-method documentation</h3>
   * 	Similar to the lookup method mechanism, the replaced-method element
   * 	is used to control IoC container method overriding: Method Injection.
   * 	This mechanism allows the overriding of a method with arbitrary code.
   * <p/>
   * </pre>
   *
   * @return the list of replaced-method children.
   */
  @NotNull
  List<ReplacedMethod> getReplacedMethods();

  /**
   * Adds new child to the list of replaced-method children.
   *
   * @return created child
   */
  ReplacedMethod addReplacedMethod();

  @Nullable
  PsiClass getBeanClass(boolean considerFactories);

  String toString();
}
