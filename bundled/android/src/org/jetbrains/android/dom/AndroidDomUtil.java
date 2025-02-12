/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.android.dom;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.util.containers.HashMap;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.android.dom.attrs.AttributeDefinition;
import org.jetbrains.android.dom.attrs.AttributeFormat;
import org.jetbrains.android.dom.converters.*;
import org.jetbrains.android.dom.layout.LayoutViewElement;
import org.jetbrains.android.dom.manifest.*;
import org.jetbrains.android.dom.menu.Group;
import org.jetbrains.android.dom.menu.Menu;
import org.jetbrains.android.dom.resources.*;
import org.jetbrains.android.dom.xml.PreferenceElement;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 11, 2009
 * Time: 6:28:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidDomUtil {
  public static final StaticEnumConverter BOOLEAN_CONVERTER = new StaticEnumConverter("true", "false");
  public static final Map<String, String> SPECIAL_RESOURCE_TYPES = new HashMap<String, String>();

  static {
    AndroidDomUtil.addSpecialResourceType("string", "label", "description", "title");
    AndroidDomUtil.addSpecialResourceType("drawable", "icon");
    AndroidDomUtil.addSpecialResourceType("style", "theme");
    AndroidDomUtil.addSpecialResourceType("anim", "animation");
    AndroidDomUtil.addSpecialResourceType("id", "id");
  }

  private AndroidDomUtil() {
  }

  @Nullable
  public static String getResourceType(@NotNull AttributeFormat format) {
    switch (format) {
      case Color:
        return "color";
      case Dimension:
        return "dimen";
      case String:
        return "string";
      default:
        return null;
    }
  }

  @Nullable
  public static ResolvingConverter<String> getStringConverter(@NotNull AttributeFormat format, @NotNull String[] values) {
    switch (format) {
      case Enum:
        return new StaticEnumConverter(values);
      case Boolean:
        return BOOLEAN_CONVERTER;
      default:
        return null;
    }
  }

  @Nullable
  public static ResourceReferenceConverter getResourceReferenceConverter(@NotNull AttributeDefinition attr) {
    boolean containsReference = false;
    Set<String> resourceTypes = new HashSet<String>();
    Set<AttributeFormat> formats = attr.getFormats();
    for (AttributeFormat format : formats) {
      if (format == org.jetbrains.android.dom.attrs.AttributeFormat.Reference) {
        containsReference = true;
      }
      String type = getResourceType(format);
      if (type != null) {
        resourceTypes.add(type);
      }
    }
    String specialResourceType = getSpecialResourceType(attr.getName());
    if (specialResourceType != null) {
      resourceTypes.add(specialResourceType);
    }
    if (containsReference) {
      if (resourceTypes.contains("color")) resourceTypes.add("drawable");
      if (resourceTypes.size() == 0) {
        resourceTypes.addAll(org.jetbrains.android.resourceManagers.ResourceManager.REFERABLE_RESOURCE_TYPES);
      }
    }
    if (resourceTypes.size() > 0) {
      return new ResourceReferenceConverter(resourceTypes);
    }
    return null;
  }

  @Nullable
  public static ResolvingConverter<String> simplify(CompositeConverter composite) {
    switch (composite.size()) {
      case 0:
        return null;
      case 1:
        return composite.getConverters().get(0);
      default:
        return composite;
    }
  }

  @Nullable
  public static ResolvingConverter getConverter(@NotNull AttributeDefinition attr) {
    Set<AttributeFormat> formats = attr.getFormats();
    CompositeConverter composite = new CompositeConverter();
    String[] values = attr.getValues();
    for (AttributeFormat format : formats) {
      ResolvingConverter<String> converter = getStringConverter(format, values);
      if (converter != null) {
        composite.addConverter(converter);
      }
    }
    ResourceReferenceConverter resConverter = getResourceReferenceConverter(attr);
    if (formats.contains(org.jetbrains.android.dom.attrs.AttributeFormat.Flag)) {
      if (resConverter != null) {
        composite.addConverter(new LightFlagConverter(values));
      }
      return new FlagConverter(simplify(composite), values);
    }
    ResolvingConverter<String> stringConverter = simplify(composite);
    if (resConverter != null) {
      resConverter.setAdditionalConverter(simplify(composite));
      return resConverter;
    }
    return stringConverter;
  }

  @Nullable
  public static String getSpecialResourceType(String attrName) {
    String type = SPECIAL_RESOURCE_TYPES.get(attrName);
    if (type != null) return type;
    if (attrName.endsWith("Animation")) return "anim";
    return null;
  }

  // for special cases
  static void addSpecialResourceType(String type, String... attrs) {
    for (String attr : attrs) {
      SPECIAL_RESOURCE_TYPES.put(attr, type);
    }
  }

  public static boolean containsAction(@NotNull IntentFilter filter, @NotNull String name) {
    for (Action action : filter.getActions()) {
      if (name.equals(action.getName().getValue())) {
        return true;
      }
    }
    return false;
  }

  public static boolean containsCategory(@NotNull IntentFilter filter, @NotNull String name) {
    for (Category category : filter.getCategories()) {
      if (name.equals(category.getName().getValue())) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public static Activity getActivityDomElementByClass(@NotNull Module module, PsiClass c) {
    AndroidFacet facet = AndroidFacet.getInstance(module);
    if (facet != null) {
      Manifest manifest = facet.getManifest();
      if (manifest != null) {
        Application application = manifest.getApplication();
        if (application != null) {
          for (Activity activity : application.getActivities()) {
            PsiClass activityClass = activity.getActivityClass().getValue();
            if (c.getManager().areElementsEquivalent(c, activityClass)) {
              return activity;
            }
          }
        }
      }
    }
    return null;
  }

  public static String[] getStaticallyDefinedSubtags(@NotNull AndroidDomElement element) {
    if (element instanceof ManifestElement) {
      return AndroidManifestUtils.getStaticallyDefinedSubtags((ManifestElement)element);
    }
    if (element instanceof LayoutViewElement) {
      return new String[]{"include"};
    }
    if (element instanceof Group || element instanceof StringArray || element instanceof Style) {
      return new String[]{"item"};
    }
    if (element instanceof org.jetbrains.android.dom.menu.Item) {
      return new String[]{"menu"};
    }
    if (element instanceof Menu) {
      return new String[]{"item", "group"};
    }
    if (element instanceof Attr) {
      return new String[]{"enum", "flag"};
    }
    if (element instanceof DeclareStyleable) {
      return new String[]{"attr"};
    }
    if (element instanceof Resources) {
      return new String[]{"string", "drawable", "dimen", "color", "style", "string-array", "declare-styleable", "attr", "item",
        "eat-comment"};
    }
    if (element instanceof StyledText) {
      return new String[] {"b", "i", "u"};
    }
    if (element instanceof PreferenceElement) {
      return new String[] {"intent"};
    }
    
    return new String[0];
  }
}
