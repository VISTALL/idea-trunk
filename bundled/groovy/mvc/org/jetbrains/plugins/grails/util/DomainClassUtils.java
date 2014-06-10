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

package org.jetbrains.plugins.grails.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassNode;
import org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo;
import static org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo.Relation;
import static org.jetbrains.plugins.grails.perspectives.graph.DomainClassRelationsInfo.Relation.*;
import org.jetbrains.plugins.grails.references.domain.DomainClassMembersProvider;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.GrListOrMap;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrArgumentLabel;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.arguments.GrNamedArgument;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrString;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinitionBody;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrClassTypeElement;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeElement;
import org.jetbrains.plugins.groovy.lang.psi.util.GroovyPropertyUtils;

import java.util.*;

/**
 * User: Dmitry.Krasilschikov
 * Date: 06.08.2007
 */
public class DomainClassUtils {
  private static final String[] EMPTY_STRING_ARRAY = new String[0];
  private static final PsiField[] EMPTY_PSI_FIELD_ARRAY = new PsiField[0];

  @NonNls public static final String DOMAIN_FIND = "findBy";
  @NonNls public static final String DOMAIN_FIND_ALL = "findAllBy";
  @NonNls public static final String DOMAIN_COUNT = "countBy";
  @NonNls public static final String DOMAIN_LIST_ORDER = "listOrderBy";
  @NonNls public static final String[] DOMAIN_CONNECTIVES = {"Or", "And"};
  @NonNls public static final String[] DOMAIN_FINDER_EXPRESSIONS =
    {"LessThanEaquls", "LessThan", "GreaterThanEquals", "GreaterThan", "Between", "Like", "Ilike", "IsNotNull", "IsNull", "Not", "Equal",
      "NotEqual"};

  @NonNls public static final String[] DOMAIN_FINDER_EXPRESSIONS_WITH_ONE_PARAMETER =
    {"LessThan", "LessThanEaquls", "GreaterThan", "GreaterThanEquals", "Like", "Ilike", "Not", "Equal", "NotEqual"};

  public static final String[] FINDER_PREFICES = {DOMAIN_COUNT, DOMAIN_FIND, DOMAIN_FIND_ALL};

  private DomainClassUtils(){}

  private static PsiFile[] getDomainClasses(Project project, VirtualFile domainDirectory) {
    PsiDirectory domainPsiDirectory = PsiManager.getInstance(project).findDirectory(domainDirectory);
    return getAllDomainClasses(domainPsiDirectory);
  }

  private static PsiFile[] getAllDomainClasses(PsiDirectory domainDirectory) {
    List<PsiFile> children = new ArrayList<PsiFile>();
    final List<PsiFile> list = getAllChildrenRecursively(domainDirectory, children);
    return list.toArray(new PsiFile[list.size()]);
  }

  private static List<PsiFile> getAllChildrenRecursively(PsiDirectory domainDirectory, List<PsiFile> children) {
    final PsiFile[] files = domainDirectory.getFiles();
    for (PsiFile file : files) {
      if (file instanceof GroovyFile && !((GroovyFile)file).isScript()) {
        children.add(file);
      }
    }

    for (PsiDirectory file : domainDirectory.getSubdirectories()) {
      getAllChildrenRecursively(file, children);
    }

    return children;
  }

  public static Map<DomainClassNode, List<DomainClassRelationsInfo>> buildNodesAndEdges(Project project, VirtualFile domainDirectory) {
    if (project.isDisposed()) return Collections.emptyMap();

    PsiFile[] domainClasses = getDomainClasses(project, domainDirectory);
    Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges = new HashMap<DomainClassNode, List<DomainClassRelationsInfo>>();
    if (domainClasses == null) return Collections.emptyMap();

    for (PsiFile domainClass : domainClasses) {
      if (!(domainClass instanceof GroovyFile)) continue;
      GroovyFile groovyDomainClass = (GroovyFile)domainClass;

      GrTypeDefinition[] typeDefinitions = groovyDomainClass.getTypeDefinitions();

      for (GrTypeDefinition typeDefinition : typeDefinitions) {
        if (typeDefinition.getQualifiedName() == null) continue;

        buildMapForTypeDefinition(sourcesToOutEdges, typeDefinition);
      }
    }

    for (DomainClassNode node : sourcesToOutEdges.keySet()) {
      processRightsRelationOfThisClass(node.getTypeDefinition(), sourcesToOutEdges);
    }

    return sourcesToOutEdges;
  }

