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
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;

public class PerforceVcsRevisionNumber implements VcsRevisionNumber {
  private final long myRevisionNumber;
  private final long myChangeNumber;
  private final boolean myBranched;

  public PerforceVcsRevisionNumber(final long revisionNumber, final long changeNumber, final boolean branched) {
    myRevisionNumber = revisionNumber;
    myChangeNumber = changeNumber;
    myBranched = branched;
  }

  public String asString() {
    return String.valueOf(myChangeNumber);
  }

  public int compareTo(final VcsRevisionNumber o) {
    if(o instanceof PerforceVcsRevisionNumber) {
      return java.lang.Long.signum(myChangeNumber - ((PerforceVcsRevisionNumber)o).myChangeNumber);
    }
    if (o instanceof VcsRevisionNumber.Long) {
      return java.lang.Long.signum(myRevisionNumber - ((VcsRevisionNumber.Long) o).getLongValue());
    }
    return 0;
  }

  public long getRevisionNumber() {
    return myRevisionNumber;
  }

  public long getChangeNumber() {
    return myChangeNumber;
  }

  public boolean isBranched() {
    return myBranched;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final PerforceVcsRevisionNumber that = (PerforceVcsRevisionNumber)o;

    if (myChangeNumber != that.myChangeNumber) return false;
    return myRevisionNumber == that.myRevisionNumber;

  }

  public int hashCode() {
    int result;
    result = (int)(myRevisionNumber ^ (myRevisionNumber >>> 32));
    result = 29 * result + (int)(myChangeNumber ^ (myChangeNumber >>> 32));
    return result;
  }

  public String toString() {
    //noinspection UnnecessaryFullyQualifiedName
    return java.lang.Long.toString(myChangeNumber);
  }
}
