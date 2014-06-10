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
package jetbrains.communicator.p2p;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.WebServer;

import java.io.IOException;

/**
 * @author Kir Maximov
 */
public class P2PServer {
  private static final Logger LOG = Logger.getLogger(P2PServer.class);
  private WebServer myWebServer;
  private final int myXmlRpcPort;

  public P2PServer(int portToListen, P2PCommand[] p2PServerCommands) throws IOException {
    myXmlRpcPort = portToListen;

    //XmlRpc.setDebug(true);
    try{
      // Trying to avoid dependency on IDEA code here:
      //noinspection UnnecessaryFullyQualifiedName
      final com.intellij.peer.PeerFactory peerFactory = com.intellij.peer.PeerFactory.getInstance();
      myWebServer = peerFactory.createWebServer(myXmlRpcPort, null,
          peerFactory.createRpcServer());
    }
    catch (Exception e) {
      LOG.debug(e.getMessage(), e);
      myWebServer = new WebServer(myXmlRpcPort);
    }

    for (P2PCommand p2PCommand : p2PServerCommands) {
      myWebServer.addHandler(p2PCommand.getXmlRpcId(), p2PCommand);
    }
    myWebServer.start();
  }

  public void shutdown() {
    myWebServer.shutdown();
//    new WaitFor() {
//      protected boolean condition() {
//        return !NetworkUtil.isPortBusy(myXmlRpcPort);
//      }
//    };

    LOG.info("IDEtalk WebServer shut down");
  }

  public void removeHandler(String handlerId) {
    myWebServer.removeHandler(handlerId);
  }
}
