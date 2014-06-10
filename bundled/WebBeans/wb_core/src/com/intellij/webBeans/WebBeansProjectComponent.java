package com.intellij.webBeans;

import com.intellij.facet.ProjectWideFacetAdapter;
import com.intellij.facet.ProjectWideFacetListenersRegistry;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.webBeans.facet.WebBeansFacet;
import com.intellij.facet.impl.ui.libraries.versions.LibraryVersionInfo;
import com.intellij.webBeans.toolWindow.WebBeansToolWindowFactory;
import com.intellij.webBeans.toolWindow.WebBeansView;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class WebBeansProjectComponent implements ProjectComponent {
  private final Project myProject;
  private WebBeansView myWebBeansView;

  private static final String TOOL_WINDOW_ID = "Web Beans";
  private Map<LibraryVersionInfo, List<LibraryInfo>> myLibraries;
  // THIS VALUE MUST BE EQUALS ID FROM plugin.xml <toolWindow id="Web Beans" ... />

  public static WebBeansProjectComponent getInstance(@NotNull Project project) {
    return project.getComponent(WebBeansProjectComponent.class);
  }

  public WebBeansProjectComponent(final Project project, final ReferenceProvidersRegistry referenceProvidersRegistry) {
    myProject = project;
    registerReferenceProviders(referenceProvidersRegistry);


  }

  private static void registerReferenceProviders(final ReferenceProvidersRegistry referenceProvidersRegistry) {
    //@Name
    //referenceProvidersRegistry.registerReferenceProvider(
    //  new AnnotationParameterFilter(PsiLiteralExpression.class, WebBeansAnnotationConstants.COMPONENT_ANNOTATION, "value"),
    //  PsiLiteralExpression.class, new WebBeansComponentNameReferenceProvider());
  }

  public void projectOpened() {
    configureToolWindow();
  }

  private void configureToolWindow() {
    ProjectWideFacetListenersRegistry.getInstance(myProject)
      .registerListener(WebBeansFacet.FACET_TYPE_ID, new ProjectWideFacetAdapter<WebBeansFacet>() {
        @Override
        public void firstFacetAdded() {
          if (ToolWindowManager.getInstance(myProject).getToolWindow(TOOL_WINDOW_ID) == null) {
            initWebBeansToolWindow();
          }
        }

        @Override
        public void allFacetsRemoved() {
          if (ToolWindowManager.getInstance(myProject).getToolWindow(TOOL_WINDOW_ID) != null) {
            deactivateWebBeansToolWindow();
          }
        }
      });
  }

  private void initWebBeansToolWindow() {
    ToolWindow window =
      ToolWindowManager.getInstance(myProject).registerToolWindow(TOOL_WINDOW_ID, true, ToolWindowAnchor.RIGHT, myProject);

    WebBeansToolWindowFactory webBeansToolWindowFactory = new WebBeansToolWindowFactory();
    webBeansToolWindowFactory.createToolWindowContent(myProject, window);
  }

  private void deactivateWebBeansToolWindow() {
    ToolWindowManager.getInstance(myProject).unregisterToolWindow(TOOL_WINDOW_ID);

    if (myWebBeansView != null) {
      Disposer.dispose(myWebBeansView);
    }
    myWebBeansView =null;
  }

  public WebBeansView getWebBeansView() {
    if (myWebBeansView == null) {
      myWebBeansView = new WebBeansView(myProject);
    }
    return myWebBeansView;
  }

  public void projectClosed() {
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return WebBeansProjectComponent.class.getName();
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }


}
