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

package org.jetbrains.plugins.ruby.ruby.codeInsight.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.REmptyType;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * @author: oleg
 * @date: Apr 13, 2008
 */
public interface RType {
    public static final RType NOT_TYPED = REmptyType.INSTANCE;

    boolean isTyped();

    @Nullable
    String getName();

    RType addMessage(@NotNull Message message);

    @NotNull
    Collection<Message> getMessages();

    Collection<Message> getMessagesForName(@Nullable String name);

    /**
     * @param message Patern message
     * @return true, if this can be matched by patterMessage
     */
    public boolean matchesMessage(@NotNull final Message message);

}
