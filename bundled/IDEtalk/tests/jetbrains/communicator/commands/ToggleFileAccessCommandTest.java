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
package jetbrains.communicator.commands;

import com.intellij.util.ArrayUtil;
import jetbrains.communicator.BaseTestCase;
import jetbrains.communicator.OptionFlag;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.impl.users.UserModelImpl;
import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserPresence;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.mock.MockTransport;
import jetbrains.communicator.mock.MockUser;
import org.jmock.Mock;

/**
 * @author Kir
 */
public class ToggleFileAccessCommandTest extends BaseTestCase {
  private ToggleFileAccessCommand myCommand;
  private Mock myUserListComponent;
  private UserModelImpl myUserModel;

  protected void setUp() throws Exception {
    super.setUp();

    myUserListComponent = mock(UserListComponent.class);
    myUserModel = new UserModelImpl(getBroadcaster());
    disposeOnTearDown(myUserModel);
    myCommand = new ToggleFileAccessCommand(myUserModel, (UserListComponent) myUserListComponent.proxy()) {
      protected boolean isFocused() {
        return true;
      }
    };
  }

  public void testEnabled() throws Exception {
    myUserListComponent.expects(once()).method("getSelectedNodes")
        .will(returnValue(ArrayUtil.EMPTY_OBJECT_ARRAY));
    assertFalse(myCommand.isEnabled());

    myUserListComponent.expects(atLeastOnce()).method("getSelectedNodes")
        .will(returnValue(new Object[]{"some group"}));
    assertTrue(myCommand.isEnabled());
  }

  public void testGetText() throws Exception {
    myUserListComponent.expects(once()).method("getSelectedNodes")
        .will(returnValue(ArrayUtil.EMPTY_OBJECT_ARRAY));
    assertNotNull(myCommand.getText());

    myUserListComponent.expects(atLeastOnce()).method("getSelectedNodes")
        .will(returnValue(new Object[]{"some group"}));
    assertNotNull(myCommand.getText());
  }
  
  public void testEnableAccessToUser() throws Exception {
    MockUser user = new MockUser();

    myUserListComponent.stubs().method("getSelectedNodes")
        .will(returnValue(new Object[]{user}));

    user.setCanAccessMyFiles(true, null);
    myCommand.execute();

    assertFalse("Should change access", user.canAccessMyFiles());
    myCommand.execute();
    assertTrue("Should change access again", user.canAccessMyFiles());
  }

  public void testEnableAccessToGroup() throws Exception {
    MockUser enabled = new MockUser("enabled", "group");
    enabled.setCanAccessMyFiles(true, myUserModel);
    MockUser disabled = new MockUser("disabled", "group");
    disabled.setCanAccessMyFiles(false, myUserModel);

    myUserModel.addUser(enabled);
    myUserModel.addUser(disabled);

    myUserListComponent.stubs().method("getSelectedNodes")
        .will(returnValue(new Object[]{"group"}));

    myCommand.execute();
    assertTrue("Should change access to disabled", disabled.canAccessMyFiles());
    assertFalse("Should change access to enabled", enabled.canAccessMyFiles());
  }

  public void testMultipleSelection_NoGroups() throws Exception {
    MockUser user1 = new MockUser("User1", "a");
    MockUser user2 = new MockUser("User2", "a");

    myUserListComponent.stubs().method("getSelectedNodes")
        .will(returnValue(new Object[]{user1, user2}));


    testCommandStateChanges(user1, user2);
  }

  public void testMultipleSelection_WithGroup() throws Exception {
    User user1 = myUserModel.createUser("user1", MockTransport.NAME);
    User user2 = myUserModel.createUser("user2", MockTransport.NAME);
    myUserModel.addUser(user1);
    myUserModel.addUser(user2);
    user1.setGroup("grp", myUserModel);

    myUserListComponent.stubs().method("getSelectedNodes")
        .will(returnValue(new Object[]{"grp", user2}));

    testCommandStateChanges(user1, user2);
  }

  private void testCommandStateChanges(User user1, User user2) {
    assertTrue(myCommand.isEnabled());
    assertFalse(myCommand.isSelected());
    user2.setCanAccessMyFiles(true, myUserModel);
    assertFalse("Different state of canAccess flag", myCommand.isEnabled());
    user1.setCanAccessMyFiles(true, myUserModel);
    assertTrue(myCommand.isEnabled());
    assertTrue(myCommand.isSelected());

    myCommand.execute();
    assertFalse(user1.canAccessMyFiles());
    assertFalse(user2.canAccessMyFiles());
  }

  public void testMultipleSelection_OfflineUsersHidden() throws Exception {
    final User user1 = myUserModel.createUser("user1", MockTransport.NAME);
    final User user2 = myUserModel.createUser("user2", MockTransport.NAME);

    Pico.getInstance().unregisterComponent(Pico.getInstance().getComponentInstanceOfType(Transport.class));
    MockTransport mockTransport = new MockTransport() {
      public UserPresence getUserPresence(User user) {
        return user == user1 ? new UserPresence(true) : new UserPresence(false);
      }
    };
    Pico.getInstance().registerComponentInstance(mockTransport);

    myUserListComponent.stubs().method("getSelectedNodes")
        .will(returnValue(new Object[]{"grp", user2}));

    myUserModel.addUser(user1);
    myUserModel.addUser(user2);
    user1.setGroup("grp", myUserModel);

    user2.setCanAccessMyFiles(true, myUserModel);
    assertFalse("Different state of canAccess flag", myCommand.isEnabled());

    OptionFlag.OPTION_HIDE_OFFLINE_USERS.change(true);
    assertTrue("Only online user should be considered", myCommand.isEnabled());

    myCommand.execute();
    assertTrue("Should change online user", user1.canAccessMyFiles());
    assertTrue("Should keep offline user", user2.canAccessMyFiles());
  }

}
