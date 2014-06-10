package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.*;
import java.util.List;

public class RelatedJobsTable {
  private JPanel myPresentation;
  private TableView<PerforceJob> myTable;
  private ListTableModel<PerforceJob> myModel;

  private TableView<Pair<String,String>> myDetailsTable;
  private ListTableModel<Pair<String,String>> myDetailsModel;

  private ListSelectionListener mySelectionListener;

  private final Map<String, List<Pair<String, String>>> myCache;

  public void setSelectionListener(ListSelectionListener selectionListener) {
    mySelectionListener = selectionListener;
  }

  private final JobDetailsLoader myLoader;

  public RelatedJobsTable(final PerforceJobSpecification specification, final List<PerforceJob> jobs, final JobDetailsLoader loader) {
    myLoader = loader;
    myCache = new HashMap<String, List<Pair<String, String>>>();
    createUI(specification, jobs);
    createListeners();

    if (! jobs.isEmpty()) {
      myTable.getSelectionModel().setSelectionInterval(0, 0);
    }
  }

  private void createUI(final PerforceJobSpecification specification, final List<PerforceJob> jobs) {
    myPresentation = new JPanel(new GridBagLayout());

    final GridBagConstraints gb = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,
                                                         new Insets(2,2,2,2),0,0);

    myModel = new ListTableModel<PerforceJob>(createStandardColumns(specification), jobs, 0);
    myTable = new TableView<PerforceJob>(myModel);
    final JScrollPane mainScroll = new JScrollPane(myTable);

    myDetailsModel = new ListTableModel<Pair<String, String>>(
      new ColumnInfo<Pair<String,String>, String>("Field") {
        public String valueOf(Pair<String, String> stringStringPair) {
          return stringStringPair.getFirst();
        }
      },
      new ColumnInfo<Pair<String,String>, String>("Value") {
        public String valueOf(Pair<String, String> stringStringPair) {
          return stringStringPair.getSecond();
        }
      });
    myDetailsTable = new TableView<Pair<String, String>>(myDetailsModel);
    myDetailsTable.setBackground(UIUtil.getBgFillColor(myPresentation));

    final JScrollPane detailsScroll = ScrollPaneFactory.createScrollPane(myDetailsTable);
    detailsScroll.setBackground(UIUtil.getBgFillColor(myPresentation));

    final Splitter splitter = new Splitter(true, 0.7f);
    splitter.setFirstComponent(mainScroll);
    splitter.setSecondComponent(detailsScroll);

    gb.fill = GridBagConstraints.BOTH;
    gb.weightx = 1;
    gb.weighty = 1;
    myPresentation.add(splitter, gb);
  }

  private void createListeners() {
    myTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    myTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (mySelectionListener != null) {
          mySelectionListener.valueChanged(e);
        }
        final int selectedRow = myTable.getSelectedRow();
        myDetailsModel.setItems(Collections.<Pair<String, String>>emptyList());
        if (selectedRow >= 0) {
          final PerforceJob job = (PerforceJob) myModel.getItem(selectedRow);
          final String jobName = job.getValueForStandardField(StandardJobFields.name.getFixedCode()).getValue();
          final List<Pair<String, String>> cached = myCache.get(jobName);
          if (cached != null) {
            myDetailsModel.setItems(cached);
          } else {
            try {
              final List<Pair<String, String>> items = myLoader.load(job);
              myCache.put(jobName, items);
              myDetailsModel.setItems(items);
            }
            catch (VcsException e1) {
              new ErrorReporter("loading job details").report(myLoader.getProject(), e1);
            }
          }
        }
        myDetailsTable.repaint();
      }
    });
  }

  private ColumnInfo[] createStandardColumns(final PerforceJobSpecification specification) {
    final List<GeneralColumn> result = new ArrayList<GeneralColumn>();

    final Collection<PerforceJobField> fields = specification.getFields();
    for (PerforceJobField field : fields) {
      if (StandardJobFields.isStandardField(field)) {
        result.add(new GeneralColumn(field));
      }
    }
    Collections.sort(result, StandardFieldsComparator.getInstance());

    return result.toArray(new ColumnInfo[result.size()]);
  }

  public JPanel getComponent() {
    return myPresentation;
  }

  @Nullable
  public PerforceJob getSelectedJob() {
    return myTable.getSelectedObject();
  }

  // todo resort items

  public void removeSelectedJob() {
    final int idx = myTable.getSelectedRow();
    if (idx >= 0) {
      myModel.removeRow(idx);
    }
  }

  public void addJob(final PerforceJob job) {
    final List<PerforceJob> list = new ArrayList<PerforceJob>(myModel.getItems());
    list.add(job);
    myModel.setItems(list);
  }

  public List<PerforceJob> getJobs() {
    return new ArrayList<PerforceJob>(myModel.getItems());
  }

  public void setJobs(final List<PerforceJob> jobs) {
    myCache.clear();
    myModel.setItems(jobs);
    myTable.repaint();
    if (jobs.isEmpty()) {
      myTable.clearSelection();
    } else {
      myTable.getSelectionModel().setSelectionInterval(0, 0);
    }
  }

  private static class StandardFieldsComparator implements Comparator<GeneralColumn> {
    private static final StandardFieldsComparator ourInstance = new StandardFieldsComparator();

    public static StandardFieldsComparator getInstance() {
      return ourInstance;
    }

    public int compare(final GeneralColumn o1, final GeneralColumn o2) {
      return new Integer(StandardJobFields.getOrder(o1.myField)).compareTo(StandardJobFields.getOrder(o2.myField));
    }
  }

  // for a standard field
  private static class GeneralColumn extends ColumnInfo<PerforceJob, String> {
    private final PerforceJobField myField;

    private GeneralColumn(final PerforceJobField field) {
      super(field.getName());
      myField = field;
    }

    public String valueOf(final PerforceJob perforceJob) {
      final PerforceJobFieldValue value = perforceJob.getValueForStandardField(myField.getCode());
      return value == null ? "" : (value.getValue() == null ? "" : value.getValue());
    }

  }
}
