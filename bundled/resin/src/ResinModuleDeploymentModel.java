/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.intellij.j2ee.web.resin;

import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.facet.pointers.FacetPointer;
import com.intellij.packaging.artifacts.ArtifactPointer;

public class ResinModuleDeploymentModel extends DeploymentModel {
    public String CONTEXT_PATH = "/";
    public String HOST = "";

    public ResinModuleDeploymentModel(CommonModel project, FacetPointer<JavaeeFacet> facetPointer) {
        super(project, facetPointer);
    }

    public ResinModuleDeploymentModel(CommonModel commonModel, ArtifactPointer artifactPointer) {
        super(commonModel, artifactPointer);
    }
}
