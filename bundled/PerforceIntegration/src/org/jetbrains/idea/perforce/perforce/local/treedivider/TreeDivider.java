package org.jetbrains.idea.perforce.perforce.local.treedivider;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.ObjectsConvertor;
import com.intellij.util.containers.Convertor;

import java.util.*;
public class TreeDivider<T, E extends Throwable> {
  private final static Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.local.treedivider.TreeDivider");
  
  private final TreeDividerPartner<T, E> myPartner;

  private final LinkedList<MyNode<T>> myQueue;

  private final MyLeftPart<T, E> myLeftPart;

  public TreeDivider(final int boundWeight, final TreeDividerPartner<T, E> partner) {
    myPartner = partner;

    myQueue = new LinkedList<MyNode<T>>();
    myLeftPart = new MyLeftPart<T, E>(boundWeight, partner);
  }

  public void go(final T t) throws E {
    LOG.debug("TreeDivider started, root: " + t.toString());
    // better work outside
    assert ! myPartner.isLeaf(t);

    myQueue.offer(new MyNode<T>(t, null));

    while (! myQueue.isEmpty()) {
      final MyNode<T> node = myQueue.getFirst();
      LOG.debug("Got '" + node.getT().toString() + "', children processed: " + node.isChildrenChecked());

      if (! node.isChildrenChecked()) {
        // go forward
        final Collection<T> children = myPartner.getChildren(node.getT());
        LOG.debug("for '" + node.getT().toString() + "' got " + children.size() + " children");
        boolean goDown = false;
        for (T child : children) {
          if (myPartner.isLeaf(child)) {
            node.addChild(child);
            node.addWeight(myPartner.weight(child));
            LOG.debug("for '" + node.getT().toString() + "' adding leaf: '" + child.toString() + "'");
          } else {
            // add into head
            myQueue.add(0, new MyNode<T>(child, node));
            goDown = true;
          }
        }
        node.setChildrenChecked(true);
        if (goDown) continue;
      }

      myQueue.removeFirst();  // node is the first
      LOG.debug("processing node '" + node.getT() + "', children: " + node.getChildren().size() + ", count: " + node.getCnt());

      myLeftPart.nodeProcessed(node);
    }
    myLeftPart.flush();
  }

  private static class MyLeftPart<T, E extends Throwable> {
    private final static float ourErrorPercent = 0.2f;
    private final int myBoundWeight;
    private final int myError;
    private final TreeDividerPartner<T, E> myPartner;

    private int myCurrentCount;
    private final LinkedList<MyNode<T>> myLeftPart;
    private LinkedHashSet<T> myData;

    private MyLeftPart(final int boundWeight, final TreeDividerPartner<T, E> partner) {
      myBoundWeight = boundWeight;
      myPartner = partner;
      myLeftPart = new LinkedList<MyNode<T>>();
      myCurrentCount = 0;
      myError = (int) (myBoundWeight * ourErrorPercent);
      myData = new LinkedHashSet<T>();
    }

    // completed parents && partly reported - highest at the head
    public void nodeProcessed(final MyNode<T> node) throws E {
      final int increased = myCurrentCount + node.getCnt();
      LOG.debug("nodeProcessed for '" + node.getT().toString() + "', increased: " + increased);
      if (increased > (myBoundWeight + myError)) {
        // next node completes... previous?
        passGathered();
        acceptNode(node);
      } else if (increased > (myBoundWeight - myError)) {
        acceptNode(node);
        passGathered();
      } else {
        acceptNode(node);
      }
    }

    private void acceptNode(final MyNode<T> node) {
      zipCompleted(node);

      myLeftPart.add(node);
      myCurrentCount += node.getCnt();
      LOG.debug("accept '" + node.getT().toString() + "'; count after increase: " + myCurrentCount);
      myData.addAll(node.getChildren());
      if (myPartner.putFoldersIntoData()) {
        myData.add(node.getT());
        ++ myCurrentCount; 
      }

      node.clearChildren();
    }

    private void zipCompleted(final MyNode<T> node) {
      if (! node.isPartlyReported()) {
        // remove all its immediate children from set
        //final List<T> children = node.getChildren();
        LOG.debug("doing zip completed, node: '" + node.getT().toString() + "'");
        for (ListIterator<MyNode<T>> iterator = myLeftPart.listIterator(myLeftPart.size()); iterator.hasPrevious();) {
          final MyNode<T> child = iterator.previous();
          if (child.isUnder(node)) {
            LOG.debug("removing '" + child.getT().toString() + "'");
            iterator.remove();
          } else {
            break;
          }
        }
      }
    }

    private void passGathered() throws E {
      LOG.debug("START passGathered...");
      final List<MyPiece<T>> heads = ObjectsConvertor.convert(myLeftPart, new Convertor<MyNode<T>, MyPiece<T>>() {
        public MyPiece<T> convert(MyNode<T> o) {
          LOG.debug("pass gathered head: " + o.getT().toString());
          // parents of all nodes that are now being reported ARE reported partly
          MyNode<T> parent = o.myParent;
          while (parent != null) {
            parent.setPartlyReported(true);
            parent = parent.myParent;
          }
          return o;
        }
      });
      myPartner.accept(heads, myData);
      myLeftPart.clear();
      myData = new LinkedHashSet<T>();
      myCurrentCount = 0;
      LOG.debug("END passGathered...");
    }

    public void flush() throws E {
      passGathered();
    }
  }

  public interface MyPiece<T> {
    T getT();
    boolean isRecursive();
  }

  private static class MyNode<T> implements MyPiece<T> {
    private final MyNode<T> myParent;
    private final T myT;
    private int myCnt;
    private int myLevel;
    private List<T> myTempImmediate;
    private boolean myPartlyReported;
    private boolean myChildrenChecked;

    private MyNode(final T t, final MyNode<T> parent) {
      myT = t;
      myParent = parent;
      myTempImmediate = new LinkedList<T>();
      myLevel = (parent == null) ? 0 : (parent.myLevel + 1);
    }

    public void setChildrenChecked(boolean childrenChecked) {
      myChildrenChecked = childrenChecked;
    }

    public boolean isChildrenChecked() {
      return myChildrenChecked;
    }

    public void addChild(final T t) {
      myTempImmediate.add(t);
    }

    public boolean isPartlyReported() {
      return myPartlyReported;
    }

    public void setPartlyReported(boolean partlyReported) {
      myPartlyReported = partlyReported;
    }

    public List<T> getChildren() {
      return myTempImmediate;
    }

    public void clearChildren() {
      myTempImmediate = null;
    }

    public T getT() {
      return myT;
    }

    public boolean isRecursive() {
      return ! myPartlyReported;
    }

    public void addWeight(final int weight) {
      myCnt += weight;
    }

    public int getCnt() {
      return myCnt;
    }

    public boolean isUnder(final MyNode<T> parent) {
      // intentionally
      return parent == myParent;
    }
  }

  public interface TreeDividerPartner<T, E extends Throwable> {
    void accept(final Collection<MyPiece<T>> heads, final Set<T> fragmentsData) throws E;
    void process(final T t) throws E;

    boolean putFoldersIntoData();

    boolean isLeaf(final T t);
    Collection<T> getChildren(final T t);
    int weight(final T t);
  }
}
