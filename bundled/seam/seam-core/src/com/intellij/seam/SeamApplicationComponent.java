package com.intellij.seam;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.meta.MetaDataRegistrar;
import com.intellij.psi.xml.XmlTag;
import com.intellij.seam.constants.SeamConstants;
import com.intellij.seam.facet.SeamFacetType;
import com.intellij.seam.highlighting.jam.*;
import com.intellij.seam.highlighting.xml.SeamDomModelInspection;
import com.intellij.seam.model.jam.SeamJamComponent;
import com.intellij.seam.model.jam.SeamJamFactory;
import com.intellij.seam.model.jam.SeamJamRole;
import com.intellij.seam.model.jam.bijection.SeamJamInjection;
import com.intellij.seam.model.jam.bijection.SeamJamOutjection;
import com.intellij.seam.model.jam.dataModel.SeamJamDataModel;
import com.intellij.seam.model.jam.jsf.SeamJamConverter;
import com.intellij.seam.model.jam.jsf.SeamJamValidator;
import com.intellij.seam.model.xml.components.BasicSeamComponent;
import com.intellij.seam.model.xml.components.SeamDomComponent;
import com.intellij.seam.resources.SeamBundle;
import com.intellij.seam.structure.SeamToolWindowFactory;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SeamApplicationComponent
    implements ApplicationComponent, FileTemplateGroupDescriptorFactory, InspectionToolProvider, Disposable {

  @NonNls
  @NotNull
  public String getComponentName() {
    return getClass().getName();
  }

  public void initComponent() {
    FacetTypeRegistry.getInstance().registerFacetType(SeamFacetType.INSTANCE);

    registerMetaData();

    TypeNameManager.registerTypeName(SeamDomComponent.class, SeamBundle.message("seam.component.type.name"));
    TypeNameManager.registerTypeName(SeamJamComponent.class, SeamBundle.message("seam.component.type.name"));
    TypeNameManager.registerTypeName(SeamJamRole.class, SeamBundle.message("seam.role.name"));
    TypeNameManager.registerTypeName(SeamJamDataModel.class, SeamBundle.message("seam.datamodel.name"));
    TypeNameManager.registerTypeName(SeamJamValidator.class, SeamBundle.message("seam.validator.name"));
    TypeNameManager.registerTypeName(SeamJamConverter.class, SeamBundle.message("seam.converter.name"));
    TypeNameManager.registerTypeName(SeamJamOutjection.class, SeamBundle.message("seam.outjection.name"));
    TypeNameManager.registerTypeName(SeamJamInjection.class, SeamBundle.message("seam.injection.name"));
    TypeNameManager.registerTypeName(SeamJamFactory.class, SeamBundle.message("seam.factory.name"));
    
    ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerAdapter() {
      @Override
      public void projectOpened(final Project project) {
        new SeamToolWindowFactory().configureToolWindow(project);
      }
    });
  }

  private static void registerMetaData() {
    MetaDataRegistrar.getInstance().registerMetaData(new ElementFilter() {
      public boolean isAcceptable(Object element, PsiElement context) {
        if (element instanceof XmlTag) {
          final XmlTag tag = (XmlTag)element;
          final DomElement domElement = DomManager.getDomManager(tag.getProject()).getDomElement(tag);

          return domElement instanceof BasicSeamComponent;
        }
        return false;
      }

      public boolean isClassAcceptable(Class hintClass) {
        return XmlTag.class.isAssignableFrom(hintClass);
      }
    }, BasicSeamComponentElementMetaData.class);
  }

  public static class BasicSeamComponentElementMetaData extends DomMetaData<BasicSeamComponent> {

    @Nullable
    protected GenericDomValue getNameElement(final BasicSeamComponent element) {
      final GenericAttributeValue<String> id = element.getName();
      if (DomUtil.hasXml(id)) {
        return id;
      }
      return null;
    }

    public void setName(final String name) throws IncorrectOperationException {
      getElement().getName().setStringValue(name);
    }
  }
  public void dispose() {
  }

  public void disposeComponent() {
    Disposer.dispose(this);
  }

  public Class[] getInspectionClasses() {
    return new Class[]{SeamDomModelInspection.class, SeamAnnotationIncorrectSignatureInspection.class,
        SeamAnnotationsInconsistencyInspection.class, SeamBijectionUndefinedContextVariableInspection.class,
        SeamBijectionIllegalScopeParameterInspection.class, SeamBijectionTypeMismatchInspection.class, SeamJamComponentInspection.class,
        SeamIllegalComponentScopeInspection.class, SeamDuplicateComponentsInspection.class};
  }

  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final FileTemplateGroupDescriptor groupDescriptor =
        new FileTemplateGroupDescriptor(SeamBundle.message("seam.framework.name"), SeamIcons.SEAM_ICON);

    groupDescriptor.addTemplate(new FileTemplateDescriptor(SeamConstants.FILE_TEMPLATE_NAME_SEAM_1_2, SeamIcons.SEAM_ICON));
    groupDescriptor.addTemplate(new FileTemplateDescriptor(SeamConstants.FILE_TEMPLATE_NAME_SEAM_2_0, SeamIcons.SEAM_ICON));
    groupDescriptor.addTemplate(new FileTemplateDescriptor(SeamConstants.FILE_TEMPLATE_NAME_PAGES_2_0, SeamIcons.SEAM_ICON));
    groupDescriptor.addTemplate(new FileTemplateDescriptor(SeamConstants.FILE_TEMPLATE_NAME_PAGEFLOW_2_0, SeamIcons.SEAM_ICON));

    return groupDescriptor;
  }
}

