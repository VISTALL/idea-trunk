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

package com.intellij.struts.dom;

import junit.framework.TestCase;
import com.intellij.struts.dom.converters.MemSizeConverter;

/**
 * @author Dmitry Avdeev
 */
public class ConvertersTest extends TestCase {

  public void testMemSizeConverter() {
    final MemSizeConverter converter = new MemSizeConverter();
    assertEquals(converter.fromString(null, null), null);
    assertEquals(converter.fromString("", null), null);
    assertEquals(converter.fromString("a", null), null);
    assertEquals(converter.fromString("ba", null), null);
    assertEquals(converter.fromString("1", null), new Long(1));
    assertEquals(converter.fromString("10K", null), new Long(10000));
    assertEquals(converter.fromString("3M", null), new Long(3000000));
    assertEquals(converter.fromString("34353G", null), new Long(34353000000000L));
  }
}
