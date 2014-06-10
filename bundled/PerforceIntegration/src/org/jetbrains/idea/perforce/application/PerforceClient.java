package org.jetbrains.idea.perforce.application;

import org.jetbrains.idea.perforce.perforce.View;

import java.util.List;

public interface PerforceClient {
  String getName();

  String getRoot();

  List<View> getViews();

  String getUserName();

  String getServerPort();
}
