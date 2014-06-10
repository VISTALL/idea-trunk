package org.jetbrains.plugins.grails.plugins;

import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

/**
 * Is used to remove <GrailsPluginsStorage> from iml file where the plugins cache was stored in IDEA 8. To remove after 9.x release
 *
 * @author peter
 */
@State(name = "GrailsPluginsStorage", storages = @Storage(id = "other", file = "$MODULE_FILE$"))
public class GrailsPluginManagerDataRemover implements ModuleComponent, PersistentStateComponent<Object> {
  public void projectOpened() {
  }

  public void projectClosed() {
  }

  public void moduleAdded() {
  }

  @NotNull
  public String getComponentName() {
    return "GrailsPluginsStorage";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public Object getState() {
    return null;
  }

  public void loadState(Object state) {
  }
}
