package com.intellij.coldFusion.UI.facet;

public class CfmlFacetSourceRootsPanel {
}/* extends TableWithCRUDButtons {
  private final CfmlFacetConfiguration myWebFacet;
  private List<WebRoot> myWebRoots;
  private AbstractTableModel myTableModel;
  public static final Icon WEB_ROOT_ICON = IconLoader.getIcon("/modules/webRoot.png");
  private final Set<WebRoot> myRootsWithDuplicatedRelativePath = new HashSet<WebRoot>();
  private final Module myModule;
  private final boolean myHighlightInvalidRoots;

  public CfmlFacetSourceRootsPanel(Module module, CfmlFacetConfiguration webFacetConfiguration, final boolean highlightInvalidRoots) {
    super(module.getProject(), true, true);
    myHighlightInvalidRoots = highlightInvalidRoots;
    myModule = module;
    myTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    myWebFacet = webFacetConfiguration;

    setupControls();
    refreshModel();
    myTable.setModel(myTableModel);
  }

  private void setupControls() {
    myTableModel = new AbstractTableModel() {
      public int getRowCount() {
        return myWebRoots.size();
      }

      public int getColumnCount() {
        return 2;
      }

      public String getColumnName(int column) {
        switch (column) {
          case 0:
            return "Resource Directory";
          case 1:
            return "Path Relative to Deployment Root";
          default:
            return "???";
        }
      }

      public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
          case 0:
            return WebRoot.class;
          case 1:
            return String.class;
          default:
            return null;
        }
      }

      public Object getValueAt(int rowIndex, int columnIndex) {
        final WebRoot webRoot = myWebRoots.get(rowIndex);
        switch (columnIndex) {
          case 0:
            return webRoot;
          case 1:
            return webRoot.getURI();
          default:
            return "???";
        }
      }

      public boolean isCellEditable(int row, int column) {
        return column == 1;
      }

      public void setValueAt(Object value, int row, int column) {
        final WebRoot webRoot = myWebRoots.get(row);
        switch (column) {
          case 0:
            break;
          case 1:
            WebRoot newWebRoot = new WebRoot(webRoot.getDirectoryUrl(), (String)value);
            myWebRoots.set(row, newWebRoot);
            updateHighlighting();
            break;
          default:
        }
      }
    };
    myTable.setDefaultRenderer(WebRoot.class, new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value != null) {
          WebRoot webRoot = (WebRoot)value;
          setIcon(WEB_ROOT_ICON);
          String file = webRoot.getPresentableUrl();
          String text = FileUtil.toSystemDependentName(file);
          setText(text);
          if (myHighlightInvalidRoots && webRoot.getFile() == null) {
            setForeground(Color.red);
          }
          else {
            setForeground(isSelected ? UIUtil.getTableSelectionForeground() : UIUtil.getTableForeground());
          }
        }
        return this;
      }
    });
    myTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        final WebRoot webRoot = myWebRoots.get(row);
        if (myRootsWithDuplicatedRelativePath.contains(webRoot)) {
          setBackground(EditorColorsManager.getInstance().getGlobalScheme().getAttributes(CodeInsightColors.WARNINGS_ATTRIBUTES).getBackgroundColor());
          setToolTipText("Warning: dublicating relative path " + webRoot.getRelativePath());
        }
        else {
          setBackground(isSelected ? UIUtil.getTableSelectionBackground() : UIUtil.getTableBackground());
          setToolTipText(null);
        }
        return this;
      }
    });
    myTable.setRowHeight(WEB_ROOT_ICON.getIconHeight());
  }

  protected void doNew() {
    TableUtil.stopEditing(myTable);
    String directory = JavaeeModuleUtil.getModuleDirectoryPath(myModule);
    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(directory);
    WebRoot webRoot = new WebRoot(file, "/");
    EditWebRootDialog dialog = new EditWebRootDialog(myProject, webRoot, true, myModule, myWebRoots);
    dialog.show();
    if (!dialog.isOK()) return;
    webRoot = dialog.saveWebRoot();
    if (webRoot == null) return;
    if (!myWebRoots.contains(webRoot)) {
      myWebRoots.add(webRoot);
      myTableModel.fireTableDataChanged();
      updateHighlighting();
    }
  }

  protected void doEdit() {
    TableUtil.stopEditing(myTable);
    final List<WebRoot> webRoots = getSelectedWebRoots();
    if (webRoots.size() != 1) return;
    WebRoot webRootToEdit = webRoots.get(0);
    ArrayList<WebRoot> existingWebRoots = new ArrayList<WebRoot>(myWebRoots);
    existingWebRoots.remove(webRootToEdit);
    int index = myWebRoots.indexOf(webRootToEdit);
    EditWebRootDialog dialog = new EditWebRootDialog(myProject, webRootToEdit, true, myModule, existingWebRoots);
    dialog.show();
    if (!dialog.isOK()) return;
    WebRoot webRoot = dialog.saveWebRoot();
    if (webRoot == null) return;
    myTableModel.fireTableDataChanged();
    myWebRoots.set(index, webRoot);
    updateHighlighting();
  }

  protected boolean isRemoveOk() {
    final List<WebRoot> webRoots = getSelectedWebRoots();
    if (webRoots.isEmpty()) return false;
    final String message;
    if (webRoots.size() == 1) {
      final String path = FileUtil.toSystemDependentName(webRoots.get(0).getPresentableUrl());
      message = "Remove resource directory " +  path + "?";
    } else {
      message = "Remove selected resource directory ?";
    }
    return Messages.showYesNoDialog(myProject, message, "Remove resource", Messages.getQuestionIcon()) == 0;
  }

  protected void doRemove() {
    myWebRoots.removeAll(getSelectedWebRoots());
    myTableModel.fireTableDataChanged();
    updateHighlighting();
  }

  private List<WebRoot> getSelectedWebRoots() {
    final int[] rows = myTable.getSelectedRows();
    List<WebRoot> webRoots = new ArrayList<WebRoot>();
    for (int row : rows) {
      final WebRoot webRoot = (WebRoot)myTable.getModel().getValueAt(row, 0);
      webRoots.add(webRoot);
    }
    return webRoots;
  }


  protected void refreshModelImpl() {
    myWebRoots = new ArrayList<WebRoot>();
    if (myWebFacet != null) {
      myWebRoots.addAll(myWebFacet.getWebRoots(false));
    }
    updateHighlighting();
    myTableModel.fireTableDataChanged();
  }

  public void dispose() {
    TableUtil.stopEditing(myTable);
  }

  private void updateHighlighting() {
    Map<String, WebRoot> paths = new HashMap<String, WebRoot>();
    myRootsWithDuplicatedRelativePath.clear();
    for (WebRoot webRoot : myWebRoots) {
      final WebRoot old = paths.put(webRoot.getRelativePath(), webRoot);
      if (old != null) {
        myRootsWithDuplicatedRelativePath.add(webRoot);
        myRootsWithDuplicatedRelativePath.add(old);
      }
    }
  }

  public void createDirectories() {
    if (myWebFacet != null) {
      final List<WebRoot> webRoots = myWebFacet.getWebRoots(false);
      for (WebRoot webRoot : webRoots) {
        if (webRoot.getFile() == null) {
          createDirectory(webRoot.getDirectoryUrl());
        }
      }
    }
  }

  private void createDirectory(final String url) {
    StringUtil.trimEnd(url, "/");
    final int index = url.lastIndexOf('/');
    String parentUrl = url.substring(0, index);
    String directoryName = url.substring(index + 1);
    final VirtualFile parent = VirtualFileManager.getInstance().findFileByUrl(parentUrl);
    if (parent != null) {
      try {
        parent.createChildDirectory(this, directoryName);
      }
      catch (IOException e) {
        //ignore
      }
    }
  }

  protected String getHelpID() {
    return "TODO: help";
  }

  public boolean isModified() {
    return !myWebFacet.getWebRoots(false).equals(myWebRoots);
  }

  public boolean isEditing() {
    return myTable.isEditing();
  }

  public void saveData() {
    TableUtil.stopEditing(myTable);
    if (myWebFacet != null) {
      myWebFacet.setWebRoots(myWebRoots);
    }
  }
}
*/