/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.ruby.ruby.presentation;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.plugins.ruby.rails.RailsIcons;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 21.11.2006
 */
public class RubyProjectPresentationUtil {
    public static ItemPresentation getPresentation(final Project project) {
        return new PresentationData(project.getName(),
                                    project.getPresentableUrl(),
                                    RailsIcons.RAILS_PROJECT_VIEW,
                                    RailsIcons.RAILS_PROJECT_VIEW, null);
    }
}
