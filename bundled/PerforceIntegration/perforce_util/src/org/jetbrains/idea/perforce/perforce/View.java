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

import com.intellij.execution.configurations.ParametersList;
import com.intellij.util.PatternUtil;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class View {
  private final String myDepotPath;
  private final String myLocalPath;
  private final String myStringRepresentation;

  private String myLocalPattern;

  private Matcher myDepotMatcher;
  private Map<Integer, Integer> myDepotPatternBackReferences;
  private List<Integer> myWildcardGroupNumbers;
  private List<Integer> myStarGroupNumbers;
  private int myDepotPatternGroupCount;


  public static View create(String viewString) {

    final String[] params = ParametersList.parse(viewString);

    final List<String> strings = Arrays.asList(params);
    if (strings.size() != 2) return null;
    String depot = strings.get(0).trim();
    String client = strings.get(1).trim();

    return new View(depot, client, viewString);
  }

  private View(final String depotPath, final String localPath, final String viewString) {
    myDepotPath = depotPath;
    myLocalPath = localPath;
    myStringRepresentation = viewString;
  }

  public String getDepotPath() {
    return myDepotPath;
  }

  public String getLocalPath() {
    return myLocalPath;
  }


  public String toString() {
    return myStringRepresentation;
  }

  public synchronized String match(final String filePath, String clientName) {
    String result = replaceDepotPathWithLocal(filePath);
    if (result == null) return null;
    if (result.startsWith("//")) result = result.substring(2);
    if (result.startsWith(clientName)) result = result.substring(clientName.length());
    if (result.startsWith("/")) result = result.substring(1);
    return result;
  }

  public boolean removeMatched() {
    return getDepotPath().startsWith("-");
  }

  public boolean replaceMatched() {
    return getDepotPath().startsWith("+");
  }

  @Nullable
  public static String getRelativePath(String filePath, String clientName, List<View> views) {
    String result = null;
    for (View view : views) {
      if (clientName != null) {
        final String localResult = view.match(filePath, clientName);
        if (localResult != null) {
          if (view.removeMatched()) {
            result = null;
          }
          else {
            result = localResult;
          }
        }
      }
    }
    if (result == null) {
      return null;
    }
    else {
      return result;
    }
  }

  interface ReferenceVisitor {
    void visitNextReference(int referenceNum);
    void visitStringFragment(CharSequence string);
    void visitWildcard();
    void visitStar();
  }

  private static void processStringWithReferences(final String stringWithReferences, ReferenceVisitor visitor) {
    int offset = 0;
    int refIndex = stringWithReferences.indexOf("%%", offset);
    int wildcardIndex = stringWithReferences.indexOf("\\.\\.\\.", offset);
    int starIndex = stringWithReferences.indexOf(".*", offset);

    while ((refIndex >= 0 && stringWithReferences.length() > refIndex + 2) || wildcardIndex >= 0 || starIndex >= 0) {

      if ( existsAndBefore(refIndex, wildcardIndex, starIndex) ) {

        // Process reference, i.e %%3 :
        if (refIndex > offset) {
          visitor.visitStringFragment(stringWithReferences.subSequence(offset, refIndex));
        }
        final String referenceName = stringWithReferences.substring(refIndex + 2, refIndex + 3);
        try {
          visitor.visitNextReference(Integer.parseInt(referenceName));
        }
        finally {
          offset = refIndex + 3;
        }
        refIndex = stringWithReferences.indexOf("%%", offset);
      }
      else if ( existsAndBefore(starIndex, wildcardIndex) ) {

        // Process star, i.e. *:
        if (starIndex > offset) {
          visitor.visitStringFragment(stringWithReferences.subSequence(offset, starIndex));
        }
        try {
          visitor.visitStar();
        }
        finally {
          offset = starIndex + 2;
        }
        starIndex = stringWithReferences.indexOf(".*", offset);
      }

      else {

        // Process wildcard, i.e. ...
        if (wildcardIndex > offset) {
          visitor.visitStringFragment(stringWithReferences.subSequence(offset, wildcardIndex));
        }
        try {
          visitor.visitWildcard();
        }
        finally {
          offset = wildcardIndex + 6;
        }
        wildcardIndex = stringWithReferences.indexOf("\\.\\.\\.", offset);

      }
    }
    if (offset < stringWithReferences.length()) {
      visitor.visitStringFragment(stringWithReferences.subSequence(offset, stringWithReferences.length()));
    }

  }

  private static boolean existsAndBefore(int index, int ... otherIndices) {
    if (index == -1) return false;
    for (int otherIdx : otherIndices) {
      if (otherIdx < index && otherIdx != -1) return false;
    }
    return true;
  }

  private String replaceDepotPathWithLocal(String filePath) {
    final Matcher matcher = getMatcher(filePath);
    if (matcher.matches()) {
      return matcher.replaceFirst(convertLocalPathToReplacement(myDepotPatternBackReferences));
      //return referencesProcessed + "/" + matcher.group(myDepotPatternGroupCount);
    } else {
      return null;
    }
  }

  private Matcher getMatcher(String filePath) {
    if (myDepotMatcher == null) {
      String depotPath = getDepotPath();
      if (depotPath.startsWith("+") ||depotPath.startsWith("-")) {
        depotPath = depotPath.substring(1);
      }

      String depotPattern = PatternUtil.convertToRegex(depotPath);
      myDepotPatternBackReferences = new HashMap<Integer, Integer>();
      myWildcardGroupNumbers = new ArrayList<Integer>();
      myStarGroupNumbers = new ArrayList<Integer>();
      myDepotPatternGroupCount = 1;
      final StringBuffer patternWithReferences = new StringBuffer();
      processStringWithReferences(depotPattern, new ReferenceVisitor() {

        public void visitWildcard() {
          visitGroup("(.*)", myWildcardGroupNumbers);
        }

        public void visitStar() {
          visitGroup("([^/]+)", myStarGroupNumbers);
        }

        private void visitGroup(String patternToAdd, List<Integer> groupNumbersCollection) {
          patternWithReferences.append(patternToAdd);
          groupNumbersCollection.add(myDepotPatternGroupCount);
          myDepotPatternGroupCount++;

        }

        public void visitNextReference(int referenceNum) {
          patternWithReferences.append("(.*)");
          myDepotPatternBackReferences.put(referenceNum, myDepotPatternGroupCount);
          myDepotPatternGroupCount++;
        }

        public void visitStringFragment(CharSequence string) {
          patternWithReferences.append(string.toString());
        }
      });
      depotPattern = patternWithReferences.toString();
      Pattern pattern = Pattern.compile(depotPattern, Pattern.CASE_INSENSITIVE);
      myDepotMatcher = pattern.matcher(filePath);
      return myDepotMatcher;
    }

    myDepotMatcher.reset(filePath);
    return myDepotMatcher;
  }

  private String convertLocalPathToReplacement(final Map<Integer, Integer> backReferences) {
    final StringBuffer result = new StringBuffer();

    processStringWithReferences(getLocalPattern(), new MyReferenceVisitor(result, backReferences));
    return result.toString();
  }

  private String getLocalPattern() {
    if (myLocalPattern == null) {
      myLocalPattern = PatternUtil.convertToRegex(getLocalPath());
    }
    return myLocalPattern;
  }

  private class MyReferenceVisitor implements ReferenceVisitor {
    private final StringBuffer myResult;
    private final Map<Integer, Integer> myBackReferences;
    private int myLastWildcardGroup = 0;
    private int myLastStarGroup = 0;

    public MyReferenceVisitor(final StringBuffer result, final Map<Integer, Integer> backReferences) {
      myResult = result;
      myBackReferences = backReferences;
    }

    public void visitWildcard() {
      myLastWildcardGroup = visitGroup(myLastWildcardGroup, myWildcardGroupNumbers);
    }

    public void visitStar() {
      myLastStarGroup = visitGroup(myLastStarGroup, myStarGroupNumbers);
    }

    private int visitGroup(int counter, List<Integer> groupNumbers) {
      if (counter < groupNumbers.size()) {
        myResult.append("$").append(String.valueOf(groupNumbers.get(counter).intValue()));
        return counter + 1;
      }
      return counter;
    }

    public void visitNextReference(int referenceNum) {
      final Integer refGroupNum = myBackReferences.get(referenceNum);
      if (refGroupNum != null) {
        myResult.append("$");
        myResult.append(refGroupNum);
      } else {
        myResult.append("%%");
        myResult.append(referenceNum);
      }
    }

    public void visitStringFragment(CharSequence string) {
      myResult.append(string.toString());
    }
  }
}
