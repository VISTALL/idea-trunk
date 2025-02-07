/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package com.intellij.j2meplugin.compiler;

import com.intellij.openapi.compiler.ValidityState;

import java.io.DataOutput;
import java.io.IOException;

/**
 * User: anna
 * Date: Oct 5, 2004
 */
public class EmptyValidityState implements ValidityState {
  public boolean equalsTo(ValidityState otherState) {
    //force recompile
    return otherState == this;
  }


  public void save(DataOutput out) throws IOException {
  }


}
