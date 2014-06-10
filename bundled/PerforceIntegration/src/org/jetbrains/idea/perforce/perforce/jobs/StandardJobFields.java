package org.jetbrains.idea.perforce.perforce.jobs;

public enum StandardJobFields {
  name (101, 0),
  status (102, 1),
  user (103, 2),
  date (104, 3),
  description (105, 4);

  private final int myFixedCode;
  private final int myRelativeDisplayOrder;

  private StandardJobFields(int fixedCode, int relativeDisplayOrder) {
    myFixedCode = fixedCode;
    myRelativeDisplayOrder = relativeDisplayOrder;
  }

  public static boolean isStandardField(final PerforceJobField field) {
    final StandardJobFields[] allFields = values();
    for (StandardJobFields standardField : allFields) {
      if (standardField.myFixedCode == field.getCode()) {
        return true;
      }
    }
    return false;
  }

  public static int getOrder(final PerforceJobField field) {
    final StandardJobFields[] allFields = values();
    for (StandardJobFields standardField : allFields) {
      if (standardField.myFixedCode == field.getCode()) {
        return standardField.myRelativeDisplayOrder;
      }
    }
    return 1000;
  }

  public int getFixedCode() {
    return myFixedCode;
  }
}
