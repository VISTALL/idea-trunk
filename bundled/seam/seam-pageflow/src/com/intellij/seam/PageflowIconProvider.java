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
package com.intellij.seam;

import com.intellij.seam.model.xml.pageflow.*;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomIconProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author peter
 */
public class PageflowIconProvider extends DomIconProvider {

  public Icon getIcon(@NotNull DomElement element, int flags) {
    if (!(element instanceof SeamPageflowDomElement)) {
      return null;
    }

    if (element instanceof Page) {
      return PageflowIcons.PAGEFLOW_PAGE;
    }
    if (element instanceof Decision) {
      return PageflowIcons.PAGEFLOW_DECISION;
    }
    if (element instanceof StartState) {
      return PageflowIcons.PAGEFLOW_START;
    }
    if (element instanceof EndState) {
      return PageflowIcons.PAGEFLOW_END_STATE;
    }
    if (element instanceof ProcessState) {
      return PageflowIcons.PAGEFLOW_PROCESS_STATE;
    }
    if (element instanceof StartPage) {
      return PageflowIcons.PAGEFLOW_START_PAGE;
    }

    return null;
  }
}
