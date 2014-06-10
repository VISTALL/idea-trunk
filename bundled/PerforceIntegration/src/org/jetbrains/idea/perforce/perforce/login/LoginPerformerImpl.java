package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.PasswordUtil;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.PerforceTimeoutException;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginPerformerImpl implements LoginPerformer {
  private final static Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.login.LoginPerformerImpl");
  @NonNls private static final String LOGGED_IN_MESSAGE = "logged in";
  @NonNls private final static String CONNECT_FAILED = "Connect to server failed; check $P4PORT.";
  @NonNls private final String NOT_LOGGED = "Perforce password (P4PASSWD) invalid or unset.";

  private final P4Connection myConnection;
  private PerforceSettings mySettings;
  private final static Pattern ourAuthenticatedByPasswordPattern = Pattern.compile("User * was authenticated by password not ticket.");
  private final static Pattern ourTicketExpiresPattern = Pattern.compile("User * ticket expires in (d*) hours (d+) minutes.");
  private PerforceConnectionManager myConnectionManager;
  // used for single connection case
  private String myRecentCredentials;

  public LoginPerformerImpl(final Project project, final P4Connection connection) {
    mySettings = PerforceSettings.getSettings(project);
    myConnectionManager = PerforceConnectionManager.getInstance(project);
    myConnection = connection;
  }

  public LoginState getLoginState() {
    try {
      final ExecResult result = myConnection.runP4CommandLine(mySettings, new String[]{"login", "-s"}, null);
      if (result.getExitCode() != 0) {
        final String stdErr = result.getStderr();
        if (stdErr.contains(CONNECT_FAILED)) {
          return new LoginState(false, -1, stdErr);
        }
        return new LoginState(false, -1, null);
      }

      final String stdOut = result.getStdout();
      return tryParseTicketExpiresTime(stdOut);
    } catch (VcsException e) {
      if (e.getCause() instanceof PerforceTimeoutException) {
        return new LoginState(false, -1, e.getMessage());
      }
      return new LoginState(false, -1, null);
    }
  }

  private LoginState tryParseTicketExpiresTime(String stdOut) {
    final Matcher matcher = ourTicketExpiresPattern.matcher(stdOut);
    if (matcher.matches()) {
      final String hours = matcher.group(2);
      final String minutes = matcher.group(3);
      if (hours != null && minutes != null) {
        try {
          final long hoursInt = Integer.parseInt(hours);
          final int minutesInt = Integer.parseInt(minutes);
          return new LoginState(true, ((hoursInt * 60) + minutesInt) * 60 * 1000, null);
        } catch (NumberFormatException e) {
          //
        }
      }
    }
    return new LoginState(true, -1, null);
  }

  public boolean isSilentLoginAllowed() {
    return mySettings.LOGIN_SILENTLY;
  }

  public boolean isCredentialsChanged() {
    if (! mySettings.useP4CONFIG) return true;
    if (! Comparing.equal(mySettings.getPasswd(), myRecentCredentials)) {
      myRecentCredentials = mySettings.getPasswd();
      return true;
    }
    return false;
  }

  public boolean isSingleConnection() {
    return ! mySettings.useP4CONFIG;
  }

  public LoginState login(final String password) {
    try {
      final StringBuffer data = new StringBuffer();
      data.append(password);
      final ExecResult loginResult = myConnection.runP4CommandLine(mySettings, new String[]{"login"}, data);
      if (loginResult.getStderr().length() > 0 || !loginResult.getStdout().contains(LOGGED_IN_MESSAGE)) {
        final String stdErr = loginResult.getStderr();
        LOG.debug("Login failed, err: " + stdErr);
        if (stdErr.contains(CONNECT_FAILED)) {
          return new LoginState(false, -1, stdErr);
        }
        return new LoginState(false, -1, null);
      }
      return new LoginState(true, -1, null);
    } catch (VcsException e) {
      if (e.getCause() instanceof PerforceTimeoutException) {
        return new LoginState(false, -1, e.getMessage());
      }
      return new LoginState(false, -1, null);
    } finally {
      myConnectionManager.refreshConnections(mySettings);
    }
  }

  public LoginState loginWithStoredPassword() {
    if (! mySettings.useP4CONFIG) {
    return login(PasswordUtil.decodePassword(mySettings.passwd));
    }
    return new LoginState(false, -1, null);
  }
}
