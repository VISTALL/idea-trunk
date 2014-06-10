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

public class AddJdoAction extends AbstractFrameworkIntegrationAction {
  @NonNls private static final String JDO_STRING_ID = "jdo";

  protected String[] getBeansClassNames() {
    return new String[]{"org.springframework.orm.jdo.LocalPersistenceManagerFactoryBean",
      "org.jpox.PersistenceManagerFactoryImpl"};
  }

   protected LibrariesInfo getLibrariesInfo(final Module module) {
    final LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("jdo");

    return new LibrariesInfo(libraryInfos, module, JDO_STRING_ID);
  }

  protected List<TemplateInfo> getTemplateInfos(final Module module) {
    List<TemplateInfo> infos = new LinkedList<TemplateInfo>();

    final TemplateSettings settings = TemplateSettings.getInstance();

    final TemplateInfo datasource = new TemplateInfo(module, settings.getTemplateById("datasource"),
                                                     SpringBundle.message("spring.patterns.data.access.data.source"), null, false);

    final TemplateInfo jpm = new TemplateInfo(module, settings.getTemplateById("jdo-persistance-manager"),
                                                     SpringBundle.message("spring.patterns.data.access.jdo.persistence.manager"), null);

    final TemplateInfo jpox = new TemplateInfo(module, settings.getTemplateById("jpox-pmf"),
                                                     SpringBundle.message("spring.patterns.data.access.jdo.jpox.persistence.manager"), null, false);

    final TemplateInfo pmp = new TemplateInfo(module, settings.getTemplateById("jdo-persistance-manager-proxy"),
                                                         SpringBundle.message("spring.patterns.data.access.jdo.persistence.manager.proxy"),
                                                         null);
    final TemplateInfo transctionManager = new TemplateInfo(module, settings.getTemplateById("jdo-transaction-manager"), SpringBundle.message(
      "spring.patterns.data.access.jdo.transaction.manager"), null);

    infos.add(jpm);
    infos.add(datasource);
    infos.add(jpox);
    infos.add(pmp);
    infos.add(transctionManager);

    return infos;
  }

  protected String getDescription() {
    return SpringBundle.message("spring.patterns.jdo");
  }

  @Nullable
  protected Icon getIcon() {
    return PatternIcons.JDO_ICON;
  }
}
