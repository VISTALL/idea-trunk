package org.jetbrains.idea.perforce.perforce.jobs;

public class PerforceJobFieldValue {
  private final PerforceJobField myField;
  private String myValue;

  public PerforceJobFieldValue(PerforceJobField field, String value) {
    myField = field;
    myValue = value;
  }

  public PerforceJobField getField() {
    return myField;
  }

  public String getValue() {
    return myValue;
  }

  public void setValue(final String value) {
    myValue = value;
  }
}
