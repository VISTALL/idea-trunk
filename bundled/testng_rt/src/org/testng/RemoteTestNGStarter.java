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

/*
 * User: anna
 * Date: 01-Jul-2009
 */
package org.testng;

import org.testng.remote.RemoteTestNG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.Vector;

public class RemoteTestNGStarter {
  public static void main(String[] args) throws Exception {
    int i = 0;
    Vector resultArgs = new Vector();
    for (; i < args.length; i++) {
      String arg = args[i];
      if (arg.equals("-temp")) {
        break;
      }
      resultArgs.add(arg);
    }

    File temp = new File(args[++i]);

    while (temp.length() == 0){
      //wait for test classes
    }

    BufferedReader reader = new BufferedReader(new FileReader(temp));

    try {
      final String cantRunMessage = "CantRunException";
      while (true) {
        String line = reader.readLine();
        while (line == null) {
          Thread.sleep(100);
          line = reader.readLine();
        }

        if (line.startsWith(cantRunMessage) && !new File(line).exists()){
          System.err.println(line.substring(cantRunMessage.length()));
          System.exit(1);
          return;
        }
        if (line.equals("end")) break;
        resultArgs.add(line);
      }
    }
    finally {
      reader.close();
    }


    Map commandLineArgs= TestNGCommandLineArgs.parseCommandLine((String[])resultArgs.toArray(new String[resultArgs.size()]));

    RemoteTestNG testNG= new RemoteTestNG();
    testNG.configure(commandLineArgs);

    testNG.run();
  }
}
