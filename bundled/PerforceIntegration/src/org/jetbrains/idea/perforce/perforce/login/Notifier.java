package org.jetbrains.idea.perforce.perforce.login;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

public interface Notifier {
  void ensureNotify(final P4Connection connection);
  void removeLazyNotification(final P4Connection connection);
  @Nullable
  String requestForPassword(final String message, final String rootDir);
  void showPasswordWasOk(final boolean value);
}
