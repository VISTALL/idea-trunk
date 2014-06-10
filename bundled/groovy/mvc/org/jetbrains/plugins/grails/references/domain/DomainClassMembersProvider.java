package org.jetbrains.plugins.grails.references.domain;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo;
import org.jetbrains.plugins.grails.util.DomainClassUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrListOrMap;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentLabel;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

import java.util.*;

/**
 * @author ilyas
 */
public class DomainClassMembersProvider {

  private final Project myProject;

  // Properties
  @NonNls public static final String[] DOMAIN_PROPERTY_NAMES = {
    "public final Long id\n",
    "public final Long version\n"
  };
  @NonNls public static final String[] DOMAIN_DYNAMIC_PROPERTIES = {
    "org.springframework.validation.Errors errors\n",
    "java.util.List<org.codehaus.groovy.grails.validation.ConstrainedProperty> constraints\n"
  };
  @NonNls public static final String[] DOMAIN_OPTIONAL_PROPERTY_NAMES = {"hasMany", "belongsTo", "relatesToMany", "constraints"};
  @NonNls public static final String[] DOMAIN_DYNAMIC_PROPERTY_NAMES = {"errors", "constraints", "properties"};

  @NonNls static final String[] DOMAIN_STATIC_FIND_ALL_METHODS_SOURCES = {
    "findAll() {}\n",
    "findAll(Object example) {}\n",
    "findAll(Object example, java.util.Map args) {}\n",

    "findAll(String query) {}\n",
    "findAll(String query, java.util.Collection positionalParams) {}\n",
    "findAll(String query, java.util.Collection positionalParams, java.util.Map paginateParams) {}\n",
    "findAll(String query, java.util.Map namedArgs) {}\n",
    "findAll(String query, java.util.Map namedArgs, java.util.Map paginateParams) {}\n",

    "list() {}\n",
    "list(java.util.Map args) {}\n",

    "findAllWhere(java.util.Map query) {}\n",

    "getAll() {}\n",
    "getAll(java.util.List<Long> ids) {}\n",

    "withCriteria(groovy.lang.Closure callable) {}\n",
    "withCriteria(java.util.Map builderArgs, groovy.lang.Closure callable) {}\n",

    "executeQuery(String query) {}\n",
    "executeQuery(String query, java.util.Collection positionalParams) {}\n",
    "executeQuery(String query, java.util.Collection positionalParams, java.util.Map paginateParams) {}\n",
    "executeQuery(String query, java.util.Map namedParams ) {}\n",
    "executeQuery(String query, java.util.Map namedParams, java.util.Map paginateParams) {}\n",
  };

  @NonNls static final String[] DOMAIN_STATIC_FIND_METHODS_SOURCES={
    "find(String query) {}\n",
    "find(String query, java.util.Collection args) {}\n",
    "find(String query, java.util.Map namedArgs) {}\n",
    "find(Object example) {}\n",
    "findWhere(java.util.Map query) {}\n",
    "get(Long id){}\n",
    "read(Long id){}\n"
  };

  @NonNls static final String[] DOMAIN_STATIC_METHODS_SOURCES ={
    "public static int executeUpdate(String query) {}\n",
    "public static int executeUpdate(String query, java.util.Collection args) {}\n",
    "public static int executeUpdate(String query, java.util.Map argMap) {}\n",

    "public static boolean exists(Long id) {}\n",
    "public static grails.orm.HibernateCriteriaBuilder createCriteria() {}\n",

    "public static Long count(){}\n",
    "public static void withTransaction(groovy.lang.Closure callable){}\n",
    "public static void withNewSession(groovy.lang.Closure callable){}\n",
    "public static void withSession(groovy.lang.Closure callable){}\n",
    "public static void lock(java.io.Serializable id){}\n",

    "public static Object methodMissing(String name, java.util.Map args){}\n",
  };

  @NonNls static final String[] DOMAIN_DYNAMIC_METHODS_SOURCES = {
    "public Object methodMissing(String name, java.util.Map args){}\n",

    "public boolean validate(){}\n",
    "public boolean validate(java.util.Map args){}\n",
    "public boolean validate(){Boolean b}\n",
    "public boolean validate(java.util.List args){}\n",

    "public void lock(){}\n",

    "public void delete(){}\n",
    "public void delete(java.util.Map args){}\n",

    "public boolean isAttached(){}\n",
    "public boolean hasErrors(){}\n",

  };

