package com.intellij.spring.model.jam.javaConfig;

import com.intellij.jam.JamElement;
import com.intellij.jam.JamPomTarget;
import com.intellij.pom.PomDescriptionProvider;
import com.intellij.pom.PomTarget;
import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.usageView.UsageViewNodeTextLocation;
import com.intellij.usageView.UsageViewTypeLocation;
import com.intellij.spring.SpringBundle;
import org.jetbrains.annotations.NotNull;

public class SpringJamPomTargetDescriptionProvider extends PomDescriptionProvider {

  public String getElementDescription(@NotNull PomTarget element, @NotNull ElementDescriptionLocation location) {
    if (element instanceof JamPomTarget) {
      JamPomTarget target = (JamPomTarget)element;
      JamElement jamElement = target.getJamElement();
      if (jamElement instanceof JavaSpringJavaBean) {
        if (location == UsageViewTypeLocation.INSTANCE) {
          return SpringBundle.message("spring.bean");
        }
        if (location == UsageViewNodeTextLocation.INSTANCE) {
          return SpringBundle.message("spring.bean") +" "+ target.getName();
        }
      }
    }
    return null;
  }
}
