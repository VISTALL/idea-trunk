package com.intellij.spring.model.actions.patterns.frameworks;

import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.module.Module;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.actions.patterns.PatternIcons;
import com.intellij.spring.model.actions.patterns.frameworks.ui.LibrariesInfo;
import com.intellij.spring.model.actions.patterns.frameworks.ui.TemplateInfo;
import com.intellij.spring.model.actions.patterns.frameworks.util.LibrariesConfigurationManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class AddJdkTimerAction extends AbstractFrameworkIntegrationAction {
  @NonNls private static final String JDK_TIMER_STRING_ID = "jdk-timer";

   protected LibrariesInfo getLibrariesInfo(final Module module) {
    final LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("jdk-timer");

    return new LibrariesInfo(libraryInfos, module, JDK_TIMER_STRING_ID);
  }

  protected String[] getBeansClassNames() {
    return new String[]{"org.springframework.scheduling.timer.ScheduledTimerTask",
      "org.springframework.scheduling.timer.TimerFactoryBean"};
  }

  protected List<TemplateInfo> getTemplateInfos(final Module module) {
    List<TemplateInfo> infos = new LinkedList<TemplateInfo>();


    final TemplateSettings settings = TemplateSettings.getInstance();

    final TemplateInfo sf = new TemplateInfo(module, settings.getTemplateById("jdk-scheduled-timer-task"),
                                                     SpringBundle.message("spring.patterns.integration.jdk.scheduled.timer.task"), null);

    final TemplateInfo tfb = new TemplateInfo(module, settings.getTemplateById("jdk-timer-factory-bean"),
                                                         SpringBundle.message("spring.patterns.integration.jdk.timer.factory.bean"),
                                                         null);

    infos.add(sf);
    infos.add(tfb);

    return infos;
  }

  protected String getDescription() {
    return SpringBundle.message("spring.patterns.jdk.timer");
  }

  @Nullable
  protected Icon getIcon() {
    return PatternIcons.JDK_ICON;
  }
}

