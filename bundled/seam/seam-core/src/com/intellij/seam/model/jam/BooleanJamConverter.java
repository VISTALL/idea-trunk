package com.intellij.seam.model.jam;

import com.intellij.jam.JamConverter;
import com.intellij.jam.JamStringAttributeElement;
import org.jetbrains.annotations.Nullable;

/**
* @author Serega.Vasiliev
*/
public class BooleanJamConverter extends JamConverter<Boolean> {
  @Override
  public Boolean fromString(@Nullable final String s, final JamStringAttributeElement<Boolean> context) {
    return s == null ? null : Boolean.valueOf(s);
  }
}
