/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.dom.struts;

import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.dom.StrutsDomConstants;
import com.intellij.struts2.dom.params.Param;
import com.intellij.struts2.dom.params.ParamImpl;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.constant.Constant;
import com.intellij.struts2.dom.struts.constant.ConstantImpl;
import com.intellij.struts2.dom.struts.impl.*;
import com.intellij.struts2.dom.struts.strutspackage.GlobalResult;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorRef;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.xml.DomFileDescription;

/**
 * <code>struts.xml</code> DOM-Model files.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2DomFileDescription extends DomFileDescription<StrutsRoot> {

  public Struts2DomFileDescription() {
    super(StrutsRoot.class, StrutsRoot.TAG_NAME);
  }

  protected void initializeFileDescription() {
    registerNamespacePolicy(StrutsDomConstants.STRUTS_NAMESPACE_KEY,
                            StrutsConstants.STRUTS_DTDS);

    registerImplementation(Action.class, ActionImpl.class);
    registerImplementation(Constant.class, ConstantImpl.class);
    registerImplementation(GlobalResult.class, GlobalResultImpl.class);
    registerImplementation(InterceptorRef.class, InterceptorRefImpl.class);
    registerImplementation(Param.class, ParamImpl.class);
    registerImplementation(Result.class, ResultImpl.class);
    registerImplementation(StrutsPackage.class, StrutsPackageImpl.class);
  }

}