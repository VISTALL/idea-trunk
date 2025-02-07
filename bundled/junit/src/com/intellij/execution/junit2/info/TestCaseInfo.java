/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package com.intellij.execution.junit2.info;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.junit2.segments.ObjectReader;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.MethodSignatureUtil;

import java.io.File;

class TestCaseInfo extends ClassBasedInfo {
  private String myMethod;

  public TestCaseInfo() {
    super(DisplayTestInfoExtractor.CLASS_FULL_NAME);
  }

  public void readPacketFrom(final ObjectReader reader) {
    myMethod = reader.readLimitedString();
    readClass(reader);
  }

  public String getName() {
    return myMethod;
  }

  public Location getLocation(final Project project) {
    final Location<PsiClass> classLocation = (Location<PsiClass>)super.getLocation(project);
    if (classLocation == null) return getPsiFile(project);
    String strippedMethodName = myMethod; //navigation to for parametr. methods
    final int idx = myMethod.indexOf('[');
    if (idx != -1) {
      strippedMethodName = myMethod.substring(0, idx);
    }
    final PsiMethod method = MethodSignatureUtil.findMethodBySignature(classLocation.getPsiElement(),
        MethodSignatureUtil.createMethodSignature(strippedMethodName, PsiType.EMPTY_ARRAY, PsiTypeParameter.EMPTY_ARRAY, PsiSubstitutor.EMPTY), true);
    if (method != null)
      return new MethodLocation(project, method, classLocation);
    final Location<PsiFile> fileLocation = getPsiFile(project);
    if (fileLocation == null) return classLocation;
    else return fileLocation;
  }

  private Location<PsiFile> getPsiFile(final Project project) {
    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(myMethod.replace(File.separatorChar, '/'));
    if (virtualFile != null)
      return PsiLocation.fromPsiElement(project, PsiManager.getInstance(project).findFile(virtualFile));
    return null;
  }

  public boolean shouldRun() {
    return true;
  }

}