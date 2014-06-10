package com.intellij.tasks.config;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.actions.IconWithTextAction;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.ui.Splitter;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.FactoryMap;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class TaskRepositoriesConfigurable implements Configurable {

  private JPanel myPanel;
  private JList myRepositoriesList;
  @SuppressWarnings({"UnusedDeclaration"})
  private JPanel myToolbarPanel;
  private Splitter mySplitter;
  private JPanel myRepositoryEditor;

  private static final Icon ADD_ICON = IconLoader.getIcon("/general/add.png");

  private List<TaskRepository> myRepositories;

  private final Project myProject;
  private DefaultActionGroup myActionGroup;

  private Consumer<TaskRepository> myChangeListener;
  @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
  private final FactoryMap<TaskRepository, String> myRepoNames = new FactoryMap<TaskRepository, String>() {

    private int count;
    @Override
    protected String create(TaskRepository repository) {

      UnnamedConfigurable editor = repository.getType().createEditor(repository, myProject, myChangeListener);
      editor.reset();
      String name = Integer.toString(count++);
      myRepositoryEditor.add(editor.createComponent(), name);
      myRepositoryEditor.doLayout();
      return name;
    }
  };
  private TaskManager myManager;

  public TaskRepositoriesConfigurable(final Project project) {

    myManager = TaskManager.getManager(project);
    TaskRepositoryType[] groups = myManager.getAllRepositoryTypes();

    AnAction[] createActions = ContainerUtil.map2Array(groups, AnAction.class, new Function<TaskRepositoryType, AnAction>() {
      public AnAction fun(final TaskRepositoryType group) {
        String text = "Add " + group.getName() + " server";
        return new IconWithTextAction(text, text, group.getIcon()) {
          @Override
          public void actionPerformed(AnActionEvent e) {
            TaskRepository repository = group.createRepository();
            ((CollectionListModel)myRepositoriesList.getModel()).add(repository);
            myRepositoriesList.setSelectedValue(repository, true);
          }
        };
      }
    });

    MultipleAddAction addAction = new MultipleAddAction(null, "Add server", createActions);
    addAction.registerCustomShortcutSet(CommonShortcuts.INSERT, myRepositoriesList);
    myActionGroup.add(addAction);
    IconWithTextAction removeAction = new IconWithTextAction(null, "Remove server", IconLoader.getIcon("/general/remove.png")) {

      @Override
      public void actionPerformed(AnActionEvent e) {
        TaskRepository repository = getSelectedRepository();
        if (repository != null) {
          ((CollectionListModel)myRepositoriesList.getModel()).remove(repository);

          if (!myRepositories.isEmpty()) {
            myRepositoriesList.setSelectedValue(myRepositories.get(0), true);
          }
          else {
            myRepositoryEditor.removeAll();
            myRepositoryEditor.repaint();
          }
        }
      }

      @Override
      public void update(AnActionEvent e) {
        TaskRepository repository = getSelectedRepository();
        e.getPresentation().setEnabled(repository != null);
      }
    };
    removeAction.registerCustomShortcutSet(CommonShortcuts.DELETE, myRepositoriesList);
    myActionGroup.add(removeAction);

    myProject = project;

    myRepositoriesList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        showSelected();
      }
    });

    myRepositoriesList.setCellRenderer(new ColoredListCellRenderer() {
      @Override
      protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
        TaskRepository repository = (TaskRepository)value;
        Icon icon = repository.getType().getIcon();
        setIcon(icon);
        append(repository.getPresentableName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      }
    });
    myChangeListener = new Consumer<TaskRepository>() {
      public void consume(TaskRepository repository) {
        ((CollectionListModel)myRepositoriesList.getModel()).contentsChanged(repository);
      }
    };
  }

  private void showSelected() {
    TaskRepository repository = getSelectedRepository();
    if (repository != null) {
      String name = myRepoNames.get(repository);
      ((CardLayout)myRepositoryEditor.getLayout()).show(myRepositoryEditor, name);
    }
  }

  @Nullable
  private TaskRepository getSelectedRepository() {
    return (TaskRepository)myRepositoriesList.getSelectedValue();
  }

  @Nls
  public String getDisplayName() {
    return "Servers";
  }

  public Icon getIcon() {
    return null;
  }

  public String getHelpTopic() {
    return "reference.settings.project.tasks.servers";
  }

  public JComponent createComponent() {
    return myPanel;
  }

  public boolean isModified() {
    return !myRepositories.equals(getReps());
  }

  public void apply() throws ConfigurationException {
    myManager.configureRepositories(ContainerUtil.map2Array(myRepositories, TaskRepository.class, new Function<TaskRepository, TaskRepository>() {
      public TaskRepository fun(TaskRepository repository) {
        return repository.clone();
      }
    }));
    myManager.updateIssues();
  }

  public void reset() {
    myRepoNames.clear();
    myRepositoryEditor.removeAll();
    myRepositories = new ArrayList<TaskRepository>();
    for (TaskRepository repository : getReps()) {
      myRepositories.add(repository.clone());
    }
    myRepositoriesList.setModel(new CollectionListModel(myRepositories));
    if (!myRepositories.isEmpty()) {
      myRepositoriesList.setSelectedValue(myRepositories.get(0), true);
    }
  }

  private List<TaskRepository> getReps() {
    return Arrays.asList(myManager.getAllRepositories());
  }

  public void disposeUIResources() {
  }

  private void createUIComponents() {
    myActionGroup = new DefaultActionGroup();
    myToolbarPanel = (JPanel)ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, myActionGroup, true).getComponent();

    myRepositoriesList = new JList();
    myRepositoryEditor = new JPanel(new CardLayout());
    mySplitter = new Splitter(false);
    mySplitter.setFirstComponent(new JScrollPane(myRepositoriesList));
    mySplitter.setSecondComponent(myRepositoryEditor);
    mySplitter.setHonorComponentsMinimumSize(true);
    mySplitter.setShowDividerControls(true);
  }

  private static class MultipleAddAction extends IconWithTextAction {
    private final AnAction[] myAdditional;

    public MultipleAddAction(String text, String description, AnAction... additional) {
      super(text, description, ADD_ICON);
      myAdditional = additional;
    }

    public void actionPerformed(AnActionEvent e) {
      DefaultActionGroup group = new DefaultActionGroup();
      for (AnAction aMyAdditional : myAdditional) {
        group.add(aMyAdditional);
      }
      ActionPopupMenu popupMenu = ActionManager.getInstance().createActionPopupMenu(ActionPlaces.TODO_VIEW_TOOLBAR, group);

      popupMenu.getComponent().show(createCustomComponent(getTemplatePresentation()), 10, 10);
    }
  }


}
