package org.jetbrains.idea.perforce.perforce;

public enum P4Command {
  add("add"),
  edit("edit"),
  revert("revert"),
  delete("delete"),
  integrate("integrate"),
  reopen("reopen"),
  clients("clients"),
  users("users"),
  user("user"),
  describe("describe"),
  change("change"),
  changes("changes"),
  opened("opened"),
  submit("submit"),
  filelog("filelog"),
  sync("sync"),
  dirs("dirs"),
  files("files"),
  jobs("jobs"),
  resolved("resolved"),
  resolve("resolve"),
  move("move");

  private final String myName;

  P4Command(String name) {
    myName = name;
  }

  public String getName() {
    return myName;
  }
}