  public static void buildMapForTypeDefinition(Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges, GrTypeDefinition typeDefinition) {
    final GrTypeDefinitionBody body = typeDefinition.getBody();
    Set<String> transients = new HashSet<String>();

    if (body == null) return;
    GrField[] fields = body.getFields();

    buildTransients(fields, transients);

    //if this class doesn't connect with anything we have to add it in nodes set
    sourcesToOutEdges.put(new DomainClassNode(typeDefinition), new ArrayList<DomainClassRelationsInfo>());
    buildSourceToOutEdgesMapByFields(fields, sourcesToOutEdges, transients);
  }

  private static boolean isTransient(String varName, Set<String> transients) {
    return transients.contains(varName);
  }

  private static void buildTransients(GrField[] fields, Set<String> transients) {
    for (GrField field : fields) {
      PsiModifierList modifierList = field.getModifierList();
      assert modifierList != null;

      if (modifierList.hasModifierProperty(PsiModifier.STATIC) && DomainClassRelationsInfo.TRANSIENTS_NAME.equals(field.getName())) {

        GrExpression initializer = field.getInitializerGroovy();
        if (!(initializer instanceof GrListOrMap)) return;

        GrListOrMap list = (GrListOrMap)initializer;

        for (GrExpression expression : list.getInitializers()) {
          if (expression instanceof GrString) {
            String varNameStr = expression.getText();

            String varName = varNameStr.substring(1, varNameStr.length() - 2);

            transients.add(varName);
            continue;
          }

          transients.add(expression.getText());
        }
      }
    }
  }

  private static boolean buildSourceToOutEdgesMapByFields(GrField[] fields,
                                                          Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges,
                                                          Set<String> transients) {
    boolean wasAdded = false;
    for (GrField field : fields) {
      if (isTransient(field.getName(), transients)) continue;

      if (isBelongsToField(field)) {
        buildBelongsToSourcesToOutEdges(sourcesToOutEdges, field);
        wasAdded = true;
      }
      else if (isHasManyField(field)) {
        buildHasManySourcesToOutEdgesMap(sourcesToOutEdges, field);
        wasAdded = true;
      }
    }

    for (GrField field : fields) {
      if (isTransient(field.getName(), transients) || isBelongsToField(field) || isHasManyField(field)) continue;

      final PsiClass thisClass = field.getContainingClass();

      final List<DomainClassRelationsInfo> outEdges = getOutEdgesByTypeDef(thisClass, sourcesToOutEdges);

      final String typeCanonocalString = field.getType().getCanonicalText();
      final String fieldName = field.getName();

      boolean isStrong = true;
      if (outEdges != null) {
        for (DomainClassRelationsInfo edge : outEdges) {
          if (typeCanonocalString == null) return false;

          //if reference was in belongs to return
          if (BELONGS_TO == edge.getRelation() &&
              typeCanonocalString.equals(edge.getTarget().getTypeDefinition().getQualifiedName())) {
            isStrong = false;
            break;
          }

          //has many
          if (fieldName == null) return false;
          if (HAS_MANY == edge.getRelation() &&
              typeCanonocalString.equals(edge.getTarget().getTypeDefinition().getQualifiedName()) &&
              fieldName.equals(edge.getVarName())) {
            isStrong = false;
            break;
          }
        }
      }

      if (isStrong) {
        buildStrongSourceToOutEdgesMap(sourcesToOutEdges, field);
        wasAdded = true;
      }
      else {
        wasAdded = false;
      }
    }
    return wasAdded;
  }

  private static void processRightsRelationOfThisClass(final PsiClass thisClass,
                                                       final Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges) {
//    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
//      public void run() {
    processRightRelationsInNode(sourcesToOutEdges, thisClass);
    processRightRelationsBetweenNodes(sourcesToOutEdges, thisClass);
//      }
//    }, ModalityState.NON_MODAL);
  }

