package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangesUtil;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.util.Consumer;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionSelector {
  private final LocalChangeList myList;
  private final PerforceConnectionManager myConnManager;
  private final PerforceSettings myConnSettings;
  private final PerforceManager myPerforceManager;

  public ConnectionSelector(final Project project, final LocalChangeList list) {
    myList = list;
    myPerforceManager = PerforceManager.getInstance(project);
    myConnManager = PerforceConnectionManager.getInstance(project);
    myConnSettings = PerforceSettings.getSettings(project);
  }

  // simplify???
  public Map<ConnectionKey, P4Connection> getConnections() {
    final Map<ConnectionKey, P4Connection> result = new HashMap<ConnectionKey, P4Connection>();

    final Collection<Change> changeCollection = myList.getChanges();
    final List<File> files = ChangesUtil.getIoFilesFromChanges(changeCollection);

    final List<P4Connection> connectionList = myConnManager.getAllConnections(myConnSettings);
    for (P4Connection connection : connectionList) {
      final PerforceClient client = myPerforceManager.getClient(connection);
      if (! isValidClient(client)) continue;
      final ConnectionKey key = new ConnectionKey(client.getServerPort(), client.getName(), client.getUserName());

      for (File file : files) {
        if (connection.handlesFile(file)) {
          result.put(key, connection);
          break;
        }
      }
    }

    return result;
  }

  private boolean isValidClient(final PerforceClient perforceClient) {
    return perforceClient.getName() != null && perforceClient.getUserName() != null && perforceClient.getServerPort() != null;
  }

  public static void selectConnection(final Map<ConnectionKey, P4Connection> map, final Consumer<ConnectionKey> consumer) {
    final MyPopup popup = new MyPopup(map.keySet().toArray(new ConnectionKey[map.size()]), consumer);
    JBPopupFactory.getInstance().createListPopup(popup).showInFocusCenter();
  }

  private static class MyPopup extends BaseListPopupStep<ConnectionKey> {
    private final Consumer<ConnectionKey> myConsumer;

    private MyPopup(final ConnectionKey[] aValues, Consumer<ConnectionKey> consumer) {
      super("Please select a connection", aValues);
      myConsumer = consumer;
    }

    @Override
    public PopupStep onChosen(ConnectionKey selectedValue, boolean finalChoice) {
      myConsumer.consume(selectedValue);
      return FINAL_CHOICE;
    }
  }
}
