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

package com.sixrr.guiceyidea;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

public class GuiceyIDEABundle{
    private static final ResourceBundle ourBundle =
            ResourceBundle.getBundle("com.sixrr.guiceyidea.GuiceyIDEABundle");

    private GuiceyIDEABundle(){
    }

    public static String message(@PropertyKey(resourceBundle = "com.sixrr.guiceyidea.GuiceyIDEABundle")String key,
                                 Object... params){
        return CommonBundle.message(ourBundle, key, params);
    }
}
