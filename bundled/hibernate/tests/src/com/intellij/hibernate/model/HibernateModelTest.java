/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.hibernate.model;

import com.intellij.facet.FacetManager;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.hibernate.facet.HibernateFacetType;
import com.intellij.hibernate.model.enums.HibernateAttributeType;
import com.intellij.hibernate.model.xml.config.Mapping;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.hibernate.view.HibernateIcons;
import com.intellij.j2ee.J2EETestCase;
import com.intellij.javaee.model.common.persistence.mapping.AttributeType;
import com.intellij.jpa.actions.NewGroupPersistence;
import com.intellij.jpa.model.manipulators.ObjectManipulatorBase;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.persistence.PersistenceDataKeys;
import com.intellij.persistence.PersistenceHelper;
import com.intellij.persistence.facet.PersistenceFacetBase;
import com.intellij.persistence.facet.PersistenceFacetConfiguration;
import com.intellij.persistence.model.*;
import com.intellij.persistence.model.manipulators.PersistenceAction;
import com.intellij.persistence.model.manipulators.PersistentObjectManipulator;
import com.intellij.persistence.model.manipulators.UserResponse;
import com.intellij.persistence.roles.PersistenceClassRole;
import com.intellij.persistence.roles.PersistenceRoleHolder;
import com.intellij.persistence.util.JavaContainerType;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author peter
 */
public class HibernateModelTest extends JavaCodeInsightFixtureTestCase {
  @NonNls private static final String IMPORTS = "import javax.persistence.*; import java.util.*;";

  private HibernateFacet myFacet;

  protected void setUp() throws Exception {
    super.setUp();
    J2EETestCase.addJavaeeLibraryToRoots(myModule);
    PsiTestUtil.addLibrary(myModule, "Hibernate", PathManager.getHomePath() + "/lib/", "hibernate3.jar");
    PersistenceRoleHolder.getInstance(getProject());
    myFacet = setupFacet();
  }

  @Override
  protected void tuneFixture(final JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }


  private void addAnnoMapping(final String text) throws Throwable {
    final PsiClass aClass = myFixture.addClass(text);
    final SessionFactory unit = (SessionFactory)myFacet.getPersistenceUnits().get(0);
    new WriteCommandAction.Simple(getProject()) {
      public void run() {
        final Mapping mapping = unit.addMapping();
        mapping.getClazz().setValue(aClass);
        assertEquals(aClass, mapping.getClazz().getValue());
      }
    }.execute();
  }

  private PersistenceMappings addXmlMapping(final String name, final String text) throws Throwable{
    final PsiFile psiFile = myFixture.addFileToProject(name, text);
    assertInstanceOf(psiFile, XmlFile.class);
    final SessionFactory unit = (SessionFactory)myFacet.getPersistenceUnits().get(0);
    new WriteCommandAction.Simple(getProject()) {
      public void run() {
        final Mapping mapping = unit.addMapping();
        mapping.getResource().setValue(psiFile);
        assertEquals(psiFile, mapping.getResource().getValue());
      }
    }.execute();
    final DomFileElement<DomElement> element = DomManager.getDomManager(getProject()).getFileElement((XmlFile)psiFile, DomElement.class);
    assertNotNull(element);
    final DomElement rootElement = element.getRootElement();
    assert rootElement instanceof PersistenceMappings;
    return (PersistenceMappings)rootElement;
  }

  public void testJpaMerging() throws Throwable {
    addAnnoMapping("package pkg;" + IMPORTS +
                                "@Entity\n" +
                                "public class Honey {\n" +
                                "        @Basic\n" +
                                "\tprivate Integer id;\n" +
                                "\tprivate String name;\n" +
                                "\tprivate String taste;\n" +
                                "}");
    addXmlMapping("mapping.hbm.xml", "<hibernate-mapping package=\"pkg\">" +
                                     "    <class name=\"pkg.Honey\" table=\"honey\">\n" +
                                     "      <id name=\"id\" type=\"java.lang.Integer\" access=\"field\">\n" +
                                     "        <generator class=\"increment\"/>\n" +
                                     "      </id>\n" +
                                     "      <property name=\"name\" type=\"java.lang.String\" access=\"field\"/>\n" +
                                     "      <property name=\"taste\" type=\"java.lang.String\"/>\n" +
                                     "      <query name=\"someName\">from pkg.Honey</query>\n" +
                                     "    </class>" +
                                     "</hibernate-mapping>"
    );
    

    // hibernate and JPA attributes merging test
    final PsiClass honey = myFixture.getJavaFacade().findClass("pkg.Honey", myModule.getModuleScope());
    assertNotNull(honey);
    final PersistenceClassRole[] roles = PersistenceCommonUtil.getPersistenceRoles(honey);
    assertEquals(1, roles.length);
    final PersistentObject honeyObj = roles[0].getPersistentObject();
    assertNotNull(honeyObj);
    final Collection<PersistentAttribute> attributes = PersistenceHelper.getHelper().getSharedModelBrowser().queryAttributes(honeyObj).findAll();
    assertEquals(3, attributes.size());
    final PersistentAttribute idAttr = ElementPresentationManager.findByName(attributes, "id");
    assertNotNull(idAttr);
    assertTrue(idAttr instanceof MergedObject);
    assertEquals(HibernateIcons.ID_ATTRIBUTE_ICON, ElementPresentationManager.getIcon(idAttr));

    final PersistentObjectManipulator manipulator =
        PersistenceHelper.getHelper().getManipulatorsRegistry().getManipulator(honeyObj, PersistentObjectManipulator.class);
    assertTrue(manipulator instanceof MergedObject);
    assertEquals(14, manipulator.getCreateActions().size());
  }

