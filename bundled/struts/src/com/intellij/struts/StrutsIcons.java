/*
 * Copyright 2000-2007 JetBrains s.r.o.
 *
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

package com.intellij.struts;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;

/**
 * Contains all commonly used Icons and related constants.
 *
 * @author Yann Cebron
 */
public class StrutsIcons {

  private StrutsIcons() {
  }

  /**
   * Base path for all icons.
   */
  @NonNls private static final String ICON_BASE_PATH = "/com/intellij/struts/icons/";

  /**
   * Loads the icon with the given name from the default icon base path.
   *
   * @param iconName Relative file name of the icon to load.
   * @return Icon.
   */
  public static Icon getIcon(@NonNls final String iconName) {
    return IconLoader.getIcon(ICON_BASE_PATH + iconName);
  }

  // Struts ------------------------------------------------------------------------------------------------------------

  public static final Icon ACTION_ICON = getIcon("ActionMapping.png");

  public static final Icon ACTION_SMALL_ICON = getIcon("ActionMapping_small.png");

  public static final Icon CONTROLLER_ICON = getIcon("Controller.png");

  public static final Icon EXCEPTION_ICON = getIcon("GlobalException.png");

  public final static Icon FORMBEAN_ICON = getIcon("FormBean.png");

  public static final Icon FORMBEAN_SMALL_ICON = getIcon("FormBean_small.png");

  public static final Icon FORWARD_ICON = getIcon("Forward.png");

  public static final Icon GLOBAL_FORWARD_ICON = getIcon("GlobalForwards.png");

  public static final Icon PLUGIN_ICON = getIcon("PlugIn.png");

  // Tiles -------------------------------------------------------------------------------------------------------------

  public final static Icon TILE_ICON = getIcon("tiles/Tile.png");

  public static final Icon TILES_SMALL_ICON = getIcon("tiles/Tile_small.png");

  // Validator ---------------------------------------------------------------------------------------------------------

  public final static Icon VALIDATOR_ICON = getIcon("validator/Validator.png");
  public static final Icon VALIDATOR_SMALL_ICON = getIcon("validator/Validator_small.png");


  /**
   * Horizontal overlay offset for small icons.
   */
  public static final int OVERLAY_ICON_OFFSET_X = 0;

  /**
   * Vertical overlay offset for small icons.
   */
  public static final int OVERLAY_ICON_OFFSET_Y = 6;


}