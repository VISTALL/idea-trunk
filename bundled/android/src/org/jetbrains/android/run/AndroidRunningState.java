package org.jetbrains.android.run;

import com.android.ddmlib.*;
import com.android.prefs.AndroidLocation;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.internal.avd.AvdManager;
import com.intellij.CommonBundle;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import static com.intellij.execution.process.ProcessOutputTypes.STDERR;
import static com.intellij.execution.process.ProcessOutputTypes.STDOUT;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AvdsNotSupportedException;
import org.jetbrains.android.sdk.AndroidSdk15;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author coyote
 */
public abstract class AndroidRunningState implements RunProfileState, AndroidDebugBridge.IClientChangeListener {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.android.run.AndroidRunningState");

  public static final int WAITING_TIME = 5;

  private final String myPackageName;
  private String myTargetPackageName;
  private final AndroidFacet myFacet;
  private final String myCommandLine;
  private final AndroidApplicationLauncher myApplicationLauncher;

  private volatile String myTargetDeviceSerialNumber;
  private volatile String myAvdName;
  private volatile boolean myDebugMode;

  private DebugLauncher myDebugLauncher;

  private final ExecutionEnvironment myEnv;

  private boolean myStopped;
  private volatile ProcessHandler myProcessHandler;
  private final Object myLock = new Object();

  public void setDebugMode(boolean debugMode) {
    myDebugMode = debugMode;
  }

  public void setDebugLauncher(DebugLauncher debugLauncher) {
    myDebugLauncher = debugLauncher;
  }

