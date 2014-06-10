package org.intellij.j2ee.web.resin.resin;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.util.JDOMUtil;
import org.intellij.j2ee.web.resin.ResinBundle;
import org.intellij.j2ee.web.resin.resin.configuration.ResinConfigurationStrategy;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class ResinConfiguration {
  private final ResinInstallation resinInstallation;
  private final File generatedConfig;
  private final File sourceConfig;
  private ResinConfigurationStrategy strategy;
  private Document document;
  private static final String JAVAC = "javac";
  private static final String ARGS = "args";

  public ResinConfiguration(ResinInstallation resinInstallation, File generatedConfig, File sourceConfig)
    throws IOException, JDOMException {
    this.resinInstallation = resinInstallation;
    this.generatedConfig = generatedConfig;
    this.sourceConfig = sourceConfig;

    if(isWritable()) {
      strategy = ResinConfigurationStrategy.getForInstallation(resinInstallation);
      if (this.sourceConfig.length() == 0) {
        InputStream is = strategy.getDefaultResinConfContent();
        if (is == null) throw new IOException(ResinBundle.message("run.resin.conf.null.error"));
        document = JDOMUtil.loadDocument(is);

        is = strategy.getDefaultResinConfContent();
        if (is == null) throw new IOException(ResinBundle.message("run.resin.confcontent.null.error"));
      }
      else {
        document = JDOMUtil.loadDocument(this.sourceConfig);
      }
      patchConfigToMakeDebuggerWork(document);
    }
  }

  private static void patchConfigToMakeDebuggerWork(Document document) {
    Element javac = document.getRootElement().getChild(JAVAC, document.getRootElement().getNamespace());
    if (javac == null) {
      javac = new Element(JAVAC, document.getRootElement().getNamespace());
      javac.setAttribute("compiler", "internal");
      javac.setAttribute(ARGS, "-source 1.5");
      document.getRootElement().addContent(javac);
    }
    final Attribute args = javac.getAttribute(ARGS);
    if (!args.getValue().contains("-g")) {
      args.setValue("-g " + args.getValue());
    }
  }

  public ResinInstallation getResinInstallation() {
    return resinInstallation;
  }

  private boolean isWritable() {
    return generatedConfig != null;
  }

  public String getConfPath() {
    return (isWritable()) ? generatedConfig.getAbsolutePath() : sourceConfig.getAbsolutePath();
  }

  public void deploy(WebApp webApp) throws ExecutionException {
    if(isWritable()) strategy.deploy(webApp, document);
  }

  public boolean undeploy(WebApp webApp) throws ExecutionException {
    return (isWritable()) ? false : strategy.undeploy(webApp, document);
  }

  public void save() throws IOException {
    if (isWritable()) JDOMUtil.writeDocument(document, generatedConfig, "\n");
  }

  public void setPort(int port) {
    if (isWritable()) strategy.setPort(port, document);
  }

  public File getSourceConfig() {
    return sourceConfig;
  }

  public void clearTempFile() {
    if (isWritable()) {
      try {
        FileWriter fw = new FileWriter(generatedConfig, false);
        fw.write("");
        fw.close();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
