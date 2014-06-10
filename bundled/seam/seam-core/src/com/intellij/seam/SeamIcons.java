package com.intellij.seam;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface SeamIcons {
  Icon SEAM_ICON = IconLoader.getIcon("/resources/icons/seam.png");
  Icon SEAM_COMPONENT_ICON = IconLoader.getIcon("/resources/icons/seam.png");
  Icon SEAM_JAM_COMPONENT_ICON = IconLoader.getIcon("/resources/icons/seam.png");
  Icon SEAM_DOM_COMPONENT_ICON = IconLoader.getIcon("/resources/icons/seam.png");
  Icon SEAM_BEGIN_CONVERSATION_ICON = IconLoader.getIcon("/resources/icons/toEndConversation.png");
  Icon SEAM_END_CONVERSATION_ICON = IconLoader.getIcon("/resources/icons/toBeginConversation.png");
}
