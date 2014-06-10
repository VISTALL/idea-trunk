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

package com.advancedtools.webservices.rt.embedded;

/**
 * @author Maxim
 */
public class WebServerHandler {
/*
  public static void main(String[] args) {
    // port, context name, webservice servlet name, web service url prefix
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    Server server = new Server(Integer.parseInt(args[0]));
    server.setHandler(contexts);
    Context context = new Context(contexts, "/" + args[1], Context.SESSIONS);
    context.addServlet(args[2], args[3] + "/*");

    try {
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static class Servlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
              response.setContentType("text/html");
              response.setStatus(HttpServletResponse.SC_OK);
              response.getWriter().println("<h1>From SimpleServlet</h1>");
              response.getWriter().println("session="+request.getSession(true).getId());
          }
  }
  */
}
