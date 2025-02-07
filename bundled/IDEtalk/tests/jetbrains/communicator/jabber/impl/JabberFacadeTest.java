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
package jetbrains.communicator.jabber.impl;

import com.intellij.openapi.util.io.FileUtil;
import jetbrains.communicator.BaseTestCase;
import jetbrains.communicator.jabber.AccountInfo;
import jetbrains.communicator.mock.MockIDEFacade;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kir
 */
public class JabberFacadeTest extends BaseTestCase {
  private JabberFacadeImpl myFacade;
  private MockIDEFacade myIDEFacade;

  protected void setUp() throws Exception {
    super.setUp();

    myIDEFacade = new MockIDEFacade(getClass());
    myFacade = new JabberFacadeImpl(myIDEFacade);
    myFacade.getMyAccount().setRememberPassword(true);
  }

  public void testGoogleTalk() throws Exception {
    try {
      assertNull(myFacade.connect("maxkir1", "123123123", "talk.google.com", 5222, false));
      assertTrue(myFacade.isConnectedAndAuthenticated());

      assertNull(myFacade.connect("maxkir1@gmail.com", "123123123", "talk.google.com", 5222, false));
      assertTrue(myFacade.isConnectedAndAuthenticated());
      assertTrue(myFacade.getConnection().isSecureConnection());

      assertEquals("Should not change", "maxkir1@gmail.com", myFacade.getMyAccount().getUsername());
    } finally {
      myFacade.disconnect();
    }
  }

  public void testPersist_Settings() throws Exception {
    myFacade.getMyAccount().setUsername("user");
    myFacade.getMyAccount().setPassword("������ ����");
    myFacade.getMyAccount().setServer("jabber.ru");
    myFacade.getMyAccount().setPort(839);

    myFacade.saveSettings();

    JabberFacadeImpl loaded = new JabberFacadeImpl(myIDEFacade);

    assertEquals("Should persist AccountInfo",
        new AccountInfo("user", "������ ����", "jabber.ru", 839).toString(),
        loaded.getMyAccount().toString());
    assertEquals("Password decoded incorrectly", "������ ����", loaded.getMyAccount().getPassword());
  }

  public void testNoPlainPassword() throws Exception {
    myFacade.getMyAccount().setPassword("pwd822");
    myFacade.saveSettings();

    File file = new File(myIDEFacade.getConfigDir(), JabberFacadeImpl.FILE_NAME);
    String fileText = new String(FileUtil.loadFileText(file));

    assertEquals("toString Should not contain plain password: " + myFacade.getMyAccount().toString(), -1,
        myFacade.getMyAccount().toString().indexOf("pwd822"));
    assertEquals("Settings file should not contain plain password: " + fileText, -1, fileText.indexOf("pwd822"));
  }

  public void testDoNotRememberPassword() throws Exception {
    myFacade.getMyAccount().setPassword("pwd822");
    myFacade.getMyAccount().setRememberPassword(false);

    myFacade.saveSettings();

    JabberFacadeImpl loaded = new JabberFacadeImpl(myIDEFacade);
    assertEquals("Should not memember password", "", loaded.getMyAccount().getPassword());
  }

  public void testGetServers() throws Exception {
    List<String> list = Arrays.asList(myFacade.getServers());
    assertTrue("List of servers should be taken from file:" + list, list.size() > 20);
    assertTrue("List should contain intellijoin.org:" + list,  list.contains("intellijoin.org"));
    for (int i = 0; i < list.size(); i++) {
      java.lang.String s = list.get(i);
      assertNotNull("Nulls not allowed:" + i +" " + list, s);
    }
  }

  public void testSkipConnect_WhenDisabled() throws Exception {
    myFacade.getMyAccount().setLoginAllowed(false);
    myFacade.connect();
    assertNull("Should not try to connect", myFacade.getConnection());
  }
}
