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

public class AddIbatisAction extends AbstractFrameworkIntegrationAction {
  @NonNls private static final String IBATIS_STRING_ID = "ibatis";

  protected String[] getBeansClassNames() {
    return new String[]{"org.springframework.orm.ibatis.SqlMapClientFactoryBean"};
  }

   protected LibrariesInfo getLibrariesInfo(final Module module) {
    final LibraryInfo[] libraryInfos = LibrariesConfigurationManager.getInstance(module.getProject()).getLibraryInfos("ibatis");

    return new LibrariesInfo(libraryInfos, module, IBATIS_STRING_ID);
  }

  protected List<TemplateInfo> getTemplateInfos(final Module module) {
    List<TemplateInfo> infos = new LinkedList<TemplateInfo>();

    final TemplateSettings settings = TemplateSettings.getInstance();

    final TemplateInfo datasource = new TemplateInfo(module, settings.getTemplateById("datasource"),
                                                     SpringBundle.message("spring.patterns.data.access.data.source"), null);

    final TemplateInfo cf = new TemplateInfo(module, settings.getTemplateById("ibatis-client-factory"),
                                                     SpringBundle.message("spring.patterns.data.access.ibatis.client.factory"), null);


    infos.add(datasource);
    infos.add(cf);

    return infos;
  }

  protected String getDescription() {
    return SpringBundle.message("spring.patterns.ibatis");
  }

  @Nullable
  protected Icon getIcon() {
    return PatternIcons.IBATIS_ICON;
  }
}

