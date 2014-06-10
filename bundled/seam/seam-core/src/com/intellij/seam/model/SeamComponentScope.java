package com.intellij.seam.model;

import com.intellij.util.xml.NamedEnum;

/**
 * http://jboss.com/products/seam/components:scopeAttrType enumeration.
   */
  public enum SeamComponentScope implements NamedEnum {
    UNSPECIFIED("UNSPECIFIED"),
    APPLICATION("APPLICATION"),
    BUSINESS_PROCESS("BUSINESS_PROCESS"),
    CONVERSATION("CONVERSATION"),
    EVENT("EVENT"),
    PAGE("PAGE"),
    SESSION("SESSION"),
    STATELESS("STATELESS"),
    METHOD("METHOD"),
    APPLICATION_LOWERCASE("application"),
    BUSINESS_PROCESS_LOWERCASE("business_process"),
    CONVERSATION_LOWERCASE("conversation"),
    EVENT_LOWERCASE("event"),
    PAGE_LOWERCASE("page"),
    SESSION_LOWERCASE("session"),
    STATELESS_LOWERCASE("stateless");

    private final String value;

    private SeamComponentScope(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public boolean isEqual(SeamComponentScope scope) {
        return scope.getValue().toLowerCase().equals(getValue().toLowerCase());
    }
  }
