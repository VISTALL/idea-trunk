/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.engine;

import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.facet.impl.FacetUtil;
import com.intellij.hibernate.HibernateMessages;
import com.intellij.ide.DataManager;
import com.intellij.jpa.AbstractQlPersistenceModel;
import com.intellij.jpa.util.JpaUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.persistence.DatabaseIcons;
import com.intellij.persistence.DatabaseMessages;
import com.intellij.persistence.PersistenceIcons;
import com.intellij.persistence.facet.PersistenceFacetBase;
import com.intellij.persistence.facet.PersistenceFacetConfiguration;
import com.intellij.persistence.model.PersistenceMappings;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.persistence.run.AbstractQueryLanguageConsole;
import com.intellij.persistence.run.ConsoleContextProvider;
import com.intellij.persistence.run.ConsoleHistoryModel;
import com.intellij.persistence.run.ConsoleRunConfiguration;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.persistence.util.PersistenceModelBrowser;
import com.intellij.ql.psi.QlFile;
import com.intellij.ql.psi.impl.QLLanguage;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.content.Content;
import com.intellij.util.PairProcessor;
import com.intellij.util.Processor;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;

/**
 * @author Gregory.Shrago
*/
public class HibernateConsole extends AbstractQueryLanguageConsole {

  private final ConsoleHistoryModel myHistory = new ConsoleHistoryModel();
  private final HibernateConsoleContextProvider.HibernateRunContext myRunContext;
  private final String myQuery;
  private EditorTextField myEditorField;
  private AnAction myRunAction;
  private AnAction mySqlAction;
  private QlFile myFile;
  private AnAction myHistoryNextAction;
  private AnAction myHistoryPrevAction;


  public static void openHqlConsole(final Project project, final Editor editor, final PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> curFacet,
                                    final PersistencePackage curUnit,
                                    final String query,
                                    final AnActionEvent event) {
    ConsoleRunConfiguration.chooseConfigurationType(project, editor, event, new Processor<ConsoleRunConfiguration>() {

      public boolean process(final ConsoleRunConfiguration configuration) {
        if (configuration == null) return false;
        final ConsoleRunConfiguration realConfiguration = configuration.clone();

        final ConsoleContextProvider.ConsoleContext context =
                new HibernateConsoleContextProvider().createConsoleContext(curFacet, curUnit, query);
        realConfiguration.setConsoleContext(context);
        realConfiguration.setName(HibernateMessages.message("title.hql.console", context.getProvider().getDisplayName(), configuration.getName(), curFacet.getModule().getName(), curUnit.getName().getValue()));

        //final RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
        //final Map<String, BeforeRunTask> steps = runManager.getBeforeRunTasks(realConfiguration);
        //steps.remove(ExecutionBundle.message("before.launch.compile.step"));
        //((RunManagerImpl)runManager).setBeforeRunTasks(realConfiguration, steps);
        final ProgramRunner runner = RunnerRegistry.getInstance().findRunnerById(DefaultRunExecutor.EXECUTOR_ID);
        if (runner != null) {
          try {
            runner.execute(DefaultRunExecutor.getRunExecutorInstance(),
                           new ExecutionEnvironment(realConfiguration, DataManager.getInstance().getDataContext()), null);
          }
          catch (ExecutionException e) {
            Messages.showErrorDialog(project, e.getMessage(), ExecutionBundle.message("error.common.title"));
          }
        }
        return true;
      }
    });
  }


  public HibernateConsole(final Project project, final String title, final String query,
                          final HibernateConsoleContextProvider.HibernateRunContext runContext) {
    super(project, title);
    myQuery = StringUtil.notNullize(query);
    myRunContext = runContext;
    myRunContext.getEngine().setOutputHandler(createOutputHandler());
    Disposer.register(this, runContext.getEngine());
  }

  public boolean isReady() {
    return !getEngine().isWaitingForResponse();
  }

  public HibernateEngine getEngine() {
    return myRunContext.getEngine();
  }

  @Override
  public void runTask(final Runnable task) {
    if (getEngine().isInitialized()) {
      super.runTask(task);
    }
    else {
      getEngine().ensureInitialized().doWhenDone(new Runnable() {
        public void run() {
          HibernateConsole.super.runTask(task);
        }
      });
    }
  }

