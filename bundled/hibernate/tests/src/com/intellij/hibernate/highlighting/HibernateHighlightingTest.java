/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.hibernate.highlighting;

import com.intellij.facet.FacetManager;
import com.intellij.hibernate.HibernateInspectionToolProvider;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.hibernate.facet.HibernateFacetType;
import com.intellij.hibernate.model.HibernateDescriptorsConstants;
import com.intellij.j2ee.J2EETestCase;
import com.intellij.javaee.dataSource.DataSource;
import com.intellij.jpa.JpaInspectionToolProvider;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.persistence.roles.PersistenceRoleHolder;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

import java.io.File;

/**
 * @author peter
 */
public class HibernateHighlightingTest extends JavaCodeInsightFixtureTestCase {

  @Override
  protected String getBasePath() {
    return "/svnPlugins/hibernate/tests/testData/highlighting";
  }

  protected void setUp() throws Exception {
    super.setUp();
    J2EETestCase.addJavaeeLibraryToRoots(myModule);
    PsiTestUtil.addLibrary(myModule, "Hibernate", PathManager.getHomePath() + "/lib/", "hibernate3.jar");
    myFixture.enableInspections(new JpaInspectionToolProvider(), new HibernateInspectionToolProvider());
    PersistenceRoleHolder.getInstance(getProject());
  }

  @Override
  protected void tuneFixture(final JavaModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
  }

  public void test1() throws Throwable {
    final VirtualFile cfgXml = myFixture.copyFileToProject(getTestName(true)+"/hibernate.cfg.xml", "hibernate.cfg.xml");
    final VirtualFile mappingXml = myFixture.copyFileToProject(getTestName(true)+ "/pkg/mapping.hbm.xml", "pkg/mapping.hbm.xml");
    final VirtualFile classesJava = myFixture.copyFileToProject(getTestName(true)+ "/pkg/classes.java", "pkg/classes.java");
    myFixture.allowTreeAccessForFile(classesJava);
    final HibernateFacet facet = setupFacet(cfgXml);
    final DataSource dataSource = J2EETestCase.loadDataSourceFromFile(myModule, new File(
      PathManager.getHomePath() + getBasePath() + "/" + getTestName(true) + "/datasource.xml"));
    assertEquals(1, facet.getPersistenceUnits().size());
    facet.setDataSourceId(facet.getPersistenceUnits().get(0), dataSource.getUniqueId());
    myFixture.testHighlightingAllFiles(true, false, false, cfgXml, mappingXml, classesJava);
  }

  protected HibernateFacet setupFacet(final VirtualFile cfgXml) {
    return new WriteCommandAction<HibernateFacet>(getProject()) {
      protected void run(final Result<HibernateFacet> result) throws Throwable {
        HibernateFacetType type = HibernateFacetType.INSTANCE;
        final HibernateFacet facet = FacetManager.getInstance(myModule).addFacet(type, type.getPresentableName(), null);
        facet.getConfiguration().getDescriptorsConfiguration().addConfigFile(HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA, cfgXml.getUrl());

        result.setResult(facet);
      }
    }.execute().getResultObject();
  }
}