package com.intellij.spring.model.xml.jms;

import com.intellij.util.xml.NamedEnum;

public enum DestinationType implements NamedEnum {
  DURABLE_TOPIC("durableTopic"),
  QUEUE("queue"),
  TOPIC("topic");

  private final String value;

  private DestinationType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