  public boolean isDebugMode() {
    return myDebugMode;
  }

  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    myProcessHandler = new DefaultDebugProcessHandler();
    ConsoleView console;
    if (isDebugMode()) {
      Project project = myFacet.getModule().getProject();
      final TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
      console = builder.getConsole();
      if (console != null) {
        console.attachToProcess(myProcessHandler);
      }
    }
    else {
      console = attachConsole();
    }
    run();
    return new DefaultExecutionResult(console, myProcessHandler);
  }

  @NotNull
  protected abstract ConsoleView attachConsole() throws ExecutionException;

  @Nullable
  public RunnerSettings getRunnerSettings() {
    return myEnv.getRunnerSettings();
  }

  public ConfigurationPerRunnerSettings getConfigurationSettings() {
    return myEnv.getConfigurationSettings();
  }

  @Nullable
  public static String getOutputPackage(AndroidFacet facet) {
    VirtualFile compilerOutput = CompilerModuleExtension.getInstance(facet.getModule()).getCompilerOutputPath();
    if (compilerOutput == null) return null;
    return new File(compilerOutput.getPath(), facet.getModule().getName() + ".apk").getPath();
  }

  public boolean isStopped() {
    return myStopped;
  }

  public Object getRunningLock() {
    return myLock;
  }

  public AndroidFacet getAndroidFacet() {
    return myFacet;
  }

  public String getPackageName() {
    return myPackageName;
  }

  public Module getModule() {
    return myFacet.getModule();
  }

  public AndroidFacet getFacet() {
    return myFacet;
  }

  public static class MyReceiver extends MultiLineReceiver {
    static final Pattern FAILURE = Pattern.compile("Failure\\s+\\[(.*)\\]");
    static final Pattern TYPED_ERROR = Pattern.compile("Error\\s+[Tt]ype\\s+(\\d+).*");
    static final String ERROR_PREFIX = "Error";

    static final int NO_ERROR = -2;
    static final int UNTYPED_ERROR = -1;

    private int errorType = NO_ERROR;
    private String failureMessage = null;
    private final StringBuilder output = new StringBuilder();

    public void processNewLines(String[] lines) {
      for (String line : lines) {
        if (line.length() > 0) {
          Matcher failureMatcher = FAILURE.matcher(line);
          if (failureMatcher.matches()) {
            failureMessage = failureMatcher.group(1);
          }
          Matcher errorMatcher = TYPED_ERROR.matcher(line);
          if (errorMatcher.matches()) {
            errorType = Integer.parseInt(errorMatcher.group(1));
          }
          else if (line.startsWith(ERROR_PREFIX) && errorType == NO_ERROR) {
            errorType = UNTYPED_ERROR;
          }
        }
        output.append(line).append('\n');
      }
    }

    public int getErrorType() {
      return errorType;
    }

    public boolean isCancelled() {
      return false;
    }

    public StringBuilder getOutput() {
      return output;
    }
  }

  public AndroidRunningState(@NotNull ExecutionEnvironment environment,
                             @NotNull AndroidFacet facet,
                             @Nullable String targetDeviceSerialNumber,
                             @Nullable String avdName,
                             @NotNull String commandLine,
                             @NotNull String packageName,
                             AndroidApplicationLauncher applicationLauncher) throws ExecutionException {
    myFacet = facet;
    myCommandLine = commandLine;
    myTargetDeviceSerialNumber = targetDeviceSerialNumber;
    myAvdName = avdName;
    myEnv = environment;
    myApplicationLauncher = applicationLauncher;
    /*final Manifest manifest = facet.getManifest();
    if (manifest == null) {
      throw new ExecutionException("Can't start application");
    }*/
    myPackageName = packageName;
    myTargetPackageName = packageName;
  }

  public void setTargetPackageName(String targetPackageName) {
    myTargetPackageName = targetPackageName;
  }

  private void chooseDeviceAutomaticaly() {
    AndroidDebugBridge bridge = myFacet.getDebugBridge();
    if (bridge == null) return;
    IDevice[] devices = bridge.getDevices();
    for (IDevice device : devices) {
      if (isMyDevice(device)) {
        myTargetDeviceSerialNumber = device.getSerialNumber();
      }
    }
  }

  private void chooseAvd() {
    IAndroidTarget buildTarget = myFacet.getConfiguration().getAndroidTarget();
    assert buildTarget != null;
    AvdManager.AvdInfo[] avds = myFacet.getValidCompatibleAvds();
    if (avds.length > 0) {
      myAvdName = avds[0].getName();
    }
    else {
      Project project = myFacet.getModule().getProject();
      AvdManager manager = null;
      try {
        manager = myFacet.getAvdManager();
      }
      catch (AvdsNotSupportedException e) {
        // can't be
        LOG.error(e);
      }
      catch (AndroidLocation.AndroidLocationException e) {
        Messages.showErrorDialog(project, e.getMessage(), CommonBundle.getErrorTitle());
        return;
      }
      CreateAvdDialog dialog = new CreateAvdDialog(project, myFacet, manager, true, true);
      dialog.show();
      if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
        AvdManager.AvdInfo createdAvd = dialog.getCreatedAvd();
        if (createdAvd != null) {
          myAvdName = createdAvd.getName();
        }
      }
    }
  }

  private void run() throws ExecutionException {
    getProcessHandler().notifyTextAvailable("Waiting for device.\n", STDOUT);
    if (myTargetDeviceSerialNumber == null) {
      chooseDeviceAutomaticaly();
      if (myTargetDeviceSerialNumber == null) {
        if (isAndroidSdk15()) {
          if (myAvdName == null) {
            chooseAvd();
          }
          if (myAvdName != null) {
            myFacet.launchEmulator(myAvdName, myCommandLine, myProcessHandler);
          }
          else {
            getProcessHandler().destroyProcess();
          }
        }
        else {
          myFacet.launchEmulator(myAvdName, myCommandLine, myProcessHandler);
        }
      }
    }
    if (myDebugMode) {
      AndroidDebugBridge.addClientChangeListener(this);
    }
    final AndroidDebugBridge.IDeviceChangeListener deviceListener = prepareAndStartAppWhenDeviceIsOnline();
    getProcessHandler().addProcessListener(new ProcessAdapter() {
      @Override
      public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
        if (myDebugMode) {
          AndroidDebugBridge.removeClientChangeListener(AndroidRunningState.this);
        }
        if (deviceListener != null) {
          AndroidDebugBridge.removeDeviceChangeListener(deviceListener);
        }
        myStopped = true;
        synchronized (myLock) {
          myLock.notifyAll();
        }
      }
    });
  }

  private boolean isAndroidSdk15() {
    return myFacet.getConfiguration().getAndroidSdk() instanceof AndroidSdk15;
  }

  public void clientChanged(Client client, int changeMask) {
    IDevice device = client.getDevice();
    if (isMyDevice(device) && device.isOnline()) {
      if (myTargetDeviceSerialNumber == null) {
        myTargetDeviceSerialNumber = device.getSerialNumber();
      }
      ClientData data = client.getClientData();
      String description = data.getClientDescription();
      if (description != null && description.equals(myTargetPackageName)) {
        if (myApplicationLauncher.isReadyForDebugging(data)) {
          if (myDebugLauncher != null) {
            String port = Integer.toString(client.getDebuggerListenPort());
            myDebugLauncher.launchDebug(device, port);
          }
        }
      }
    }
  }

  private boolean isMyDevice(@NotNull IDevice device) {
    if (myTargetDeviceSerialNumber != null) {
      return device.getSerialNumber().equals(myTargetDeviceSerialNumber);
    }
    if (!isAndroidSdk15()) {
      return true;
    }
    String avdName = device.getAvdName();
    if (myAvdName != null) {
      return myAvdName.equals(avdName);
    }
    if (avdName == null) {
      // seems it is a real device
      return true;
    }
    AvdManager.AvdInfo avdInfo = getAvdByName(avdName);
    return avdInfo != null && myFacet.isCompatibleAvd(avdInfo);
  }

  @Nullable
  private AvdManager.AvdInfo getAvdByName(String avdName) {
    avdName = StringUtil.capitalize(avdName);
    AvdManager.AvdInfo result = null;
    for (AvdManager.AvdInfo info : myFacet.getAllAvds()) {
      String name = StringUtil.capitalize(info.getName());
      if (avdName.equals(name)) {
        result = info;
      }
    }
    return result;
  }

  @Nullable
  private IDevice getDeviceBySerialNumber(@NotNull String serialNumber) {
    AndroidDebugBridge bridge = myFacet.getDebugBridge();
    if (bridge == null) return null;
    IDevice[] devices = bridge.getDevices();
    for (IDevice device : devices) {
      if (device.getSerialNumber().equals(serialNumber)) {
        return device;
      }
    }
    return null;
  }

  @Nullable
  private AndroidDebugBridge.IDeviceChangeListener prepareAndStartAppWhenDeviceIsOnline() {
    if (myTargetDeviceSerialNumber != null) {
      IDevice targetDevice = getDeviceBySerialNumber(myTargetDeviceSerialNumber);
      if (targetDevice != null && targetDevice.isOnline()) {
        prepareAndStartAppInSeparateThread(targetDevice);
      }
      return null;
    }
    AndroidDebugBridge.IDeviceChangeListener deviceListener = new AndroidDebugBridge.IDeviceChangeListener() {
      boolean installed = false;

      public void deviceConnected(IDevice device) {
        if (isMyDevice(device)) {
          getProcessHandler().notifyTextAvailable("Device connected: " + device.getSerialNumber() + '\n', STDOUT);
        }
      }

      public void deviceDisconnected(IDevice device) {
        if (isMyDevice(device)) {
          getProcessHandler().notifyTextAvailable("Device disconnected: " + device.getSerialNumber() + "\n", STDOUT);
        }
      }

      public void deviceChanged(final IDevice device, int changeMask) {
        if (!installed && isMyDevice(device) && device.isOnline()) {
          if (myTargetDeviceSerialNumber == null) {
            myTargetDeviceSerialNumber = device.getSerialNumber();
          }
          getProcessHandler().notifyTextAvailable("Device is online.\n", STDOUT);
          installed = true;
          prepareAndStartAppInSeparateThread(device);
        }
      }
    };
    AndroidDebugBridge.addDeviceChangeListener(deviceListener);
    return deviceListener;
  }

  public synchronized void setProcessHandler(ProcessHandler processHandler) {
    myProcessHandler = processHandler;
  }

  public synchronized ProcessHandler getProcessHandler() {
    return myProcessHandler;
  }

  private void prepareAndStartAppInSeparateThread(final IDevice device) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        try {
          if (!prepareAndStartApp(device) && !myStopped) {
            getProcessHandler().destroyProcess();
          }
        }
        catch (IOException e) {
          getProcessHandler().notifyTextAvailable("IOException: " + e.getMessage(), STDERR);
          getProcessHandler().destroyProcess();
        }
      }
    });
    thread.start();
  }

  private boolean prepareAndStartApp(IDevice device) throws IOException {
    StringBuilder deviceMessageBuilder = new StringBuilder("Target device: ");
    deviceMessageBuilder.append(device.getSerialNumber());
    if (device.getAvdName() != null) {
      deviceMessageBuilder.append(" (").append(device.getAvdName()).append(')');
    }
    deviceMessageBuilder.append('\n');
    getProcessHandler().notifyTextAvailable(deviceMessageBuilder.toString(), STDOUT);
    String remotePath = "/data/local/tmp/" + myPackageName;
    String localPath = getOutputPackage(myFacet);
    if (!uploadApp(device, remotePath, localPath)) return false;
    if (!installApp(device, remotePath)) return false;
    return myApplicationLauncher.launch(this, device);
  }

  private boolean uploadApp(IDevice device, String remotePath, String localPath) throws IOException {
    if (myStopped) return false;
    getProcessHandler().notifyTextAvailable("Uploading file\n\tlocal path: " + localPath + "\n\tremote path: " + remotePath + '\n', STDOUT);
    SyncService service = device.getSyncService();
    if (service == null) {
      getProcessHandler().notifyTextAvailable("Can't upload file: device is not available.\n", STDERR);
      return false;
    }
    SyncService.SyncResult result = service.pushFile(localPath, remotePath, SyncService.getNullProgressMonitor());
    if (result.getCode() != SyncService.RESULT_OK) {
      getProcessHandler().notifyTextAvailable("Can't upload file: " + result.getMessage() + ".\n", STDERR);
      return false;
    }
    return true;
  }

  public void executeDeviceCommandAndWriteToConsole(IDevice device, String command, IShellOutputReceiver receiver) throws IOException {
    getProcessHandler().notifyTextAvailable("DEVICE SHELL COMMAND: " + command + "\n", STDOUT);
    device.executeShellCommand(command, receiver);
  }

  private static boolean isSuccess(MyReceiver receiver) {
    return receiver.errorType == MyReceiver.NO_ERROR && receiver.failureMessage == null;
  }

  private boolean installApp(IDevice device, String remotePath) {
    getProcessHandler().notifyTextAvailable("Installing application.\n", STDOUT);
    MyReceiver receiver = new MyReceiver();
    while (true) {
      if (myStopped) return false;
      try {
        executeDeviceCommandAndWriteToConsole(device, "pm install \"" + remotePath + "\"", receiver);
      }
      catch (IOException e) {
        getProcessHandler().notifyTextAvailable(e.getMessage() + '\n', STDERR);
        return false;
      }
      if (receiver.errorType != 1 && receiver.errorType != MyReceiver.UNTYPED_ERROR) {
        break;
      }
      getProcessHandler().notifyTextAvailable("Device is not ready. Waiting for " + WAITING_TIME + " sec.\n", STDOUT);
      synchronized (myLock) {
        try {
          myLock.wait(WAITING_TIME * 1000);
        }
        catch (InterruptedException e) {
        }
      }
      receiver = new MyReceiver();
    }
    if (receiver.failureMessage != null && receiver.failureMessage.equals("INSTALL_FAILED_ALREADY_EXISTS")) {
      if (myStopped) return false;
      receiver = new MyReceiver();
      getProcessHandler().notifyTextAvailable("Application is already installed. Reinstalling.\n", STDOUT);
      try {
        executeDeviceCommandAndWriteToConsole(device, "pm install -r \"" + remotePath + '\"', receiver);
      }
      catch (IOException e) {
        getProcessHandler().notifyTextAvailable(e.getMessage() + '\n', STDERR);
        return false;
      }
    }
    if (!isSuccess(receiver)) {
      getProcessHandler().notifyTextAvailable("Can't reinstall application. Installing from scratch.\n", STDOUT);
      try {
        executeDeviceCommandAndWriteToConsole(device, "pm uninstall \"" + remotePath + '\"', receiver);
        executeDeviceCommandAndWriteToConsole(device, "pm install \"" + remotePath + '\"', receiver);
      }
      catch (IOException e) {
        getProcessHandler().notifyTextAvailable(e.getMessage() + '\n', STDERR);
        return false;
      }
    }
    boolean success = isSuccess(receiver);
    getProcessHandler().notifyTextAvailable(receiver.output.toString(), success ? STDOUT : STDERR);
    return success;
  }
}
