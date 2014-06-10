/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.make;

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Builder;
import aQute.lib.osgi.Jar;
import aQute.lib.osgi.Verifier;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osmorc.frameworkintegration.LibraryBundlificationRule;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.settings.ApplicationSettings;

import javax.swing.SwingUtilities;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which wraps bnd and integrates it into IntellIJ.
 * <p/>
 * TODO: pedantic setting
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class BndWrapper
{

  /**
   * Wraps an existing jar file using bnd's analyzer. This class will check and use any applying bundlification rules
   * for this library that have been set up in Osmorcs library bundlification dialog.
   * <p/>
   *
   * @param compileContext a compile context
   * @param sourceJarUrl   the URL to the source jar file
   * @param outputPath     the path where to place the bundled library.
   * @return the URL to the bundled library.
   */
  public String wrapLibrary(@NotNull CompileContext compileContext, final String sourceJarUrl, String outputPath)
  {
    try
    {
      File targetDir = new File(outputPath);
      File sourceFile = new File(VfsUtil.urlToPath(sourceJarUrl));

      File targetFile = new File(targetDir.getPath() + File.separator + sourceFile.getName());
      Map<String, String> additionalProperties = new HashMap<String, String>();

      // okay try to find a rule for this nice package:
      long lastModified = Long.MIN_VALUE;
      ApplicationSettings settings = ServiceManager.getService(ApplicationSettings.class);
      for (LibraryBundlificationRule bundlificationRule : settings.getLibraryBundlificationRules())
      {
        if (bundlificationRule.appliesTo(sourceFile.getName()))
        {
          if (bundlificationRule.isDoNotBundle())
          {
            return null; // make it quick in this case
          }
          additionalProperties.putAll(bundlificationRule.getAdditionalPropertiesMap());
          // if a rule applies which has been changed recently we need to re-bundle the file
          lastModified = Math.max(lastModified, bundlificationRule.getLastModified());

          // if stop after this rule is true, we will no longer try to find any more matching rules
          if ( bundlificationRule.isStopAfterThisRule()) {
            break;
        }
      }
      }


      if (!targetFile.exists() || targetFile.lastModified() < sourceFile.lastModified() ||
          targetFile.lastModified() < lastModified)
      {
        if (doWrap(compileContext, sourceFile, targetFile, additionalProperties))
        {
          return VfsUtil.pathToUrl(targetFile.getCanonicalPath());
        }
      }
    }
    catch (final Exception e)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          Messages.showErrorDialog(
              OsmorcBundle.getTranslation("bundlecompiler.bundlifying.problem.message", sourceJarUrl, e.toString()),
              OsmorcBundle.getTranslation("error"));
        }
      });

    }
    return null;
  }

  /**
   * Internal function which does the actual wrapping. This is 90% borrowed from bnd's source code.
   *
   * @param compileContext the compile context
   * @param inputJar       the input file
   * @param outputJar      the output file
   * @param properties     properties for the manifest. these may contain bnd instructions
   * @return true if the bundling was successful, false otherwise.
   * @throws Exception in case something goes wrong.
   */
  private boolean doWrap(@NotNull CompileContext compileContext, @NotNull File inputJar, @NotNull File outputJar,
                         @NotNull Map<String, String> properties) throws Exception
  {
    String sourceFileUrl = VfsUtil.pathToUrl(inputJar.getPath());
    Analyzer analyzer = new ReportingAnalyzer(compileContext, sourceFileUrl);
    analyzer.setPedantic(false);
    analyzer.setJar(inputJar);
    Jar dot = analyzer.getJar();
    analyzer.putAll(properties, false);
    if (analyzer.getProperty(Constants.IMPORT_PACKAGE) == null)
    {
      analyzer.setProperty(Constants.IMPORT_PACKAGE, "*;resolution:=optional");
    }
    if (analyzer.getProperty(Constants.BUNDLE_SYMBOLICNAME) == null)
    {
      Pattern p = Pattern.compile("(" + Verifier.SYMBOLICNAME.pattern() + ")(-[0-9])?.*\\.jar");
      String base = inputJar.getName();
      Matcher m = p.matcher(base);
      if (m.matches())
      {
        base = m.group(1);
      }
      else
      {
        compileContext.addMessage(CompilerMessageCategory.ERROR,
            "Can not calculate name of output bundle, rename jar or use -properties", sourceFileUrl, 0, 0);
        return false;
      }

      analyzer.setProperty(Constants.BUNDLE_SYMBOLICNAME, base);
    }
    if (analyzer.getProperty(Constants.EXPORT_PACKAGE) == null)
    {
      // avoid spurious error messages about string starting with ","
      // String export = analyzer.calculateExportsFromContents(dot).replaceFirst("^\\s*,", "");
      analyzer.setProperty(Constants.EXPORT_PACKAGE, "*");
//      analyzer.setProperty(Constants.EXPORT_PACKAGE, export);
    }
    analyzer.mergeManifest(dot.getManifest());
    String version = analyzer.getProperty(Constants.BUNDLE_VERSION);
    if (version != null)
    {
      version = Builder.cleanupVersion(version);
      analyzer.setProperty(Constants.BUNDLE_VERSION, version);
    }
    Manifest mf = analyzer.calcManifest();
    Jar jar = analyzer.getJar();
    File f = File.createTempFile("tmpbnd", ".jar");
    jar.write(f);
    jar.close();
    analyzer.close();
    if (!f.renameTo(outputJar))
    {
      VirtualFile src = LocalFileSystem.getInstance().findFileByIoFile(f);
      VirtualFile target = LocalFileSystem.getInstance().findFileByIoFile(outputJar);
      VfsUtil.copyFile(this, src, target);
    }
    return true;
  }


  public boolean build(@NotNull CompileContext compileContext, @NotNull String bndFileUrl,
                       @NotNull String[] classPathUrls, @NotNull String outputPath,
                       @NotNull Map<String, String> additionalProperties)
  {

    File[] classPathEntries = new File[classPathUrls.length];
    for (int i = 0; i < classPathUrls.length; i++)
    {
      String classPathUrl = classPathUrls[i];
      classPathEntries[i] = new File(VfsUtil.urlToPath(classPathUrl));
    }

    File bndFile = new File(VfsUtil.urlToPath(bndFileUrl));
    File outFile = new File(outputPath);
    Properties props = new Properties();
    for (Map.Entry<String, String> stringStringEntry : additionalProperties.entrySet())
    {
      props.put(stringStringEntry.getKey(), stringStringEntry.getValue());
    }
    try
    {
      return doBuild(compileContext, bndFile, classPathEntries, outFile, props );
    }
    catch (Exception e)
    {
      compileContext.addMessage(CompilerMessageCategory.ERROR, "Unexpected error: " + e.getMessage(), null, 0,0);
      return false;
    }

  }

  private boolean doBuild(@NotNull CompileContext compileContext, @NotNull File bndFile, @NotNull File classpath[],
                          @NotNull File output, @NotNull Properties additionalProperties)
      throws Exception
  {
    ReportingBuilder builder = new ReportingBuilder(compileContext, VfsUtil.pathToUrl(bndFile.getPath()));
    builder.setPedantic(false);
    builder.setProperties(bndFile);
    builder.mergeProperties(additionalProperties, true);

    // FIX for IDEADEV-39089
    // am not really sure if this is a good idea all the time but then again what use is building a bundle without exports in 90% of the cases?
    if ( builder.getProperty(Constants.EXPORT_PACKAGE) == null ) {
      builder.setProperty(Constants.EXPORT_PACKAGE, "*");
    }
    builder.setClasspath(classpath);
    // XXX: seems to be a new bug in bnd, when calling build(), begin is not called, therefore the ignores dont work..
    // so i have overridden it and calling it manually here..
    builder.begin();
    Jar jar = builder.build();
    jar.setName(output.getName());
    jar.write(output);
    builder.close();
    return true;
  }


}
