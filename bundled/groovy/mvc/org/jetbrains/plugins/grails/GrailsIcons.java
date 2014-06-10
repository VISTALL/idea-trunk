/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.grails;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

public interface GrailsIcons {
  @NonNls String GRAILS_ICONS_PATH = "/images/grails/";

  Icon GRAILS_MODULE_ICON = IconLoader.findIcon(GRAILS_ICONS_PATH + "grails_module.png");

  Icon GSP_FILE_TYPE = IconLoader.findIcon(GRAILS_ICONS_PATH + "gsp_logo.png");

  Icon GRAILS_ICON = IconLoader.findIcon(GRAILS_ICONS_PATH + "grails.png");
  Icon CONTROLLER = IconLoader.findIcon(GRAILS_ICONS_PATH + "controller.png");
  Icon SERVICE = IconLoader.findIcon(GRAILS_ICONS_PATH + "service.png");
  Icon DOMAIN_CLASS = IconLoader.findIcon(GRAILS_ICONS_PATH + "domain_class.png");
  Icon GRAILS_APP = IconLoader.findIcon(GRAILS_ICONS_PATH + "grails_app.png");
  Icon TAG_LIB = IconLoader.findIcon(GRAILS_ICONS_PATH + "taglib.png");

  Icon GRAILS_SDK = IconLoader.findIcon(GRAILS_ICONS_PATH + "grails_sdk.png");

  Icon GRAILS_PLUGIN = IconLoader.findIcon(GRAILS_ICONS_PATH + "grails_plugin.png");
  Icon GRAILS_TEST_RUN_CONFIGURATION = IconLoader.findIcon(GRAILS_ICONS_PATH + "grails_test.png");

  Icon GRAILS_PLUGINS_REFRESH = IconLoader.findIcon(GRAILS_ICONS_PATH + "refresh.png");
  Icon GRAILS_PLUGIN_INFO_DOWNLOAD = IconLoader.findIcon(GRAILS_ICONS_PATH + "download.png");

  /************** Project view ************/

  Icon GRAILS_CONTROLLER_NODE = CONTROLLER;
  Icon GRAILS_DOMAIN_CLASS_NODE = DOMAIN_CLASS;
  Icon GRAILS_CONFIG_FOLDER_NODE = IconLoader.findIcon(GRAILS_ICONS_PATH + "projectView/config_folder_closed.png");
  Icon GRAILS_ACTION_NODE = IconLoader.findIcon(GRAILS_ICONS_PATH + "projectView/action_method.png");  
  Icon GRAILS_TEST_METHOD_NODE = GRAILS_TEST_RUN_CONFIGURATION;

  Icon GRAILS_CONTROLERS_FOLDER_NODES =  IconLoader.findIcon("/nodes/keymapTools.png");
  Icon GRAILS_DOMAIN_CLASSES_FOLDER_NODE = IconLoader.findIcon(GRAILS_ICONS_PATH + "projectView/modelesNode.png"); 

}
