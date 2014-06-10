package org.jetbrains.idea.perforce;

import org.jetbrains.idea.perforce.perforce.View;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: kir
 * Date: Nov 3, 2008
 */
public class PerforceViewTest extends Assert {
  @Test
  public void should_support_wildcards_recursive() throws Throwable {
    final View view = View.create("//depot/...  //kir-mac/...");

    final String path = view.match("//depot/file.txt", "kir-mac");
    assertEquals("file.txt", path);
  }

  @Test
  public void should_support_wildcards_unrecursive() throws Throwable {
    final View view = View.create("//depot/*  //kir-mac/*");

    final String path = view.match("//depot/file.txt", "kir-mac");
    assertEquals("file.txt", path);

    final String path1 = view.match("//depot/path/file.txt", "kir-mac");
    assertNull(path1);

  }

  @Test
  public void should_support_wildcards_unrecursive2() throws Throwable {
    final View view = View.create("//depot/.../sss/*  //kir-mac/.../sss/*");

    final String path = view.match("//depot/a/b/c/sss/file.txt", "kir-mac");
    assertEquals("a/b/c/sss/file.txt", path);

    final String path1 = view.match("//depot/a/b/c/sss/ddd/file.txt", "kir-mac");
    assertNull(path1);

  }
}
