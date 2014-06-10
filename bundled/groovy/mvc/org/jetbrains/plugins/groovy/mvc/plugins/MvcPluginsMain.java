package org.jetbrains.plugins.groovy.mvc.plugins;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.ide.ui.search.SearchableOptionsRegistrar;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.util.Alarm;
import com.intellij.util.net.HttpConfigurable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.groovy.mvc.MvcLoadingPanel;
import org.jetbrains.plugins.groovy.mvc.plugins.actions.AddCustomPluginAction;
import org.jetbrains.plugins.groovy.mvc.plugins.actions.DownloadAllMvcPluginInfosAction;
import org.jetbrains.plugins.groovy.mvc.plugins.actions.DownloadSinglePluginInfoAction;
import org.jetbrains.plugins.groovy.mvc.plugins.actions.ReloadMvcPluginListAction;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * User: Dmitry.Krasilschikov
 * Date: 02.09.2008
 */
public class MvcPluginsMain {
  private JPanel main;
  private JLabel myAuthorEmailLabel;
  private JLabel myAuthorLabel;
  private JEditorPane myDescriptionTextArea;
  private JLabel myPluginUrlLabel;
  private JLabel myVersionLabel;
  private JPanel myTablePanel;
  private JPanel myToolbarPanel;
  private JButton myHttpProxySettingsButton;
  private JPanel myLoadingPanelWrapper;
  private JPanel myPanelForTable;
  private final MvcLoadingPanel myLoadingPanel;

  private final AvailablePluginsModel myAvailablePluginsModel;
  private final MvcPluginsTable availablePluginsTable;
  private final ActionToolbar myActionToolbar;

  private final MyPluginsFilter myFilter = new MyPluginsFilter();
  private final Module myModule;
  private final DialogBuilder myDialogBuilder;
  private final MvcPluginManager myManager;

  @NonNls public static final String TEXT_PREFIX = "<html><body style=\"font-family: Arial; font-size: 12pt;\">";
  @NonNls public static final String TEXT_SUFIX = "</body></html>";

  @NonNls private static final String HTML_PREFIX = "<html><body><a href=\"\">";
  @NonNls private static final String HTML_SUFIX = "</a></body></html>";

  private final Map<String, String> myPluginNameToPath = new HashMap<String, String>();

