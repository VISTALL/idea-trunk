/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.hibernate.engine;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.model.HibernateDescriptorsConstants;
import com.intellij.hibernate.model.HibernatePropertiesConstants;
import com.intellij.hibernate.model.xml.config.HibernateConfiguration;
import com.intellij.hibernate.model.xml.config.SessionFactory;
import com.intellij.hibernate.util.HibernateUtil;
import com.intellij.ide.DataManager;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.jam.JamElement;
import com.intellij.javaee.dataSource.DataSource;
import com.intellij.javaee.dataSource.DataSourceManager;
import com.intellij.javaee.model.common.persistence.mapping.Entity;
import com.intellij.javaee.module.view.dataSource.LocalDataSource;
import com.intellij.javaee.module.view.dataSource.classpath.DataSourceClasspathElement;
import com.intellij.jpa.model.annotations.mapping.JamEntityMappings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataContext;
import static com.intellij.openapi.actionSystem.impl.SimpleDataContext.getSimpleContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.persistence.database.psi.DbDataSourceElement;
import com.intellij.persistence.model.PersistenceMappings;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.persistence.run.AbstractQueryLanguageConsole;
import com.intellij.persistence.run.ConsoleContextProvider;
import com.intellij.persistence.run.ConsoleOutputProcessAdapter;
import com.intellij.persistence.util.PersistenceUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.ModelMergerUtil;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @author Gregory.Shrago
 */
public class HibernateEngine implements Disposable {
  @NonNls static final String MAIN_CLASS = "com.intellij.hibernate.console.HibernateConsoleMain";

  private static final Object WAITING = new Object();

  private final HibernateConsoleContextProvider.HibernateRunContext myRunContext;
  private AbstractQueryLanguageConsole.OutputHandler myOutputHandler;
  private volatile ProcessHandler myProcessHandler;
  private RunContentDescriptor myDescriptor;
  private File myTemporaryHibernateConfig;
  private final Ref<Object> myResponse = Ref.create(null);
  private ProcessListener myProcessListener;

  private enum State {INIT, CONFIG, FACTORY}
  private State myState = null;

