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

package org.jetbrains.groovy.grails.tests;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Maxim.Medvedev
 */
public class GrailsIdeaTestListener implements GrailsTestListener {
  private final Map properties = new HashMap();
  private final PrintStream out;
  private final PrintStream err;

  public GrailsIdeaTestListener(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
  }

  static String replaceEscapedSymbols(String s) {
    if (s == null) return null;
    s = s.replace("|", "||");
    s = s.replace("'", "|'");
    s = s.replace("\n", "|n");
    s = s.replace("\r", "|r");
    s = s.replace("]", "|]");
    return s;
  }

  private void printFailed(String testName, Throwable e) {
    String error = e instanceof AssertionFailedError ? "" : "error='true'";
    out.println("\n##teamcity[testFailed name='" +
                       replaceEscapedSymbols(testName) +
                       "' message='" +
                       replaceEscapedSymbols(e.getMessage()) +
                       "' details='" +
                       getStackTrace(e) +
                       "' " +
                       error +
                       "]");
  }

  static String getStackTrace(Throwable e) {
    StringWriter writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    return replaceEscapedSymbols(writer.getBuffer().toString());
  }

  public void addError(Test test, Throwable throwable) {
    if (test instanceof TestCase) {
      printFailed(((TestCase)test).getName(), throwable);
    }
  }

  public void addFailure(Test test, AssertionFailedError assertionFailedError) {
    if (test instanceof TestCase) {
      printFailed(((TestCase)test).getName(), assertionFailedError);
    }
  }

  public void endTest(Test test) {
    if (test instanceof TestCase) {
      String testName = replaceEscapedSymbols(((TestCase)test).getName());
      long duration = System.currentTimeMillis() - ((Long)properties.get(test)).longValue();
      out.println("\n##teamcity[testFinished name='" + testName + "' duration='" + duration + "']");
    }
  }

  public void startTest(Test test) {
    if (test instanceof TestCase) {
      String testName = replaceEscapedSymbols(((TestCase)test).getName());
      String className = test.getClass().getCanonicalName();
      String testLocation = replaceEscapedSymbols(className + '.' + ((TestCase)test).getName());
      out.println("\n##teamcity[testStarted name='" +
                         testName +
                         "' captureStandardOutput='false' locationHint='grails://methodName::" +
                         testLocation +
                         "']");
      properties.put(test, new Long(System.currentTimeMillis()));
    }
  }

  public void startTestSuite(Test test) {
    if (test instanceof TestSuite) {
      String testName = replaceEscapedSymbols(((TestSuite)test).getName());
      out.println("\n##teamcity[testSuiteStarted name='" + testName + "' location='grails://className::" + testName + "']");
      properties.put(test, new Long(System.currentTimeMillis()));
    }
  }

  public void finishTestSuite(Test test) {
    if (test instanceof TestSuite) {
      String testName = replaceEscapedSymbols(((TestSuite)test).getName());
      long duration = System.currentTimeMillis() - ((Long)properties.get(test)).longValue();
      out.println("\n##teamcity[testSuiteFinished name='" + testName + "' duration='" + duration + "']");
    }
  }   
}
