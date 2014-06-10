package com.intellij.hibernate.engine;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.facet.Facet;
import com.intellij.facet.pointers.FacetPointersManager;
import com.intellij.hibernate.HibernateMessages;
import com.intellij.hibernate.view.HibernateIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.facet.PersistenceFacetBase;
import com.intellij.persistence.facet.PersistenceFacetConfiguration;
import com.intellij.persistence.model.PersistencePackage;
import com.intellij.persistence.run.ConsoleContextProvider;
import com.intellij.persistence.run.ConsoleRunConfiguration;
import com.intellij.persistence.util.PersistenceCommonUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.JavaeeIcons;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ElementPresentationManager;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Gregory.Shrago
 */
public class HibernateConsoleContextProvider implements ConsoleContextProvider {
  @NonNls private static final String PROVIDER_ID = "Hibernate";
  private static final Logger LOG = Logger.getInstance("#com.intellij.hibernate.engine.HibernateConsoleContextProvider");

  @NotNull
  public String getId() {
    return PROVIDER_ID;
  }

  public String getDisplayName() {
    return HibernateMessages.message("hqlconsole.context.provider.name");
  }

  public Icon getIcon() {
    return HibernateIcons.HIBERNATE_ICON;
  }

  public ConsoleContext createConsoleContext() {
    return new HibernateConsoleContext(null, null);
  }

  public boolean isAvailable(final Project project) {
    return getPossiblePersistenceUnits(project).length != 0;
  }

  public void setupContextPresentation(final Project project, final ConsoleContext context, final SimpleColoredComponent component) {
    final HibernateConsoleContext hibernateContext = (HibernateConsoleContext)context;
    final Pair<String, String> id = hibernateContext.getUnitId();
    final UnitInfo unitInfo = ContainerUtil.find(getPossiblePersistenceUnits(project), new Condition<UnitInfo>() {
      public boolean value(final UnitInfo info) {
        return info.getId().equals(id);
      }
    });
    if (unitInfo == null) {
      component.setIcon(JavaeeIcons.PERSISTENCE_UNIT_ICON);
      component.append(id.getFirst()+"/"+id.getSecond(), SimpleTextAttributes.ERROR_ATTRIBUTES);
    }
    else {
      component.setIcon(unitInfo.getIcon());
      component.append(unitInfo.getDisplayName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
  }

  public boolean editContext(final ConsoleContext context, final Project project) {
    final HibernateConsoleContext hibernateContext = (HibernateConsoleContext)context;
    final HibernateConsoleContextProvider.UnitInfo[] units = HibernateConsoleContextProvider.getPossiblePersistenceUnits(project);
    Arrays.sort(units, new Comparator<UnitInfo>() {
      public int compare(HibernateConsoleContextProvider.UnitInfo o1, HibernateConsoleContextProvider.UnitInfo o2) {
        return Comparing.compare(StringUtil.toLowerCase(o1.getDisplayName()), StringUtil.toLowerCase(o2.getDisplayName()));
      }
    });
    final JComboBox comboBox = new JComboBox(new DefaultComboBoxModel(units));
    comboBox.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(final JList list,
                                                    final Object value,
                                                    final int index,
                                                    final boolean isSelected,
                                                    final boolean cellHasFocus) {
        final Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        final HibernateConsoleContextProvider.UnitInfo element = (HibernateConsoleContextProvider.UnitInfo)value;
        setIcon(element.getIcon());
        setText(element.getDisplayName());
        return result;
      }
    });
    comboBox.setEditable(false);

    final Pair<String, String> selectionId = hibernateContext.getUnitId();
    for (HibernateConsoleContextProvider.UnitInfo info : units) {
      if (info.getId().equals(selectionId)) {
        comboBox.setSelectedItem(info);
      }
    }

    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(comboBox, BorderLayout.CENTER);

    final DialogBuilder builder = new DialogBuilder(project);
    builder.addOkAction();
    builder.addCancelAction();
    builder.setCenterPanel(panel);
    builder.setTitle(HibernateMessages.message("hqlconsole.context.unit"));
    if (builder.show() == DialogWrapper.OK_EXIT_CODE) {
      hibernateContext.setUnitId(((HibernateConsoleContextProvider.UnitInfo)comboBox.getSelectedItem()).getId());
      return true;
    }
    return false;
  }

  public ConsoleContext createConsoleContext(final PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> facet, final PersistencePackage unit, final String initialText) {
    return new HibernateConsoleContext(new UnitInfo(facet, unit).getId(), initialText);
  }

  public class HibernateConsoleContext implements ConsoleContext {
    private Pair<String, String> myUnitId;
    private final String myInitialText;

    private HibernateConsoleContext(final Pair<String, String> unitId, final String initialText) {
      myUnitId = unitId;
      myInitialText = initialText;
    }

    public Pair<String, String> getUnitId() {
      return myUnitId;
    }

    public void setUnitId(final Pair<String, String> unitId) {
      myUnitId = unitId;
    }

    public Element getState() {
      final Element element = new Element("persistenceUnit");
      element.setAttribute("facet", myUnitId == null? "" : StringUtil.notNullize(myUnitId.getFirst()));
      element.setAttribute("unit", myUnitId == null? "": StringUtil.notNullize(myUnitId.getSecond()));
      return element;
    }

    public void loadState(final Element state) {
      final Element child = state.getChild("persistenceUnit");
      if (child != null) {
        myUnitId = Pair.create(child.getAttributeValue("facet"), child.getAttributeValue("unit"));
      }
    }

