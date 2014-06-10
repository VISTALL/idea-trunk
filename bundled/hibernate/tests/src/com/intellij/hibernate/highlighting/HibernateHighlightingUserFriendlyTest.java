/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.hibernate.highlighting;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.xml.util.CheckXmlFileWithXercesValidatorInspection;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.hibernate.facet.HibernateFacetType;
import com.intellij.hibernate.facet.HibernateFacet;
import com.intellij.hibernate.model.HibernateDescriptorsConstants;
import com.intellij.hibernate.HibernateInspectionToolProvider;
import com.intellij.facet.FacetManager;
import com.intellij.jpa.JpaInspectionToolProvider;

import java.io.IOException;

/**
 * @author peter
 */
public class HibernateHighlightingUserFriendlyTest extends JavaCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(new JpaInspectionToolProvider(), new HibernateInspectionToolProvider());
  }

  protected String getBasePath() {
    return "/svnPlugins/hibernate/tests/testData/highlighting";
  }

  public void testXmlEntities() throws Throwable {
    myFixture.enableInspections(new CheckXmlFileWithXercesValidatorInspection());
    myFixture.addClass("package bar; class User {}");
    myFixture.addFileToProject("foo/incl.hbm.xml", "<id column=\"id\" name=\"id\" type=\"java.lang.Long\">\n" +
                                                   "<generator class=\"increment\"/>\n" +
                                                   "</id>");
    myFixture.testHighlighting(getTestName(false) + ".xml");
  }

  public void testTabCompletionInExtendClassReference() throws Throwable {
    myFixture.addClass("package org.hibernate.dialect; public class Dialect {}");
    myFixture.addClass("package org.hibernate.dialect; public class DerbyDialect extends org.hibernate.dialect.Dialect {}");

    myFixture.testCompletionVariants(getTestName(false) + ".xml", "dialect.DerbyDialect", "dialect.Dialect");
    myFixture.type(Lookup.REPLACE_SELECT_CHAR);
    myFixture.checkResultByFile(getTestName(false) + "_after.xml");
  }

  public void testEmbeddableAttributeAttribute() throws Throwable {
    addHibernateAnnotations();
    setupHibernateFacet();

    myFixture.copyFileToProject(getTestName(false) + ".java", "bar/classes.java");
    myFixture.testHighlighting(false, false, false, getTestName(false) + ".xml");
  }

  private void addHibernateAnnotations() throws IOException {
    myFixture.addClass("package javax.persistence; public @interface Embeddable {}");
    myFixture.addClass("package javax.persistence; public @interface Embedded {}");
    myFixture.addClass("package javax.persistence; public @interface Entity {}");
    myFixture.addClass("package javax.persistence; public @interface Basic {}");
  }

  private void setupHibernateFacet() throws IOException {
    myFixture.addFileToProject("hibernate.cfg.xml",
                               "<?xml version='1.0' encoding='utf-8'?>" +
                               "<!DOCTYPE hibernate-configuration PUBLIC \"-//Hibernate/Hibernate Configuration DTD//EN\" \"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd\">" +
                               "<hibernate-configuration>" +
                               "<session-factory name=\"MyUnit\">" +
                               "<mapping resource=\"" + getTestName(false) + ".xml\"/>" +
                               "</session-factory>" +
                               "</hibernate-configuration>");

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        HibernateFacetType type = HibernateFacetType.INSTANCE;
        HibernateFacet myFacet = FacetManager.getInstance(myModule).addFacet(type, type.getPresentableName(), null);
        myFacet.getConfiguration().getDescriptorsConfiguration().addConfigFile(HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA,
                                                                           VfsUtil.pathToUrl(myFixture.getTempDirPath() + "/hibernate.cfg.xml"));
      }
    });
  }

}
