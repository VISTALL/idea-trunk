package com.intellij.spring.model.actions.patterns.frameworks.util;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.containers.HashMap;
import com.intellij.util.xmlb.XmlSerializer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class StandardBeansDocLinksManager implements Disposable {
  private final Map<String, Pair<String, String>> myDocLinks = new HashMap<String, Pair<String, String>>();


  @NotNull
  public static StandardBeansDocLinksManager getInstance(@NotNull Project project) {
    synchronized (project) {
      return ServiceManager.getService(project, StandardBeansDocLinksManager.class);
    }
  }

  @NonNls private static final String DOC_LINKS_RESOURCE_XML = "/resources/frameworks/docLinks.xml";

  public StandardBeansDocLinksManager() {
    final StandardBeansDocLinks docLinks =
      XmlSerializer.deserialize(StandardBeansDocLinksManager.class.getResource(DOC_LINKS_RESOURCE_XML), StandardBeansDocLinks.class);

    assert docLinks != null;
    assert docLinks.getDocLinks() != null;

    for (StandardBeansDocLink docLink : docLinks.getDocLinks()) {
      myDocLinks.put(docLink.getBeanId(), new Pair<String, String>(docLink.getApiLink(), docLink.getReferenceLink()));
    }
  }

  @Nullable
  public String getApiLink(final String beanId) {
    final Pair<String, String> pair = myDocLinks.get(beanId);

    return pair == null ? null : pair.getFirst();
  }

  @Nullable
  public String getReferenceLink(final String beanId) {
    final Pair<String, String> pair = myDocLinks.get(beanId);

    return pair == null ? null : pair.getSecond();
  }

  public void dispose() {

  }
}
