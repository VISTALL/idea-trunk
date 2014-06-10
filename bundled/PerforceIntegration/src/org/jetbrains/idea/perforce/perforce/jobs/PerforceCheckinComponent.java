package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.checkin.CheckinChangeListSpecificComponent;
import com.intellij.ui.table.TableView;
import com.intellij.util.Consumer;
import com.intellij.util.Icons;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class PerforceCheckinComponent implements CheckinChangeListSpecificComponent, JobsTablePresentation {
  private final Project myProject;
  private JPanel myPanel;

  private LocalChangeList myCurrent;
  private final Map<LocalChangeList, Map<ConnectionKey, List<PerforceJob>>> myCache;
  private final PerforceVcs myVcs;
  private final Map<ConnectionKey, P4JobsLogicConn> myConnMap;
  private final AdderRemover myAdderRemover;

  private final JobsMasterDetails myDetails;

  public PerforceCheckinComponent(final Project project) {
    myProject = project;
    myVcs = PerforceVcs.getInstance(myProject);
    myConnMap = new HashMap<ConnectionKey, P4JobsLogicConn>();
    myCache = new HashMap<LocalChangeList, Map<ConnectionKey, List<PerforceJob>>>();

    myDetails = new JobsMasterDetails(myProject) {
      @Override
      protected Dimension getPanelPrefferedSize() {
        return new Dimension(200, 70);
      }
    };
    myDetails.onlyMain();

    initUI();
    myAdderRemover = new WiseAdderRemover(myProject, this);
  }

  public void refreshJobs(PerforceJob job) throws VcsException {
    if (! myCurrent.hasDefaultName()) {
      final Map<ConnectionKey, List<PerforceJob>> data = loadOnSelect(myCurrent);
      myCache.put(myCurrent, data);
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          setItems(flattenList(data.values()));
        }
      });
    }
  }

  public void addJob(PerforceJob job) {
    // todo todo 2
    final Map<ConnectionKey, List<PerforceJob>> data = getCurrentListJobs();
    if (data != null) {
      List<PerforceJob> jobs = data.get(job.getConnectionKey());
      if (jobs == null) {
        jobs = new ArrayList<PerforceJob>();
        data.put(job.getConnectionKey(), jobs);
      }
      jobs.add(job);
      saveJobsInCache(data);
      setItems(flattenList(data.values()));
    }
  }

  private Map<ConnectionKey, List<PerforceJob>> getCurrentListJobs() {
    final Map<ConnectionKey, List<PerforceJob>> data;
    if (myCurrent.hasDefaultName()) {
      data = myVcs.getDefaultAssociated();
    } else {
      data = myCache.get(myCurrent);
    }
    return data;
  }

  public void removeSelectedJob() {
    final PerforceJob job = myDetails.getSelectedJob();
    if (job == null) return;
    myDetails.removeSelectedJob();

    final Map<ConnectionKey, List<PerforceJob>> data = getCurrentListJobs();
    if (data != null) {
      data.get(job.getConnectionKey()).remove(job);
      setItems(flattenList(data.values()));
    }
  }

  public void selectDefault() {
    // todo
  }

  private JComponent createToolbar() {
    final DefaultActionGroup group = new DefaultActionGroup();
    group.add(new AnAction("Unlink selected job", "Unlink selected job", Icons.DELETE_ICON) {
      public void actionPerformed(AnActionEvent e) {
        final PerforceJob job = myDetails.getSelectedJob();
        if (job != null) {
          final VcsException vcsException = myAdderRemover.remove(job, myCurrent, myProject);
          if (vcsException != null) {
            new ErrorReporter("removing job from changelist").report(myProject, vcsException);
          }
        }
      }

      @Override
      public void update(AnActionEvent e) {
        final PerforceJob job = myDetails.getSelectedJob();
        e.getPresentation().setEnabled(job != null);
      }
    });

    group.add(new AnAction("Edit Associated Jobs", "Edit Associated Jobs", IconLoader.getIcon("/actions/editSource.png")) {
      @Override
      public void actionPerformed(AnActionEvent e) {
        final Map<ConnectionKey, List<PerforceJob>> data = getCurrentListJobs();
        if (data == null) {
          return;
        }
        ensureDefaultConnections();

        final EditChangelistJobsDialog dialog =
          new EditChangelistJobsDialog(myProject, myCurrent, myCurrent.hasDefaultName(), myConnMap, data);
        dialog.show();
        final Map<ConnectionKey, List<PerforceJob>> jobs = dialog.getJobs();
        saveJobsInCache(jobs);
        setItems(flattenList(jobs.values()));
      }
    });
    group.add(new MyTextFieldAction("Search in Job View", "Search in Job View", IconLoader.getIcon("/actions/include.png")));

    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true);
    return actionToolbar.getComponent();
  }

  private class MyTextFieldAction extends TextFieldAction {
    private MyTextFieldAction(String text, String description, Icon icon) {
      super(text, description, icon);

      myField.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            e.consume();
            searchByJobviewAndFilter();
          }
        }
      });
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      searchByJobviewAndFilter();
    }

    private void searchByJobviewAndFilter() {
      final String text = myField.getText().trim();
      if (text.length() > 0) {
        ensureDefaultConnections();

        if (myCurrent != null) {
          final ConnectionSelector selector = new ConnectionSelector(myProject, myCurrent);
          final Map<ConnectionKey, P4Connection> p4ConnectionMap = selector.getConnections();
          if (p4ConnectionMap.isEmpty()) return;
          if (p4ConnectionMap.size() == 1) {
            final ConnectionKey key = p4ConnectionMap.keySet().iterator().next();
            final Pair<ConnectionKey,P4Connection> pair = new Pair<ConnectionKey, P4Connection>(key, p4ConnectionMap.get(key));
            addImpl(text, pair);
          } else {
            ConnectionSelector.selectConnection(p4ConnectionMap, new Consumer<ConnectionKey>() {
              public void consume(ConnectionKey connectionKey) {
                if (connectionKey != null) {
                  addImpl(text, new Pair<ConnectionKey, P4Connection>(connectionKey, p4ConnectionMap.get(connectionKey)));
                }
              }
            });
          }
        }
      }
    }

    private void addImpl(String text, Pair<ConnectionKey, P4Connection> pair) {
      if (pair != null) {
        final P4JobsLogicConn connMap = myConnMap.get(pair.getFirst());
        if (connMap != null) {
          final JobsWorker worker = new JobsWorker(myProject);
          final List<PerforceJob> jobs;
          final JobViewSearchSpecificator searchSpecificator = new JobViewSearchSpecificator(connMap.getJobView(), text);
          try {
            jobs = worker.getJobs(connMap.getSpec(), searchSpecificator, pair.getSecond(), pair.getFirst());
          } catch (VcsException e1) {
            new ErrorReporter("searching job to add").report(myProject, e1);
            return;
          }
          if (jobs.isEmpty()) {
            // todo title???
            Messages.showMessageDialog(myProject, "There are no jobs matching pattern", "Add Perforce Job", Messages.getInformationIcon());
            return;
          }

          final Consumer<PerforceJob> consumer = new Consumer<PerforceJob>() {
            public void consume(final PerforceJob perforceJob) {
              myAdderRemover.add(perforceJob, myCurrent, myProject);
              myField.setText("");
            }
          };

          if (jobs.size() > 1) {
            showListPopup(jobs, myField, consumer, connMap.getJobView(), searchSpecificator.getMaxCount());
          } else {
            consumer.consume(jobs.get(0));
          }
        }
      }
    }
  }

  private void initUI() {
    myPanel = new JPanel(new GridBagLayout());

    final GridBagConstraints gb = DefaultGb.create();
    gb.insets = new Insets(0,0,0,0);
    gb.anchor = GridBagConstraints.WEST;

    // todo prompt message when jobs ought to exist for CL
    final JLabel jobsPrompt = new JLabel("Jobs:");
    gb.gridwidth = 3;
    myPanel.add(jobsPrompt, gb);

    ++ gb.gridy;
    gb.fill = GridBagConstraints.HORIZONTAL;
    myPanel.add(createToolbar(), gb);

    ++ gb.gridy;
    gb.fill = GridBagConstraints.BOTH;
    final JComponent comp = myDetails.createComponent();
    myPanel.add(comp, gb);
  }

  private void saveJobsInCache(Map<ConnectionKey, List<PerforceJob>> jobs) {
    if (myCurrent.hasDefaultName()) {
      myVcs.setDefaultAssociated(jobs);
    } else {
      myCache.put(myCurrent, jobs);
    }
  }

  private final static ColumnInfo<PerforceJob, String> JOB = new ColumnInfo<PerforceJob, String>("Job") {
    @Override
    public String valueOf(PerforceJob perforceJob) {
      return perforceJob.getName();
    }
  };
  private final static ColumnInfo<PerforceJob, String> STATUS = new ColumnInfo<PerforceJob, String>("Status") {
    @Override
    public String valueOf(PerforceJob perforceJob) {
      return perforceJob.getValueForStandardField(StandardJobFields.status.getFixedCode()).getValue();
    }
  };
  private final static ColumnInfo<PerforceJob, String> USER = new ColumnInfo<PerforceJob, String>("User") {
    @Override
    public String valueOf(PerforceJob perforceJob) {
      return perforceJob.getValueForStandardField(StandardJobFields.user.getFixedCode()).getValue();
    }
  };
  private final static ColumnInfo<PerforceJob, String> DATE = new ColumnInfo<PerforceJob, String>("Date") {
    @Override
    public String valueOf(PerforceJob perforceJob) {
      return perforceJob.getValueForStandardField(StandardJobFields.date.getFixedCode()).getValue();
    }
  };
  private final static ColumnInfo<PerforceJob, String> DESCRIPTION = new ColumnInfo<PerforceJob, String>("Description") {
    @Override
    public String valueOf(PerforceJob perforceJob) {
      return perforceJob.getValueForStandardField(StandardJobFields.description.getFixedCode()).getValue();
    }
  };

  private static final ColumnInfo[] columns = new ColumnInfo[] {JOB, STATUS, USER, DATE, DESCRIPTION};

  private final static int MAX_JOBVEW = 100;
  private JPanel createSouthPanel(final TableView<PerforceJob> table, final String warningText) {
    final JPanel southPanel = new JPanel(new BorderLayout());

    final JPanel stuffPanel = new JPanel(new BorderLayout());

    if (warningText != null) {
      stuffPanel.setPreferredSize(new Dimension(300, 120));
      southPanel.add(stuffPanel, BorderLayout.NORTH);
      final JLabel warningLabel = new JLabel(warningText);
      warningLabel.setFont(warningLabel.getFont().deriveFont(Font.BOLD));
      southPanel.add(warningLabel, BorderLayout.SOUTH);
    } else {
      southPanel.add(stuffPanel);
    }
    /*
    // todo draw job view text
    if (jobViewText != null) {

      stuffPanel.setPreferredSize(new Dimension(300, 100));
      southPanel.add(stuffPanel, BorderLayout.NORTH);

      final StringCutter stringCutter = new StringCutter("Job View: " + jobViewText, 20);
      stringCutter.cutString();
      final String s = stringCutter.getResult();

      final JLabel label = new JLabel(s) {
        @Override
        public Dimension getPreferredSize() {
          final Dimension oldSize = super.getPreferredSize();
          return new Dimension(oldSize.width, stringCutter.getNumLines() * lineSize);
        }
      };
      label.setUI(new MultiLineLabelUI());
      label.setForeground(UIUtil.getInactiveTextColor());

      final JScrollPane scroll = new JScrollPane(label);
      scroll.setPreferredSize(new Dimension(300, 40));
      southPanel.add(scroll, BorderLayout.SOUTH);
    } else {
      southPanel.add(stuffPanel);
    }*/

    southPanel.setPreferredSize(new Dimension(300, 140));
    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        final PerforceJob job = table.getSelectedObject();
        if (job == null) {
          stuffPanel.removeAll();
        } else {
          final SelfLoadingJobDetailsPanel panel = new SelfLoadingJobDetailsPanel(myProject, job);
          stuffPanel.removeAll();
          final JScrollPane scrollPane = new JScrollPane(panel.getPanel());
          stuffPanel.add(scrollPane);
        }
        southPanel.revalidate();
        southPanel.repaint();
      }
    });
    return southPanel;
  }

  @Nullable
  private void showListPopup(final List<PerforceJob> jobs, final Component component, final Consumer<PerforceJob> consumer,
                             final String jobViewString, final int maxCount) {
    final List<PerforceJob> jobsNarrowed = (jobs.size() > maxCount) ? jobs.subList(0, maxCount) : jobs;
    final TableView<PerforceJob> table = new TableView<PerforceJob>(new ListTableModel<PerforceJob>(columns, jobsNarrowed, 0));
    table.setShowHorizontalLines(false);
    table.setTableHeader(null);
    Runnable runnable = new Runnable() {
      public void run() {
        PerforceJob job = table.getSelectedObject();
        if (job != null) {
          consumer.consume(job);
        }
      }
    };

    if (table.getModel().getRowCount() == 0) {
      table.clearSelection();
    }

    table.setMinimumSize(new Dimension(300, 50));
    final PopupChooserBuilder builder = new PopupChooserBuilder(table);
    
    builder.setSouthComponent(createSouthPanel(table, (jobs.size() > maxCount) ?
                                                      PerforceBundle.message("perforce.jobs.search.limit.exceeded.warning", maxCount) : null));

    builder.setTitle(PerforceBundle.message("perforce.jobs.select.one")).
        setItemChoosenCallback(runnable).
        setResizable(true).
        setDimensionServiceKey("org.jetbrains.idea.perforce.perforce.jobs.PerforceCheckinComponent.SelectOneJob").
        setMinSize(new Dimension(300, 300));
    final JBPopup popup = builder.createPopup();

    popup.showUnderneathOf(component);
  }

  private void ensureDefaultConnections() {
    if (myCurrent.hasDefaultName()) {
      final JobDetailsLoader loader = new JobDetailsLoader(myProject);
      loader.fillConnections(myCurrent, myConnMap);
    }
  }

  public JComponent getComponent() {
    return myPanel;
  }

  public void refresh() {

  }

  public void saveState() {
    if (myCurrent != null && myCurrent.hasDefaultName()) {
      keepDefaultListJobs();
    }
  }

  public void restoreState() {
    
  }

  public void onChangeListSelected(final LocalChangeList list) {
    if (! Comparing.equal(list, myCurrent)) {
      if (list != null && (! list.hasDefaultName()) && (myCurrent != null) && (myCurrent.hasDefaultName())) {
        keepDefaultListJobs();
      }
      myCurrent = list;

      if (myCurrent == null) {
        setItems(Collections.<PerforceJob>emptyList());
      } else if (myCurrent.hasDefaultName()) {
        correctDefaultAssociated(list);
        final Map<ConnectionKey, List<PerforceJob>> data = myVcs.getDefaultAssociated();

        final List<PerforceJob> filtered = new ArrayList<PerforceJob>();
        for (Map.Entry<ConnectionKey, List<PerforceJob>> entry : data.entrySet()) {
          filtered.addAll(entry.getValue());
        }

        setItems(filtered);
      } else {
        Map<ConnectionKey, List<PerforceJob>> data = myCache.get(myCurrent);
        if (data == null) {
          data = loadOnSelect(myCurrent);
          myCache.put(list, data);
        }
        setItems(flattenList(data.values()));
      }
    }
  }

  private void correctDefaultAssociated(final LocalChangeList defaultList) {
    final Map<ConnectionKey, List<PerforceJob>> data = myVcs.getDefaultAssociated();

    final ConnectionSelector connectionSelector = new ConnectionSelector(myProject, defaultList);
    final Map<ConnectionKey,P4Connection> map = connectionSelector.getConnections();

    final Map<ConnectionKey, List<PerforceJob>> filtered = new HashMap<ConnectionKey, List<PerforceJob>>();
    for (Map.Entry<ConnectionKey, List<PerforceJob>> entry : data.entrySet()) {
      if (map.containsKey(entry.getKey())) {
        filtered.put(entry.getKey(), entry.getValue());
      }
    }

    myVcs.setDefaultAssociated(filtered);
  }

  private void keepDefaultListJobs() {
    final List<PerforceJob> perforceJobs = myDetails.getJobs();
    final Map<ConnectionKey, List<PerforceJob>> jobs = new HashMap<ConnectionKey, List<PerforceJob>>();
    for (PerforceJob job : perforceJobs) {
      List<PerforceJob> oldList = jobs.get(job.getConnectionKey());
      if (oldList == null) {
        oldList = new ArrayList<PerforceJob>();
        jobs.put(job.getConnectionKey(), oldList);
      }
      oldList.add(job);
    }
    myVcs.setDefaultAssociated(jobs);
  }

  private List<PerforceJob> flattenList(final Collection<List<PerforceJob>> collList) {
    final List<PerforceJob> result = new ArrayList<PerforceJob>();
    for (List<PerforceJob> jobList : collList) {
      result.addAll(jobList);
    }
    return result;
  }

  private Map<ConnectionKey, List<PerforceJob>> loadOnSelect(final LocalChangeList list) {
    final JobDetailsLoader loader = new JobDetailsLoader(myProject);
    final Map<ConnectionKey, List<PerforceJob>> perforceJobs = new HashMap<ConnectionKey, List<PerforceJob>>();
    loader.loadJobsForList(list, myConnMap, perforceJobs);
    return perforceJobs;
  }

  private void setItems(final List<PerforceJob> items) {
    myDetails.fillTree(items, items.isEmpty() ? null : items.get(0));
  }

  public Object getDataForCommit() {
    final Map<ConnectionKey, List<PerforceJob>> data = myCurrent.hasDefaultName() ? myVcs.getDefaultAssociated() : myCache.get(myCurrent);
    return data == null ? null : flattenList(data.values());
  }
}