    @NotNull
    public ConsoleContextProvider getProvider() {
      return HibernateConsoleContextProvider.this;
    }

    public RunContext createRunContext(final ExecutionEnvironment env) throws ExecutionException {
      final Project project = PlatformDataKeys.PROJECT.getData(env.getDataContext());
      LOG.assertTrue(project != null);

      final UnitInfo pair = myUnitId == null? null : discoverFacetUnit(project, myUnitId);
      if (pair == null) {
        throw new ExecutionException(HibernateMessages.message("hqlconsole.unit.not.found"));
      }

      return new HibernateRunContext(pair.facet, pair.unit, myInitialText, (ConsoleRunConfiguration)env.getRunProfile(), myUnitId);
    }

    public ConsoleContext clone() {
      return new HibernateConsoleContext(myUnitId, myInitialText);
    }
  }

  public static class HibernateRunContext implements RunContext {
    private final PersistenceFacetBase<?, PersistencePackage> myFacet;
    private PersistencePackage myPersistenceUnit;

    private final ConsoleRunConfiguration myRunConfiguration;
    private final Pair<String, String> myUnitId;
    private final HibernateEngine myEngine;
    private final HibernateConsole myConsole;

    private HibernateRunContext(final PersistenceFacetBase<?, PersistencePackage> facet, final PersistencePackage persistenceUnit, final String initialText,
                                final ConsoleRunConfiguration runConfiguration,
                                final Pair<String, String> unitId) {
      myFacet = facet;
      myPersistenceUnit = persistenceUnit;
      myRunConfiguration = runConfiguration;
      myUnitId = unitId;
      myEngine = new HibernateEngine(this);
      myConsole = new HibernateConsole(facet.getModule().getProject(), runConfiguration.getName(), initialText, this);
    }

    public ConsoleRunConfiguration getRunConfiguration() {
      return myRunConfiguration;
    }

    public PersistenceFacetBase<?, PersistencePackage> getFacet() {
      return myFacet;
    }

    @Nullable
    public PersistencePackage getPersistenceUnit() {
      if (myPersistenceUnit == null || !myPersistenceUnit.isValid()) {
        final UnitInfo pair = discoverFacetUnit(myConsole.getProject(), myUnitId);
        myPersistenceUnit = pair == null? null : pair.unit;
      }
      return myPersistenceUnit;
    }

    @NotNull
    public Module getModule() {
      return myFacet.getModule();
    }

    public ProcessListener getProcessListener() {
      return myEngine.getProcessListener();
    }

    public String getMainClassName() {
      return HibernateEngine.MAIN_CLASS;
    }

    public String[] getClassPath() {
      return myEngine.getClassPath();
    }

    public String getJarPath() {
      return HibernateEngine.getJarPath();
    }

    public TextConsoleBuilder getConsoleBuilder() {
      return myConsole.getTextConsoleBuilder();
    }

    public AnAction[] getActions() {
      return AnAction.EMPTY_ARRAY;
    }

    public RunContext checkRunContext() throws ExecutionException {
      if (getPersistenceUnit() == null) {
        throw new ExecutionException(HibernateMessages.message("hqlconsole.unit.not.found"));
      }
      return this;
    }

    public HibernateEngine getEngine() {
      return myEngine;
    }

    public HibernateConsole getConsole() {
      return myConsole;
    }
  }


  @Nullable
  public static UnitInfo discoverFacetUnit(final Project project, final Pair<String, String> unitId) {
    final Facet foundFacet = FacetPointersManager.getInstance(project).create(unitId.getFirst()).getFacet();
    if (foundFacet == null) return null;
    LOG.assertTrue(foundFacet instanceof PersistenceFacetBase);
    final PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> facet = (PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage>)foundFacet;
    for (PersistencePackage unit : facet.getPersistenceUnits()) {
      if (getUnitId(unit).equals(unitId.getSecond())) return new UnitInfo(facet, unit);
    }
    return null;
  }


  private static String getUnitId(final PersistencePackage unit) {
    String unitName = unit.getName().getValue();
    final XmlTag tag = unit.getXmlTag();
    final PsiFile containingFile = unit.getContainingFile();
    if (StringUtil.isEmpty(unitName) && tag != null && containingFile != null) {
      unitName = "<anonymous>" + containingFile.getName() + '@' + tag.getTextOffset();
    }
    LOG.assertTrue(unitName != null);
    return unitName;
  }

  public static class UnitInfo {
    PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> facet;
    PersistencePackage unit;

    public UnitInfo(final PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> facet, final PersistencePackage unit) {
      this.facet = facet;
      this.unit = unit;
    }

    public String getDisplayName() {
      return FacetPointersManager.constructId(facet)+"/"+getUnitId(unit);
    }

    public Icon getIcon() {
      return ElementPresentationManager.getIcon(unit);
    }

    public Pair<String, String> getId() {
      return Pair.create(FacetPointersManager.constructId(facet), getUnitId(unit));
    }

  }

  public static UnitInfo[] getPossiblePersistenceUnits(final Project project) {
    final ArrayList<UnitInfo> model = new ArrayList<UnitInfo>();
    for (PersistenceFacetBase<PersistenceFacetConfiguration, PersistencePackage> facet : PersistenceCommonUtil.getAllPersistenceFacets(project)) {
      if (!HibernateEngine.isAvailable(facet.getModule())) continue;
      for (PersistencePackage unit : facet.getPersistenceUnits()) {
        model.add(new UnitInfo(facet, unit));
      }
    }
    return model.toArray(new UnitInfo[model.size()]);
  }

}
