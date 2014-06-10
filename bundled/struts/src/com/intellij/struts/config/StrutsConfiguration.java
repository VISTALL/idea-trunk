/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

/**
 * Date: 06.04.2005 Time: 17:59:21
 * @author Dmitry Avdeev
 */
package com.intellij.struts.config;

public class StrutsConfiguration {

  private static final StrutsConfiguration instance = new StrutsConfiguration();

  public static StrutsConfiguration getInstance() {
    return instance;
  }

  public boolean autoscrollToSource = false;
  
  public boolean autoscrollFromSource = false;

  private StrutsConfiguration() {
  }
}