  private static synchronized void processRightRelationsBetweenNodes(Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges,
                                                                     PsiClass thisClass) {
    final DomainClassNode thisClassNode = new DomainClassNode(thisClass);
    final List<DomainClassRelationsInfo> thisOutEdges = sourcesToOutEdges.get(thisClassNode);

    if (thisOutEdges == null) return;
    final Iterator<DomainClassRelationsInfo> thisOutEdgeIterator = thisOutEdges.iterator();
    while (thisOutEdgeIterator.hasNext()) {
      DomainClassRelationsInfo thisOutEdge = thisOutEdgeIterator.next();

      final DomainClassNode target = thisOutEdge.getTarget();
      if (target.equals(thisClassNode)) continue;

      final List<DomainClassRelationsInfo> backs = sourcesToOutEdges.get(target);

      if (backs == null) continue;
      final Iterator<DomainClassRelationsInfo> backIt = backs.iterator();
      while (backIt.hasNext()) {
        DomainClassRelationsInfo back = backIt.next();

        if (thisClassNode.equals(back.getTarget())) {
          //Strong - Strong
          if (thisOutEdge.getRelation() == STRONG && back.getRelation() == STRONG) {
            back.setRelation(DOUBLESTRONG);

            thisOutEdgeIterator.remove();
            break;
          }
          else if (thisOutEdge.getRelation() == STRONG && back.getRelation() == BELONGS_TO) {
            //Strong - Belongs To
            backIt.remove();
          }
          else if (thisOutEdge.getRelation() == BELONGS_TO && back.getRelation() == STRONG) {
            //Belongs To - Strong
            thisOutEdgeIterator.remove();
            break;
          }
          else if (thisOutEdge.getRelation() == BELONGS_TO && back.getRelation() == BELONGS_TO) {
            //Belongs To - Belongs To

            thisOutEdgeIterator.remove();
            backIt.remove();
            break;
          }
          else if (thisOutEdge.getRelation() == HAS_MANY && back.getRelation() == HAS_MANY) {
            //Has Many - Has Many

            back.setRelation(MANY_TO_MANY);
            thisOutEdgeIterator.remove();
            break;
          }
          else if (thisOutEdge.getRelation() == MANY_TO_MANY /*&& back.getRelation() == HAS_MANY*/) {
            //Many To Many - Has Many

            backIt.remove();
            break;
          }
          else if (/*thisOutEdge.getRelation() == HAS_MANY &&*/ back.getRelation() == MANY_TO_MANY) {
            //Has many - Many To Many

            thisOutEdgeIterator.remove();
            break;
          }
          else if (thisOutEdge.getRelation() == HAS_MANY /*&& (back.getRelation() == BELONGS_TO || back.getRelation() == STRONG)*/) {
            //Has Many - Belongs To or Strong
            backIt.remove();
            break;
          }
          else if (back.getRelation() == HAS_MANY /*&& (thisOutEdge.getRelation() == BELONGS_TO || thisOutEdge.getRelation() == STRONG)*/) {
            //Belongs To or Strong - Has Many

            thisOutEdgeIterator.remove();
            break;
          } /*else if (thisOutEdge.getRelation() == MANY_TO_MANY && back.getRelation() == MANY_TO_MANY) {
            //Has many - Many To Many

            thisOutEdgeIterator.remove();
            break;
          }*/
        }
      }
    }
  }

  private static synchronized void processRightRelationsInNode(Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges,
                                                               PsiClass thisClass) {
    final List<DomainClassRelationsInfo> outEdges = sourcesToOutEdges.get(new DomainClassNode(thisClass));
    if (outEdges == null) return;

    int i = 0;
    while (i < outEdges.size()) {
      DomainClassRelationsInfo edge1 = outEdges.get(i);

      int j = i + 1;
      while (j < outEdges.size()) {
        DomainClassRelationsInfo edge2 = outEdges.get(j);
        if (edge1.getTarget().equals(edge2.getTarget())) {
          if (edge1.getRelation() == HAS_MANY && (edge2.getRelation() == BELONGS_TO || edge2.getRelation() == STRONG)) {
            outEdges.remove(edge2);
          }
          else if (edge2.getRelation() == HAS_MANY &&
                   (edge1.getRelation() == BELONGS_TO || edge1.getRelation() == STRONG)) {
            outEdges.remove(edge1);
            break;
          }
        }
        j++;
      }
      i++;
    }
  }

  private static List<DomainClassRelationsInfo> getOutEdgesByTypeDef(PsiClass thisClass,
                                                                     Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges) {
    for (DomainClassNode node : sourcesToOutEdges.keySet()) {
      if (node.getTypeDefinition().equals(thisClass)) return sourcesToOutEdges.get(node);
    }

    return new ArrayList<DomainClassRelationsInfo>();
  }

  public static boolean isBelongsToField(GrField field) {
    return field.hasModifierProperty(PsiModifier.STATIC) && DomainClassRelationsInfo.BELONGS_TO_NAME.equals(field.getName());
  }

  public static boolean isHasManyField(GrField field) {
    return field.hasModifierProperty(PsiModifier.STATIC) && DomainClassRelationsInfo.HAS_MANY_NAME.equals(field.getName());
  }

