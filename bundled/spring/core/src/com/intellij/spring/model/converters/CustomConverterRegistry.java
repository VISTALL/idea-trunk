package com.intellij.spring.model.converters;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.GenericDomValue;
import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CustomConverterRegistry {
  private final Map<Class, Pair<Condition<GenericDomValue>, Converter>> myCustomConverters =
    new THashMap<Class, Pair<Condition<GenericDomValue>, Converter>>();

  public void registryConverter(Class aClass, Pair<Condition<GenericDomValue>, Converter> pair) {
    myCustomConverters.put(aClass, pair);
  }

  @Nullable
  public Converter getCustomConverter(Class aClass, GenericDomValue context) {
    final Pair<Condition<GenericDomValue>, Converter> pair = myCustomConverters.get(aClass);
    if (pair != null && pair.first.value(context)) {
      return pair.second;
    }
    return null;
  }

}
