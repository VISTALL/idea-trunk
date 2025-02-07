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

package org.jetbrains.plugins.ruby.support.utils;

import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Dec 8, 2007
 */
public class RubyUIUtil {

    /**
     * Returns Html string with current Label Font and in <body/> tag.
     * Method isn't injective-safe.
     * @param text Plain or Html tex
     * @return html
     */
    public static String wrapToHtmlWithLabelFont(@NotNull final String text) {
        final StringBuilder buff = new StringBuilder();
        buff.append("<html><head>");
        buff.append(UIUtil.getCssFontDeclaration(UIUtil.getLabelFont()));
        buff.append("</head><body>");
        buff.append(text);
        buff.append("</body></html>");

        return buff.toString();
    }
}
