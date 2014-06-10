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
package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.util.text.LineTokenizer;

import java.util.*;

import org.jetbrains.annotations.NonNls;

public class FormParser {
  private static final String COMMENT_PREFIX = "#";
  private final List<String> myAvailableFields;
  private final List<String> myLines;
  private final Map<String, List<String>> myResult = new LinkedHashMap<String, List<String>>();
  private String myCurrentField;

  private FormParser(List<String> availableFields, List<String> lines) {
    myAvailableFields = availableFields;
    myLines = lines;
  }

  public static Map<String, List<String>> execute(String source, @NonNls String[] fields) {
    FormParser formParser = new FormParser(new ArrayList<String>(Arrays.asList(fields)), splitIntoStringsExceptComments(source));
    formParser.executeInt();
    return formParser.myResult;
  }

  private void executeInt() {
    for (Iterator<String> iterator = myLines.iterator(); iterator.hasNext();) {
      String line = iterator.next();
      String field = isBeginOfNewField(line);
      String value = line;
      if (field != null) {
        myAvailableFields.remove(field);
        value = line.substring(field.length());
        value = value.trim();
        myCurrentField = field;
      }

      if (myCurrentField != null && value.length() > 0) {
        if (!myResult.containsKey(myCurrentField)) {
          myResult.put(myCurrentField, new ArrayList<String>());
        }

        myResult.get(myCurrentField).add(value);
      }
    }
  }

  private String isBeginOfNewField(String line) {
    for (Iterator<String> iterator = myAvailableFields.iterator(); iterator.hasNext();) {
      String field = iterator.next();
      if (line.startsWith(field)) return field;
    }
    return null;
  }

  private static List<String> splitIntoStringsExceptComments(String source) {
    ArrayList<String> result = new ArrayList<String>();
    if (source != null) {
      String[] lines = LineTokenizer.tokenize(source, false);
      for (int i = 0; i < lines.length; i++) {
        String line = lines[i];
        line = line.trim();
        if (line.length() > 0 && !line.startsWith(COMMENT_PREFIX)) {
          result.add(line);
        }
      }
    }
    return result;
  }

}
