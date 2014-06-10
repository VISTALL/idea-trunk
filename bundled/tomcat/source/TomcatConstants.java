/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.tomcat;

import com.intellij.util.descriptors.ConfigFileVersion;
import com.intellij.util.descriptors.ConfigFileMetaData;
import org.jdom.Comment;
import org.jetbrains.annotations.NonNls;

public interface TomcatConstants {
  @NonNls String CATALINA_CONFIG_DIRECTORY_NAME = "conf";
  @NonNls String CATALINA_WORK_DIRECTORY_NAME = "work";
  @NonNls String SCRATCHDIR_NAME = "_scratchdir";
  @NonNls String SERVER_XML = "server.xml";
  @NonNls String WEB_XML = "web.xml";
  @NonNls String CATALINA_BIN_DIRECTORY_NAME = "bin";
  @NonNls String CATALINA_COMMON_DIRECTORY_NAME = "common";
  @NonNls String CATALINA_LIB_DIRECTORY_NAME = "lib";
  Comment CONTEXT_COMMENT = new Comment(
    TomcatBundle.message("comment.text.context.generated.by.idea")
  );
  @NonNls String CONTEXT_XML_TEMPLATE_FILE_NAME = "context.xml";


  @NonNls String TOMCAT_VERSION_5x = "5.x";
  ConfigFileVersion[] DESCRIPTOR_VERSIONS = {
    new ConfigFileVersion(TOMCAT_VERSION_5x, CONTEXT_XML_TEMPLATE_FILE_NAME)
  };
  ConfigFileMetaData CONTEXT_XML_META_DATA =
    new ConfigFileMetaData(TomcatBundle.message("tomcat.deployment.descriptor.title"), "context.xml", "META-INF", DESCRIPTOR_VERSIONS, null,
                           true, true, true);

}