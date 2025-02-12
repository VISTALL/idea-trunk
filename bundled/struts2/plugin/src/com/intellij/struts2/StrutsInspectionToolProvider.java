/*
 * Copyright 2008 The authors
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
package com.intellij.struts2;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.struts2.dom.inspection.Struts2ModelInspection;
import com.intellij.struts2.dom.inspection.ValidatorConfigModelInspection;
import com.intellij.struts2.dom.inspection.ValidatorModelInspection;

/**
 * Registers all inspections.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsInspectionToolProvider implements InspectionToolProvider {

  public Class[] getInspectionClasses() {
    return new Class[]{Struts2ModelInspection.class,
                       ValidatorModelInspection.class,
                       ValidatorConfigModelInspection.class};
  }

}