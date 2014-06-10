package com.intellij.hibernate.model.enums;

import com.intellij.util.xml.NamedEnum;
import org.jetbrains.annotations.NonNls;

public enum EventType implements NamedEnum {
  AUTO_FLUSH("auto-flush"),
  MERGE("merge"),
  CREATE("create"),
  CREATE_ONFLUSH("create-onflush"),
  DELETE("delete"),
  DIRTY_CHECK("dirty-check"),
  EVICT("evict"),
  FLUSH("flush"),
  FLUSH_ENTITY("flush-entity"),
  LOAD("load"),
  LOAD_COLLECTION("load-collection"),
  LOCK("lock"),
  REFRESH("refresh"),
  REPLICATE("replicate"),
  SAVE_UPDATE("save-update"),
  SAVE("save"),
  UPDATE("update"),
  PRE_LOAD("pre-load"),
  PRE_UPDATE("pre-update"),
  PRE_INSERT("pre-insert"),
  PRE_DELETE("pre-delete"),
  PRE_COLLECTION_RECREATE("pre-collection-recreate"),
  PRE_COLLECTION_REMOVE("pre-collection-remove"),
  PRE_COLLECTION_UPDATE("pre-collection-update"),
  POST_LOAD("post-load"),
  POST_UPDATE("post-update"),
  POST_INSERT("post-insert"),
  POST_DELETE("post-delete"),
  POST_COLLECTION_RECREATE("post-collection-recreate"),
  POST_COLLECTION_REMOVE("post-collection-remove"),
  POST_COLLECTION_UPDATE("post-collection-update"),
  POST_COMMIT_UPDATE("post-commit-update"),
  POST_COMMIT_INSERT("post-commit-insert"),
  POST_COMMIT_DELETE("post-commit-delete");

  private final String myValue;

  private EventType(final @NonNls String value) {
    myValue = value;
  }

  public String getValue() {
    return myValue;
  }

}
