/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.spring.SpringApplicationComponent;
import com.intellij.testFramework.LexerTestCase;

/**
 * @author peter
 */
public class AopLexerTest extends LexerTestCase {
  @Override
  protected void setUp() throws Exception {
    new SpringApplicationComponent();
    super.setUp();
  }

  public void testExecution1() throws Throwable {
    doTest("execution (* transfer(..))");
  }

  public void testExecution2() throws Throwable {
    doTest("execution(public * *(..))");
  }

  public void testExecution3() throws Throwable {
    doTest("execution(* com.xyz.within.service.*.*(..) throws *)");
  }

  public void testExecution4() throws Throwable {
    doTest("execution(void set*(..))");
  }

  public void testExecution5() throws Throwable {
    doTest("execution(int set*(boolean, long, double, float, char..., byte))");
  }

  public void testExecution6() throws Throwable {
    doTest("execution");
  }

  public void testExecution7() throws Throwable {
    doTest("execution(java.util.List<? extends java.lang.Object,java.util.List<? super java..*>,*> set*(List<?>, List<? super T>, public, ..))");
  }
  
  public void testExecution8() throws Throwable {
    doTest("execution(public * com.a.services.Serv.*(..)) or execution(public * com.b..*(..))");
  }

  public void testWithin1() throws Throwable {
    doTest("within(com.xyz.someapp.trading..*");
  }

  public void testWithin2() throws Throwable {
    doTest("within(com.execution.service.*+)");
  }

  public void testReference1() throws Throwable {
    doTest("anyPublicOperation() && inTrading()");
  }

  public void testReference2() throws Throwable {
    doTest("com.xyz.within.SystemArchitecture.businessService()");
  }

  public void testReference3() throws Throwable {
    doTest("anyPublicOperation() and not inTrading() or inTrading()");
  }

  public void testReference4() throws Throwable {
    doTest("anyPublicOperation()&&!inTrading()||inTrading()");
  }

  public void testThis() throws Throwable {
    doTest("this(com.xyz.service.AccountService)");
  }

  public void testTarget() throws Throwable {
    doTest("target(com.xyz.service.AccountService)");
  }

  public void testArgs() throws Throwable {
    doTest("args(java.io.Serializable)");
  }
  
  public void testArgs2() throws Throwable {
    doTest("args(foo<bar,?,? extends foo, ? super bar>)");
  }

  public void testAtTarget() throws Throwable {
    doTest("@target(org.springframework.transaction.args.Transactional)");
  }

  public void testAtWithin() throws Throwable {
    doTest("@within(org.springframework.transaction.annotation.Transactional)");
  }

  public void testAtAnnotation() throws Throwable {
    doTest("@annotation(org.springframework.transaction.annotation.Transactional)");
  }

  public void testAtArgs() throws Throwable {
    doTest("@args(com.xyz.security.Classified)");
  }

  public void testBean() throws Throwable {
    doTest("bean(x) and x(bean)");
  }

  protected Lexer createLexer() {
    return new AopLexer();
  }

  protected String getDirPath() {
    return "/svnPlugins/aop-common/tests/com/intellij/aop/lexer/data";
  }
}
