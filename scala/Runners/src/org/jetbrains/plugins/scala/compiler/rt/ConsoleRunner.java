package org.jetbrains.plugins.scala.compiler.rt;

import scala.Function1;
import scala.Some;
import scala.collection.mutable.ListBuffer;
import scala.tools.nsc.GenericRunnerCommand;
import scala.tools.nsc.GenericRunnerSettings;
import scala.tools.nsc.InterpreterLoop;
import scala.tools.nsc.Settings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * User: Alexander Podkhalyuzin
 * Date: 10.02.2009
 */
public class ConsoleRunner {
  public static void main(String[] args) {
    (new InterpreterLoop(new Some(new BufferedReader(new InputStreamReader(System.in))),
        new PrintWriter(System.out))).main(args);
  }
}
