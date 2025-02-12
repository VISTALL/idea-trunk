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

package org.jetbrains.android.sdk;

import com.android.sdklib.IAndroidTarget;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Jun 2, 2009
 * Time: 2:37:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidSdk11 extends AndroidSdk {
  private final AndroidTarget11 myTarget;

  public AndroidSdk11(@NotNull String location) {
    myTarget = new AndroidTarget11(location);
  }

  @NotNull
  @Override
  public String getLocation() {
    return myTarget.getLocation();
  }

  @NotNull
  @Override
  public String getName() {
    return "Android SDK 1.1";
  }

  @NotNull
  @Override
  public IAndroidTarget[] getTargets() {
    return new IAndroidTarget[]{myTarget};
  }

  @NotNull
  @Override
  public String getDefaultTargetName() {
    return myTarget.getName();
  }

  @Override
  public int getExpectedTargetCount() {
    return 1;
  }

  public boolean isValid() {
    return myTarget.isValid();
  }
}
