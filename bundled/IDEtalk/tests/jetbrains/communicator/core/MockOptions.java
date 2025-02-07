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

package jetbrains.communicator.core;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kir
 */
public class MockOptions implements IDEtalkOptions {
  private final Map<String, Boolean> myOptions = new HashMap<String, Boolean>();
  private final Map<String, Double> myNumbers = new HashMap<String, Double>();

  public boolean isSet(String option) {
    return isSet(option, false);
  }

  public boolean isSet(String option, boolean defaultValue) {
    Boolean val = myOptions.get(option);
    if (val == null) return defaultValue;
    return val;
  }

  public double getNumber(String option, double defaultValue) {
    Double aDouble = myNumbers.get(option);
    return aDouble == null ? defaultValue : aDouble;
  }

  public void setNumber(String option, double value) {
    myNumbers.put(option, value);
  }

  public void setOption(String option, boolean value) {
    myOptions.put(option, Boolean.valueOf(value));
  }
}
