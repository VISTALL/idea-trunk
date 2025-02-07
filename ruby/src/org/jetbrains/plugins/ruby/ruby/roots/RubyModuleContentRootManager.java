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

package org.jetbrains.plugins.ruby.ruby.roots;

import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: May 16, 2008
 */
public interface RubyModuleContentRootManager {
    void projectClosed();

    void setTestUnitFolderUrls(@NotNull List<String> urls);

    @NotNull
    Set<String> getTestUnitFolderUrls();

    void addContentRootsListener(@NotNull RModuleContentRootsListener l,
                                        @NotNull Disposable parentDisposable);
}
