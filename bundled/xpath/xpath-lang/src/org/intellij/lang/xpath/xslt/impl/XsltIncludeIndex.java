/*
 * Copyright 2005-2008 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.intellij.lang.xpath.xslt.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.include.FileIncludeManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.NullableFunction;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.lang.xpath.xslt.XsltSupport;
import org.jetbrains.annotations.NotNull;

/*
* Created by IntelliJ IDEA.
* User: sweinreuter
* Date: 15.12.2008
*/
public class XsltIncludeIndex {


  public static boolean isReachableFrom(XmlFile which, XmlFile from) {
        return from == which || _isReachableFrom(from.getVirtualFile(), FileIncludeManager.getManager(which.getProject()).getIncludingFiles(which.getVirtualFile(), true));
    }

    private static boolean _isReachableFrom(VirtualFile from, VirtualFile[] which) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < which.length; i++) {
            final VirtualFile file = which[i];
            if (file == from) {
                return true;
            }
        }
        return false;
    }

  public static boolean processBackwardDependencies(@NotNull XmlFile file, Processor<XmlFile> processor) {
      Project project = file.getProject();
      final PsiManager psiManager = PsiManager.getInstance(project);
      VirtualFile[] files = FileIncludeManager.getManager(project).getIncludingFiles(file.getVirtualFile(), true);
      PsiFile[] psiFiles = ContainerUtil.map2Array(files, PsiFile.class, new NullableFunction<VirtualFile, PsiFile>() {
        public PsiFile fun(VirtualFile file) {
          return psiManager.findFile(file);
        }
      });
      for (final PsiFile psiFile : psiFiles) {
        if (XsltSupport.isXsltFile(psiFile)) {
          if (!processor.process((XmlFile)psiFile)) {
            return false;
          }
        }
      }
      return true;
    }

}