package org.jetbrains.idea.perforce.perforce.jobs;

import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerforceJob {
  private final P4Connection myConnection;
  private final ConnectionKey myKey;

  private final Map<Integer, PerforceJobFieldValue> myStandardFields;
  private final Map<Integer, PerforceJobFieldValue> myOtherFields;

  public PerforceJob(List<PerforceJobFieldValue> standardFields, List<PerforceJobFieldValue> otherFields, final P4Connection connection,
                     ConnectionKey key) {
    myKey = key;
    myStandardFields = new HashMap<Integer, PerforceJobFieldValue>(standardFields.size(), 1);
    myOtherFields = new HashMap<Integer, PerforceJobFieldValue>(otherFields.size(), 1);
    myConnection = connection;

    for (PerforceJobFieldValue field : standardFields) {
      myStandardFields.put(field.getField().getCode(), field);
    }
    for (PerforceJobFieldValue field : otherFields) {
      myOtherFields.put(field.getField().getCode(), field);
    }
  }

  public Collection<PerforceJobFieldValue> getStandardFields() {
    return myStandardFields.values();
  }

  public Collection<PerforceJobFieldValue> getOtherFields() {
    return myOtherFields.values();
  }

  public PerforceJobFieldValue getValueForStandardField(final int id) {
    return myStandardFields.get(id);
  }

  public String getName() {
    return myStandardFields.get(StandardJobFields.name.getFixedCode()).getValue();
  }

  public PerforceJobFieldValue getNameValue() {
    return myStandardFields.get(StandardJobFields.name.getFixedCode());
  }

  public P4Connection getConnection() {
    return myConnection;
  }

  public ConnectionKey getConnectionKey() {
    return myKey;
  }
}
