/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.facet;

import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import static org.hamcrest.Matchers.equalTo;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class AddFacetTest {
    @Before
    public void setUp() throws Exception {
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
                JavaTestFixtureFactory.createFixtureBuilder();
        final JavaModuleFixtureBuilder moduleBuilder = fixtureBuilder.addModule(JavaModuleFixtureBuilder.class);

        myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
        fixture = fixtureBuilder.getFixture();

        myTempDirFixture.setUp();
        moduleBuilder.addContentRoot(myTempDirFixture.getTempDirPath());
        fixture.setUp();

        orgTestDialog = Messages.setTestDialog(new TestDialog() {
            public int show(String message) {
                shownMessage = message;
                return dialogResult;
            }
        });

    }

    @After
    public void tearDown() throws Exception {
        Messages.setTestDialog(orgTestDialog);
        fixture.tearDown();
        myTempDirFixture.tearDown();
    }

    @Test
    public void testAddFacetAfterCreatingManifest() throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    VirtualFile[] files = ModuleRootManager.getInstance(fixture.getModule()).getContentRoots();
                    VirtualFile childDirectory = files[0].createChildDirectory(this, "META-INF");
                    VirtualFile data = childDirectory.createChildData(this, "MANIFEST.MF");
                    OutputStream outputStream = data.getOutputStream(this);
                    PrintWriter writer = new PrintWriter(outputStream);
                    writer.write("Manifest-Version: 1.0\n" +
                            "Bundle-ManifestVersion: 2\n" +
                            "Bundle-Name: Test\n" +
                            "Bundle-SymbolicName: test\n" +
                            "Bundle-Version: 1.0.0\n");
                    writer.flush();
                    writer.close();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                ModifiableFacetModel modifiableFacetModel =
                        FacetManager.getInstance(fixture.getModule()).createModifiableModel();
                OsmorcFacet facet = new OsmorcFacet(fixture.getModule());
                facet.getConfiguration().setOsmorcControlsManifest(false);
                modifiableFacetModel.addFacet(facet);
                modifiableFacetModel.commit();
            }
        });


    }

    @Test
    public void testAddFacetWithoutManifest() throws Exception {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {

                ModifiableFacetModel modifiableFacetModel =
                        FacetManager.getInstance(fixture.getModule()).createModifiableModel();
                OsmorcFacet facet = new OsmorcFacet(fixture.getModule());
                facet.getConfiguration().setOsmorcControlsManifest(false);
                modifiableFacetModel.addFacet(facet);

                shownMessage = null;
                dialogResult = DialogWrapper.CANCEL_EXIT_CODE;

                modifiableFacetModel.commit();
                assertThat(shownMessage,
                        equalTo("The manifest file META-INF/MANIFEST.MF was not found. Should it be created now?"));
            }
        });


    }


    private IdeaProjectTestFixture fixture;
    private String shownMessage;
    private TestDialog orgTestDialog;
    private int dialogResult;
    private TempDirTestFixture myTempDirFixture;
}
