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

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.codehaus.groovy.grails.test.FormattedOutput;
import org.codehaus.groovy.grails.test.GrailsTestRunner;
import org.codehaus.groovy.grails.test.PlainFormatter;
import org.codehaus.groovy.grails.test.XMLFormatter;

import java.io.*;
import java.util.*;


/**
 * @author Maxim.Medvedev
 */
public class GrailsIdeaTestRunner implements GrailsTestRunner {
  ArrayList listeners = new ArrayList();

  private PrintStream savedOut;
  private PrintStream savedErr;
  private ByteArrayOutputStream out;
  private ByteArrayOutputStream err;

  private PrintStream outPrintStream;
  private PrintStream errPrintStream;

  private List formattedOutputs;

  private File reportsDir;
  private List formats;

  public GrailsIdeaTestRunner(File reportsDir, List formats) {
    this.reportsDir = reportsDir;

    // Defensive copy.
    this.formats = new ArrayList(formats);
  }

  public TestResult runTests(TestSuite suite) {
    TestResult result = createTestResult();

    listeners.add(new GrailsIdeaTestListener(System.out, System.err));

    for (int i1 = 0, listenersSize = listeners.size(); i1 < listenersSize; i1++) {
      result.addListener((GrailsTestListener)listeners.get(i1));
    }

    for (Enumeration tests = suite.tests(); tests.hasMoreElements();) {
      TestSuite test = (TestSuite)tests.nextElement();
      reset();

      JUnitTest junitTest = new JUnitTest(test.getName());
      try {
        replaceStandardStreams();
        prepareReports(test);

        for (int i2 = 0, formattedOutputsSize = formattedOutputs.size(); i2 < formattedOutputsSize; i2++) {
          FormattedOutput output = (FormattedOutput)formattedOutputs.get(i2);
          result.addListener(output.getFormatter());
          output.start(junitTest);
        }

        for (int i1 = 0, listenersSize = listeners.size(); i1 < listenersSize; i1++) {
          ((GrailsTestListener)listeners.get(i1)).startTestSuite(test);
        }

        // Starting...now!
        long start = System.currentTimeMillis();
        int runCount = result.runCount();
        int failureCount = result.failureCount();
        int errorCount = result.errorCount();

        for (int i = 0; i < test.testCount(); i++) {
          TestCase t = (TestCase)test.testAt(i);
          outPrintStream.println("--Output from " + t.getName() + "--");
          errPrintStream.println("--Output from " + t.getName() + "--");

          test.runTest(t, result);
        }
        junitTest.setCounts(result.runCount() - runCount, result.failureCount() - failureCount, result.errorCount() - errorCount);
        junitTest.setRunTime(System.currentTimeMillis() - start);
      }
      finally {
        for (int i = 0, listenersSize = listeners.size(); i < listenersSize; i++) {
          ((GrailsTestListener)listeners.get(i)).finishTestSuite(test);
        }
        for (int i = 0; i < formattedOutputs.size(); i++) {
          FormattedOutput output = (FormattedOutput)formattedOutputs.get(i);
          output.end(junitTest, out.toString(), err.toString());
        }
        restoreStandardStreams();
      }
    }

    return result;
  }

  /*
  public TestResult runTests(TestSuite suite) {
    TestResult result = createTestResult();

    try {
      final String className = System.getProperty("grails.test.listener");
      if (className != null) {
        GrailsTestListener listener = (GrailsTestListener)Class.forName(className).newInstance();
        listeners.add(listener);
      }
    }
    catch (Exception e) {
      System.out.println("Error while creating listener:" + e);
    }

    listeners.add(new GrailsIdeaTestListener());

    for (GrailsTestListener listener : listeners) {
      result.addListener(listener);
    }

    for (Enumeration tests = suite.tests(); tests.hasMoreElements();) {
      TestSuite test = (TestSuite)tests.nextElement();
      for (GrailsTestListener listener : listeners) {
        listener.startTestSuite(test);
      }
      test.run(result);
      for (GrailsTestListener listener : listeners) {
        listener.finishTestSuite(test);
      }
    }

    for (GrailsTestListener listener : listeners) {
      result.removeListener(listener);
    }
    return result;
  }
  */

  private TestResult createTestResult() {
    return new TestResult();
  }

  public void reset() {
    this.formattedOutputs = null;
    this.savedOut = null;
    this.savedErr = null;
    this.out = null;
    this.err = null;
  }

  public void prepareReports(TestSuite test) {
    formattedOutputs = new ArrayList(formats.size());
    for (int i = 0, formatsSize = formats.size(); i < formatsSize; i++) {
      String format = (String)formats.get(i);
      formattedOutputs.add(createFormatter(format, test));
    }
  }

  public FormattedOutput createFormatter(String type, TestSuite test) {
    if (type.equals("xml")) {
      return new FormattedOutput(new File(reportsDir, "TEST-" + test.getName() + ".xml"), new XMLFormatter());
    }
    else if (type.equals("plain")) {
      return new FormattedOutput(new File(reportsDir, "plain/TEST-" + test.getName() + ".txt"), new PlainFormatter());
    }
    else {
      throw new RuntimeException("Unknown formatter type: $type");
    }
  }

  private void replaceStandardStreams() {
    this.savedOut = System.out;
    this.savedErr = System.err;

    out = new ByteArrayOutputStream();
    outPrintStream = new PrintStream(out);
    err = new ByteArrayOutputStream();
    errPrintStream = new PrintStream(err);

    MultiOutputStream multiOut = new MultiOutputStream();
    multiOut.addOutputStream(out);
    multiOut.addOutputStream(System.out);

    MultiOutputStream multiErr = new MultiOutputStream();
    multiErr.addOutputStream(err);
    multiErr.addOutputStream(System.err);

    System.setOut(new PrintStream(multiOut));
    System.setErr(new PrintStream(multiErr));
  }

  private void restoreStandardStreams() {
    if (this.savedOut != null) System.setOut(this.savedOut);
    if (this.savedErr != null) System.setErr(this.savedErr);
  }

  private static class MultiOutputStream extends OutputStream {
    private final Set streams = new HashSet();

    public void addOutputStream(OutputStream stream) {
      if (stream == this) return;
      streams.add(stream);
    }

    public void write(int b) throws IOException {
      for (Iterator iterator = streams.iterator(); iterator.hasNext();) {
        OutputStream stream = (OutputStream)iterator.next();
        stream.write(b);
      }
    }

    public void write(byte[] b) throws IOException {
      for (Iterator iterator = streams.iterator(); iterator.hasNext();) {
        OutputStream stream = (OutputStream)iterator.next();
        stream.write(b);
      }
    }

    public void write(byte[] b, int off, int len) throws IOException {
      for (Iterator iterator = streams.iterator(); iterator.hasNext();) {
        OutputStream stream = (OutputStream)iterator.next();
        stream.write(b, off, len);
      }
    }

    public void flush() throws IOException {
      for (Iterator iterator = streams.iterator(); iterator.hasNext();) {
        OutputStream stream = (OutputStream)iterator.next();
        stream.flush();
      }
    }

    public void close() throws IOException {
      for (Iterator iterator = streams.iterator(); iterator.hasNext();) {
        OutputStream stream = (OutputStream)iterator.next();
        stream.close();
      }
    }
  }
}