  @Override
  protected void printError(final Throwable throwable) {
    if (!StringUtil.getThrowableText(throwable).contains("org.hibernate.") &&
      !(throwable instanceof IllegalStateException)) {
      LOG.error(throwable);
    }
    else {
      super.printError(throwable);
    }
  }

  protected void buildConsoleUi(final ConsoleView consoleView) {
    final RunnerLayoutUi layoutUi = getUi();
    layoutUi.getContentManager().addDataProvider(new DataProvider() {
      public Object getData(@NonNls final String dataId) {
        if (ConsoleContextProvider.RUN_CONTEXT.getName().equals(dataId)) {
          return myRunContext;
        }
        return null;
      }
    });
    if (myEditorField == null) {
      final PersistenceFacetBase<?, PersistencePackage> facet = myRunContext.getFacet();
      final Pair<EditorTextField, QlFile> pair = JpaUtil.createQlEditor(facet.getModule().getProject(), myQuery, false,
                                                                        facet.getQlLanguage(), new MyAbstractQlPersistenceModel(facet, myRunContext.getPersistenceUnit()));
      myEditorField = pair.getFirst();
      myFile = pair.getSecond();
    }
    getTabularDataHandler().resetOutputTabCounter();

    layoutUi.getDefaults().initTabDefaults(0, HibernateMessages.message("hqlconsole.tab.title"), null);
    final Content input = layoutUi.createContent(ID_INPUT, wrap(myEditorField.getComponent()), HibernateMessages.message("hqlconsole.tab.title.input"), PersistenceIcons.CONSOLE_ICON, myEditorField.getComponent());
    final Content output = layoutUi.createContent(ID_OUTPUT, wrap(consoleView.getComponent()), HibernateMessages.message("hqlconsole.tab.title.output"), PersistenceIcons.CONSOLE_OUTPUT_ICON, consoleView.getPreferredFocusableComponent());
    input.setActions(createActionGroup(), ActionPlaces.UNKNOWN, myEditorField.getComponent());
    input.setCloseable(false);
    output.setCloseable(false);
    layoutUi.addContent(input, 0, PlaceInGrid.center, false);
    layoutUi.addContent(output, 0, PlaceInGrid.bottom, false);
  }

  @Override
  protected JComponent getEditorComponent() {
    return myEditorField.getComponent();
  }

  @NotNull
  @Override
  protected String getConsoleId() {
    return "HibernateConsole";
  }

  public void dispose() {
    super.dispose();
  }

  public void executeQuery(final String query) {
    myHistory.addToHistory(query);
    getConsoleView().print("> " + query + "\n", ConsoleViewContentType.USER_INPUT);
    getEngine().executeHqlQuery(query, Collections.<String,String>emptyMap());
  }

  protected void doAfterCommandResultDisplayed() {
    if (myHistory.hasHistory(true)) {
      final String text1 = KeymapUtil.getShortcutText(myHistoryNextAction.getShortcutSet().getShortcuts()[0]);
      final String text2 = KeymapUtil.getShortcutText(myHistoryPrevAction.getShortcutSet().getShortcuts()[0]);
      WindowManager.getInstance().getIdeFrame(getProject()).getStatusBar().setInfo(DatabaseMessages.message("jdbc.console.history.message.0.1", text1, text2));
    }
  }

  public void generateSQL(final String query) {
    try {
      getConsoleView().print("> " + query + "\n", ConsoleViewContentType.USER_INPUT);
      if (query.length() > 0) {
        getEngine().getGeneratedSql(query);
      }
    }
    catch (Throwable ex) {
      printError(ex);
    }
  }

  public void generateDDL() {
    try {
      getEngine().getGeneratedDdl();
    }
    catch (Throwable ex) {
      printError(ex);
    }
  }

