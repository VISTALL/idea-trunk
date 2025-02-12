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

package org.jetbrains.groovy.grails;

import groovy.lang.GroovyResourceLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.grails.compiler.injection.ClassInjector;
import org.codehaus.groovy.grails.compiler.injection.DefaultGrailsDomainClassInjector;
import org.codehaus.groovy.grails.compiler.injection.GrailsAwareInjectionOperation;
import org.jetbrains.groovy.compiler.rt.CompilationUnitPatcher;

/**
 * @author peter
 */
public class GrailsInjectingPatcher implements CompilationUnitPatcher {

  public void patchCompilationUnit(CompilationUnit compilationUnit, GroovyResourceLoader resourceLoader) {
    final DefaultGrailsDomainClassInjector injector = new DefaultGrailsDomainClassInjector();
    compilationUnit.addPhaseOperation(new GrailsAwareInjectionOperation(resourceLoader, new ClassInjector[]{injector}), Phases.CONVERSION);
  }

}