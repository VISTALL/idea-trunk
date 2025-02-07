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

package git4idea.vfs;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EventListener;

/**
 * The listener for configuration change events
 */
public interface GitConfigListener extends EventListener {
  /**
   * This method is invoked when configuration changes for configured git root.
   * If ".git/config" changes, only appropriate root is notified. If "~/.gitconfig" changes,
   * all configured roots are notified.
   * <p/>
   * When root configuration changes, the listeners are called for all new roots.
   *
   * @param gitRoot    the affected git root
   * @param configFile the changed configuration file, the parameter might be null if config file is missing
   */
  void configChanged(@NotNull VirtualFile gitRoot, @Nullable VirtualFile configFile);
}
