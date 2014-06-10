package com.intellij.spring.usages;

import com.intellij.usages.impl.FileStructureGroupRuleProvider;
import com.intellij.usages.rules.UsageGroupingRule;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public class SpringBeanGroupRuleProvider implements FileStructureGroupRuleProvider {

  @Nullable
  public UsageGroupingRule getUsageGroupingRule(final Project project) {
    return new SpringBeansGroupingRule();  
  }
}
