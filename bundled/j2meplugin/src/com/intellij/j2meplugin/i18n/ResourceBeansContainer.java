package com.intellij.j2meplugin.i18n;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.psi.PsiClass;
import org.jdom.Element;

public class ResourceBeansContainer implements PersistentStateComponent<Element> {
  private final ResourceBundlesBean myBean;
  private static final Logger LOG = Logger.getInstance("#" + ResourceBeansContainer.class.getName());

  public static ResourceBeansContainer getInstance(Project project) {
    return ServiceManager.getService(project, ResourceBeansContainer.class);
  }

  public ResourceBeansContainer(final Project project) {
    myBean = new ResourceBundlesBean(project);
  }

  public Element getState() {
    final Element element = new Element("state");
    try {
      myBean.writeExternal(element);
    }
    catch (WriteExternalException e) {
      LOG.error(e);
    }
    return element;
  }

  public void loadState(Element state) {
    try {
      myBean.readExternal(state);
    }
    catch (InvalidDataException e) {
      LOG.error(e);
    }
  }

  public void registerResourceBundle(final PsiClass aClass) {
    myBean.registerResourceBundle(aClass);
  }

  public PsiClass getResourceBundle() {
    return myBean.getResourceBundle();
  }
}
