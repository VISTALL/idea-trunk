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

package org.jetbrains.idea.perforce;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import junit.framework.Assert;
import org.jetbrains.idea.perforce.perforce.local.treedivider.TreeDivider;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TreeDividerTest extends CodeInsightFixtureTestCase {
  private MyDividerPartner myPartner;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myPartner = new MyDividerPartner();
  }

  public void testSimple() throws Exception {
    final TreeDivider<MyItem, Exception> divider = new TreeDivider<MyItem, Exception>(10, myPartner);

    final MyItem[] list = new MyItem[] {new MyItem("f.txt", true)};
    final MyItem dir = new MyItem("c:/dir", false);
    dir.add(Arrays.asList(list));

    divider.go(dir);

    final List<MyHolder> result = myPartner.getOut();
    Assert.assertTrue(result.size() == 1);
    final MyHolder holder = result.get(0);
    holder.assertNamesAre(new String[] {"c:/dir"}, new String[] {"f.txt"});
    holder.assertRecursive("c:/dir");
  }

  public void testTwoParts() throws Exception {
    // 10 +- 20% -> 8..12
    final TreeDivider<MyItem, Exception> divider = new TreeDivider<MyItem, Exception>(10, myPartner);

    final MyItem d1 = dirPlusFiles("first", "one.txt");
    final MyItem d2 = dirPlusFiles("second", "s1.txt", "s2.txt", "s3.txt", "s4.txt", "s5.txt");
    final MyItem d3 = dirPlusFiles("third", "t1.txt", "t2.txt", "t3.txt", "t4.txt");

    final MyItem interm1 = new MyItem("interm1", false);
    interm1.add(Arrays.asList(d3, d2, d1));

    final MyItem interm2 = new MyItem("interm2", false);

    final MyItem i1 = dirPlusFiles("i1", "i1.txt");
    final MyItem i2 = dirPlusFiles("i2", "i2.txt");

    interm2.add(Arrays.asList(i2, i1, interm1));

    final MyItem right = dirPlusFiles("right", "r1.txt", "r2.txt");

    final MyItem root = new MyItem("c:/root", false);
    root.add(Arrays.asList(right, interm2));

    divider.go(root);

    final List<MyHolder> result = myPartner.getOut();
    Assert.assertTrue(result.size() == 2);
    final MyHolder holder = result.get(0);
    holder.assertNamesAre(new String[] {"first", "second", "third"}, new String[] {"one.txt", "s1.txt", "s2.txt", "s3.txt", "s4.txt", "s5.txt", "t1.txt",
      "t2.txt", "t3.txt", "t4.txt"});
    holder.assertRecursive("first", "second", "third");

    final MyHolder h2 = result.get(1);
    h2.assertNamesAre(new String[] {"i1", "i2", "right", "interm1", "interm2", "c:/root"}, new String[] {"i1.txt", "i2.txt", "r1.txt", "r2.txt"});
    h2.assertRecursive("i1", "i2", "right");
  }

  public void testBigNodeWaits() throws Exception {
    // 10 +- 20% -> 8..12
    final TreeDivider<MyItem, Exception> divider = new TreeDivider<MyItem, Exception>(10, myPartner);

    final MyItem d1 = dirPlusFiles("first", "one.txt");
    final MyItem d2 = dirPlusFiles("second", "s1.txt", "s2.txt", "s3.txt", "s4.txt", "s5.txt");
    final MyItem d3 = dirPlusFiles("third", "t1.txt", "t2.txt", "t3.txt", "t4.txt", "t5.txt", "t6.txt", "t7.txt");

    final MyItem interm1 = new MyItem("interm1", false);
    interm1.add(Arrays.asList(d3, d2, d1));

    final MyItem interm2 = new MyItem("interm2", false);

    final MyItem i1 = dirPlusFiles("i1", "i1.txt");
    final MyItem i2 = dirPlusFiles("i2", "i2.txt");

    interm2.add(Arrays.asList(i2, i1, interm1));

    final MyItem right = dirPlusFiles("right", "r1.txt", "r2.txt");

    final MyItem root = new MyItem("c:/root", false);
    root.add(Arrays.asList(right, interm2));

    divider.go(root);

    final List<MyHolder> result = myPartner.getOut();
    Assert.assertTrue(result.size() == 3);
    final MyHolder holder = result.get(0);
    holder.assertNamesAre(new String[] {"first", "second"}, new String[] {"one.txt", "s1.txt", "s2.txt", "s3.txt", "s4.txt", "s5.txt"});
    holder.assertRecursive("first", "second");

    final MyHolder h2 = result.get(1);
    h2.assertNamesAre(new String[] {"third", "i1", "i2", "interm1"}, new String[] {"t1.txt", "t2.txt", "t3.txt", "t4.txt", "t5.txt", "t6.txt", "t7.txt", "i1.txt", "i2.txt"});
    h2.assertRecursive("third", "i1", "i2");

    final MyHolder h3 = result.get(2);
    h3.assertNamesAre(new String[] {"right", "c:/root", "interm2"}, new String[] {"r1.txt", "r2.txt"});
    h3.assertRecursive("right");
  }

  public void testWithEmptyInTheBottom() throws Exception {
    // 5 +- 20% -> 4..6
    final TreeDivider<MyItem, Exception> divider = new TreeDivider<MyItem, Exception>(5, myPartner);

    final MyItem d1 = dirPlusFiles("first", "one.txt");
    final MyItem d2 = dirPlusFiles("second");
    final MyItem root = new MyItem("c:/root", false);
    root.add(Arrays.asList(d1, d2));

    divider.go(root);

    final List<MyHolder> result = myPartner.getOut();
    Assert.assertTrue(result.size() == 1);
    final MyHolder holder = result.get(0);
    // todo check recursiveness
    holder.assertNamesAre(new String[] {"c:/root"}, new String[] {"one.txt"});
    holder.assertRecursive("c:/root");
  }

  public void testWithFilledMiddle() throws Exception {
    // 5 +- 20% -> 4..6
    final TreeDivider<MyItem, Exception> divider = new TreeDivider<MyItem, Exception>(5, myPartner);

    final MyItem d1 = dirPlusFiles("first", "one.txt");
    final MyItem d2 = dirPlusFiles("second");
    final MyItem d3 = dirPlusFiles("interm", "three.txt");
    d3.add(Arrays.asList(d1, d2));
    final MyItem root = new MyItem("c:/root", false);
    root.add(Arrays.asList(d3));

    divider.go(root);

    final List<MyHolder> result = myPartner.getOut();
    Assert.assertTrue(result.size() == 1);
    final MyHolder holder = result.get(0);
    holder.assertNamesAre(new String[] {"c:/root"}, new String[] {"one.txt", "three.txt"});
    holder.assertRecursive("c:/root");
  }

  private MyItem[] generateFiles(final String prefix, final int number) {
    final MyItem[] children = new MyItem[number];
    for (int j = 0; j < number; j++) {
      children[j] = new MyItem(prefix + j + ".txt", true);
    }
    return children;
  }

  private void generateInnerStuff(final int downCnt, final int numDirs, final int numFiles, final MyItem parent) {
    final List<MyItem> children = new LinkedList<MyItem>();
    final MyItem[] files = generateFiles("file" + parent.myName + downCnt, numFiles);
    Collections.addAll(children, files);
    if (downCnt > 0) {
      for (int i = 0; i < numDirs; i++) {
        final MyItem dir = new MyItem("dirUnder" + parent.myName + i, false);
        generateInnerStuff(downCnt - 1, numDirs, numFiles, dir);
        children.add(dir);
      }
    }
    parent.add(children);
  }

  public void testWideTreeIsReportedInOneStepWhenNeeded() throws Exception {
    final TreeDivider<MyItem, Exception> divider = new TreeDivider<MyItem, Exception>(1000, myPartner);

    final MyItem root = new MyItem("root", false);
    generateInnerStuff(5, 3, 2, root);

    divider.go(root);

    final List<MyHolder> result = myPartner.getOut();
    Assert.assertTrue(result.size() == 1);
    final MyHolder holder = result.get(0);
    Assert.assertEquals(1, holder.myHeads.size());
    Assert.assertEquals("root", holder.myHeads.values().iterator().next().myName);
    holder.assertRecursive("root");
  }

  /*public void testReal() throws Exception {
    final File root = new File("C:\\jdk1.5.0_15");
    final MyRealPartner partner = new MyRealPartner(root);
    final TreeDivider<File, IOException> divider = new TreeDivider<File, IOException>(20, partner);
    divider.go(root);
    partner.checkHeads();
    partner.checkFragments();
  }*/
  
  private MyItem dirPlusFiles(final String dirName, final String... files) {
    final List<MyItem> children = new ArrayList<MyItem>();
    for (String s : files) {
      children.add(new MyItem(s, true));
    }
    final MyItem result = new MyItem(dirName, false);
    result.add(children);
    return result;
  }

  private static class MyHolder {
    private final Map<String, MyItem> myHeads;
    private final Map<String, MyItem> myFragmentsData;
    private final Set<String> myWhoIsRecursive;

    public MyHolder(Collection<TreeDivider.MyPiece<MyItem>> heads, Collection<MyItem> fragmentsData) {
      myHeads = new HashMap<String, MyItem>();
      myFragmentsData = new HashMap<String, MyItem>();
      myWhoIsRecursive = new HashSet<String>();

      // todo also put recurs info somewhere
      for (TreeDivider.MyPiece<MyItem> head : heads) {
        final MyItem t = head.getT();
        myHeads.put(t.myName, head.getT());
        if (head.isRecursive()) {
          myWhoIsRecursive.add(t.myName);
        }
      }
      for (MyItem item : fragmentsData) {
        myFragmentsData.put(item.myName, item);
      }
    }

    public void assertRecursive(final String... names) {
      Assert.assertTrue(String.valueOf(myWhoIsRecursive.size()), names.length == myWhoIsRecursive.size());
      for (String name : names) {
        Assert.assertTrue(name, myWhoIsRecursive.contains(name));
      }
    }

    public void assertNamesAre(final String[] folderNames, final String[] fragmentsNames) {
      for (String folderName : folderNames) {
        Assert.assertTrue(folderName, myHeads.containsKey(folderName));
      }
      for (String fragmentName : fragmentsNames) {
        Assert.assertTrue(fragmentName, myFragmentsData.containsKey(fragmentName));
      }
    }
  }

  private static class MyRealPartner implements TreeDivider.TreeDividerPartner<File, IOException> {
    private final Map<File, TreeDivider.MyPiece<File>> myHeads;
    private final Set<File> myFragmentsData;
    private final File myRoot;

    private MyRealPartner(final File root) {
      myRoot = root;
      myHeads = new HashMap<File, TreeDivider.MyPiece<File>>();
      myFragmentsData = new HashSet<File>();
    }

    public void accept(Collection<TreeDivider.MyPiece<File>> heads, Set<File> fragmentsData) throws IOException {
      final int prevSize = myHeads.size();
      for (TreeDivider.MyPiece<File> head : heads) {
        myHeads.put(head.getT(), head);
      }
      final int afterSize = myHeads.size();
      Assert.assertEquals(prevSize + heads.size(), afterSize);

      final int prevSize1 = myFragmentsData.size();
      myFragmentsData.addAll(fragmentsData);
      final int afterSize1 = myFragmentsData.size();
      Assert.assertEquals(prevSize1 + fragmentsData.size(), afterSize1);
    }

    public void process(File file) throws IOException {
      //
    }

    public void checkHeads() throws IOException {
      for (File file : myHeads.keySet()) {
        // all siblings must be in a set
        // all dirs from parent chain must be non recursively included
        File parentFile = file.getParentFile();

        if (! myRoot.equals(file)) {
          final File[] siblings = parentFile.listFiles();
          for (File sibling : siblings) {
            if ((! sibling.equals(file)) && (sibling.isDirectory())) {
              final TreeDivider.MyPiece<File> siblingHead = myHeads.get(sibling);
              Assert.assertTrue(sibling.getAbsolutePath(), siblingHead != null);
            }
          }
        }

        while ((parentFile != null) && (FileUtil.isAncestor(myRoot, parentFile, false))) {
          final TreeDivider.MyPiece<File> parentHead = myHeads.get(parentFile);
          Assert.assertTrue(parentHead != null);
          Assert.assertTrue(! parentHead.isRecursive());
          parentFile = parentFile.getParentFile();
        }
      }
    }

    public void checkFragments() {
      final LinkedList<File> queue = new LinkedList<File>();
      queue.add(myRoot);
      
      while (! queue.isEmpty()) {
        final File item = queue.removeFirst();
        if (item.isDirectory()) {
          queue.addAll(Arrays.asList(item.listFiles()));
        } else {
          Assert.assertTrue(myFragmentsData.contains(item));
        }
      }
    }

    public boolean putFoldersIntoData() {
      return false;
    }

    public boolean isLeaf(File file) {
      return file.isFile();
    }

    public Collection<File> getChildren(File file) {
      return Arrays.asList(file.listFiles());
    }

    public int weight(File file) {
      return 1;
    }
  }

  private static class MyDividerPartner implements TreeDivider.TreeDividerPartner<MyItem, Exception> {
    private final List<MyHolder> myOut;

    private MyDividerPartner() {
      myOut = new ArrayList<MyHolder>();
    }

    public void accept(Collection<TreeDivider.MyPiece<MyItem>> heads, Set<MyItem> fragmentsData) throws Exception {
      myOut.add(new MyHolder(heads, fragmentsData));
    }

    public void process(MyItem myItem) throws Exception {
      System.out.println(myItem.toString());
    }

    public boolean putFoldersIntoData() {
      return false;
    }

    public boolean isLeaf(MyItem myItem) {
      return myItem.isFile();
    }

    public Collection<MyItem> getChildren(MyItem myItem) {
      return myItem.getChildren();
    }

    public int weight(MyItem myItem) {
      return 1;
    }

    public List<MyHolder> getOut() {
      return myOut;
    }
  }

  private static class MyItem {
    private MyItem myParent;
    private final String myName;
    private final List<MyItem> myChildren;
    private final boolean isFile;

    private MyItem(String name, boolean file) {
      myName = name;
      isFile = file;
      myChildren = new ArrayList<MyItem>();
    }

    public void add(final Collection<MyItem> children) {
      assert ! isFile;
      myChildren.addAll(children);
      for (MyItem child : myChildren) {
        child.setParent(this);
      }
    }

    public void setParent(MyItem parent) {
      myParent = parent;
    }

    public List<MyItem> getChildren() {
      return myChildren;
    }

    public boolean isFile() {
      return isFile;
    }

    @Override
    public String toString() {
      final LinkedList<String> path = new LinkedList<String>();
      MyItem current = this;
      while (current != null) {
        path.addFirst(current.myName);
        current = current.myParent;
      }

      final StringBuilder sb = new StringBuilder();
      for (String s : path) {
        if (sb.length() > 0) {
          sb.append('/');
        }
        sb.append(s);
      }
      return sb.toString();
    }
  }
}
