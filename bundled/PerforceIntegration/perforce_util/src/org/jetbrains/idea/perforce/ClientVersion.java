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

package org.jetbrains.idea.perforce;

/**
 * Rev. P4/NTX86/2009.1/205670 (2009/06/29).
 * Rev. P4/NTX86/2006.2/112639 (2006/12/14).
 * Rev. P4/LINUX26X86/2008.2/179173 (2008/12/05).
 */
public class ClientVersion {
  public static ClientVersion UNKNOWN = new ClientVersion(-1, -1, -1);

  private final long myYear;
  private final long myVersion;
  private final long myBuild;

  public ClientVersion(long year, long version, long build) {
    myYear = year;
    myVersion = version;
    myBuild = build;
  }

  public long getYear() {
    return myYear;
  }

  public long getVersion() {
    return myVersion;
  }

  public long getBuild() {
    return myBuild;
  }

  public boolean supportsMove() {
    return myYear >= 2009;
  }

  @Override
  public String toString() {
    return myYear + "." + myVersion + "/" + myBuild;
  }
}