  private static void buildStrongSourceToOutEdgesMap(Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges,
                                                     GrField field) {
    final PsiClass sourceClass = field.getContainingClass();
    final String varName = field.getName();

    GrTypeElement type = field.getTypeElementGroovy();
    if (type == null) return;

    if (!(type instanceof GrClassTypeElement)) return;
    PsiReference targetClass = ((GrClassTypeElement)type).getReferenceElement();

    PsiElement psiClass = targetClass.resolve();
    if (!(psiClass instanceof PsiClass)) return;

    if (!isDomainClassFile(psiClass.getContainingFile().getVirtualFile(), psiClass.getProject())) {
      return;
    }

    addEdgeWithName(sourcesToOutEdges, varName, targetClass, STRONG, sourceClass);
  }

  public static void buildBelongsToSourcesToOutEdges(Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges,
                                                     GrField field) {
    GrExpression expression = field.getInitializerGroovy();
    if (expression == null) return;

    final PsiClass sourceClass = field.getContainingClass();
    if (sourceClass.getQualifiedName() == null) return;

    if (expression instanceof GrListOrMap) {
      //static belongsTo = [...]
      buildSourcesToOutEdgesMapFromListOrMap(sourcesToOutEdges, field.getInitializerGroovy(), BELONGS_TO, sourceClass);

    }
    else {
      //static belongsTo = Book
      findVarNameAndAddEdge(sourcesToOutEdges, expression, BELONGS_TO, sourceClass);
    }
  }

  public static void buildHasManySourcesToOutEdgesMap(Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges,
                                                      GrField field) {
    final PsiClass psiClass = field.getContainingClass();
    if (psiClass.getQualifiedName() == null) return;

    buildSourcesToOutEdgesMapFromListOrMap(sourcesToOutEdges, field.getInitializerGroovy(), HAS_MANY, psiClass);
  }

  private static void buildSourcesToOutEdgesMapFromListOrMap(Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges,
                                                             GrExpression initializer,
                                                             Relation relation,
                                                             PsiClass sourceClass) {
    String varName;

    if (!(initializer instanceof GrListOrMap)) return;

    GrListOrMap list = (GrListOrMap)initializer;
    GrExpression[] expressions = list.getInitializers();

    for (GrExpression expression : expressions) {
      findVarNameAndAddEdge(sourcesToOutEdges, expression, relation, sourceClass);
    }

    GrNamedArgument[] grNamedArguments = list.getNamedArguments();
    for (GrNamedArgument namedArgument : grNamedArguments) {
      //var name
      GrArgumentLabel argumentLabel = namedArgument.getLabel();
      if (argumentLabel == null) return;
      varName = argumentLabel.getName();

      GrExpression grExpression = namedArgument.getExpression();
      if (grExpression == null) return;

      addEdgeWithName(sourcesToOutEdges, varName, grExpression.getReference(), relation, sourceClass);
    }
  }

  private static void findVarNameAndAddEdge(Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges,
                                            GrExpression expression,
                                            Relation relation,
                                            PsiClass containingClass) {
    String varName;
    PsiReference reference = expression.getReference();
    if (reference == null) return;

    PsiElement targetClass = reference.resolve();
    if (!(targetClass instanceof PsiClass) || ((PsiClass)targetClass).getQualifiedName() == null) return;

    DomainClassNode target = new DomainClassNode((PsiClass)targetClass);
    varName = findBelongsToItemFieldName(containingClass, target.getUniqueName());
    addEdgeWithName(sourcesToOutEdges, varName, expression.getReference(), relation, containingClass);
  }

  private static void addEdgeWithName(Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges,
                                      String varName,
                                      PsiReference domainClassReference,
                                      Relation relation,
                                      PsiClass sourceClass) {
    DomainClassNode source = new DomainClassNode(sourceClass);

    if (domainClassReference == null) return;

    PsiElement targetClass = domainClassReference.resolve();
    if (!(targetClass instanceof PsiClass)) return;

    DomainClassNode target = new DomainClassNode((PsiClass)targetClass);
    DomainClassRelationsInfo outEdge = new DomainClassRelationsInfo(source, target, relation);
    outEdge.setVarName(varName);

    addOutEdgeToSourceMap(sourcesToOutEdges, source, outEdge);
  }

  private static String findBelongsToItemFieldName(PsiClass typeDefinition, String type) {
    final PsiField[] fields = typeDefinition.getFields();

    for (PsiField field : fields) {
      if (field.getType().equalsToText(type)) return field.getName();
    }

    return type;
  }

