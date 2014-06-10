package com.intellij.spring.model.values;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.lang.properties.references.CreatePropertyFix;
import com.intellij.lang.properties.PropertiesFilesManager;
import com.intellij.lang.properties.references.PropertyReference;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.xml.XmlElement;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.converters.ResourceResolverUtils;
import com.intellij.spring.model.converters.SpringConverterUtil;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.spring.model.xml.context.PropertyPlaceholder;
import com.intellij.spring.model.xml.util.UtilProperties;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.ArrayUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class PlaceholderUtils {
  public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
  public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

  @NonNls public static final String PLACEHOLDER_PREFIX_PROPERTY_NAME = "placeholderPrefix";
  @NonNls public static final String PLACEHOLDER_SUFFIX_PROPERTY_NAME = "placeholderSuffix";

  public static final String PLACEHOLDER_CONFIGURER_CLASS = "org.springframework.beans.factory.config.PropertyPlaceholderConfigurer";

  @NonNls private static final String LOCATION_PROPERTY_NAME = "location";
  @NonNls private static final String LOCATIONS_PROPERTY_NAME = "locations";
  @NonNls private static final String PROPERTIES_PROPERTY_NAME = "properties";
  @NonNls private static final String PROPERTIES_ARRAY_PROPERTY_NAME = "propertiesArray";

  private static final Key<CachedValue<Pair<String, String>>> PLACEHOLDER_PREFIX_SUFFIX = Key.create("PLACEHOLDER_PREFIX_SUFFIX");

  private PlaceholderUtils() {
  }

  public static List<SpringBaseBeanPointer> getPlaceholderConfigurerBeans(final DomElement domElement) {
    Project project = domElement.getManager().getProject();
    PsiClass placeholderClass =
        JavaPsiFacade.getInstance(project).findClass(PLACEHOLDER_CONFIGURER_CLASS, GlobalSearchScope.allScope(project));

    if (placeholderClass == null) return Collections.emptyList();

    final SpringModel model = SpringConverterUtil.getSpringModel(domElement);
    if (model == null) return Collections.emptyList();

    List<SpringBaseBeanPointer> configurers = model.findBeansByPsiClassWithInheritance(placeholderClass);

    return configurers == null ? Collections.<SpringBaseBeanPointer>emptyList() : configurers;
  }

  public static List<PropertiesFile> getResources(final CommonSpringBean configurerBean) {
    List<PropertiesFile> resources = new ArrayList<PropertiesFile>();
    final List<Pair<String, PsiElement>> locations = getLocations(configurerBean);

    boolean hasNotResolvedLocations = false;

    for (Pair<String, PsiElement> location : locations) {
      Set<PropertiesFile> propertiesFiles = getPropertiesFile(location);
      if (propertiesFiles != null && !propertiesFiles.isEmpty()) {
        resources.addAll(propertiesFiles);
      }
      else {
        hasNotResolvedLocations = true;
      }
    }
    if (locations.size() > 0 && hasNotResolvedLocations) {
      //some locations were not resolved, so we'll try load all properties files(IDEADEV-16888)
      final PsiManager psiManager = configurerBean.getPsiManager();
      final Collection<VirtualFile> files = PropertiesFilesManager.getInstance(psiManager.getProject()).getAllPropertiesFiles();
      for (VirtualFile file : files) {
        if (file.isValid()) {
          final PsiFile psiFile = psiManager.findFile(file);
          if (psiFile instanceof PropertiesFile && !resources.contains(psiFile)) {
            resources.add((PropertiesFile)psiFile);
          }
        }
      }
    }
    return resources;
  }

  @Nullable
  private static Set<PropertiesFile> getPropertiesFile(final Pair<String, PsiElement> location) {
    return ResourceResolverUtils
        .addResourceFilesFrom(location.second, location.first, ",", new THashSet<PropertiesFile>(), new Condition<PsiFileSystemItem>() {
          public boolean value(final PsiFileSystemItem item) {
            return item instanceof PropertiesFile;
          }
        });
  }

  private static List<Pair<String, PsiElement>> getLocations(final CommonSpringBean configurerBean) {
    List<Pair<String, PsiElement>> locations = new ArrayList<Pair<String, PsiElement>>();

    if (configurerBean instanceof PropertyPlaceholder) {
      GenericAttributeValue<String> location = ((PropertyPlaceholder)configurerBean).getLocation();
      if (!StringUtil.isEmptyOrSpaces(location.getStringValue())) {
        locations.add(new Pair<String, PsiElement>(location.getStringValue(), location.getXmlElement()));
      }
    }
    else {
      final SpringPropertyDefinition locationProperty = SpringUtils.findPropertyByName(configurerBean, LOCATION_PROPERTY_NAME);
      if (locationProperty != null) {
        final Pair<String, PsiElement> value = SpringUtils.getPropertyValue(locationProperty);
        if (value != null) {
          locations.add(value);
        }
      }
      final SpringPropertyDefinition locationsProperty = SpringUtils.findPropertyByName(configurerBean, LOCATIONS_PROPERTY_NAME);
      if (locationsProperty instanceof SpringProperty) {
        final Pair<String, PsiElement> propertyValue = SpringUtils.getPropertyValue(locationsProperty);
        if (propertyValue != null) {
          locations.add(propertyValue);
        }
        else {
          final ListOrSet list = ((SpringProperty)locationsProperty).getList();

          for (SpringValue value : list.getValues()) {
            if (!StringUtil.isEmptyOrSpaces(value.getStringValue())) {
              locations.add(new Pair<String, PsiElement>(value.getStringValue(), value.getXmlElement()));
            }
          }
          final ListOrSet set = ((SpringProperty)locationsProperty).getSet();
          for (SpringValue value : set.getValues()) {
            if (!StringUtil.isEmptyOrSpaces(value.getStringValue())) {
              locations.add(new Pair<String, PsiElement>(value.getStringValue(), value.getXmlElement()));
            }
          }
        }
      }
    }
    final SpringPropertyDefinition propertiesProperty = SpringUtils.findPropertyByName(configurerBean, PROPERTIES_PROPERTY_NAME);
    if (propertiesProperty != null) {
      final GenericDomValue<SpringBeanPointer> element = propertiesProperty.getRefElement();
      if (element != null) {
        addLocations(locations, element.getValue());
      }
    }

    final SpringPropertyDefinition propertiesArrayProperty = SpringUtils.findPropertyByName(configurerBean, PROPERTIES_ARRAY_PROPERTY_NAME);
    if (propertiesArrayProperty != null) {
      final GenericDomValue<SpringBeanPointer> element = propertiesArrayProperty.getRefElement();
      if (element != null) {
        addLocations(locations, element.getValue());
      }
      if (propertiesArrayProperty instanceof SpringProperty) {
        final ListOrSet list = ((SpringProperty)propertiesArrayProperty).getList();
        for (SpringRef value : list.getRefs()) {
          addLocations(locations, value.getBean().getValue());
          addLocations(locations, value.getLocal().getValue());
        }

        final ListOrSet set = ((SpringProperty)propertiesArrayProperty).getSet();
        for (SpringRef value : set.getRefs()) {
          addLocations(locations, value.getBean().getValue());
          addLocations(locations, value.getLocal().getValue());
        }
      }
    }

    return locations;
  }

  private static void addLocations(final List<Pair<String, PsiElement>> locations, final SpringBeanPointer beanPointer) {
    if (beanPointer != null) {
      CommonSpringBean springBean = beanPointer.getSpringBean();
      GenericDomValue<String> location = getLocationDomElementValue(springBean);

      if (location != null && !StringUtil.isEmptyOrSpaces(location.getStringValue())) {
        locations.add(new Pair<String, PsiElement>(location.getStringValue(), DomUtil.getValueElement(location)));
      }
    }
  }

  @Nullable
  private static GenericDomValue<String> getLocationDomElementValue(final CommonSpringBean springBean) {
    if (springBean instanceof UtilProperties) {
      UtilProperties utilProperties = (UtilProperties)springBean;
      return utilProperties.getLocation();
    }
    if (springBean instanceof SpringBean) {
      // location for org.springframework.beans.factory.config.PropertiesFactoryBean
      PsiClass psiClass = springBean.getBeanClass();
      if (psiClass != null && UtilProperties.BEAN_CLASS_NAME.equals(psiClass.getQualifiedName())) {
        SpringPropertyDefinition location = SpringUtils.findPropertyByName(springBean, LOCATION_PROPERTY_NAME);
        if (location != null) {
          return (GenericDomValue<String>)location.getValueElement();
        }
      }
    }
    return null;
  }

  public static boolean isPlaceholder(final GenericDomValue genericDomValue) {

    final String stringValue = genericDomValue.getStringValue();

    if (stringValue != null && !StringUtil.isEmptyOrSpaces(stringValue)) {
      List<SpringBaseBeanPointer> configurers = getPlaceholderConfigurerBeans(genericDomValue);

      return isPlaceholder(stringValue, configurers);
    }
    return false;
  }

  public static boolean isPlaceholder(final String stringValue, final List<SpringBaseBeanPointer> configurers) {
    for (SpringBaseBeanPointer configurer : configurers) {
      final CommonSpringBean bean = configurer.getSpringBean();
      if (bean instanceof DomSpringBean) {
        final Pair<String, String> prefixAndSuffix = getPlaceholderPrefixAndSuffix((DomSpringBean)bean);
        final int prefixPos = stringValue.indexOf(prefixAndSuffix.first);
        if (prefixPos >= 0 && prefixPos < stringValue.indexOf(prefixAndSuffix.second)) {
          return true;
        }
      }
    }
    return false;
  }

  public static Pair<String, String> getPlaceholderPrefixAndSuffix(final DomSpringBean placeholderBean) {
    CachedValue<Pair<String, String>> cachedValue = placeholderBean.getUserData(PLACEHOLDER_PREFIX_SUFFIX);
    if (cachedValue == null) {
      cachedValue =
          placeholderBean.getPsiManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<Pair<String, String>>() {
            public Result<Pair<String, String>> compute() {
              return new Result<Pair<String, String>>(getPlaceholderPrefixAndSuffixInner(placeholderBean), placeholderBean.getXmlElement());
            }
          }, false);
      placeholderBean.putUserData(PLACEHOLDER_PREFIX_SUFFIX, cachedValue);
    }
    return cachedValue.getValue();
  }

  public static Pair<String, String> getPlaceholderPrefixAndSuffixInner(DomSpringBean placeholderBean) {
    String prefix = DEFAULT_PLACEHOLDER_PREFIX;
    String suffix = DEFAULT_PLACEHOLDER_SUFFIX;

    final SpringPropertyDefinition userDefinedPrefixProperty =
        SpringUtils.findPropertyByName(placeholderBean, PLACEHOLDER_PREFIX_PROPERTY_NAME);
    if (userDefinedPrefixProperty != null) {
      final String value = SpringUtils.getStringPropertyValue(userDefinedPrefixProperty);
      if (!StringUtil.isEmptyOrSpaces(value)) {
        prefix = value;
      }
    }

    final SpringPropertyDefinition userDefinedSuffixProperty =
        SpringUtils.findPropertyByName(placeholderBean, PLACEHOLDER_SUFFIX_PROPERTY_NAME);
    if (userDefinedSuffixProperty != null) {
      final String value = SpringUtils.getStringPropertyValue(userDefinedSuffixProperty);
      if (!StringUtil.isEmptyOrSpaces(value)) {
        suffix = value;
      }
    }

    return new Pair<String, String>(prefix, suffix);
  }

  @NotNull
  public static PsiReference[] createPlaceholderPropertiesReferences(GenericDomValue genericDomValue) {
    final Map<TextRange, Info> textRanges = getTextRanges(genericDomValue);
    if (textRanges.isEmpty()) return PsiReference.EMPTY_ARRAY;

    final XmlElement valueElement = DomUtil.getValueElement(genericDomValue);
    final List<PsiReference> references = new ArrayList<PsiReference>();
    for (TextRange textRange : textRanges.keySet()) {
      final Info info = textRanges.get(textRange);
      references.add(createPropertyReference(valueElement, textRange, info.text, info.placeholders));
    }
    return references.toArray(new PsiReference[references.size()]);
  }

  private static PropertyReference createPropertyReference(final PsiElement element,
                                                           final TextRange textRange,
                                                           final String value,
                                                           final List<SpringBaseBeanPointer> placeholders) {
    return new PlaceholderPropertyReference(element, textRange, value, placeholders);
  }

  private static Map<TextRange, Info> getTextRanges(final GenericDomValue domValue) {
    final List<SpringBaseBeanPointer> configurerBeans = getPlaceholderConfigurerBeans(domValue);
    if (configurerBeans.size() > 0) {
      PsiElement psiElement = DomUtil.getValueElement(domValue);
      return getTextRanges(configurerBeans, psiElement);
    }
    return Collections.emptyMap();
  }

  private static Map<TextRange, Info> getTextRanges(final List<SpringBaseBeanPointer> configurers, final PsiElement element) {
    Map<TextRange, Info> textRanges = new THashMap<TextRange, Info>();
    for (SpringBaseBeanPointer configurer : configurers) {
      final CommonSpringBean bean = configurer.getSpringBean();
      if (!(bean instanceof DomSpringBean)) continue;

      String text = element.getText();
      final Pair<String, String> prefixAndSuffix = getPlaceholderPrefixAndSuffix((DomSpringBean)bean);
      int prefixIndex = text.indexOf(prefixAndSuffix.first);
      int suffixIndex = text.indexOf(prefixAndSuffix.second);
      while (prefixIndex >= 0 && prefixIndex < suffixIndex) {
        final int offset = prefixIndex + prefixAndSuffix.first.length();
        final int length = suffixIndex - prefixIndex - prefixAndSuffix.first.length();

        final TextRange textRange = TextRange.from(offset, length);
        List<SpringBaseBeanPointer> placeholders =
            textRanges.get(textRange) != null ? textRanges.get(textRange).placeholders : new ArrayList<SpringBaseBeanPointer>();
        placeholders.add(configurer);

        textRanges.put(textRange, new Info(text.substring(offset, offset + length), placeholders,
                                           new TextRange(prefixIndex, suffixIndex + prefixAndSuffix.second.length())));

        if (suffixIndex + 1 >= text.length()) break;

        prefixIndex = text.indexOf(prefixAndSuffix.first, suffixIndex + 1);
        suffixIndex = text.indexOf(prefixAndSuffix.second, suffixIndex + 1);
      }
    }
    return textRanges;
  }

  private static class Info {
    final String text;
    final List<SpringBaseBeanPointer> placeholders;
    final TextRange fullTextRange;

    public Info(final String text, final List<SpringBaseBeanPointer> placeholders, final TextRange fullTextRange) {
      this.text = text;
      this.placeholders = placeholders;
      this.fullTextRange = fullTextRange;
    }
  }

  private static class PlaceholderPropertyReference extends PropertyReference implements LocalQuickFixProvider {
    private final String myKey;
    private final List<SpringBaseBeanPointer> myPlaceholders;

    public PlaceholderPropertyReference(@NotNull PsiElement psiElement,
                                        @NotNull TextRange textRange,
                                        @NotNull final String key,
                                        final List<SpringBaseBeanPointer> placeholders) {
      super(key, psiElement, null, true, textRange);

      myKey = key;
      myPlaceholders = placeholders;
    }

    public Object[] getVariants() {
      Set<Object> variants = new THashSet<Object>();
      for (SpringBaseBeanPointer placeholder : myPlaceholders) {
        for (PropertiesFile propertiesFile : getResources(placeholder.getSpringBean())) {
          addVariantsFromFile(propertiesFile, variants);
        }
      }

      for (SpringBaseBeanPointer placeholder : myPlaceholders) {
        final CommonSpringBean placeholderBean = placeholder.getSpringBean();
        final SpringPropertyDefinition propertyDefinition = SpringUtils.findPropertyByName(placeholderBean, "properties");
        if (propertyDefinition instanceof SpringProperty) {
          for (Prop prop : ((SpringProperty)propertyDefinition).getProps().getProps()) {
            final String keyValue = prop.getKey().getStringValue();
            if (!StringUtil.isEmptyOrSpaces(keyValue)) {
              variants.add(keyValue);
            }
          }

          for (Object str : getValueProperties((SpringProperty)propertyDefinition).keySet()) {
            if (!StringUtil.isEmptyOrSpaces((String)str)) variants.add(str);
          }
        }
      }

      return ArrayUtil.toObjectArray(variants);
    }

    @NotNull
    private static Properties getValueProperties(final SpringProperty property) {
      Properties props = new Properties();
      final String value = property.getValue().getStringValue();
      if (!StringUtil.isEmptyOrSpaces(value)) {
        try {
          props.load(new ByteArrayInputStream(value.getBytes()));
        }
        catch (IOException ignored) {
        }
      }
      return props;
    }

    @NotNull
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
      Set<Property> properties = new HashSet<Property>();
      for (SpringBaseBeanPointer placeholder : myPlaceholders) {
        for (PropertiesFile resource : getResources(placeholder.getSpringBean())) {
          properties.addAll(resource.findPropertiesByKey(myKey));
        }
      }

      Set<DomElement> configurerProperties = getPlaceholderConfigurerProperties(myKey, myPlaceholders);  // IDEADEV-29790 && IDEADEV-31411

      final ResolveResult[] result = new ResolveResult[properties.size() + configurerProperties.size()];
      if (properties.size() > 0 || configurerProperties.size() > 0) {
        int i = 0;
        for (Property property : properties) {
          result[i++] = new PsiElementResolveResult(property);
        }
        for (DomElement configurerProperty : configurerProperties) {
          result[i++] = new PsiElementResolveResult(configurerProperty.getXmlElement());
        }
      }
      else if (System.getProperties().getProperty(myKey) != null) {
        return new ResolveResult[]{new PsiElementResolveResult(getElement())};

      }
      return result;
    }

    @NotNull
    private static Set<DomElement> getPlaceholderConfigurerProperties(@NotNull final String key, final List<SpringBaseBeanPointer> placeholders) {
      final Set<DomElement> proprs = new HashSet<DomElement>();
      for (SpringBaseBeanPointer placeholder : placeholders) {
        final CommonSpringBean placeholderBean = placeholder.getSpringBean();
        final SpringPropertyDefinition propertyDefinition = SpringUtils.findPropertyByName(placeholderBean, "properties");
        if (propertyDefinition instanceof SpringProperty) {
          for (Prop prop : ((SpringProperty)propertyDefinition).getProps().getProps()) {
            if (key.equals(prop.getKey().getStringValue())) {
              proprs.add(prop);
            }
          }
          for (Object propName : getValueProperties((SpringProperty)propertyDefinition).keySet() ) {
            if (key.equals(propName)) {
              proprs.add(((SpringProperty)propertyDefinition).getValue());
              break;
            }
          }
        }
      }

      return proprs;
    }

    public LocalQuickFix[] getQuickFixes() {
      List<PropertiesFile> propertiesFiles = new ArrayList<PropertiesFile>();
      for (SpringBaseBeanPointer placeholder : myPlaceholders) {
        propertiesFiles.addAll(getResources(placeholder.getSpringBean()));

      }
      CreatePropertyFix fix = new CreatePropertyFix(getElement(), myKey, propertiesFiles);

      return new LocalQuickFix[]{fix};
    }
  }

  public static Collection<String> getExpandedVariants(final GenericDomValue value) {
    final String stringValue = value.getStringValue();
    final Map<TextRange, Info> textRanges = getTextRanges(value);
    if (textRanges.isEmpty()) return ContainerUtil.createMaybeSingletonList(stringValue);

    final TextRange[] ranges = textRanges.keySet().toArray(new TextRange[textRanges.size()]);
    Arrays.sort(ranges, new Comparator<TextRange>() {
      public int compare(final TextRange o1, final TextRange o2) {
        return o1.getStartOffset() - o2.getStartOffset();
      }
    });
    final XmlElement valueElement = DomUtil.getValueElement(value);
    assert valueElement != null;
    final THashSet<String> result = new THashSet<String>();
    final ResolveResult[][] variants = new ResolveResult[ranges.length][];
    for (int i = 0; i < ranges.length; i++) {
      final TextRange range = ranges[i];
      final Info info = textRanges.get(range);
      final PropertyReference reference = createPropertyReference(valueElement, range, info.text, info.placeholders);
      final ResolveResult[] results = reference.multiResolve(false);
      variants[i] = results;
    }
    final int[] indices = new int[ranges.length];
    final ElementManipulator<XmlElement> manipulator = ElementManipulators.getManipulator(valueElement);
    assert manipulator != null;
    final int offsetBase = -manipulator.getRangeInElement(valueElement).getStartOffset();
    while (true) {
      int offset = offsetBase;
      final StringBuilder sb = new StringBuilder(stringValue);
      for (int i = 0; i < indices.length; i++) {
        final Info info = textRanges.get(ranges[i]);
        if (variants[i].length == 0) continue;
        final ResolveResult resolveResult = variants[i][indices[i]];
        final PsiElement element = resolveResult.getElement();
        if (!(element instanceof Property)) continue;
        final Property property = (Property)element;
        final String replacement = property.getValue();
        if (replacement == null) continue;
        sb.replace(offset + info.fullTextRange.getStartOffset(), offset + info.fullTextRange.getEndOffset(), replacement);
        offset += replacement.length() - info.fullTextRange.getLength();
      }
      result.add(sb.toString());

      boolean quit = true;
      for (int idx = 0; idx < indices.length; idx++) {
        if (indices[idx] < variants[idx].length - 1) {
          quit = false;
          indices[idx]++;
          for (int i = 0; i < idx; i++) {
            indices[i] = 0;
          }
          break;
        }
      }
      if (quit) break;

    }
    return result;
  }
}
