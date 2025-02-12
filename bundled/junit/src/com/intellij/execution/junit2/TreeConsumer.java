/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package com.intellij.execution.junit2;

import com.intellij.execution.junit2.segments.ObjectReader;
import com.intellij.execution.junit2.segments.PacketConsumer;
import com.intellij.rt.execution.junit.segments.PoolOfDelimiters;

public abstract class TreeConsumer implements PacketConsumer {
  public void readPacketFrom(final ObjectReader reader) {
    final TestProxy root = readNode(reader);
    onTreeAvailable(root);
  }

  public String getPrefix() {
    return PoolOfDelimiters.TREE_PREFIX;
  }

  public void onFinished() {
  }

  protected abstract void onTreeAvailable(TestProxy treeRoot);

  private static TestProxy readNode(final ObjectReader reader) {
    final TestProxy node = reader.readObject();
    final int childCount = reader.readInt();
    for (int i = 0; i < childCount; i++)
      node.addChild(readNode(reader));
    return node;
  }
}