  private DefaultActionGroup createActionGroup() {
    final DefaultActionGroup group = new DefaultActionGroup();
    myRunAction = new AnAction(HibernateMessages.message("hqlconsole.action.execute.query"),
                               HibernateMessages.message("hqlconsole.action.execute.query"),
                               IconLoader.getIcon("/actions/execute.png")) {
      public void actionPerformed(final AnActionEvent e) {
        final String query = myEditorField.getText().trim();
        myEditorField.selectAll();
        runTask(new Runnable() {
          public void run() {
            executeQuery(query);
          }
        });
      }

      public void update(final AnActionEvent e) {
        e.getPresentation().setEnabled(isReady() && myEditorField != null && myEditorField.getText().trim().length() > 0);
      }
    };
    mySqlAction = new AnAction(HibernateMessages.message("hqlconsole.action.generate.sql"),
                               HibernateMessages.message("hqlconsole.action.generate.sql"),
                               IconLoader.getIcon("/actions/dump.png")) {
      public void actionPerformed(final AnActionEvent e) {
        final String query = myEditorField.getText().trim();
        runTask(new Runnable() {
          public void run() {
            generateSQL(query);
          }
        });
      }

      public void update(final AnActionEvent e) {
        e.getPresentation().setEnabled(isReady() && myEditorField != null && myEditorField.getText().trim().length() > 0);
      }
    };
    final AnAction ddlAction = new AnAction(HibernateMessages.message("hqlconsole.action.generate.ddl"),
                               HibernateMessages.message("hqlconsole.action.generate.ddl"), DatabaseIcons.DATASOURCE_ICON) {
      public void actionPerformed(final AnActionEvent e) {
        runTask(new Runnable() {
          public void run() {
            generateDDL();
          }
        });
      }

      public void update(final AnActionEvent e) {
        e.getPresentation().setEnabled(isReady());
      }
    };
    group.add(myRunAction);
    group.add(mySqlAction);
    group.add(ddlAction);
    myRunAction.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK)),
                                          myEditorField.getComponent());
    mySqlAction.registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)),
                                          myEditorField.getComponent());

    final PairProcessor<AnActionEvent, String> historyProcessor = new PairProcessor<AnActionEvent, String>() {
      public boolean process(final AnActionEvent e, final String s) {
        new WriteCommandAction(getProject(), myFile) {
          protected void run(final Result result) throws Throwable {
            myEditorField.getDocument().setText(s == null ? "" : s);
          }
        }.execute();
        return true;
      }
    };
    myHistoryNextAction = ConsoleHistoryModel.createHistoryAction(myHistory, true, historyProcessor);
    myHistoryPrevAction = ConsoleHistoryModel.createHistoryAction(myHistory, false, historyProcessor);
    myHistoryNextAction.registerCustomShortcutSet(myHistoryNextAction.getShortcutSet(), myEditorField.getComponent());
    myHistoryPrevAction.registerCustomShortcutSet(myHistoryPrevAction.getShortcutSet(), myEditorField.getComponent());
    return group;
  }

  private static class MyAbstractQlPersistenceModel extends AbstractQlPersistenceModel {
    private final PersistenceFacetBase<?, PersistencePackage> myFacet;
    private final PersistencePackage myPersistenceUnit;

    public MyAbstractQlPersistenceModel(final PersistenceFacetBase<?, PersistencePackage> facet, final PersistencePackage persistenceUnit) {
      myFacet = facet;
      myPersistenceUnit = persistenceUnit instanceof DomElement
                          ? (PersistencePackage)((DomElement)persistenceUnit).createStableCopy() : persistenceUnit;
    }

    protected void processPersistenceMappings(final PairProcessor<PersistenceMappings, PersistenceModelBrowser> processor) {
      if (FacetUtil.isRegistered(myFacet) && myPersistenceUnit.isValid()) {
        final PersistenceMappings entityMappings = myFacet.getEntityMappings(myPersistenceUnit);
        final PersistenceModelBrowser browser = PersistenceCommonUtil.createFacetAndUnitModelBrowser(myFacet, myPersistenceUnit, null);
        processor.process(entityMappings, browser);
      }
    }

    protected boolean isHibernate() {
      return myFacet.getQlLanguage() == QLLanguage.HIBERNATE_QL;
    }

    @NotNull
    @Override
    protected List<? extends PersistenceFacetBase> getPersistenceFacets() {
      return Collections.singletonList(myFacet);
    }
  }
}
