/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * User: anna
 * Date: 21-Dec-2006
 */
package com.intellij.j2meplugin.compiler.ant;

import com.intellij.compiler.ant.BuildProperties;
import com.intellij.compiler.ant.ModuleChunk;
import com.intellij.compiler.ant.Tag;
import com.intellij.compiler.ant.taskdefs.Target;
import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.emulator.Emulator;
import com.intellij.j2meplugin.emulator.EmulatorType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Pair;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreverifyTarget extends Target {
  public PreverifyTarget(final ModuleChunk chunk) {
    super(J2MEBuildProperties.getPreverifyTargetName(chunk.getName()), null, J2MEBundle.message("ant.preverify.description", chunk.getName()), null);
    final Sdk projectJdk = chunk.getJdk();
    if (projectJdk != null) {
      final SdkAdditionalData additionalData = projectJdk.getSdkAdditionalData();
      if (additionalData instanceof Emulator) {
        final Emulator emulator = (Emulator)additionalData;
        final EmulatorType emulatorType = emulator.getEmulatorType();
        if (emulatorType != null) {
          final String emulatorPreverifyPath = emulatorType.getPreverifyPath();
          if (emulatorPreverifyPath != null) {
            final String preverifyPath = BuildProperties.propertyRef(BuildProperties.getJdkHomeProperty(projectJdk.getName())) + "/" + emulatorPreverifyPath;
            final Tag preverifyTag = new Tag("exec", new Pair[]{pair("executable", preverifyPath)});
            preverifyTag.add(new Arg("-d " + BuildProperties.propertyRef(BuildProperties.getTempDirForModuleProperty(chunk.getName()))));

            final List<String> urls = new ArrayList<String>();
            final Module[] modules = chunk.getModules();
            for (Module module : modules) {
              final OrderEntry[] orderEntries = ModuleRootManager.getInstance(module).getOrderEntries();
              for (OrderEntry orderEntry : orderEntries) {
                if (orderEntry instanceof LibraryOrderEntry) {
                  urls.addAll(Arrays.asList(orderEntry.getUrls(OrderRootType.CLASSES)));
                }
              }
            }
            final StringBuffer classpath = new StringBuffer();
            classpath.append(BuildProperties.propertyRef(BuildProperties.getModuleChunkJdkClasspathProperty(chunk.getName())));
            for (String url : urls) {
              classpath.append(File.separator).append(PathUtil.toPresentableUrl(url));
            }
            preverifyTag.add(new Arg("-classpath " + classpath.toString()));

            for (Module module : modules) {
              preverifyTag.add(new Arg(BuildProperties.propertyRef(BuildProperties.getOutputPathProperty(module.getName()))));
            }
            add(preverifyTag);
          }
        }
      }
    }
  }

  private static class Arg extends Tag {
    public Arg(@NonNls String value) {
      super("arg", new Pair[]{pair("line", value)});
    }
  }
}