  public void testAttributeTypes() {
    final DomManager dom = DomManager.getDomManager(getProject());
    assertEquals(HibernateAttributeType.ANY, AttributeType.getAttributeType(dom.createMockElement(HbmAny.class, myModule, true)));
    assertEquals(HibernateAttributeType.COMPONENT, AttributeType.getAttributeType(dom.createMockElement(HbmComponent.class, myModule, true)));
    assertEquals(HibernateAttributeType.COMPOSITE_ELEMENTS, AttributeType.getAttributeType(dom.createMockElement(HbmCompositeElement.class, myModule, true)));
    assertEquals(HibernateAttributeType.COMPOSITE_ID, AttributeType.getAttributeType(dom.createMockElement(HbmCompositeId.class, myModule, true)));
    assertEquals(HibernateAttributeType.DYNAMIC_COMPONENT, AttributeType.getAttributeType(dom.createMockElement(HbmDynamicComponent.class, myModule, true)));
    assertEquals(HibernateAttributeType.ELEMENTS, AttributeType.getAttributeType(dom.createMockElement(HbmElement.class, myModule, true)));
    assertEquals(HibernateAttributeType.ID, AttributeType.getAttributeType(dom.createMockElement(HbmId.class, myModule, true)));
    assertEquals(HibernateAttributeType.MANY_TO_ONE, AttributeType.getAttributeType(dom.createMockElement(HbmKeyManyToOne.class, myModule, true)));
    assertEquals(HibernateAttributeType.PROPERTY, AttributeType.getAttributeType(dom.createMockElement(HbmKeyProperty.class, myModule, true)));
    assertEquals(HibernateAttributeType.MANY_TO_MANY, AttributeType.getAttributeType(dom.createMockElement(HbmManyToMany.class, myModule, true)));
    assertEquals(HibernateAttributeType.MANY_TO_ONE, AttributeType.getAttributeType(dom.createMockElement(HbmManyToOne.class, myModule, true)));
    assertEquals(HibernateAttributeType.COMPONENT, AttributeType.getAttributeType(dom.createMockElement(HbmNestedCompositeElement.class, myModule, true)));
    assertEquals(HibernateAttributeType.ONE_TO_MANY, AttributeType.getAttributeType(dom.createMockElement(HbmOneToMany.class, myModule, true)));
    assertEquals(HibernateAttributeType.ONE_TO_ONE, AttributeType.getAttributeType(dom.createMockElement(HbmOneToOne.class, myModule, true)));
    assertEquals(HibernateAttributeType.PROPERTY, AttributeType.getAttributeType(dom.createMockElement(HbmProperty.class, myModule, true)));
    assertEquals(HibernateAttributeType.TIMESTAMP, AttributeType.getAttributeType(dom.createMockElement(HbmTimestamp.class, myModule, true)));
    assertEquals(HibernateAttributeType.VERSION, AttributeType.getAttributeType(dom.createMockElement(HbmVersion.class, myModule, true)));
  }

  public void testPersistenceActions() throws Throwable {
    final HbmHibernateMapping mapping = (HbmHibernateMapping)addXmlMapping("mapping.hbm.xml", "<hibernate-mapping package=\"pkg\">" +
                                     "    <class name=\"pkg.PersistenceActions\"/>\n" +
                                     "</hibernate-mapping>");

    final PsiClass entityClass = myFixture.addClass("package pkg;" + IMPORTS + " public class PersistenceActions { }");
    final PsiClass embeddableClass = myFixture.addClass("package pkg;" + IMPORTS + " public class EmbeddableMain { }");
    final PsiClass embeddableDeuxClass = myFixture.addClass("package pkg;" + IMPORTS + " public class EmbeddableDeux { }");
    final HbmClass hbmClass = mapping.getClasses().get(0);
    assertEquals(hbmClass.getClazz().getValue(), entityClass);
    assertEquals(0, mapping.getModelHelper().getPersistentEmbeddables().size());

    final PsiClassType entityType = JavaPsiFacade.getInstance(getProject()).getElementFactory().createType(entityClass);
    runCreateActions(hbmClass, JavaPsiFacade.getInstance(getProject()).getElementFactory().createType(embeddableClass), entityType);
    assertEquals(1, mapping.getModelHelper().getPersistentEmbeddables().size());
    for (PersistentEmbeddable embeddable : mapping.getModelHelper().getPersistentEmbeddables()) {
      runCreateActions(embeddable, JavaPsiFacade.getInstance(getProject()).getElementFactory().createType(embeddableDeuxClass), entityType);
    }
    assertEquals(14, hbmClass.getObjectModelHelper().getAttributes().size());
    assertEquals(2, mapping.getModelHelper().getPersistentEmbeddables().size());
    assertEquals(1, PersistenceCommonUtil.getPersistenceRoles(embeddableClass).length);
    final PersistentObject embeddable = PersistenceCommonUtil.getPersistenceRoles(embeddableClass)[0].getPersistentObject();
    assertEquals(1, PersistenceCommonUtil.getPersistenceRoles(embeddableDeuxClass).length);
    final PersistentObject embeddableDeux = PersistenceCommonUtil.getPersistenceRoles(embeddableDeuxClass)[0].getPersistentObject();

    assertEquals(8, embeddable.getObjectModelHelper().getAttributes().size());
    assertEquals(0, embeddableDeux.getObjectModelHelper().getAttributes().size());
  }