  private static void addOutEdgeToSourceMap(Map<DomainClassNode, List<DomainClassRelationsInfo>> sourcesToOutEdges,
                                            DomainClassNode source,
                                            DomainClassRelationsInfo outEdge) {
    List<DomainClassRelationsInfo> outEdges = sourcesToOutEdges.get(source);

    if (outEdges == null) {
      outEdges = new ArrayList<DomainClassRelationsInfo>();
    }

    outEdges.add(outEdge);
    sourcesToOutEdges.put(source, outEdges);
  }

  public static boolean isDomainClassFile(VirtualFile file, Project project) {
    if (file == null) return false;
    final Module module = ModuleUtil.findModuleForFile(file, project);
    VirtualFile domainDirectory = GrailsUtils.findDomainClassDirectory(module);
    if (domainDirectory != null && VfsUtil.isAncestor(domainDirectory, file, true)) return true;

    // For completion tests
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      final PsiDirectory mockDomainDirectory = getMockDomainDirectory(project);
      return mockDomainDirectory != null && VfsUtil.isAncestor(mockDomainDirectory.getVirtualFile(), file, true);
    }

    return false;
  }

  private static PsiDirectory getMockDomainDirectory(Project project) {
    String path = FileUtil.toSystemIndependentName(PathManager.getHomePath()) + "/svnPlugins/groovy/mvc-testdata/mockDomainDir/domain";
    VirtualFile fileByUrl = VirtualFileManager.getInstance().findFileByUrl("file://" + path);
    assert fileByUrl != null;
    return PsiManager.getInstance(project).findDirectory(fileByUrl);
  }

  public static boolean isDomainClass(PsiClass clazz) {
    if (clazz == null) return false;
    PsiFile psiFile = clazz.getContainingFile();
    if (psiFile == null) return false;
    VirtualFile file = psiFile.getOriginalFile().getVirtualFile();
    return file != null && isDomainClassFile(file, psiFile.getProject());
  }

  public static boolean isDomainClass(PsiType type) {
    if (type instanceof PsiClassType) {
      return isDomainClass(((PsiClassType)type).resolve());
    }
    return false;
  }

  @Nullable
  public static PsiClass getDomainClass(GrExpression qual) {
    if (qual.getType() instanceof PsiClassType) {
      PsiClassType psiClass = (PsiClassType)qual.getType();
      if (psiClass == null) return null;
      PsiClass resolved = psiClass.resolve();
      if (resolved != null && isDomainClass(resolved)) {
        return resolved;
      }
    }
    return null;
  }

  public static TreeSet<String> definedDomainClassFields(PsiClass clazz) {
    TreeSet<String> set = new TreeSet<String>();
    for (PsiField field : clazz.getFields()) {
      set.add(field.getName());
    }
    return set;
  }

  public static String[] getDomainFieldNames(PsiClass clazz) {
    final Set<String> set = getDomainFields(clazz).keySet();
    return set.toArray(new String[set.size()]);
  }

  public static Map<String, PsiType> getDomainFields(PsiClass clazz, PsiField[] dcInstanceFields) {
    if (clazz != null && isDomainClass(clazz)) {
      final PsiMethod[] methods = clazz.getAllMethods();
      Map<String, PsiType> result = new HashMap<String, PsiType>();
      for (PsiMethod method : methods) {
        if (GroovyPropertyUtils.isSimplePropertyGetter(method) && !method.hasModifierProperty(PsiModifier.STATIC)) {
          final String propertyName = GroovyPropertyUtils.getPropertyNameByGetter(method);
          if (!result.containsKey(propertyName)) {
            result.put(propertyName, method.getReturnType());
          }
        }
      }

      for (PsiField field : dcInstanceFields) {
        final String name = field.getName();
        if (!result.containsKey(name)) {
          result.put(name, field.getType());
        }
      }

      result.remove("metaClass");
      result.remove("class");
      for (String name : DomainClassMembersProvider.DOMAIN_OPTIONAL_PROPERTY_NAMES) {
        result.remove(name);
      }

      for (String name : DomainClassMembersProvider.DOMAIN_DYNAMIC_PROPERTY_NAMES) {
        result.remove(name);
      }
      return result;
    }
    return Collections.emptyMap();
  }

  public static Map<String, PsiType> getDomainFields(PsiClass clazz) {
    final PsiField[] instanceFields = DomainClassMembersProvider.getInstance(clazz.getProject()).getDCInstanceFields(clazz);
    return getDomainFields(clazz, instanceFields);
  }

  public static boolean endsWithDomainConnectivity(String name) {
    for (String connective : DOMAIN_CONNECTIVES) {
      if (name.endsWith(connective)) return true;
    }
    return false;
  }
}
