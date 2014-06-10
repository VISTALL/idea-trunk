package com.intellij.spring.model.xml;

import com.intellij.openapi.util.Comparing;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public interface QualifierAttribute {

  @Nullable
  String getAttributeKey();

  @Nullable
  String getAttributeValue();

  TObjectHashingStrategy<QualifierAttribute> HASHING_STRATEGY = new TObjectHashingStrategy<QualifierAttribute>() {

    public int computeHashCode(final QualifierAttribute object) {
      final String key = object.getAttributeKey();
      final String value = object.getAttributeValue();
      return (key == null ? 0 : key.hashCode()) + (value == null ? 0 : value.hashCode());
    }

    public boolean equals(final QualifierAttribute o1, final QualifierAttribute o2) {
      return Comparing.equal(o1.getAttributeKey(), o2.getAttributeKey()) && Comparing.equal(o1.getAttributeValue(), o2.getAttributeValue());
    }
  };
}
