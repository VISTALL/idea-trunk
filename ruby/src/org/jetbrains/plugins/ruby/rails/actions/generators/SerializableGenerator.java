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

package org.jetbrains.plugins.ruby.rails.actions.generators;

import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 25.03.2007
 */
public class SerializableGenerator implements Serializable {
    public boolean group;
    public String name;
    public ArrayList<SerializableGenerator> children = new ArrayList<SerializableGenerator>();
    @Transient
    private SerializableGenerator parent;

    public SerializableGenerator() {
    }

    public SerializableGenerator(final String name, final boolean group,
                                 @Nullable final SerializableGenerator parent) {
        this.name = name;
        this.group = group;
        this.parent = parent;
    }

    public void addChild(final SerializableGenerator child) {
        children.add(child);
    }

    public boolean isGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public List<SerializableGenerator> getChildren() {
        return children;
    }

    public void setParent(final SerializableGenerator parent) {
        this.parent = parent;
    }

    public void removeFromParent() {
        if (parent != null) {
            parent.children.remove(this);
        }
    }
}
