/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * JirasoapserviceV2SoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.atlassian.theplugin.jira.api.soap.axis;

public class JirasoapserviceV2SoapBindingStub extends org.apache.axis.client.Stub implements com.atlassian.theplugin.jira.api.soap.axis.JiraSoapService {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[93];
        _initOperationDesc1();
        _initOperationDesc2();
        _initOperationDesc3();
        _initOperationDesc4();
        _initOperationDesc5();
        _initOperationDesc6();
        _initOperationDesc7();
        _initOperationDesc8();
        _initOperationDesc9();
        _initOperationDesc10();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getComment");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"), long.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteComment"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteComment.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getCommentReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteUser"), com.atlassian.theplugin.jira.api.soap.axis.RemoteUser.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteGroup"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "createGroupReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getUser");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteUser"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteUser.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getUserReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteGroup"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getGroupReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("login");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "loginReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getServerInfo");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteServerInfo"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteServerInfo.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getServerInfoReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("logout");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "logoutReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getIssue");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteIssue"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getIssueReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getComponents");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteComponent"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteComponent[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getComponentsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createUser");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in4"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteUser"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteUser.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "createUserReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createIssue");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteIssue"), com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteIssue"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "createIssueReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getAvailableActions");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteNamedObject"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteNamedObject[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getAvailableActionsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProjects");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteProject"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getProjectsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("updateIssue");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteFieldValue"), com.atlassian.theplugin.jira.api.soap.axis.RemoteFieldValue[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteIssue"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "updateIssueReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getConfiguration");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteConfiguration"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteConfiguration.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getConfigurationReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("updateProject");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "updateProjectReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProjectByKey");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getProjectByKeyReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getPriorities");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemotePriority"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemotePriority[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getPrioritiesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getResolutions");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteResolution"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteResolution[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getResolutionsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getIssueTypes");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteIssueType"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getIssueTypesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[19] = oper;

    }

    private static void _initOperationDesc3(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getStatuses");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteStatus"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteStatus[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getStatusesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[20] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getSubTaskIssueTypes");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteIssueType"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getSubTaskIssueTypesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[21] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProjectRoles");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteProjectRole"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getProjectRolesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[22] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProjectRole");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"), long.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getProjectRoleReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[23] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProjectRoleActors");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRoleActors"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRoleActors.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getProjectRoleActorsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[24] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getDefaultRoleActors");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteRoleActors"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteRoleActors.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getDefaultRoleActorsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[25] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeAllRoleActorsByNameAndType");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[26] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeAllRoleActorsByProject");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[27] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteProjectRole");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[28] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("updateProjectRole");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[29] = oper;

    }

    private static void _initOperationDesc4(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createProjectRole");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "createProjectRoleReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[30] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("isProjectRoleNameUnique");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "isProjectRoleNameUniqueReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[31] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addActorsToProjectRole");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_xsd_string"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in4"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[32] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeActorsFromProjectRole");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_xsd_string"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in4"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[33] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addDefaultActorsToProjectRole");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_xsd_string"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[34] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeDefaultActorsFromProjectRole");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_xsd_string"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[35] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getAssociatedNotificationSchemes");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteScheme"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getAssociatedNotificationSchemesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[36] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getAssociatedPermissionSchemes");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteScheme"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getAssociatedPermissionSchemesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[37] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getCustomFields");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteField"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteField[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getCustomFieldsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[38] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getSavedFilters");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteFilter"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteFilter[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getSavedFiltersReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[39] = oper;

    }

    private static void _initOperationDesc5(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getComments");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteComment"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteComment[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getCommentsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[40] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("archiveVersion");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[41] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getVersions");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteVersion"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getVersionsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[42] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createProject");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in4"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in5"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in6"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionScheme"), com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in7"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteScheme"), com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in8"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteScheme"), com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "createProjectReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[43] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addComment");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteComment"), com.atlassian.theplugin.jira.api.soap.axis.RemoteComment.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[44] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getFieldsForEdit");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteField"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteField[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getFieldsForEditReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[45] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getIssueTypesForProject");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteIssueType"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getIssueTypesForProjectReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[46] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getSubTaskIssueTypesForProject");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteIssueType"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getSubTaskIssueTypesForProjectReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[47] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addUserToGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteGroup"), com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteUser"), com.atlassian.theplugin.jira.api.soap.axis.RemoteUser.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[48] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeUserFromGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteGroup"), com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteUser"), com.atlassian.theplugin.jira.api.soap.axis.RemoteUser.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[49] = oper;

    }

    private static void _initOperationDesc6(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProjectById");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"), long.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getProjectByIdReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[50] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteProject");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[51] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("releaseVersion");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteVersion"), com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[52] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteIssue");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[53] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addAttachmentsToIssue");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_xsd_string"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_xsd_base64Binary"), byte[][].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "addAttachmentsToIssueReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[54] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getAttachmentsFromIssue");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteAttachment"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteAttachment[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getAttachmentsFromIssueReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[55] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("hasPermissionToEditComment");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteComment"), com.atlassian.theplugin.jira.api.soap.axis.RemoteComment.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "hasPermissionToEditCommentReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[56] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("editComment");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteComment"), com.atlassian.theplugin.jira.api.soap.axis.RemoteComment.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteComment"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteComment.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "editCommentReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[57] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getFieldsForAction");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteField"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteField[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getFieldsForActionReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[58] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("progressWorkflowAction");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteFieldValue"), com.atlassian.theplugin.jira.api.soap.axis.RemoteFieldValue[].class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteIssue"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "progressWorkflowActionReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[59] = oper;

    }

    private static void _initOperationDesc7(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getIssueById");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteIssue"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getIssueByIdReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[60] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addWorklogWithNewRemainingEstimate");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog"), com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "addWorklogWithNewRemainingEstimateReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[61] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addWorklogAndAutoAdjustRemainingEstimate");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog"), com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "addWorklogAndAutoAdjustRemainingEstimateReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[62] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addWorklogAndRetainRemainingEstimate");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog"), com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "addWorklogAndRetainRemainingEstimateReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[63] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteWorklogWithNewRemainingEstimate");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[64] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteWorklogAndAutoAdjustRemainingEstimate");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[65] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteWorklogAndRetainRemainingEstimate");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[66] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("updateWorklogWithNewRemainingEstimate");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog"), com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[67] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("updateWorklogAndAutoAdjustRemainingEstimate");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog"), com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[68] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("updateWorklogAndRetainRemainingEstimate");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog"), com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[69] = oper;

    }

    private static void _initOperationDesc8(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getWorklogs");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteWorklog"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getWorklogsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[70] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("hasPermissionToCreateWorklog");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "hasPermissionToCreateWorklogReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[71] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("hasPermissionToDeleteWorklog");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "hasPermissionToDeleteWorklogReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[72] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("hasPermissionToUpdateWorklog");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "hasPermissionToUpdateWorklogReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        _operations[73] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getNotificationSchemes");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteScheme"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getNotificationSchemesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[74] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getPermissionSchemes");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemotePermissionScheme"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getPermissionSchemesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[75] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createPermissionScheme");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionScheme"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "createPermissionSchemeReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[76] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deletePermissionScheme");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[77] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addPermissionTo");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionScheme"), com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermission"), com.atlassian.theplugin.jira.api.soap.axis.RemotePermission.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteEntity"), com.atlassian.theplugin.jira.api.soap.axis.RemoteEntity.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionScheme"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "addPermissionToReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[78] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deletePermissionFrom");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionScheme"), com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermission"), com.atlassian.theplugin.jira.api.soap.axis.RemotePermission.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteEntity"), com.atlassian.theplugin.jira.api.soap.axis.RemoteEntity.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionScheme"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "deletePermissionFromReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[79] = oper;

    }

    private static void _initOperationDesc9(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getAllPermissions");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemotePermission"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemotePermission[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getAllPermissionsReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[80] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getIssueCountForFilter");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        oper.setReturnClass(long.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getIssueCountForFilterReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[81] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getIssuesFromTextSearch");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteIssue"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getIssuesFromTextSearchReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[82] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getIssuesFromTextSearchWithProject");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_xsd_string"), java.lang.String[].class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in3"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"), int.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteIssue"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getIssuesFromTextSearchWithProjectReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[83] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteUser");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[84] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("updateGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteGroup"), com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteGroup"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "updateGroupReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[85] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteGroup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[86] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("refreshCustomFields");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[87] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getProjectsNoSchemes");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteProject"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getProjectsNoSchemesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[88] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addVersion");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteVersion"), com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteVersion"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "addVersionReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[89] = oper;

    }

    private static void _initOperationDesc10(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createProjectFromObject");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"), com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "createProjectFromObjectReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[90] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getSecuritySchemes");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteScheme"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getSecuritySchemesReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException"), 
                      true
                     ));
        _operations[91] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getIssuesFromFilter");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in0"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "in1"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteIssue"));
        oper.setReturnClass(com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "getIssuesFromFilterReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "fault"),
                      "com.atlassian.theplugin.jira.api.soap.axis.RemoteException",
                      new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException"), 
                      true
                     ));
        _operations[92] = oper;

    }

    public JirasoapserviceV2SoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public JirasoapserviceV2SoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public JirasoapserviceV2SoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.1");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "AbstractNamedRemoteEntity");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.AbstractNamedRemoteEntity.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "AbstractRemoteConstant");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.AbstractRemoteConstant.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "AbstractRemoteEntity");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.AbstractRemoteEntity.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteAttachment");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteAttachment.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteComment");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteComment.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteComponent");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteComponent.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteConfiguration");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteConfiguration.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteCustomFieldValue");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteCustomFieldValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteEntity");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteEntity.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteField");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteField.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteFieldValue");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteFieldValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteFilter");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteFilter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteGroup");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteIssue");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteIssueType");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteNamedObject");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteNamedObject.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermission");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemotePermission.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionMapping");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionMapping.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionScheme");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePriority");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemotePriority.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRoleActors");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRoleActors.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteResolution");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteResolution.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteRoleActor");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteRoleActor.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteRoleActors");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteRoleActors.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteScheme");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteServerInfo");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteServerInfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteStatus");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteUser");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteUser.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteVersion");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteAuthenticationException");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteException");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemotePermissionException");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://exception.rpc.jira.atlassian.com", "RemoteValidationException");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteAttachment");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteAttachment[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteAttachment");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteComment");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteComment[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteComment");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteComponent");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteComponent[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteComponent");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteCustomFieldValue");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteCustomFieldValue[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteCustomFieldValue");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteEntity");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteEntity[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteEntity");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteField");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteField[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteField");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteFieldValue");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteFieldValue[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteFieldValue");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteFilter");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteFilter[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteFilter");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteIssue");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteIssue");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteIssueType");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteIssueType");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteNamedObject");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteNamedObject[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteNamedObject");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemotePermission");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemotePermission[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermission");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemotePermissionMapping");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionMapping[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionMapping");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemotePermissionScheme");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePermissionScheme");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemotePriority");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemotePriority[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemotePriority");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteProject");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProject");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteProjectRole");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteProjectRole");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteResolution");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteResolution[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteResolution");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteRoleActor");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteRoleActor[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteRoleActor");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteScheme");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteScheme");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteStatus");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteStatus[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteStatus");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteUser");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteUser[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteUser");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteVersion");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteVersion");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_tns1_RemoteWorklog");
            cachedSerQNames.add(qName);
            cls = com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://beans.soap.rpc.jira.atlassian.com", "RemoteWorklog");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_xsd_base64Binary");
            cachedSerQNames.add(qName);
            cls = byte[][].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "byte");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://jira.atlassian.com/rpc/soap/jirasoapservice-v2", "ArrayOf_xsd_string");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteComment getComment(java.lang.String in0, long in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getComment"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, new java.lang.Long(in1)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteComment) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteComment) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteComment.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup createGroup(java.lang.String in0, java.lang.String in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteUser in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "createGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteUser getUser(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getUser"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteUser) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteUser) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteUser.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup getGroup(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public java.lang.String login(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "login"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteServerInfo getServerInfo(java.lang.String in0) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getServerInfo"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteServerInfo) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteServerInfo) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteServerInfo.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public boolean logout(java.lang.String in0) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "logout"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue getIssue(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getIssue"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteComponent[] getComponents(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getComponents"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteComponent[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteComponent[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteComponent[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteUser createUser(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "createUser"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3, in4});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteUser) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteUser) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteUser.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue createIssue(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "createIssue"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteNamedObject[] getAvailableActions(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getAvailableActions"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteNamedObject[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteNamedObject[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteNamedObject[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[] getProjects(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getProjects"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue updateIssue(java.lang.String in0, java.lang.String in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteFieldValue[] in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "updateIssue"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteConfiguration getConfiguration(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getConfiguration"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteConfiguration) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteConfiguration) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteConfiguration.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProject updateProject(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "updateProject"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProject getProjectByKey(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getProjectByKey"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemotePriority[] getPriorities(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getPriorities"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePriority[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePriority[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemotePriority[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteResolution[] getResolutions(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getResolutions"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteResolution[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteResolution[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteResolution[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[] getIssueTypes(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getIssueTypes"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteStatus[] getStatuses(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getStatuses"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteStatus[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteStatus[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteStatus[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[] getSubTaskIssueTypes(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[21]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getSubTaskIssueTypes"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole[] getProjectRoles(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[22]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getProjectRoles"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole getProjectRole(java.lang.String in0, long in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[23]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getProjectRole"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, new java.lang.Long(in1)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRoleActors getProjectRoleActors(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[24]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getProjectRoleActors"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRoleActors) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRoleActors) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRoleActors.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteRoleActors getDefaultRoleActors(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[25]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getDefaultRoleActors"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteRoleActors) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteRoleActors) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteRoleActors.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void removeAllRoleActorsByNameAndType(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[26]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "removeAllRoleActorsByNameAndType"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void removeAllRoleActorsByProject(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[27]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "removeAllRoleActorsByProject"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void deleteProjectRole(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in1, boolean in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[28]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "deleteProjectRole"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, new java.lang.Boolean(in2)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void updateProjectRole(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[29]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "updateProjectRole"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole createProjectRole(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[30]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "createProjectRole"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public boolean isProjectRoleNameUnique(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[31]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "isProjectRoleNameUnique"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void addActorsToProjectRole(java.lang.String in0, java.lang.String[] in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in2, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject in3, java.lang.String in4) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[32]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "addActorsToProjectRole"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3, in4});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void removeActorsFromProjectRole(java.lang.String in0, java.lang.String[] in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in2, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject in3, java.lang.String in4) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[33]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "removeActorsFromProjectRole"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3, in4});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void addDefaultActorsToProjectRole(java.lang.String in0, java.lang.String[] in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in2, java.lang.String in3) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[34]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "addDefaultActorsToProjectRole"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void removeDefaultActorsFromProjectRole(java.lang.String in0, java.lang.String[] in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in2, java.lang.String in3) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[35]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "removeDefaultActorsFromProjectRole"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[] getAssociatedNotificationSchemes(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[36]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getAssociatedNotificationSchemes"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[] getAssociatedPermissionSchemes(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteProjectRole in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[37]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getAssociatedPermissionSchemes"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteField[] getCustomFields(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[38]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getCustomFields"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteField[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteField[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteField[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteFilter[] getSavedFilters(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[39]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getSavedFilters"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteFilter[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteFilter[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteFilter[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteComment[] getComments(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[40]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getComments"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteComment[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteComment[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteComment[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void archiveVersion(java.lang.String in0, java.lang.String in1, java.lang.String in2, boolean in3) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[41]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "archiveVersion"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, new java.lang.Boolean(in3)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion[] getVersions(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[42]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getVersions"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProject createProject(java.lang.String in0, java.lang.String in1, java.lang.String in2, java.lang.String in3, java.lang.String in4, java.lang.String in5, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme in6, com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme in7, com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme in8) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[43]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "createProject"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3, in4, in5, in6, in7, in8});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void addComment(java.lang.String in0, java.lang.String in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteComment in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[44]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "addComment"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteField[] getFieldsForEdit(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[45]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getFieldsForEdit"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteField[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteField[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteField[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[] getIssueTypesForProject(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[46]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getIssueTypesForProject"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[] getSubTaskIssueTypesForProject(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[47]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getSubTaskIssueTypesForProject"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssueType[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void addUserToGroup(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteUser in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[48]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "addUserToGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void removeUserFromGroup(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteUser in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[49]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "removeUserFromGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProject getProjectById(java.lang.String in0, long in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[50]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getProjectById"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, new java.lang.Long(in1)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void deleteProject(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[51]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "deleteProject"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void releaseVersion(java.lang.String in0, java.lang.String in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[52]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "releaseVersion"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void deleteIssue(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[53]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "deleteIssue"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public boolean addAttachmentsToIssue(java.lang.String in0, java.lang.String in1, java.lang.String[] in2, byte[][] in3) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[54]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "addAttachmentsToIssue"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteAttachment[] getAttachmentsFromIssue(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[55]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getAttachmentsFromIssue"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteAttachment[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteAttachment[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteAttachment[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public boolean hasPermissionToEditComment(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteComment in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[56]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "hasPermissionToEditComment"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteComment editComment(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteComment in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[57]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "editComment"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteComment) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteComment) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteComment.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteField[] getFieldsForAction(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[58]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getFieldsForAction"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteField[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteField[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteField[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue progressWorkflowAction(java.lang.String in0, java.lang.String in1, java.lang.String in2, com.atlassian.theplugin.jira.api.soap.axis.RemoteFieldValue[] in3) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[59]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "progressWorkflowAction"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue getIssueById(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[60]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getIssueById"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog addWorklogWithNewRemainingEstimate(java.lang.String in0, java.lang.String in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog in2, java.lang.String in3) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[61]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "addWorklogWithNewRemainingEstimate"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog addWorklogAndAutoAdjustRemainingEstimate(java.lang.String in0, java.lang.String in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[62]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "addWorklogAndAutoAdjustRemainingEstimate"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog addWorklogAndRetainRemainingEstimate(java.lang.String in0, java.lang.String in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[63]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "addWorklogAndRetainRemainingEstimate"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void deleteWorklogWithNewRemainingEstimate(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[64]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "deleteWorklogWithNewRemainingEstimate"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void deleteWorklogAndAutoAdjustRemainingEstimate(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[65]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "deleteWorklogAndAutoAdjustRemainingEstimate"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void deleteWorklogAndRetainRemainingEstimate(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[66]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "deleteWorklogAndRetainRemainingEstimate"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void updateWorklogWithNewRemainingEstimate(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog in1, java.lang.String in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[67]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "updateWorklogWithNewRemainingEstimate"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void updateWorklogAndAutoAdjustRemainingEstimate(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[68]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "updateWorklogAndAutoAdjustRemainingEstimate"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void updateWorklogAndRetainRemainingEstimate(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[69]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "updateWorklogAndRetainRemainingEstimate"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog[] getWorklogs(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[70]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getWorklogs"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteWorklog[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public boolean hasPermissionToCreateWorklog(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[71]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "hasPermissionToCreateWorklog"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public boolean hasPermissionToDeleteWorklog(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[72]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "hasPermissionToDeleteWorklog"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public boolean hasPermissionToUpdateWorklog(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[73]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "hasPermissionToUpdateWorklog"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[] getNotificationSchemes(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[74]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getNotificationSchemes"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme[] getPermissionSchemes(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[75]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getPermissionSchemes"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme createPermissionScheme(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[76]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "createPermissionScheme"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void deletePermissionScheme(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[77]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "deletePermissionScheme"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme addPermissionTo(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme in1, com.atlassian.theplugin.jira.api.soap.axis.RemotePermission in2, com.atlassian.theplugin.jira.api.soap.axis.RemoteEntity in3) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[78]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "addPermissionTo"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme deletePermissionFrom(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme in1, com.atlassian.theplugin.jira.api.soap.axis.RemotePermission in2, com.atlassian.theplugin.jira.api.soap.axis.RemoteEntity in3) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[79]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "deletePermissionFrom"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, in3});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionScheme.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemotePermission[] getAllPermissions(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[80]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getAllPermissions"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePermission[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemotePermission[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemotePermission[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public long getIssueCountForFilter(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[81]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getIssueCountForFilter"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Long) _resp).longValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Long) org.apache.axis.utils.JavaUtils.convert(_resp, long.class)).longValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[] getIssuesFromTextSearch(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[82]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getIssuesFromTextSearch"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[] getIssuesFromTextSearchWithProject(java.lang.String in0, java.lang.String[] in1, java.lang.String in2, int in3) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[83]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getIssuesFromTextSearchWithProject"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2, new java.lang.Integer(in3)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void deleteUser(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[84]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "deleteUser"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup updateGroup(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[85]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "updateGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteGroup.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void deleteGroup(java.lang.String in0, java.lang.String in1, java.lang.String in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[86]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "deleteGroup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void refreshCustomFields(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[87]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "refreshCustomFields"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        extractAttachments(_call);
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[] getProjectsNoSchemes(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[88]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getProjectsNoSchemes"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion addVersion(java.lang.String in0, java.lang.String in1, com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion in2) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[89]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "addVersion"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1, in2});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteVersion.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteProject createProjectFromObject(java.lang.String in0, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[90]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "createProjectFromObject"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteProject) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteProject.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteValidationException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[] getSecuritySchemes(java.lang.String in0) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException, com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[91]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getSecuritySchemes"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteScheme[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemotePermissionException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteAuthenticationException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[] getIssuesFromFilter(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, com.atlassian.theplugin.jira.api.soap.axis.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[92]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://soap.rpc.jira.atlassian.com", "getIssuesFromFilter"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {in0, in1});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.atlassian.theplugin.jira.api.soap.axis.RemoteIssue[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.atlassian.theplugin.jira.api.soap.axis.RemoteException) {
              throw (com.atlassian.theplugin.jira.api.soap.axis.RemoteException) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
