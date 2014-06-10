package org.jetbrains.idea.perforce.perforce.jobs;

public class PerforceJobField {
  private int myCode;
  private PerforceJobFieldType myType;
  private String myName;
  private PerforceJobPersistenceType myPersistence;

  public int getCode() {
    return myCode;
  }

  public PerforceJobFieldType getType() {
    return myType;
  }

  public String getName() {
    return myName;
  }

  public PerforceJobPersistenceType getPersistence() {
    return myPersistence;
  }

  public void setCode(int code) {
    myCode = code;
  }

  public void setType(PerforceJobFieldType type) {
    myType = type;
  }

  public void setName(String name) {
    myName = name;
  }

  public void setPersistence(PerforceJobPersistenceType persistence) {
    myPersistence = persistence;
  }

  public boolean filled() {
    return ((myCode > 100) && (myCode < 200)) && myName != null && myPersistence != null && myType != null;
  }
}
