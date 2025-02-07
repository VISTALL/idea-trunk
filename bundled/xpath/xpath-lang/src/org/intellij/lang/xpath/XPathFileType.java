/*
 * Copyright 2005 Sascha Weinreuter
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
package org.intellij.lang.xpath;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class XPathFileType extends LanguageFileType {

    public static final XPathFileType XPATH = new XPathFileType();

    private XPathFileType() {
        super(new XPathLanguage());
    }

    @NotNull
    public String getName() {
        return "XPath";
    }

    @NotNull
    public String getDescription() {
        return "XPath";
    }

    @NotNull
    public String getDefaultExtension() {
        return "xpath";
    }

    public Icon getIcon() {
        return IconLoader.findIcon("/icons/xpath.png");
    }
}