  @NonNls static final String[] DOMAIN_DYNAMIC_FIND_METHODS_SOURCES = {
    "save(){}\n",
    "save(java.util.Map args){}\n",
    "save(Boolean validate){}\n",

    "merge(){}\n",
    "merge(java.util.Map args){}\n",

    "clearErrors(){}\n",
    "ident(){}\n",

    "refresh(){}\n",
    "discard(){}\n",

    "attach(){}\n",
  };


  @NonNls private final PsiMethod[] myDCStaticMethods = new PsiMethod[
    DOMAIN_STATIC_METHODS_SOURCES.length];
  @NonNls private final PsiMethod[] myDCDynamicMethods = new PsiMethod[DOMAIN_DYNAMIC_METHODS_SOURCES.length];
  @NonNls private final PsiField[] myDCInstanceFields;

  /**
   * hack for preventing stack overflow
   */
  private static final Set<PsiClass> unSOF = new com.intellij.util.containers.hash.HashSet<PsiClass>();

  public static DomainClassMembersProvider getInstance(Project project) {
    return ServiceManager.getService(project, DomainClassMembersProvider.class);
  }

  public DomainClassMembersProvider(Project project) {
    myProject = project;

    final GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(myProject);

    for (int i = 0; i < myDCDynamicMethods.length; i++) {
      myDCDynamicMethods[i] = factory.createMethodFromText(DOMAIN_DYNAMIC_METHODS_SOURCES[i]);
    }

    for (int i = 0; i < DOMAIN_STATIC_METHODS_SOURCES.length; i++) {
      myDCStaticMethods[i] = factory.createMethodFromText(DOMAIN_STATIC_METHODS_SOURCES[i]);
    }

    StringBuilder classText = new StringBuilder();
    classText.append("class DummyDomainClass {\n");
    for (String name : DOMAIN_PROPERTY_NAMES) classText.append(name).append('\n');
    for (String name : DOMAIN_DYNAMIC_PROPERTIES) classText.append(name).append('\n');

    final GroovyFile file = factory.createGroovyFile(classText.toString(), false, null);
    final PsiClass clazz = file.getClasses()[0];
    myDCInstanceFields = clazz.getFields();
  }

  public PsiMethod[] getDCInstanceMethods(PsiClass domainClass) {
    ArrayList<PsiMethod> methods = new ArrayList<PsiMethod>();
    Collections.addAll(methods, myDCDynamicMethods);

    final GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(myProject);

    final String name = domainClass.getQualifiedName();
    for (String methodsSource : DOMAIN_DYNAMIC_FIND_METHODS_SOURCES) {
      methods.add(factory.createMethodFromText("public " + name + " " + methodsSource));
    }

    for (Trinity<String, PsiClass, GrField> trinity : findAllProperties(
      domainClass.findFieldByName(DomainClassRelationsInfo.HAS_MANY_NAME, true), domainClass)) {
      if (trinity.third == null || InheritanceUtil.isInheritor(trinity.third.getTypeGroovy(), CommonClassNames.JAVA_UTIL_COLLECTION)) {
        final String paramName = StringUtil.decapitalize(trinity.second.getName());
        final String propertyName = StringUtil.capitalize(trinity.first);
        final String qName = trinity.second.getQualifiedName();
        methods.add(factory.createMethodFromText("public void addTo" + propertyName + '(' + qName + ' ' + paramName + "){}"));
        methods.add(factory.createMethodFromText("public void removeFrom" + propertyName + '(' + qName + ' ' + paramName + "){}"));
      }
    }

    return methods.toArray(new PsiMethod[methods.size()]);
  }

