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

package org.jetbrains.tfsIntegration.core.configuration;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.PasswordUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

@Tag(value = "credentials")
public class Credentials {

  private @NotNull String myUserName;

  private @NotNull String myDomain;

  private @Nullable String myPassword;

  private boolean myStorePassword;

  public Credentials() {
    this("", "", null, false);
  }

  public Credentials(final @NotNull String userName,
                     final @NotNull String domain,
                     final @Nullable String password,
                     final boolean storePassword) {
    myUserName = userName;
    myDomain = domain;
    myPassword = password;
    myStorePassword = storePassword;
  }

  /**
   * @return null is no password set
   */
  @Nullable
  public String getPassword() {
    return myPassword;
  }

  public void resetPassword() {
    myPassword = null;
    myStorePassword = false;
  }

  @Nullable
  @Tag(value = "password")
  public String getEncodedPassword() {
    return myStorePassword && myPassword != null ? PasswordUtil.encodePassword(myPassword) : null;
  }

  public void setEncodedPassword(String encodedPassword) {
    myPassword = encodedPassword != null ? PasswordUtil.decodePassword(encodedPassword) : null;
    myStorePassword = true;
  }

  @NotNull
  @Tag(value = "domain")
  public String getDomain() {
    return myDomain;
  }

  public void setDomain(final @NotNull String domain) {
    myDomain = domain;
  }

  @NotNull
  @Tag(value = "username")
  public String getUserName() {
    return myUserName;
  }

  public void setUserName(final @NotNull String userName) {
    myUserName = userName;
  }

  @NotNull
  public String getQualifiedUsername() {
    if (getDomain().length() > 0) {
      return MessageFormat.format("{0}\\{1}", getDomain(), getUserName());
    }
    else {
      return getUserName();
    }
  }

  public boolean equalsTo(final @NotNull Credentials c) {
    if (!getUserName().equals(c.getUserName())) {
      return false;
    }
    if (!getDomain().equals(c.getDomain())) {
      return false;
    }
    return Comparing.equal(getPassword(), c.getPassword(), true);
  }

  @NonNls
  public String toString() {
    //noinspection ConstantConditions
    return getQualifiedUsername() + "," + (getPassword() != null ? getPassword().replaceAll(".", "x") : "(no password)");
  }

}
