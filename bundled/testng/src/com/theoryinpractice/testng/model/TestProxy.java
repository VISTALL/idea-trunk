package com.theoryinpractice.testng.model;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.stacktrace.StackTraceLine;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.Filter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diff.LineTokenizer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.theoryinpractice.testng.ui.Printable;
import com.theoryinpractice.testng.ui.TestNGConsoleView;
import org.jetbrains.annotations.Nullable;
import org.testng.remote.strprotocol.MessageHelper;
import org.testng.remote.strprotocol.TestResultMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Hani Suleiman Date: Jul 28, 2005 Time: 10:52:51 PM
 */
public class TestProxy implements AbstractTestProxy {
  private final List<TestProxy> results = new ArrayList<TestProxy>();
  private TestResultMessage resultMessage;
  private String name;
  private TestProxy parent;
  private List<Printable> output;
  private SmartPsiElementPointer psiElement;
  private boolean inProgress;
  private int myExceptionMark;

  public TestProxy() {}

  public TestProxy(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Nullable
  public PsiElement getPsiElement() {
    return psiElement != null ? psiElement.getElement() : null;
  }

  public void setPsiElement(PsiElement psiElement) {
    if (psiElement != null) {
      final Project project = psiElement.getProject();
      PsiDocumentManager.getInstance(project).commitAllDocuments();
      this.psiElement = SmartPointerManager.getInstance(project).createLazyPointer(psiElement);
    } else {
      this.psiElement = null;
    }
  }

  public boolean isResult() {
    return resultMessage != null;
  }

  public List<AbstractTestProxy> getResults(Filter filter) {
    return filter.select(results);
  }

  public List<Printable> getOutput() {
    if (output != null) return output;
    List<Printable> total = new ArrayList<Printable>();
    for (TestProxy child : results) {
      final List<Printable> out = child.getOutput();
      if (total.size() > 0 && out.size() > 0) {
        total.add(new TestNGConsoleView.Chunk("\n===============================================\n\n", ConsoleViewContentType.NORMAL_OUTPUT));
      }
      total.addAll(out);
    }
    return total;
  }

  public List<TestProxy> getChildren() {
    return results;
  }

  public TestResultMessage getResultMessage() {
    return resultMessage;
  }

  public void setResultMessage(final TestResultMessage resultMessage) {
    //if we have a result, then our parent is a class, so we can look up our method
    //this is a bit fragile as it assumes parent is set first and correctly
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        PsiClass psiClass = (PsiClass)getParent().getPsiElement();
        if (psiClass != null) {
          PsiMethod[] methods = psiClass.getAllMethods();
          for (PsiMethod method : methods) {
            if (method.getName().equals(resultMessage.getMethod())) {
              setPsiElement(method);
              break;
            }
          }
        }
      }
    });

    TestProxy current = this;
    while (current != null) {
      current.inProgress = resultMessage.getResult() == MessageHelper.TEST_STARTED;
      current = current.getParent();
    }
    if (this.resultMessage == null || this.resultMessage.getResult() == MessageHelper.TEST_STARTED) {
      this.resultMessage = resultMessage;
      this.name = resultMessage.toDisplayString();
    }
  }

  public boolean isInProgress() {
    final TestProxy parentProxy = getParent();
    return (parentProxy == null || parentProxy.isInProgress()) && inProgress;
  }

  public boolean isDefect() {
    return isNotPassed();
  }

  public boolean shouldRun() {
    return true;
  }

  public int getMagnitude() {
    return -1;
  }

  public boolean isLeaf() {
    return isResult();
  }

  public boolean isPassed() {
    return !isNotPassed();
  }

  public Location getLocation(final Project project) {
    if (psiElement == null) return null;
    final PsiElement element = psiElement.getElement();
    if (element == null) return null;
    return new PsiLocation<PsiElement>(project, element);
  }

  public Navigatable getDescriptor(final Location location) {
    if (location == null) return null;
    if (isNotPassed() && output != null) {
      final PsiLocation<?> psiLocation = location.toPsiLocation();
      final PsiClass containingClass = psiLocation.getParentElement(PsiClass.class);
      if (containingClass != null) {
        String containingMethod = null;
        for (Iterator<Location<PsiMethod>> iterator = psiLocation.getAncestors(PsiMethod.class, false); iterator.hasNext();) {
          final PsiMethod psiMethod = iterator.next().getPsiElement();
          if (containingClass.equals(psiMethod.getContainingClass())) containingMethod = psiMethod.getName();
        }
        if (containingMethod != null) {
          final String qualifiedName = containingClass.getQualifiedName();
          for (Printable aStackTrace : output) {
            if (aStackTrace instanceof TestNGConsoleView.Chunk) {
              final String[] stackTrace = new LineTokenizer(((TestNGConsoleView.Chunk)aStackTrace).text).execute();
              for (String line : stackTrace) {
                final StackTraceLine stackLine = new StackTraceLine(containingClass.getProject(), line);
                if (containingMethod.equals(stackLine.getMethodName()) && Comparing.strEqual(qualifiedName, stackLine.getClassName())) {
                  return stackLine.getOpenFileDescriptor(containingClass.getContainingFile().getVirtualFile());
                }
              }
            }
          }
        }
      }
    }
    return EditSourceUtil.getDescriptor(location.getPsiElement());
  }

  public TestProxy[] getPathFromRoot() {
    ArrayList<TestProxy> arraylist = new ArrayList<TestProxy>();
    TestProxy testproxy = this;
    do {
      arraylist.add(testproxy);
    }
    while ((testproxy = testproxy.getParent()) != null);
    Collections.reverse(arraylist);
    return arraylist.toArray(new TestProxy[arraylist.size()]);
  }

  @Override
  public String toString() {
    return name + ' ' + results;
  }

  public void addResult(TestProxy proxy) {
    results.add(proxy);
    proxy.setParent(this);
  }

  public void setParent(TestProxy parent) {
    this.parent = parent;
  }

  public TestProxy getParent() {
    return parent;
  }

  public void setOutput(List<Printable> output) {
    this.output = output;
  }

  public boolean isNotPassed() {
    if (resultNotPassed()) return true;
    //we just added the node, so we don't know if it has passes or fails
    if (resultMessage == null && results.size() == 0) return true;
    for (TestProxy child : results) {
      if (child.isNotPassed()) return true;
    }
    return false;
  }

  private boolean resultNotPassed() {
    return resultMessage != null && resultMessage.getResult() != MessageHelper.PASSED_TEST;
  }

  public List<TestProxy> getAllTests() {
    List<TestProxy> total = new ArrayList<TestProxy>();
    total.add(this);
    for (TestProxy child : results) {
      total.addAll(child.getAllTests());
    }
    return total;
  }

  public int getChildCount() {
    return results.size();
  }

  public TestProxy getChildAt(int i) {
    return results.get(i);
  }

  public TestProxy getFirstDefect() {
    for (TestProxy child : results) {
      if (child.isNotPassed() && child.isResult()) return child;
      TestProxy firstDefect = child.getFirstDefect();
      if (firstDefect != null) return firstDefect;
    }
    return null;
  }

  public boolean childExists(String child) {
    for (int count = 0; count < getChildCount(); count++) {
      if (child.equals(getChildAt(count).getName())) {
        return true;
      }
    }
    return false;
  }

  public int getExceptionMark() {
    if (myExceptionMark == 0 && getChildCount() > 0) {
      return (output != null ? output.size() : 0) + getChildAt(0).getExceptionMark();
    }
    return myExceptionMark;
  }

  public void setExceptionMark(int exceptionMark) {
    myExceptionMark = exceptionMark;
  }

  public boolean isInterrupted() {
    return !isInProgress() && inProgress;
  }
}
