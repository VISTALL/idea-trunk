/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package com.intellij.struts.dom.converters;

import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.struts.StrutsManager;
import com.intellij.struts.dom.Action;
import com.intellij.struts.dom.Forward;
import com.intellij.struts.dom.StrutsConfig;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Autocompletion suggestions using already existing names for &lt;forward&gt; "name" attribute as well as the path (if set).
 *
 * @author Yann Cebron
 */
public class ForwardNameConverter extends NameConverter<Forward> {

  public ForwardNameConverter() {
    super("Forward");
  }

  protected GenericDomValue<PsiClass> getClassElement(Forward parent) {
    return parent.getClassName();
  }

  protected List<Forward> getSiblings(Forward parent) {
    return null;
  }

  @NotNull
  public Collection<? extends String> getVariants(final ConvertContext convertContext) {

    final Set<String> result = new LinkedHashSet<String>(super.getVariants(convertContext));

    final Forward element = (Forward) convertContext.getInvocationElement().getParent();
    assert element != null;

    // propose already set "path" as forward name
    final PathReference value = element.getPath().getValue();
    if (value != null && StringUtil.isNotEmpty(value.getPath())) {
      final String path = value.getTrimmedPath();
      final String[] strings = path.split("/");
      if (strings.length > 0) {
        String fileName = strings[strings.length - 1];
        fileName = FileUtil.getNameWithoutExtension(fileName);
        fileName = StringUtil.decapitalize(fileName);
        result.add(fileName);
      }
    }

    // propose existing forward names from other actions
    final DomElement parent = element.getParent();
    if (parent instanceof Action) {
      final StrutsConfig config = StrutsManager.getInstance().getContext(element);
      final Set<String> variants = new HashSet<String>();
      final List<Action> actions = config.getActionMappings().getActions();
      for (final Action action : actions) {
        final List<Forward> forwards = action.getForwards();
        for (final Forward forward : forwards) {
          String forwardName = forward.getName().getStringValue();
          if (StringUtil.isNotEmpty(forwardName)) {
            variants.add(forwardName);
          }
        }
      }
      for (Forward forward : ((Action) parent).getForwards()) {
        variants.remove(forward.getName().getStringValue());
      }
      result.addAll(variants);
    }
    return result;
  }

}