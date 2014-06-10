/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.hibernate;

import com.intellij.hibernate.highlighting.HibernateHighlightingTest;
import com.intellij.hibernate.highlighting.HibernateHighlightingUserFriendlyTest;
import com.intellij.hibernate.model.HibernateModelTest;
import com.intellij.hibernate.springIntegration.SpringIntegrationTest;
import com.intellij.javaee.persistence.JpaTestSuite;
import junit.framework.TestSuite;

/**
 * @author Gregory.Shrago
 */
public class HibernateTestSuite {
  public static TestSuite suite() {
    final TestSuite testSuite = JpaTestSuite.suite();
    testSuite.addTestSuite(HibernateModelTest.class);
    testSuite.addTestSuite(HibernateHighlightingUserFriendlyTest.class);
    testSuite.addTestSuite(HibernateHighlightingTest.class);
    testSuite.addTestSuite(SpringIntegrationTest.class);
    //testSuite.addTestSuite(InspectionDescriptionTest.class);
    return testSuite;
  }

}
