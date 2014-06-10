package com.intellij.coldFusion.UI.components;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateContext;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 07.11.2008
 */
public class CfmlProjectComponent extends AbstractProjectComponent {

    public CfmlProjectComponent(Project project) {
      super(project);
    }

    @NotNull
    public String getComponentName() {
        return "CFML component";  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static void initTemplate(final Template template) {
        TemplateContext templateContext = ((TemplateImpl) template).getTemplateContext();
        for (TemplateContextType contextType : Extensions.getExtensions(TemplateContextType.EP_NAME)) {
            if (contextType.isInContext(StdFileTypes.HTML)) {
                templateContext.setEnabled(contextType, true);
                // contextType.setEnabled(templateContext,  true);
            }
        }

        /*
        template.setToReformat(true);
        final Application application = ApplicationManager.getApplication();

        if (application.isDispatchThread() ||
            application.isHeadlessEnvironment() // for tests
           ) {
          TemplateSettings.getInstance().addTemplate(template);
        } else {
          final Runnable runnable = new Runnable() {
            public void run() {
              TemplateSettings.getInstance().addTemplate(template);
            }
          };
          if (application.isReadAccessAllowed()) {
            application.invokeLater(runnable);
          } else {
            application.invokeAndWait(runnable, ModalityState.defaultModalityState());
          }
        }
        */
        TemplateSettings.getInstance().addTemplate(template);
    }

    public void initComponent() {
    }

  private void initTemplates() {
    Template template = TemplateManager.getInstance(myProject).createTemplate("cfs", "cfml",
            "<cfset $VAR$ = $VALUE$/>");
    template.addVariable("VAR", "\"var\"", "\"var\"", true);
    template.addVariable("VALUE", "\"value\"", "\"value\"", true);
    initTemplate(template);

    template = TemplateManager.getInstance(myProject).createTemplate("cfd", "cfml",
            "<cfdump var=\"#$VAR$#\">");
    template.addVariable("VAR", "\"var\"", "\"var\"", true);
    initTemplate(template);

    template = TemplateManager.getInstance(myProject).createTemplate("cfo", "cfml",
            "<cfoutput>$CONTENT$</cfoutput>");
    template.addVariable("CONTENT", "\"component\"", "\"component\"", true);
    initTemplate(template);

    template = TemplateManager.getInstance(myProject).createTemplate("coj", "cfml",
            "createObject(\"java\", \"$CLASSPATH$\")");
    template.addVariable("CLASSPATH", "\"CLASSPATH\"", "\"CLASSPATH\"", true);
    initTemplate(template);

    template = TemplateManager.getInstance(myProject).createTemplate("coc", "cfml",
            "createObject(\"component\", \"$CFCPATH$\")");
    template.addVariable("CFCPATH", "\"CFCPATH\"", "\"CFCPATH\"", true);
    initTemplate(template);

    template = TemplateManager.getInstance(myProject).createTemplate("cow", "cfml",
            "createObject(\"webservice\", \"$WSDL$\")");
    template.addVariable("WSDL", "\"WSDL\"", "\"WSDL\"", true);
    initTemplate(template);

    template = TemplateManager.getInstance(myProject).createTemplate("r.", "cfml",
            "request.$VAR$");
    template.addVariable("VAR", "\"var\"", "\"var\"", true);
    initTemplate(template);

    template = TemplateManager.getInstance(myProject).createTemplate("v.", "cfml",
            "variables.$VAR$");
    template.addVariable("VAR", "\"var\"", "\"var\"", true);
    initTemplate(template);

    template = TemplateManager.getInstance(myProject).createTemplate("a.", "cfml",
            "application.$VAR$");
    template.addVariable("VAR", "\"var\"", "\"var\"", true);
    initTemplate(template);

    template = TemplateManager.getInstance(myProject).createTemplate("u.", "cfml",
            "url.$VAR$");
    template.addVariable("VAR", "\"var\"", "\"var\"", true);
    initTemplate(template);

    template = TemplateManager.getInstance(myProject).createTemplate("f.", "cfml",
            "form.$VAR$");
    template.addVariable("VAR", "\"var\"", "\"var\"", true);
    initTemplate(template);
  }

  @Override
  public void projectOpened() {
    initTemplates();
  }
}
