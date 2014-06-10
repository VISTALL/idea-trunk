/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.struts2;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import org.jetbrains.annotations.NonNls;

/**
 * Provides "External resources" (DTDs).
 *
 * @author Dmitry Avdeev
 */
public class Struts2ResourceProvider implements StandardResourceProvider {

  @NonNls
  private static final String DTD_PATH = "/resources/dtds/";

  public void registerResources(final ResourceRegistrar registrar) {
    addDTDResource(StrutsConstants.STRUTS_2_0_DTD_URI,
                   StrutsConstants.STRUTS_2_0_DTD_ID,
                   "struts-2.0.dtd", registrar);

    addDTDResource(StrutsConstants.STRUTS_2_1_DTD_URI,
                   StrutsConstants.STRUTS_2_1_DTD_ID,
                   "struts-2.1.dtd", registrar);

    addDTDResource(StrutsConstants.STRUTS_2_1_7_DTD_URI,
                   StrutsConstants.STRUTS_2_1_7_DTD_ID,
                   "struts-2.1.7.dtd", registrar);


    addDTDResource(StrutsConstants.VALIDATOR_1_00_DTD_URI,
                   StrutsConstants.VALIDATOR_1_00_DTD_ID,
                   "xwork-validator-1.0.dtd", registrar);

    addDTDResource(StrutsConstants.VALIDATOR_1_02_DTD_URI,
                   StrutsConstants.VALIDATOR_1_02_DTD_ID,
                   "xwork-validator-1.0.2.dtd", registrar);

    addDTDResource(StrutsConstants.VALIDATOR_CONFIG_DTD_URI,
                   StrutsConstants.VALIDATOR_CONFIG_DTD_ID,
                   "xwork-validator-config-1.0.dtd", registrar);


    addDTDResource(StrutsConstants.TILES_2_0_DTD_URI_STRUTS,
                   StrutsConstants.TILES_2_0_DTD_ID,
                   "struts-tiles-config_2_0.dtd", registrar);

    addDTDResource(StrutsConstants.TILES_2_0_DTD_URI,
                   StrutsConstants.TILES_2_0_DTD_ID,
                   "tiles-config_2_0.dtd", registrar);

    addDTDResource(StrutsConstants.TILES_2_1_DTD_URI,
                   StrutsConstants.TILES_2_1_DTD_ID,
                   "tiles-config_2_1.dtd", registrar);
  }

  /**
   * Adds a DTD resource from local DTD resource path.
   *
   * @param uri       Resource URI.
   * @param id        Resource ID.
   * @param localFile DTD filename.
   * @param registrar Resource registrar.
   */
  private static void addDTDResource(@NonNls final String uri,
                                     @NonNls final String id,
                                     @NonNls final String localFile,
                                     final ResourceRegistrar registrar) {
    registrar.addStdResource(uri, DTD_PATH + localFile, StrutsApplicationComponent.class);
    registrar.addStdResource(id, DTD_PATH + localFile, StrutsApplicationComponent.class);
  }

}