  private void runCreateActions(final PersistentObject object, final PsiClassType embeddableType, final PsiClassType entityType) throws IncorrectOperationException {
    final PersistentObjectManipulator<?> manipulator =
        PersistenceHelper.getHelper().getManipulatorsRegistry().getManipulator(object, PersistentObjectManipulator.class);
    assertNotNull(manipulator);
    final int[] idx = {0};
    final DataContext dataContext = new DataContext() {
      public Object getData(@NonNls final String dataId) {
        if (PersistenceDataKeys.PERSISTENCE_FACET.getName().equals(dataId)) return myFacet;
        return null;
      }
    };
    final PsiClass objectClass = object.getClazz().getValue();
    assertNotNull(objectClass);
    final PsiClassType javaLangString = PsiType.getJavaLangString(getPsiManager(), GlobalSearchScope.allScope(getProject()));
    final PsiType javaLangList = JavaContainerType.LIST.createCollectionType(objectClass, javaLangString, null);
    final PsiType embeddableList = JavaContainerType.LIST.createCollectionType(objectClass, embeddableType, null);
    final PsiType entityList = JavaContainerType.LIST.createCollectionType(objectClass, entityType, null);

    assertNotNull(((PsiClassReferenceType)PersistenceCommonUtil.getContainerType(entityList).getSecond()).resolve());
    assertNotNull(((PsiClassReferenceType)PersistenceCommonUtil.getContainerType(embeddableList).getSecond()).resolve());
    for (final PersistenceAction action : manipulator.getCreateActions()) {
      idx[0] ++;
      final UserResponse attributeUserResponse = new UserResponse() {
        public String getPersistenceUnitName(@NotNull final PersistenceFacetBase<?, ? extends PersistencePackage> facet) {
          return null;
        }

        @NotNull
        public VirtualFile[] getPersistenceMappingFiles(@NotNull final PersistenceFacetBase<?, ?> facet,
                                                        final String templateName,
                                                        final Class<? extends PersistenceMappings> mappingsClass) {
          return VirtualFile.EMPTY_ARRAY;
        }

        public Pair<PsiDirectory, String> getClassName(@NotNull final Module module, final String title, final String helpID) {
          return null;
        }

        public void askUserToSetupListener(final PersistenceAction action,
                                           final PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> facet,
                                           final PsiClass aClass) {
        }

        public AttributeInfo getAttributeName(final PersistentObject object,
                                              final RelationshipType relationshipType,
                                              final Function<PsiType, String> typeValidator,
                                              final String title,
                                              final String defaultName) {
          final AttributeType type =
              action instanceof ObjectManipulatorBase.MyAttributeAction ? ((ObjectManipulatorBase.MyAttributeAction)action)
                  .getAttributeType() : null;
          final boolean isCollection = type != null && type.isContainer();
          final boolean isEmbeddable = type != null && type.isEmbedded();
          final PsiType o = relationshipType == null? (isEmbeddable && isCollection? embeddableList :
                           (isEmbeddable ? embeddableType : (isCollection? javaLangList : javaLangString))) :
                           (isCollection? entityList: entityType);
          return new AttributeInfo("p" + idx[0], o, true, null, false);
        }
      };
      NewGroupPersistence.runAction(getProject(), dataContext, action, attributeUserResponse);
    }
  }

  protected HibernateFacet setupFacet() throws Exception {
    final PsiFile file = myFixture.addFileToProject("hibernate.cfg.xml", "<hibernate-configuration><session-factory name=\"unit\"/></hibernate-configuration>");
    return new WriteCommandAction<HibernateFacet>(getProject()) {
      protected void run(final Result<HibernateFacet> result) throws Throwable {
        HibernateFacetType type = HibernateFacetType.INSTANCE;
        final HibernateFacet facet = FacetManager.getInstance(myModule).addFacet(type, type.getPresentableName(), null);
        facet.getConfiguration().getDescriptorsConfiguration().addConfigFile(HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA, file.getVirtualFile().getUrl());

        result.setResult(facet);
      }
    }.execute().getResultObject();
  }

}