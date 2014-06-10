package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;

class SelfLoadingJobDetailsPanel {
  private JPanel myMain;
  private final Project myProject;
  private final PerforceJob myJob;
  private final List<Pair<String, String>> myDetails;
  private final JPanel myPanel;

  SelfLoadingJobDetailsPanel(final Project project, final PerforceJob job) {
    myProject = project;
    myJob = job;
    myDetails = new ArrayList<Pair<String, String>>();
    myPanel = new JPanel(new BorderLayout());
    createDetailsPanel();
  }

  private void initData() {

    final ModalityState state = ModalityState.current();

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        if( load()) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              myMain.removeAll();

              final GridBagConstraints gb = DefaultGb.create();
              gb.anchor = GridBagConstraints.NORTHWEST;
              for (Pair<String, String> detail : myDetails) {
                final JLabel label = new JLabel(detail.getFirst() + ":");
                final JTextArea text = new JTextArea(detail.getSecond().trim());
                text.setEditable(false);
                text.setBackground(UIUtil.getBgFillColor(myMain));

                gb.gridx = 0;
                gb.fill = GridBagConstraints.NONE;
                gb.weightx = 0;
                myMain.add(label, gb);
                ++ gb.gridx;
                gb.fill = GridBagConstraints.HORIZONTAL;
                gb.weightx = 1;
                myMain.add(text, gb);
                ++ gb.gridy;
              }
              myPanel.revalidate();
              myPanel.repaint();
            }
          }, state);
        }
      }
    });
  }

  private void createDetailsPanel() {
    myMain = new JPanel(new GridBagLayout());
    final GridBagConstraints gb = DefaultGb.create();
    gb.anchor = GridBagConstraints.CENTER;
    gb.fill = GridBagConstraints.BOTH;
    gb.weightx = gb.weighty = 1;
    myMain.add(new JLabel("Loading..."), gb);

    myPanel.add(myMain, BorderLayout.NORTH);

    initData();
  }

  private boolean load() {
    try {
      final List<Pair<String, String>> items = new JobDetailsLoader(myProject).load(myJob);
      if (items != null) {
        // remove name
        final PerforceJobFieldValue nameValue = myJob.getNameValue();
        if (nameValue != null) {
          final String fieldName = nameValue.getField().getName();
          final String name = nameValue.getValue();
          items.remove(new Pair<String, String>(fieldName, name));
        }

        myDetails.addAll(items);
      }
    }
    catch (VcsException e) {
      new ErrorReporter("loading job details").report(myProject, e);
      return false;
    }
    return true;
  }

  public JPanel getPanel() {
    return myPanel;
  }
}