  public PsiMethod[] getDCStaticMethods(PsiClass domainClass) {
    PsiMethod[] dynMethods = new PsiMethod[DOMAIN_STATIC_METHODS_SOURCES.length +
                                           DOMAIN_STATIC_FIND_ALL_METHODS_SOURCES.length +
                                           DOMAIN_STATIC_FIND_METHODS_SOURCES.length];
    GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(myProject);
    System.arraycopy(myDCStaticMethods, 0, dynMethods, 0, myDCStaticMethods.length);
    int i = myDCStaticMethods.length;
    final String qname = domainClass.getQualifiedName();
    for (String method : DOMAIN_STATIC_FIND_ALL_METHODS_SOURCES) {
      dynMethods[i++] = factory.createMethodFromText("public static java.util.List<" + qname + "> " + method);
    }
    for (String method : DOMAIN_STATIC_FIND_METHODS_SOURCES) {
      dynMethods[i++] = factory.createMethodFromText("public static " + qname + " " + method);
    }
    return dynMethods;
  }

  public PsiField[] getDCInstanceFields(PsiClass domainClass) {
    ArrayList<PsiField> fields = new ArrayList<PsiField>(Arrays.asList(myDCInstanceFields));
    addFieldsFromBelongsTo(domainClass, fields);
    addFieldsFromHasMany(domainClass, fields);
    return fields.toArray(new PsiField[fields.size()]);
  }

  private void addFieldsFromBelongsTo(PsiClass domainClass, ArrayList<PsiField> fields) {
    final GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(myProject);
    final PsiElementFactory jfactory = JavaPsiFacade.getElementFactory(myProject);

    final PsiField belongs = domainClass.findFieldByName(DomainClassRelationsInfo.BELONGS_TO_NAME, true);
    for (Trinity<String, PsiClass, GrField> trinity : findAllProperties(belongs, domainClass)) {
      if (trinity.third == null) {
        final PsiType type = jfactory.createType(trinity.second);
        fields.add((PsiField)factory.createFieldDeclaration(ArrayUtil.EMPTY_STRING_ARRAY, trinity.first, null, type).getVariables()[0]);
      }
    }
  }

  private void addFieldsFromHasMany(PsiClass domainClass, ArrayList<PsiField> fields) {
    final PsiElementFactory jfactory = JavaPsiFacade.getElementFactory(myProject);
    final GroovyPsiElementFactory factory = GroovyPsiElementFactory.getInstance(myProject);

    final PsiField hasMany = domainClass.findFieldByName(DomainClassRelationsInfo.HAS_MANY_NAME, true);
    for (Trinity<String, PsiClass, GrField> trinity : findAllProperties(hasMany, domainClass)) {
      if (trinity.third == null) {
        final PsiClassType type = (PsiClassType)jfactory
          .createTypeFromText(CommonClassNames.JAVA_UTIL_SET + '<' + trinity.second.getQualifiedName() + '>', domainClass);
        fields
          .add((PsiField)factory.createFieldDeclaration(ArrayUtil.EMPTY_STRING_ARRAY, trinity.first, null, type).getVariables()[0]);
      }
    }
  }

  private static List<Trinity<String, PsiClass, GrField>> findAllProperties(PsiField field, PsiClass domainClass) {
    if (!(field instanceof GrField) || !field.hasModifierProperty(PsiModifier.STATIC)) return Collections.emptyList();

    final GrExpression initializer = ((GrField)field).getInitializerGroovy();
    if (!(initializer instanceof GrListOrMap) || !((GrListOrMap)initializer).isMap()) return Collections.emptyList();

    final GrListOrMap lom = (GrListOrMap)initializer;

    List<Trinity<String, PsiClass, GrField>> result=new ArrayList<Trinity<String, PsiClass, GrField>>();
    for (GrNamedArgument argument : lom.getNamedArguments()) {
      final GrArgumentLabel label = argument.getLabel();
      final GrExpression expr = argument.getExpression();
      if (label != null && expr instanceof GrReferenceExpression) {
        GrReferenceExpression ref = (GrReferenceExpression)expr;
        final String name = label.getName();
        final PsiElement resolved = ref.resolve();
        if (resolved instanceof PsiClass && DomainClassUtils.isDomainClass(((PsiClass)resolved))) {
          result.add(new Trinity<String, PsiClass, GrField>(name, (PsiClass)resolved, resolveLabel(label, domainClass)));
        }
      }
    }
    return result;
  }

  @Nullable
  private static GrField resolveLabel(GrArgumentLabel label, PsiClass domainClass) {
    final PsiField field = domainClass.findFieldByName(label.getName(), true);
    return field instanceof GrField ? (GrField)field : null;
  }
}
