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

package org.jetbrains.plugins.ruby.ruby.lang.documentation;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import junit.framework.Assert;
import junit.framework.Test;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.PathUtil;
import org.jetbrains.plugins.ruby.ruby.testCases.FileSetTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Dec 11, 2007
 */
public class MarkupUtilTest extends FileSetTestCase {
    @NonNls
    private static final String DATA_PATH = PathUtil.getDataPath(MarkupUtilTest.class);

    @NonNls
    private static final String DATA_DELIMITER = "----";

    public MarkupUtilTest() {
        super(DATA_PATH);
    }

    protected void runTest(File file) throws Throwable {
        String content = new String(FileUtil.loadFileText(file));
        Assert.assertNotNull(content);

        List<String> input = new ArrayList<String>();

        int separatorIndex;

        content = StringUtil.replace(content, "\r", "");

        while ((separatorIndex = content.indexOf(DATA_DELIMITER)) >= 0) {
            input.add(content.substring(0, separatorIndex));
            content = content.substring(separatorIndex);
            while (StringUtil.startsWithChar(content, '-') || StringUtil.startsWithChar(content, ' ')){
                content = content.substring(1);
            }
            // we eat last eol if presents
            if (StringUtil.startsWithChar(content, '\n')){
                content = content.substring(1);
            }

        }

        String result = content;

        Assert.assertTrue("No data found in source file", input.size() > 0);

        final String transformed = MarkupUtil.processText(input.get(0));

        assertEquals(result, transformed);
    }

    protected void assertEquals(String result, String transformed) {
        Assert.assertEquals(result.trim(), transformed.trim().replace("\\", "\\\\"));
    }

    public static Test suite() {
        return new MarkupUtilTest();
    }

}
