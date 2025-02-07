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

package org.jetbrains.plugins.ruby.ruby.presentation;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 06.08.2007
 */
public interface RPresentationConstants {
    /**
     * Show variable, method or class name
     */
    public static final int SHOW_NAME = 0x0001; // variable, method, class

//    public static final int SHOW_TYPE = 0x0002; // variable, method
//    public static final int TYPE_AFTER = 0x0004; // variable, method
//    public static final int SHOW_MODIFIERS = 0x0008; // variable, method, class
//    public static final int MODIFIERS_AFTER = 0x0010; // variable, method, class
//    public static final int SHOW_REDUNDANT_MODIFIERS = 0x0020; // variable, method, class, modifier list
//    public static final int SHOW_PACKAGE_LOCAL = 0x0040; // variable, method, class, modifier list

    /**
     * Show initializer for variables and methow parameters
     */
    public static final int SHOW_INITIALIZER = 0x0080; // variable

    /**
     * Show method parameters
     */
    public static final int SHOW_PARAMETERS = 0x0100;
    /**
     * Show full(but not qualified) name of class, method, module.
     * (e.g. self.foo() instead of foo(), A.foo1() instead of foo1(), A::B instead of B)
     */
    public static final int SHOW_FULL_NAME = 0x0200; // method
//    public static final int SHOW_THROWS = 0x0200; // method
//    public static final int SHOW_EXTENDS_IMPLEMENTS = 0x0400; // class
//    public static final int SHOW_FQ_NAME = 0x0800; // class, field, method
//    public static final int SHOW_CONTAINING_CLASS = 0x1000; // field, method
//    public static final int SHOW_FQ_CLASS_NAMES = 0x2000; // variable, method, class
//    public static final int JAVADOC_MODIFIERS_ONLY = 0x4000; // field, method, class
//    public static final int SHOW_ANONYMOUS_CLASS_VERBOSE = 0x8000; // class
//    public static final int MAX_PARAMS_TO_SHOW = 7;
}
