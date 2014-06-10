package org.jetbrains.plugins.scala.settings;

/**
 * @author ilyas
 */

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

import java.util.ArrayList;

@State(
    name = "ScalaApplicationSettings",
    storages = {
    @Storage(
        id = "scala_config",
        file = "$APP_CONFIG$/scala_config.xml"
    )}
)
public class ScalaApplicationSettings implements PersistentStateComponent<ScalaApplicationSettings> {

  public String DEFAULT_SCALA_LIB_NAME = null;
  public Boolean SPECIFY_TYPE_EXPLICITLY = null;
  public Boolean INTRODUCE_LOCAL_CREATE_VARIABLE = null;
  public Boolean SPECIFY_RETURN_TYPE_EXPLICITLY = null;
  public ArrayList<String> CONSOLE_HISTORY = new ArrayList<String>(20);

  public ScalaApplicationSettings getState() {
    return this;
  }

  public void loadState(ScalaApplicationSettings scalaApplicationSettings) {
    XmlSerializerUtil.copyBean(scalaApplicationSettings, this);
  }

  public static ScalaApplicationSettings getInstance() {
    return ServiceManager.getService(ScalaApplicationSettings.class);
  }

}
