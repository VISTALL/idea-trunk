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

package com.sixrr.guiceyidea.utils;

import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.net.URL;

public class IconHelper{
    private IconHelper(){
        super();
    }

    public static ImageIcon getIcon(@NonNls String location){
        final Class<IconHelper> thisClass = IconHelper.class;
        final URL resource = thisClass.getResource(location);
        return new ImageIcon(resource);
    }
}
