package org.jetbrains.idea.svn.config;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnConfiguration;
import org.jetbrains.idea.svn.SvnServerFileManager;
import org.jetbrains.idea.svn.SvnVcs;
import org.tmatesoft.svn.core.SVNException;

import javax.swing.*;
import java.awt.*;

public class SvnConfigureProxiesDialog extends DialogWrapper implements ValidationListener, TestConnectionPerformer {
  private final SvnConfigureProxiesComponent mySystemTab;
  private final SvnConfigureProxiesComponent myUserTab;
  private JPanel myPanel;
  private JTabbedPane myTabbedPane;
  private final GroupsValidator myValidator;
  private final Project myProject;

  public SvnConfigureProxiesDialog(final Project project) {
    super(project, true);
    valid = true;
    myProject = project;
    
    setTitle(SvnBundle.message("dialog.title.edit.http.proxies.settings"));

    final Ref<SvnServerFileManager> systemManager = new Ref<SvnServerFileManager>();
    final Ref<SvnServerFileManager> userManager = new Ref<SvnServerFileManager>();

    SvnConfiguration.getInstance(project).getServerFilesManagers(systemManager, userManager);

    myValidator = new GroupsValidator(this);
    mySystemTab = new SvnConfigureProxiesComponent(systemManager.get(), myValidator, this);
    myUserTab = new SvnConfigureProxiesComponent(userManager.get(), myValidator, this);

    init();
    
    mySystemTab.reset();
    myUserTab.reset();
    myValidator.run();
  }

  public void onError(final String text, final JComponent component, final boolean forbidSave) {
    myTabbedPane.setSelectedComponent(component);
    String prefixString = "";
    for (int i = 0; i < myTabbedPane.getComponentCount(); i++) {
      final Component currentComponent = myTabbedPane.getComponentAt(i);
      // compare referencies - same objects
      if (currentComponent == component) {
        prefixString = myTabbedPane.getTitleAt(i) + ": ";
      }
    }
    setOKActionEnabled(! forbidSave);
    setInvalid(prefixString + text);
  }

  public void onSuccess() {
    if (isVisible()) {
      setOKActionEnabled(true);
      setInvalid(null);
    }
  }

  public boolean shouldCloseOnCross() {
    return true;
  }

  private boolean applyToTab(final SvnConfigureProxiesComponent component) {
    try {
      component.apply();
    } catch (ConfigurationException e) {
      myTabbedPane.setSelectedComponent(component.createComponent());
      setInvalid(e.getMessage());
      return false;
    }
    return true;
  }

  public void doCancelAction() {
    myValidator.stop();
    super.doCancelAction();
  }

  private boolean applyImpl() {
    if (! applyToTab(myUserTab)) {
      return false;
    }
    if (! applyToTab(mySystemTab)) {
      return false;
    }
    return true;
  }

  protected void doOKAction() {
    if (getOKAction().isEnabled()) {
      if(! applyImpl()) {
        return;
      }
      myValidator.stop();
      close(OK_EXIT_CODE);
    }
  }

  public void execute(final String url) {
    Messages.showInfoMessage(myProject, SvnBundle.message("dialog.edit.http.proxies.settings.test.connection.settings.will.be.stored.text"),
                             SvnBundle.message("dialog.edit.http.proxies.settings.test.connection.settings.will.be.stored.title"));
    if(! applyImpl()) {
      return;
    }
    try {
      SvnVcs.getInstance(myProject).createRepository(url).testConnection();
    } catch (SVNException exc) {
      Messages.showErrorDialog(myProject, exc.getMessage(), SvnBundle.message("dialog.edit.http.proxies.settings.test.connection.error.title"));
      return;
    }
    Messages.showInfoMessage(myProject, SvnBundle.message("dialog.edit.http.proxies.settings.test.connection.succes.text"),
                             SvnBundle.message("dialog.edit.http.proxies.settings.test.connection.succes.title"));
  }

  private boolean valid;

  public void setInvalid(final String text) {
    valid = (text == null) || ("".equals(text.trim()));
    setErrorText(text);
  }

  public boolean enabled() {
    return valid;
  }

  protected JComponent createCenterPanel() {
    myTabbedPane = new JTabbedPane();
    myTabbedPane.add(myUserTab.createComponent(), SvnBundle.message("dialog.edit.http.proxies.settings.tab.edit.user.file.title"));
    myTabbedPane.add(mySystemTab.createComponent(), SvnBundle.message("dialog.edit.http.proxies.settings.tab.edit.system.file.title"));
    myPanel.add(myTabbedPane, BorderLayout.NORTH);
    return myPanel;
  }
}