  private final Alarm myAlarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);
  private JCheckBox myOnlineDownloadInfo;

  public MvcPluginsMain(final Module module, final DialogBuilder dialogBuilder, MvcPluginManager manager) {
    myModule = module;
    myDialogBuilder = dialogBuilder;
    myManager = manager;
    myDescriptionTextArea.addHyperlinkListener(new MyHyperlinkListener());

    myLoadingPanelWrapper.setLayout(new BorderLayout());
    myLoadingPanel = new MvcLoadingPanel();
    myLoadingPanelWrapper.add(myLoadingPanel, BorderLayout.PAGE_START);

    main.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        IdeFocusManager.getInstance(module.getProject()).requestFocus(myFilter, true);
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


    final Map<String, MvcPluginDescriptor> installedPlugins = MvcPluginUtil.getInstalledPlugins(module, manager.getFramework());
    myAvailablePluginsModel = new AvailablePluginsModel(myModule, installedPlugins, myManager.getAvailablePlugins());
    availablePluginsTable = new MvcPluginsTable(myAvailablePluginsModel);
    //  Downloads
    JScrollPane availableScrollPane = ScrollPaneFactory.createScrollPane(availablePluginsTable);

    final ActionGroup actionGroup = getActionGroup();
    installTableActions(availablePluginsTable, actionGroup, installedPlugins);

    myHttpProxySettingsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (ShowSettingsUtil.getInstance().editConfigurable(myModule.getProject(), getProxyConfigurable())) {
          setMvcFrameworkProxy();
        }
      }
    });

    main.setPreferredSize(new Dimension(800, 500));
    myTablePanel.setMinimumSize(new Dimension(550, -1));
    myDescriptionTextArea.setPreferredSize(new Dimension(-1, 300));

    myOnlineDownloadInfo = new NonFocusableCheckBox("Download plugin info on the fly");
    myOnlineDownloadInfo.setToolTipText("Get plugin info immediately when plugin is selected");
    myOnlineDownloadInfo.setSelected(true);

    myPanelForTable.setLayout(new BorderLayout());
    myPanelForTable.setBorder(IdeBorderFactory.createTitledBorder(myManager.getFramework().getDisplayName() + " plugins"));

    myPanelForTable.add(availableScrollPane, BorderLayout.BEFORE_FIRST_LINE);

    myToolbarPanel.setLayout(new BorderLayout());

    myActionToolbar = ActionManager.getInstance().createActionToolbar("PluginManager", actionGroup, true);
    myToolbarPanel.add(myActionToolbar.getComponent(), BorderLayout.WEST);
    myToolbarPanel.add(myOnlineDownloadInfo, BorderLayout.CENTER);

    myToolbarPanel.add(myFilter, BorderLayout.EAST);

    myActionToolbar.updateActionsImmediately();

    myOnlineDownloadInfo.setMnemonic('D');

    final MvcPluginsTable pluginTable = getPluginTable();
    TableUtil.ensureSelectionExists(pluginTable);
    myActionToolbar.updateActionsImmediately();
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        final String filter = myFilter.getFilter();
        if (!"".equals(filter) && filter != null) {
          myFilter.filter();
        }
      }
    });
    dialogBuilder.setPreferedFocusComponent(availablePluginsTable);

    GuiUtils.replaceJSplitPaneWithIDEASplitter(main);

    setMvcFrameworkProxy();
    tableSelectionChanged(pluginTable, myManager, installedPlugins);
  }

  private static HttpConfigurable getProxyConfigurable() {
    return HttpConfigurable.getInstance();
  }

  public MvcPluginManager getManager() {
    return myManager;
  }

  private void setMvcFrameworkProxy() {
    HttpConfigurable configurable = getProxyConfigurable();
    MvcPluginUtil.setFrameworkProxy(configurable.USE_HTTP_PROXY, configurable.PROXY_HOST, configurable.PROXY_PORT, configurable.PROXY_LOGIN, configurable.getPlainProxyPassword(), myModule,
                                         myManager.getFramework());
  }

  private ActionGroup getActionGroup() {
    DefaultActionGroup group = new DefaultActionGroup();
    group.add(new AddCustomPluginAction(this));
    group.add(new ReloadMvcPluginListAction(this));
    group.add(new DownloadAllMvcPluginInfosAction(this));
    group.add(new DownloadSinglePluginInfoAction(this));
    return group;
  }

  public JComponent getMainPanel() {
    return main;
  }

  public void putCustomPluginToPath(final String name, final String path) {
    myPluginNameToPath.put(name, path);
  }

  public Map<String, String> getPluginNameToPath() {
    return myPluginNameToPath;
  }

  public void getSinglePluginInfo(final String pluginName) {
    final MvcPluginDescriptor loadingPlugin = new MvcPluginDescriptor();
    loadingPlugin.setDescription("Loading...");
    pluginInfoUpdate(loadingPlugin);
    myManager.parseSinglePluginInfo(pluginName).addProcessListener(new ProcessAdapter() {
      @Override
      public void startNotified(final ProcessEvent event) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            myLoadingPanel.getRefreshIcon().resume();
          }
        }, ModalityState.stateForComponent(myLoadingPanel));
      }

      @Override
      public void processTerminated(final ProcessEvent event) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            myLoadingPanel.getRefreshIcon().suspend();
            final MvcPluginDescriptor pluginInfo = myManager.getFullPluginInfo(pluginName);
            if (pluginInfo != null) {
              pluginInfoUpdate(pluginInfo);
            }
          }
        }, ModalityState.stateForComponent(myLoadingPanel));
      }

    });
  }

  private static class MyHyperlinkListener implements HyperlinkListener {
    public void hyperlinkUpdate(HyperlinkEvent e) {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        JEditorPane pane = (JEditorPane)e.getSource();
        if (e instanceof HTMLFrameHyperlinkEvent) {
          HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
          HTMLDocument doc = (HTMLDocument)pane.getDocument();
          doc.processHTMLFrameHyperlinkEvent(evt);
        }
        else {
          URL url = e.getURL();
          if (url != null) BrowserUtil.launchBrowser(url.toString());
        }
      }
    }
  }

  public class MyPluginsFilter extends FilterComponent {
    private final List<MvcPluginDescriptor> myFilteredAvailable = new ArrayList<MvcPluginDescriptor>();

    public MyPluginsFilter() {
      super("PLUGIN_FILTER", 5);
    }

    public void filter() {
      filter(myAvailablePluginsModel, myFilteredAvailable);
    }

    private void filter(AvailablePluginsModel model, final List<MvcPluginDescriptor> filtered) {
      final String filter = getFilter().toLowerCase();
      final SearchableOptionsRegistrar optionsRegistrar = SearchableOptionsRegistrar.getInstance();
      final Set<String> search = optionsRegistrar.getProcessedWords(filter);
      final ArrayList<MvcPluginDescriptor> current = new ArrayList<MvcPluginDescriptor>();
      final List<MvcPluginDescriptor> view = model.getView();
      final LinkedHashSet<MvcPluginDescriptor> toBeProcessed = new LinkedHashSet<MvcPluginDescriptor>(view);
      toBeProcessed.addAll(filtered);
      filtered.clear();
      for (MvcPluginDescriptor mvcPluginDescriptor : toBeProcessed) {
        if (mvcPluginDescriptor.getName().toLowerCase().indexOf(filter) != -1) {
          current.add(mvcPluginDescriptor);
          continue;
        }
        if (isAccepted(search, current, mvcPluginDescriptor, mvcPluginDescriptor.getName())) {
          continue;
        }
        else {
          final String description = mvcPluginDescriptor.getTitle();
          if (description != null && isAccepted(search, current, mvcPluginDescriptor, description)) {
            continue;
          }
        }
        filtered.add(mvcPluginDescriptor);
      }
      model.clearData();
      model.addData(current);
    }

    private boolean isAccepted(final Set<String> search,
                               final ArrayList<MvcPluginDescriptor> current,
                               final MvcPluginDescriptor descriptor,
                               final String description) {
      final SearchableOptionsRegistrar optionsRegistrar = SearchableOptionsRegistrar.getInstance();
      final HashSet<String> descriptionSet = new HashSet<String>(search);
      descriptionSet.removeAll(optionsRegistrar.getProcessedWords(description));
      if (descriptionSet.isEmpty()) {
        current.add(descriptor);
        return true;
      }
      return false;
    }

    public void dispose() {
      super.dispose();
      myFilteredAvailable.clear();
    }
  }

  public MyPluginsFilter getFilter() {
    return myFilter;
  }

  private void installTableActions(final MvcPluginsTable pluginTable, final ActionGroup actionGroup,
                                  final Map<String, MvcPluginDescriptor> installedPlugins) {
    myAvailablePluginsModel.addTableModelListener(new TableModelListener() {
      public void tableChanged(final TableModelEvent e) {
        if (TableModelEvent.UPDATE == e.getType()) {
          final MvcPluginIsInstalledColumnInfo pluginIsInstalledColumnInfo =
                (MvcPluginIsInstalledColumnInfo)myAvailablePluginsModel.getColumnInfos()[AvailablePluginsModel.COLUMN_IS_INSTALLED];

          myDialogBuilder.setOkActionEnabled(pluginIsInstalledColumnInfo.getToInstallPlugins().size() > 0 ||
                                             pluginIsInstalledColumnInfo.getToRemovePlugins().size() > 0);
        }
      }
    });

    pluginTable.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final int[] selectedObjects = pluginTable.getSelectedRows();

        for (int selectedRowNum : selectedObjects) {
          final Object boolVal = myAvailablePluginsModel.getValueAt(selectedRowNum, AvailablePluginsModel.COLUMN_IS_INSTALLED);
          assert boolVal instanceof Boolean;

          myAvailablePluginsModel.setValueAt(!((Boolean)boolVal).booleanValue(), selectedRowNum, AvailablePluginsModel.COLUMN_IS_INSTALLED);
          pluginTable.getSelectionModel().setSelectionInterval(selectedRowNum, selectedRowNum);
        }
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);

    pluginTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        tableSelectionChanged(pluginTable, myManager, installedPlugins);
      }
    });

    PopupHandler.installUnknownPopupHandler(pluginTable, actionGroup, ActionManager.getInstance());

    myAuthorEmailLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    myAuthorEmailLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        MvcPluginDescriptor pluginDescriptor = getPluginTable().getSelectedObject();
        if (pluginDescriptor != null) {
          //noinspection HardCodedStringLiteral
          launchBrowserAction(pluginDescriptor.getEmail(), "mailto:");
        }
      }
    });

    myPluginUrlLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    myPluginUrlLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.isConsumed()) return;
        MvcPluginDescriptor pluginDescriptor = getPluginTable().getSelectedObject();
        if (pluginDescriptor != null) {
          launchBrowserAction(pluginDescriptor.getUrl(), "");
          e.consume();
        }
      }
    });

    new MySpeedSearchBar(pluginTable);
  }

  private void tableSelectionChanged(MvcPluginsTable pluginTable, final MvcPluginManager manager,
                                     final Map<String, MvcPluginDescriptor> installedPlugins) {
    final MvcPluginDescriptor[] plugins = pluginTable.getSelectedObjects();
    MvcPluginDescriptor mvcPluginDescriptor = plugins != null && plugins.length == 1 ? plugins[0] : null;

    if (mvcPluginDescriptor != null) {
      final String pluginName = mvcPluginDescriptor.getName();

      final MvcPluginDescriptor installedPlugin = installedPlugins.get(pluginName);
      if (installedPlugin != null) {
        pluginInfoUpdate(installedPlugin);
        return;
      }

      final Map<String, String> pluginNameToPath = getPluginNameToPath();
      final String path = pluginNameToPath.get(pluginName);
      if (path != null) {
        final MvcPluginDescriptor plugin = manager.extractPluginInfo(path);
        pluginInfoUpdate(plugin);
        return;
      }

      Map<String, MvcPluginDescriptor> fullPluginsInfoMap = manager.getFullPluginsInfo();
      if (fullPluginsInfoMap != null) {
        MvcPluginDescriptor descriptor = fullPluginsInfoMap.get(pluginName);
        if (descriptor != null) {
          pluginInfoUpdate(descriptor);
          return;
        }
      }

      if (!myOnlineDownloadInfo.isSelected()) {
        pluginInfoUpdate(null);
        return;
      }

      myAlarm.cancelAllRequests();
      myAlarm.addRequest(new Runnable() {
        public void run() {
          getSinglePluginInfo(pluginName);
        }
      }, 300);
    }
    else {
      pluginInfoUpdate(null);
    }

    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        myActionToolbar.updateActionsImmediately();
      }
    });
  }

  public MvcPluginsTable getPluginTable() {
    return availablePluginsTable;
  }

  private static void launchBrowserAction(String cmd, String prefix) {
    if (cmd != null && cmd.trim().length() > 0) {
      try {
        BrowserUtil.launchBrowser(prefix + cmd.trim());
      }
      catch (IllegalThreadStateException ex) {
        /* not a problem */
      }
    }
  }

  public Module getModule() {
    return myModule;
  }

  public void pluginInfoUpdate(MvcPluginDescriptor plugin) {
    if (plugin != null) {

      myAuthorLabel.setText(plugin.getAuthor());
      final String fullDescription = plugin.getDescription();
      setTextValue(fullDescription != null ? SearchUtil.markup(fullDescription, myFilter.getFilter()) : null, myDescriptionTextArea);
      setHtmlValue(plugin.getEmail(), myAuthorEmailLabel);
      setHtmlValue(plugin.getUrl(), myPluginUrlLabel);
      setTextValue(plugin.getRelease(), myVersionLabel);

    }
    else {
      myAuthorLabel.setText("");
      setTextValue(null, myDescriptionTextArea);
      myAuthorEmailLabel.setText("");
      myPluginUrlLabel.setText("");
      myVersionLabel.setText("");
    }
  }

  private static void setTextValue(String val, JEditorPane pane) {
    if (val != null) {
      pane.setText(TEXT_PREFIX + val.trim() + TEXT_SUFIX);
      pane.setCaretPosition(0);
    }
    else {
      pane.setText(TEXT_PREFIX + TEXT_SUFIX);
    }
  }

  private static void setTextValue(String val, JLabel label) {
    label.setText((val != null) ? val : "");
  }

  private static void setHtmlValue(final String val, JLabel label) {
    boolean isValid = (val != null && val.trim().length() > 0);
    String setVal = isValid ? HTML_PREFIX + val.trim() + HTML_SUFIX : "(not specified)";

    label.setText(setVal);
    label.setCursor(isValid ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  public DialogBuilder getDialogBuilder() {
    return myDialogBuilder;
  }

  private static class MySpeedSearchBar extends SpeedSearchBase<MvcPluginsTable> {
    public MySpeedSearchBar(MvcPluginsTable pluginsTable) {
      super(pluginsTable);
    }

    public int getSelectedIndex() {
      return myComponent.getSelectedRow();
    }

    public Object[] getAllElements() {
      return myComponent.getElements();
    }

    public String getElementText(Object element) {
      return ((MvcPluginDescriptor)element).getName();
    }

    public void selectElement(Object element, String selectedText) {
      for (int i = 0; i < myComponent.getRowCount(); i++) {
        if (myComponent.getPluginAt(i).getName().equals(((MvcPluginDescriptor)element).getName())) {
          myComponent.setRowSelectionInterval(i, i);
          TableUtil.scrollSelectionToVisible(myComponent);
          break;
        }
      }
    }
  }
}
