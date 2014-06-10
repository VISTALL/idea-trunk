/*
 * Copyright 2006 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.lang.xpath.xslt.run;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ConnectException;

class OutputTabAdapter extends ProcessAdapter {
    private final ProcessHandler myStartedProcess;
    private final HighlightingOutputConsole myConsole;

    public OutputTabAdapter(ProcessHandler startedProcess, HighlightingOutputConsole console) {
        myStartedProcess = startedProcess;
        myConsole = console;
    }

    public void startNotified(ProcessEvent event) {
        final XsltCommandLineState state = event.getProcessHandler().getUserData(XsltCommandLineState.STATE);
        attachOutputConsole(state.getPort());
    }

    public void attachOutputConsole(final int port) {
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            public void run() {
                try {
                    final InputStream stream;
                    if ((stream = connect(port)) == null) {
                        // process terminated prematurely
                        return;
                    }

                    final InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
                    final HighlightingProcessHandler process = new HighlightingProcessHandler(reader) {
                        private boolean mySelectionChanged;

                        @Override
                        public void notifyTextAvailable(String text, Key outputType) {
                            super.notifyTextAvailable(text, outputType);

                            if (mySelectionChanged) {
                                return;
                            }
                            mySelectionChanged = true;
                            myConsole.selectOutputTab();
                        }
                    };

                    myConsole.getConsole().attachToProcess(process);

                    process.start();
                } catch (UnsupportedEncodingException e) {
                    // cannot happen
                    throw new Error(e);
                } catch (IOException e) {
                    myStartedProcess.notifyTextAvailable("Could not connect to runner: " + e.toString() + "\n", ProcessOutputTypes.SYSTEM);
                }
            }
        });
    }

    @Nullable
    private InputStream connect(int port) throws IOException {
        final long s = System.currentTimeMillis();
        final InetSocketAddress endpoint = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port);

        int tries = 0;
        IOException ex;
        do {
            final int d = (int)(System.currentTimeMillis() - s);
            try {
                final Socket socket = new Socket();
                socket.connect(endpoint, Math.max(5000 - d, 100));

                myStartedProcess.notifyTextAvailable("Connected to XSLT runner on " + endpoint + "\n", ProcessOutputTypes.SYSTEM);
                return socket.getInputStream();
            } catch (ConnectException e) {
                ex = e;
                try { Thread.sleep(500); } catch (InterruptedException e1) { break; }
            }
            if (myStartedProcess.isProcessTerminated() || myStartedProcess.isProcessTerminating()) {
                return null;
            }
        } while (tries++ < 10);

        throw ex;
    }
}
