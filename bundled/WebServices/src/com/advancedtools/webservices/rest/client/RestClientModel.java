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

package com.advancedtools.webservices.rest.client;

import com.intellij.openapi.project.Project;

import java.util.Set;

/**
 * @author Konstantin Bulenkov
 */
public interface RestClientModel {
  //Header
  String getHeaderName(int ind);
  String getHeaderValue(int ind);
  int getHeaderSize();
  // Use it for add or set header parameter's record. Remember, name is unique key
  void setHeader(String name, String value);
  void setResponseHeader(String header);

  //Parameters
  String getParameterName(int ind);
  String getParameterValue(int ind);
  int getParametersSize();
  // Use it for add or set parameter's record. Remember, name is unique key
  void setParameter(String name, String value);
  boolean isParametersEnabled();

  //URL
  String getURL();
  String getURLBase();
  void setURLTemplates(Set<String> templates);

  //Response
  String getResponse();
  void setResponse(String response);
  void appendToResponse(final String text);  

  //HttpMethod
  HTTPMethod getHttpMethod();

  //update
  void updateModel(Project project);

  void setStatus(String status);
}
