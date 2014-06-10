/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
package jetbrains.communicator.idea.config;

import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.communicator.util.StringUtil;

/**
 * @author Kir
 */
public class OptionShowPopupOn_Message extends OptionExpandToolWindow1 {
  public OptionShowPopupOn_Message() {
    super(IdeaFlags.POPUP_ON_MESSAGE);
  }

  public void update(final AnActionEvent e) {
    super.update(e);
    e.getPresentation().setText(StringUtil.getMsg("show.popup.4.incoming.message"));
    e.getPresentation().setDescription(StringUtil.getMsg("show.popup.4.incoming.message.description"));
  }
}
