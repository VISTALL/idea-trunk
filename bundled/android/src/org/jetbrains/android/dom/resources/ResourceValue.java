package org.jetbrains.android.dom.resources;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class ResourceValue {
  private String myValue;
  private char myPrefix;
  private String myPackage;
  private String myResourceType;
  private String myResourceName;

  private ResourceValue() {
  }

  public char getPrefix() {
    return myPrefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ResourceValue that = (ResourceValue)o;

    if (myPrefix != that.myPrefix) return false;
    if (myPackage != null ? !myPackage.equals(that.myPackage) : that.myPackage != null) return false;
    if (myResourceName != null ? !myResourceName.equals(that.myResourceName) : that.myResourceName != null) return false;
    if (myResourceType != null ? !myResourceType.equals(that.myResourceType) : that.myResourceType != null) return false;
    if (myValue != null ? !myValue.equals(that.myValue) : that.myValue != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myValue != null ? myValue.hashCode() : 0;
    result = 31 * result + (int)myPrefix;
    result = 31 * result + (myPackage != null ? myPackage.hashCode() : 0);
    result = 31 * result + (myResourceType != null ? myResourceType.hashCode() : 0);
    result = 31 * result + (myResourceName != null ? myResourceName.hashCode() : 0);
    return result;
  }

  @Nullable
  public static ResourceValue parse(String s, boolean withLiterals) {
    if (s == null) {
      return null;
    }
    if (s.startsWith("@")) {
      return reference(s);
    }
    return withLiterals ? literal(s) : null;
  }

  public static ResourceValue literal(String value) {
    ResourceValue result = new ResourceValue();
    result.myValue = value;
    return result;
  }

  public static ResourceValue reference(String value) {
    ResourceValue result = new ResourceValue();
    assert value.length() > 0;
    result.myPrefix = value.charAt(0);
    int pos = value.indexOf('/');
    if (pos > 0) {
      final String resType = value.substring(1, pos);
      int colonIndex = resType.indexOf(':');
      if (colonIndex > 0) {
        result.myPackage = resType.substring(0, colonIndex);
        result.myResourceType = resType.substring(colonIndex + 1);
      }
      else {
        result.myResourceType = resType;
      }
      result.myResourceName = value.substring(pos + 1);
    }
    else {
      result.myResourceName = value.substring(1);
    }
    return result;
  }

  public static ResourceValue referenceTo(char prefix, @Nullable String resPackage, String resourceType, String resourceName) {
    ResourceValue result = new ResourceValue();
    result.myPrefix = prefix;
    result.myPackage = resPackage;
    result.myResourceType = resourceType;
    result.myResourceName = resourceName;
    return result;
  }

  public boolean isReference() {
    return myValue == null;
  }

  @Nullable
  public String getValue() {
    return myValue;
  }

  @Nullable
  public String getResourceType() {
    return myResourceType;
  }

  @Nullable
  public String getResourceName() {
    return myResourceName;
  }

  @Nullable
  public String getPackage() {
    return myPackage;
  }

  @NotNull
  public String toString() {
    if (myValue != null) {
      return myValue;
    }
    final StringBuilder builder = new StringBuilder().append(myPrefix);
    if (myPackage != null) {
      builder.append(myPackage).append(":");
    }
    builder.append(myResourceType).append("/").append(myResourceName);
    return builder.toString();
  }
}
