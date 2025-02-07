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

import jetbrains.communicator.LightTestCase;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.NullProgressIndicator;
import jetbrains.communicator.util.StringUtil;
import org.jmock.Mock;
import org.jmock.core.Invocation;
import org.jmock.core.stub.CustomStub;

import java.util.concurrent.FutureTask;

/**
 * @author Kir
 */
public class ClearHistoryCommandTest extends LightTestCase {
  private ClearHistoryCommand myCommand;
  private Mock myDispatcherMock;
  private Mock myIdeMock;

  protected void setUp() throws Exception {
    super.setUp();

    myDispatcherMock = mock(LocalMessageDispatcher.class);
    myIdeMock = mock(IDEFacade.class);

    myCommand = new ClearHistoryCommand(
        (LocalMessageDispatcher) myDispatcherMock.proxy(),
        (IDEFacade)myIdeMock.proxy());
  }

  public void testEnabled() throws Exception {
    myDispatcherMock.expects(once()).method("isHistoryEmpty").will(returnValue(true));
    assertFalse(myCommand.isEnabled());

    myDispatcherMock.expects(once()).method("isHistoryEmpty").will(returnValue(false));
    assertTrue(myCommand.isEnabled());
  }

  public void testClearHistory() throws Exception {
    myIdeMock.expects(once()).method("askQuestion").with(
        eq(StringUtil.getMsg("ClearHistoryCommand.title")),
        eq(StringUtil.getMsg("ClearHistoryCommand.text"))
    ).will(returnValue(true));
    myIdeMock.expects(once()).method("runLongProcess").will(new CustomStub("runner") {
      public Object invoke(Invocation invocation) throws Throwable {
        ((IDEFacade.Process) invocation.parameterValues.get(1)).run(new NullProgressIndicator());
        return null;
      }
    });

    myIdeMock.expects(once()).method("runOnPooledThread").will(new CustomStub("foo"){
      public Object invoke(Invocation invocation) throws Throwable {
        final FutureTask task = new FutureTask((Runnable) invocation.parameterValues.get(0), null);
        task.run();
        return task;  //To change body of implemented methods use File | Settings | File Templates.
      }
    });
    myDispatcherMock.expects(once()).method("clearHistory");

    myCommand.execute();
  }
}
