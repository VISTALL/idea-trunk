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

import jetbrains.communicator.core.transport.Transport;
import jetbrains.communicator.core.transport.XmlMessage;
import org.jdom.Element;

/**
 * @author Kir
 */
public class MockXmlMessage implements XmlMessage {
  private String myTagName;
  private String myNamespace;
  private boolean myShouldWaitForResponse;

  public MockXmlMessage() {
    this("mockMessage");
  }

  public MockXmlMessage(String tagName) {
    this(tagName, Transport.NAMESPACE);
  }

  public MockXmlMessage(String tagName, String namespace) {
    myTagName = tagName;
    myNamespace = namespace;
  }

  public String getTagName() {
    return myTagName;
  }

  public String getTagNamespace() {
    return myNamespace;
  }

  public void fillRequest(Element element) {
  }

  public boolean needsResponse() {
    return myShouldWaitForResponse;
  }

  public void processResponse(Element responseElement) {
  }

  public void setShouldWaitForResponse(boolean shouldWaitForResponse) {
    myShouldWaitForResponse = shouldWaitForResponse;
  }
}
