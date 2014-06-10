package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

public class ConnectionKey {
  public String server;
  public String client;
  public String user;

  public ConnectionKey(PerforceClient client) {
    this(client.getServerPort(), client.getName(), client.getUserName());
  }

  public ConnectionKey(final String server, final String client, final String user) {
    this.server = server;
    this.client = client;
    this.user = user;
  }

  @Nullable
  public static ConnectionKey create(final Project project, final P4Connection connection) {
    final PerforceManager perforceManager = PerforceManager.getInstance(project);
    return create(perforceManager, connection);
  }

  @Nullable
  public static ConnectionKey create(final PerforceManager manager, final P4Connection connection) {
    final PerforceClient client = manager.getClient(connection);
    if (! isValidClient(client)) return null;
    return new ConnectionKey(client.getServerPort(), client.getName(), client.getUserName());
  }
  
  private static boolean isValidClient(final PerforceClient perforceClient) {
    return perforceClient.getName() != null && perforceClient.getUserName() != null && perforceClient.getServerPort() != null;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final ConnectionKey that = (ConnectionKey)o;

    if (!client.equals(that.client)) return false;
    if (!server.equals(that.server)) return false;
    if (!user.equals(that.user)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = server.hashCode();
    result = 31 * result + client.hashCode();
    result = 31 * result + user.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return server + ", " + user + "@" + client;
  }
}
