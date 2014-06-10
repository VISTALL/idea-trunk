/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.references.manager;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspGroovyFile;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GrImplicitVariableImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ilyas
 */
public class GrailsImplicitVariableManagerImpl extends GrailsImplicitVariableManager {

  private final Map<PsiFile, Map<String, PsiElement>> myImplicitFieldsMap = new HashMap<PsiFile, Map<String, PsiElement>>();

  private void fillImplicitVariableMap(@NotNull PsiFile file) {
    if (file instanceof GspGroovyFile) {
      fillGspImplicitFields(((GspGroovyFile) file));
      return;
    }
    PsiFile originalFile = file.getOriginalFile();
    if (GrailsUtils.isControllerClassFile(originalFile.getVirtualFile(), file.getProject())) {
      fillControllerImplicitFields(file);
    }
  }

  private void fillControllerImplicitFields(PsiFile file) {
    PsiManager manager = PsiManager.getInstance(file.getProject());
    Map<String, PsiElement> fileFieldsMap = new HashMap<String, PsiElement>(11);
    fileFieldsMap.put("actionName", new GrImplicitVariableImpl(null, manager, "actionName", "java.lang.String", file));
    fileFieldsMap.put("actionUri", new GrImplicitVariableImpl(null, manager, "actionUri", "java.net.URI", file));
    fileFieldsMap.put("controllerName", new GrImplicitVariableImpl(null, manager, "controllerName", "java.lang.String", file));
    fileFieldsMap.put("controllerUri", new GrImplicitVariableImpl(null, manager, "controllerUri", "java.net.URI", file));
    fileFieldsMap.put("flash", new GrImplicitVariableImpl(null, manager, "flash", "org.codehaus.groovy.grails.web.servlet.FlashScope", file));
    fileFieldsMap.put("log", new GrImplicitVariableImpl(null, manager, "log", "org.apache.commons.logging.Log", file));
    fileFieldsMap.put("params", new GrImplicitVariableImpl(null, manager, "params", "java.util.Map", file));
    fileFieldsMap.put("request", new GrImplicitVariableImpl(null, manager, "request", "javax.servlet.http.HttpServletRequest", file));
    fileFieldsMap.put("response", new GrImplicitVariableImpl(null, manager, "response", "javax.servlet.http.HttpServletResponse", file));
    fileFieldsMap.put("session", new GrImplicitVariableImpl(null, manager, "session", "javax.servlet.http.HttpSession", file));
    fileFieldsMap.put("servletContext", new GrImplicitVariableImpl(null, manager, "servletContext", "javax.servlet.ServletContext", file));
    myImplicitFieldsMap.put(file, fileFieldsMap);
  }

  private void fillGspImplicitFields(GspGroovyFile file) {
    PsiManager manager = PsiManager.getInstance(file.getProject());
    Map<String, PsiElement> fileFieldsMap = new HashMap<String, PsiElement>(8);
    fileFieldsMap.put("application", new GrImplicitVariableImpl(null, manager, "application", "javax.servlet.ServletContext", file));
    fileFieldsMap.put("flash", new GrImplicitVariableImpl(null, manager, "flash", "org.codehaus.groovy.grails.web.servlet.FlashScope", file));
    fileFieldsMap.put("out", new GrImplicitVariableImpl(null, manager, "out", "java.io.Writer", file));
    fileFieldsMap.put("params", new GrImplicitVariableImpl(null, manager, "params", "java.util.Map", file));
    fileFieldsMap.put("request", new GrImplicitVariableImpl(null, manager, "request", "javax.servlet.http.HttpServletRequest", file));
    fileFieldsMap.put("response", new GrImplicitVariableImpl(null, manager, "response", "javax.servlet.http.HttpServletResponse", file));
    fileFieldsMap.put("session", new GrImplicitVariableImpl(null, manager, "session", "javax.servlet.http.HttpSession", file));
    fileFieldsMap.put("grailsApplication", new GrImplicitVariableImpl(null, manager, "grailsApplication", "org.codehaus.groovy.grails.commons.GrailsApplication", file));
    myImplicitFieldsMap.put(file, fileFieldsMap);
  }

  public PsiElement getImplicitVariable(String name, PsiFile file) {
    if (name == null || file == null) {
      return null;
    }

    if (myImplicitFieldsMap.get(file) == null) {
      fillImplicitVariableMap(file);
    }

    Map<String, PsiElement> elementMap = myImplicitFieldsMap.get(file);
    if (elementMap == null) return null;
    return elementMap.get(name);
  }

}
