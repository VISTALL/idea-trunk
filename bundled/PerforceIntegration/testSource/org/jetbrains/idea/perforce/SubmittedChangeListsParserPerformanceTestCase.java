package org.jetbrains.idea.perforce;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import junit.framework.TestCase;
import org.jetbrains.idea.perforce.perforce.OutputMessageParser;

import java.io.File;

public class SubmittedChangeListsParserPerformanceTestCase extends TestCase{
  public void test() throws Exception{
    File testData = new File(PathManager.getHomePath() + "/svnPlugins/PerforceIntegration/testData/changes.txt");
    final String output = new String(FileUtil.loadFileBytes(testData));
    final long start = System.currentTimeMillis();
    OutputMessageParser.processChangesOutput(output);
    final long executionTime = System.currentTimeMillis() - start;
    assertTrue("Execution time: " + executionTime, executionTime < 30000);
  }
}