  public static boolean isAvailable(final Module module ) {
    final PsiClass psiClass = JavaPsiFacade.getInstance(module.getProject())
      .findClass("org.hibernate.engine.SessionFactoryImplementor", GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
    final PsiFile containingFile = psiClass == null? null : psiClass.getContainingFile();
    return containingFile != null &&
           ProjectRootManager.getInstance(module.getProject()).getFileIndex().isInLibraryClasses(containingFile.getVirtualFile());
  }

  public HibernateEngine(final HibernateConsoleContextProvider.HibernateRunContext runContext) {
    myRunContext = runContext;
    myProcessListener = createProcessListener(new ActionCallback());
  }

  public boolean isWaitingForResponse() {
    synchronized (myResponse) {
      return WAITING.equals(myResponse.get());
    }
  }

  public boolean isInitialized() {
    final ProcessHandler processHandler = myProcessHandler;
    return processHandler != null && !processHandler.isProcessTerminating() && !processHandler.isProcessTerminated();
  }

  public void setOutputHandler(final AbstractQueryLanguageConsole.OutputHandler outputHandler) {
    myOutputHandler = outputHandler;
  }

  public AbstractQueryLanguageConsole.OutputHandler getOutputHandler() {
    return myOutputHandler;
  }

  public ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  public String[] getClassPath() {
    final ArrayList<String> result = new ArrayList<String>();
    ContainerUtil.addIfNotNull(PathUtil.getJarPathForClass(StringUtil.class), result);
    ContainerUtil.addIfNotNull(PathUtil.getJarPathForClass(NotNull.class), result);

    final PersistencePackage unit = myRunContext.getPersistenceUnit();
    if (unit != null) {
      final Collection<DbDataSourceElement> dataSources =
        PersistenceUtil.getDataSources(myRunContext.getModule().getProject(), Collections.singletonList(unit));
      if (dataSources.size() == 1) {
        final Object delegate = dataSources.iterator().next().getDelegate();
        if (delegate instanceof LocalDataSource) {
          for (DataSourceClasspathElement pathElement : ((LocalDataSource)delegate).getClasspathElements()) {
            for (String url : pathElement.getClassesRootUrls()) {
              ContainerUtil.addIfNotNull(PathUtil.toPresentableUrl(url), result);
            }
          }
        }
      }
    }
    return result.toArray(new String[result.size()]);
  }

  public ProcessListener getProcessListener() {
    return myProcessListener;
  }

  public static String getJarPath() {
    return AbstractQueryLanguageConsole.getConsoleJarPath(HibernateEngine.class, MAIN_CLASS);
  }

  private ConsoleOutputProcessAdapter createProcessListener(final ActionCallback callback) {
    return new ConsoleOutputProcessAdapter() {

      @Override
      public void processTerminated(final ProcessEvent event) {
        super.processTerminated(event);
        myState = null;
        myProcessHandler = null;
        cleanup();
      }

      @Override
      public void startNotified(final ProcessEvent event) {
        super.startNotified(event);
        myState = State.INIT;
        myProcessHandler = event.getProcessHandler();
        myProcessHandler.putUserData(ProcessHandler.SILENTLY_DESTROY_ON_CLOSE, Boolean.TRUE);
        myDescriptor = ExecutionManager.getInstance(myRunContext.getRunConfiguration().getProject()).getContentManager().findContentDescriptor(
                DefaultRunExecutor.getRunExecutorInstance(), myProcessHandler);
        setResponse(true);
        if (isInitialized()) {
          callback.setDone();
        }
        else {
          callback.setRejected();
        }
      }

      public AbstractQueryLanguageConsole.OutputHandler getOutputHandler() {
        return myOutputHandler;
      }

      @Override
      protected int handleDefaultOutput(final Element root) {
        resetCurrentResultNumber();
        return super.handleDefaultOutput(root);
      }
    };
  }

  public ActionCallback ensureInitialized() {
    final ProgramRunner runner = RunnerRegistry.getInstance().findRunnerById(DefaultRunExecutor.EXECUTOR_ID);
    if (runner != null) {
      final ActionCallback result = new ActionCallback();
      myProcessListener = createProcessListener(result);
      try {
        setResponse(WAITING);
        final DataContext dataContext = getSimpleContext(DefaultProgramRunner.CONTENT_TO_REUSE, myDescriptor,
                                                         getSimpleContext(ConsoleContextProvider.RUN_CONTEXT.getName(), myRunContext,
                                                                          DataManager.getInstance().getDataContext()));
        runner.execute(DefaultRunExecutor.getRunExecutorInstance(), new ExecutionEnvironment(myRunContext.getRunConfiguration(), dataContext));
      }
      catch (ExecutionException e) {
        myOutputHandler.error(null, e);
        setResponse(false);
        result.setRejected();
      }
      return result;
    }
    else {
      return new ActionCallback.Rejected();
    }
  }

  private void cleanup() {
    if (myTemporaryHibernateConfig != null) {
      myTemporaryHibernateConfig.delete();
      myTemporaryHibernateConfig = null;
    }
  }

  public void getGeneratedSql(final String hqlQuery) {
    ensureState(State.FACTORY);
    sendCommand("translateQuery " + hqlQuery);
  }

  public void executeHqlQuery(final String hqlQuery, final Map<String,String> parameters) {
    ensureState(State.FACTORY);
    sendCommand("prepareHql " + hqlQuery);
    sendCommand("executeQuery "+ AbstractQueryLanguageConsole.encodeMap(parameters));
  }

  public void executeSqlQuery(final String sqlQuery, final Map<String, String> parameters) {
    ensureState(State.FACTORY);
    sendCommand("prepareSql " + sqlQuery);
    sendCommand("executeQuery " + AbstractQueryLanguageConsole.encodeMap(parameters));
  }

  public void getGeneratedDdl() {
    ensureState(State.CONFIG);
    sendCommand("generateDDL");
  }

  private void sendCommand(@NonNls final String command) {
    try {
      final ProcessHandler processHandler = myProcessHandler;
      if (processHandler != null) {
        final OutputStream input = processHandler.getProcessInput();
        if (input != null) {
          input.write(StringUtil.escapeStringCharacters(command).getBytes());
          input.write(("\n").getBytes());
          input.flush();
        }
      }
    }
    catch (IOException e) {
      myOutputHandler.error(null, e);
    }
  }

  private void ensureState(final State requiredState) {
    while (myState.compareTo(requiredState) < 0) {
      if (myState == State.INIT) {
        final Pair<File, String> pair = getHibernateConfigAndFactoryClass();
        if (pair == null) {
          throw new IllegalStateException(HibernateMessages.message("hqlconsole.unit.not.found"));
        }
        final String cfgClass = myRunContext.getRunConfiguration().USER_CFG_CLASS;
        sendCommand("initUserConfigurator "+ (StringUtil.isEmpty(cfgClass) ? "" : cfgClass));
        sendCommand("setConfigurationClass "+pair.second);
        sendCommand("initConfiguration "+ pair.first);
        myState = State.CONFIG;
      }
      else if (myState == State.CONFIG) {
        sendCommand("initSessionFactory");
        myState = State.FACTORY;
      }
    }
  }

  @Nullable
  private Pair<File, String> getHibernateConfigAndFactoryClass() {
    return ApplicationManager.getApplication().runReadAction(new Computable<Pair<File, String>>() {
      @Nullable
      public Pair<File, String> compute() {
        final PersistencePackage unit = myRunContext.getPersistenceUnit();
        if (unit == null) {
          return null;
        }
        else if (unit instanceof SessionFactory) {
          final PsiFile containingFile = unit.getContainingFile();
          assert containingFile != null;
          return Pair.create(VfsUtil.virtualToIoFile(containingFile.getVirtualFile()), getHibernateConfigurationClass());
        }
        else {
          myTemporaryHibernateConfig = createTemporaryHibernateConfig(myRunContext.getModule(), unit);
          return Pair.create(myTemporaryHibernateConfig, getHibernateConfigurationClass());
        }
      }
    });
  }

  public void dispose() {
    cleanup();
    myDescriptor = null;
    setResponse(false);
  }

  private String getHibernateConfigurationClass() {
    final PersistenceMappings mappings = myRunContext.getFacet().getEntityMappings(myRunContext.getPersistenceUnit());
    for (PersistenceMappings persistenceMappings : ModelMergerUtil.getFilteredImplementations(mappings)) {
      if (persistenceMappings instanceof JamElement) {
        if (!persistenceMappings.getModelHelper().getPersistentEntities().isEmpty()) {
          return "org.hibernate.cfg.AnnotationConfiguration";
        }
      }
    }
    return "org.hibernate.cfg.Configuration";
  }

  private File createTemporaryHibernateConfig(final Module module, final PersistencePackage unit) {
    @NonNls final String fileName = module.getName().replace(' ', '_') + "-hibernate-" + System.currentTimeMillis() + ".cfg.xml";
    final FileTemplate template = FileTemplateManager.getInstance()
        .getJ2eeTemplate(HibernateDescriptorsConstants.HIBERNATE_CONFIGURATION_META_DATA.getDefaultVersion().getTemplateName());
    final XmlFile xmlFile = (XmlFile)PsiFileFactory.getInstance(module.getProject()).createFileFromText(fileName, StdFileTypes.XML, template.getText());

    final DomFileElement<HibernateConfiguration> fileElement =
        DomManager.getDomManager(module.getProject()).getFileElement(xmlFile, HibernateConfiguration.class);
    assert fileElement != null;
    final SessionFactory sf = fileElement.getRootElement().getSessionFactory();
    for (GenericValue<PsiPackage> value : unit.getModelHelper().getPackages()) {
      sf.addMapping().getPackage().setStringValue(value.getStringValue());
    }
    for (GenericValue<PsiFile> value : unit.getModelHelper().getJarFiles()) {
      sf.addMapping().getJar().setStringValue(value.getStringValue());
    }
    final THashSet<String> classesNames = new THashSet<String>();
    final List<? extends GenericValue<PsiClass>> classes = unit.getModelHelper().getClasses();
    for (GenericValue<PsiClass> value : classes) {
      ContainerUtil.addIfNotNull(value.getStringValue(), classesNames);
    }
    if (classes.isEmpty()) {
      final JamEntityMappings jamMappings = ModelMergerUtil.getImplementation(
              myRunContext.getFacet().getEntityMappings(myRunContext.getPersistenceUnit()), JamEntityMappings.class);
      if (jamMappings != null) {
        for (Entity entity : jamMappings.getEntities()) {
          ContainerUtil.addIfNotNull(entity.getClazz().getStringValue(), classesNames);
        }
      }
    }
    for (String name : classesNames) {
      sf.addMapping().getClazz().setStringValue(name);
    }
    final ProjectFileIndex index = ProjectRootManager.getInstance(module.getProject()).getFileIndex();
    for (GenericValue<PersistenceMappings> value : unit.getModelHelper().getMappingFiles(PersistenceMappings.class)) {
      final PersistenceMappings mappings = value.getValue();
      if (mappings != null) {
        final PsiFile containingFile = mappings.getContainingFile();
        assert containingFile != null;
        final VirtualFile virtualFile = containingFile.getVirtualFile();
        final VirtualFile sourceRoot = index.getSourceRootForFile(virtualFile);
        if (sourceRoot != null) {
          sf.addMapping().getResource().setStringValue(VfsUtil.getRelativePath(virtualFile, sourceRoot, '/'));
        }
        else {
          // this will not work for orm.xml due to the bug in hibernate SAX parser configuration for "file" elements
          sf.addMapping().getFile().setStringValue(VfsUtil.virtualToIoFile(containingFile.getVirtualFile()).getAbsolutePath());
        }
      }
    }
    final Properties properties = unit.getModelHelper().getPersistenceUnitProperties();
    for (Object key : properties.keySet()) {
      final String name = (String)key;
      HibernateUtil.setSessionFactoryProperty(sf, name, properties.getProperty(name));
    }
    if (!properties.containsKey(HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.URL))) {
      final String dataSourceId = myRunContext.getFacet().getDataSourceId(myRunContext.getPersistenceUnit());
      if (dataSourceId != null) {
        final DataSource dataSource = DataSourceManager.getInstance(myRunContext.getModule().getProject()).getDataSourceByID(dataSourceId);
        if (dataSource instanceof LocalDataSource) {
          final LocalDataSource localDataSource = (LocalDataSource)dataSource;

          HibernateUtil.setSessionFactoryProperty(sf, HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.DRIVER), localDataSource.getDriverClass());
          HibernateUtil.setSessionFactoryProperty(sf, HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.URL), localDataSource.getUrl());
          HibernateUtil.setSessionFactoryProperty(sf, HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.USER), localDataSource.getUsername());
          HibernateUtil.setSessionFactoryProperty(sf, HibernateUtil.getFullPropertyName(HibernatePropertiesConstants.PASS), localDataSource.getPassword());
        }
      }
    }
    try {
      CodeStyleManager.getInstance(module.getProject()).reformat(xmlFile);
    }
    catch (IncorrectOperationException e) {
      // nothing
    }
    final File path = CompilerPaths.getGeneratedDataDirectory(module.getProject());
    final File file = new File(path, fileName);
    try {
      file.getParentFile().mkdirs();
      FileUtil.writeToFile(file, xmlFile.getText().getBytes());
      return file;
    }
    catch (IOException e) {
      myOutputHandler.error(null, e);
    }
    return file;
  }

  private void setResponse(final Object response) {
    synchronized (myResponse) {
      myResponse.set(response);
      myResponse.notifyAll();
    }
  }

}
