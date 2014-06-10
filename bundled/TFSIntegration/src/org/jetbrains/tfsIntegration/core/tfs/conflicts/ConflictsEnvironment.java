/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.tfsIntegration.core.tfs.conflicts;

public class ConflictsEnvironment {

  private static ConflictsHandler ourConflictsHandler = new DialogConflictsHandler();
  private static NameMerger ourNameMerger = new DialogNameMerger();
  private static ContentMerger ourContentMerger = new DialogContentMerger();

  public static NameMerger getNameMerger() {
    return ourNameMerger;
  }

  public static void setNameMerger(NameMerger nameMerger) {
    ourNameMerger = nameMerger;
  }

  public static ContentMerger getContentMerger() {
    return ourContentMerger;
  }

  public static void setContentMerger(ContentMerger contentMerger) {
    ourContentMerger = contentMerger;
  }

  public static void setConflictsHandler(ConflictsHandler conflictsHandler) {
    ourConflictsHandler = conflictsHandler;
  }

  public static ConflictsHandler getConflictsHandler() {
    return ourConflictsHandler;
  }


}
