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

package org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.RubyIcons;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubySimpleLookupItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public class HashParamDef extends ParamDef {
    private Map<String, ParamDef> myParams = new HashMap<String, ParamDef>();

    @Nullable
    public List<RubyLookupItem> getVariants(ParamContext context) {
        List<RubyLookupItem> result = new ArrayList<RubyLookupItem>();
        for(String key: myParams.keySet()) {
            result.add(new RubySimpleLookupItem(":" + key, null, 0, true, RubyIcons.RUBY_ICON));
        }
        return result;
    }

    public void add(String optionName, ParamDef paramDef) {
        myParams.put(optionName, paramDef);
    }

    public ParamDef getValueParamDef(String key) {
        if (key.startsWith(":")) {
            return myParams.get(key.substring(1));
        }
        return myParams.get(key);        
    }
}
