package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.ConnectionKey;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

public class AddJobToChangeListDialog extends DialogWrapper {
  private final Project myProject;
  private final JobsWorker myWorker;
  private final PerforceJobSpecification mySpecification;

  private JPanel myMainPanel;

  private JobSearchParametersPanel mySearchParametersPanel;
  private JobsMasterDetails myTable;
  private JButton mySearchBtn;
  private PerforceJob mySelectedJob;

  private final P4Connection myConnection;
  private final ConnectionKey myKey;

  private JPanel myResultsPanel;
  private final static String NO_RESULTS = "NO_RESULTS";
  private final static String FOUND_JOBS = "FOUND_JOBS";
  private JComponent myTableComponent;
  private JLabel myLimitExceededLabel;

  public AddJobToChangeListDialog(final Project project, final boolean canBeParent, final JobsWorker worker,
                                  final JobDetailsLoader loader, final PerforceJobSpecification specification, P4Connection connection,
                                  ConnectionKey key) {
    super(project, canBeParent);
    myProject = project;
    myWorker = worker;
    mySpecification = specification;
    myConnection = connection;
    myKey = key;
    createUI();
    setTitle("Link job to changelist");

    init();
    setOKActionEnabled(myTableComponent.isVisible() && myTable.getSelectedJob() != null);
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return mySearchParametersPanel.getPrefferedFocusTarget();
  }

  @Override
  protected String getHelpId() {
    return "reference.vcs.perforce.searchJob";
  }

  @Override
  protected String getDimensionServiceKey() {
    return "org.jetbrains.idea.perforce.perforce.jobs.AddJobToChangeListDialog";
  }

  private void createUI() {
    myMainPanel = new JPanel(new GridBagLayout());

    final GridBagConstraints gb = DefaultGb.create();

    ++ gb.gridy;
    gb.gridx = 0;
    gb.anchor = GridBagConstraints.WEST;
    final JLabel paramsLabel = new JLabel("Specify search parameters:");
    paramsLabel.setFont(paramsLabel.getFont().deriveFont(Font.BOLD));
    final Insets insets = gb.insets;
    gb.insets = new Insets(2,2,7,2);
    myMainPanel.add(paramsLabel, gb);
    gb.insets = insets;

    ++ gb.gridy;
    final java.util.List<String> freeFields = myWorker.getFreeFields(mySpecification);
    mySearchParametersPanel = new JobSearchParametersPanel(freeFields, myProject);
    myMainPanel.add(mySearchParametersPanel.getComponent(), gb);

    ++ gb.gridx;
    gb.anchor = GridBagConstraints.NORTHWEST;
    mySearchBtn = new JButton("Search");
    myMainPanel.add(mySearchBtn, gb);

    myTable = new JobsMasterDetails(myProject);

    myResultsPanel = new JPanel(new CardLayout());
    final JPanel noResultsPanel = new JPanel(new GridBagLayout());
    final GridBagConstraints noResultsGb = DefaultGb.create();
    noResultsGb.anchor = GridBagConstraints.CENTER;
    noResultsPanel.add(new JLabel("Nothing found"), noResultsGb);

    myResultsPanel.add(noResultsPanel, NO_RESULTS);
    myTableComponent = myTable.createComponent();
    myResultsPanel.add(myTableComponent, FOUND_JOBS);
    ((CardLayout) myResultsPanel.getLayout()).show(myResultsPanel, NO_RESULTS);

    gb.gridx = 0;
    ++ gb.gridy;
    gb.anchor = GridBagConstraints.WEST;
    final JLabel resultsLabel = new JLabel("Search results:");
    gb.insets = new Insets(7,2,2,2);
    resultsLabel.setFont(resultsLabel.getFont().deriveFont(Font.BOLD));
    myMainPanel.add(resultsLabel, gb);

    myLimitExceededLabel = new JLabel();
    myLimitExceededLabel.setForeground(Color.red);
    ++ gb.gridx;
    gb.insets = new Insets(2,2,2,2);
    myMainPanel.add(myLimitExceededLabel, gb);

    gb.gridx = 0;
    gb.gridwidth = 2;
    ++ gb.gridy;
    gb.fill = GridBagConstraints.BOTH;
    gb.weightx = 1;
    gb.weighty = 1;
    gb.anchor = GridBagConstraints.CENTER;
    myMainPanel.add(myResultsPanel, gb);

    mySearchBtn.setMnemonic(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK).getKeyCode());

    mySearchBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final JobsSearchSpecificator specificator = mySearchParametersPanel.createSpecificator();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
          public void run() {
            try {
              final List<PerforceJob> jobs = myWorker.getJobs(mySpecification, specificator, myConnection, myKey);
              ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                  myLimitExceededLabel.setText("");
                  if (! jobs.isEmpty()) {
                    ((CardLayout) myResultsPanel.getLayout()).show(myResultsPanel, FOUND_JOBS);
                  } else {
                    ((CardLayout) myResultsPanel.getLayout()).show(myResultsPanel, NO_RESULTS);
                  }
                  final int maxCount = specificator.getMaxCount();
                  List<PerforceJob> toShow = jobs;
                  if ((maxCount > 0) && (maxCount < jobs.size())) {
                    toShow = jobs.subList(0, maxCount);
                    myLimitExceededLabel.setText(PerforceBundle.message("perforce.jobs.search.limit.exceeded.warning", maxCount));
                  }
                  myTable.fillTree(toShow, toShow.isEmpty() ? null : toShow.get(0));
                }
              });
            }
            catch (VcsException e1) {
              final String[] params = specificator.addParams(new String[0]);
              final StringBuilder sb = new StringBuilder("searching for jobs. Search request: 'p4 jobs ");
              for (String param : params) {
                sb.append(param).append(' ');
              }
              sb.append("' ");
              new ErrorReporter(sb.toString()).report(myProject, e1);
            }
          }
        }, "Searhing for jobs", false, myProject);
      }
    });

    myTable.setSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        setOKActionEnabled(myTableComponent.isVisible() && myTable.getSelectedJob() != null);
      }
    });
  }

  @Override
  protected void doOKAction() {
    mySelectedJob = myTable.getSelectedJob();
    if (mySelectedJob != null) {
      super.doOKAction();
    }
  }

  public PerforceJob getSelectedJob() {
    return mySelectedJob;
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }
}
