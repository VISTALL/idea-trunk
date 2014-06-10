/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.spring;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineBuilder;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.jsp.JspSpiUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.impl.model.CustomBeanWrapperImpl;
import com.intellij.spring.schemas.SpringSchemaProvider;
import com.intellij.util.*;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author peter
 */
@State(
    name = "CustomBeanRegistry",
    storages = {
        @Storage(id = "default", file = "$WORKSPACE_FILE$"),
        @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/customSpringBeans.xml", scheme = StorageScheme.DIRECTORY_BASED)
    })
public class CustomBeanRegistry implements PersistentStateComponent<CustomBeanRegistry.MyBean> {
  private static boolean isDebug() {
    return false;
  }
  private static final int CURRENT_VERSION = 1;
  private static final Logger LOG = Logger.getInstance("#com.intellij.spring.CustomBeanRegistry");

  @NonNls private static final String CUSTOM_BEAN_PARSER = "com.intellij.spring.model.xml.custom.CustomBeanParser";
  private Map<String, List<CustomBeanInfo>> myText2Infos = new THashMap<String, List<CustomBeanInfo>>();
  private Map<MyQName,CustomBeanInfo> myPolicies = new THashMap<MyQName, CustomBeanInfo>();
  @NonNls private static final String FAKE_ID = "IntelliJIDEARulezzz";

  @NonNls public static final String CUSTOM_SPRING_BEANS_PARSING_TIMEOUT = "custom.spring.beans.parsing.timeout";

  private static int getTimeout() {
    try {
      return Integer.parseInt(System.getProperty(CUSTOM_SPRING_BEANS_PARSING_TIMEOUT));
    }
    catch (NumberFormatException e) {
      return isDebug() ? 10000000 : 10000;
    }
  }

  public MyBean getState() {
    final MyBean bean = new MyBean();
    bean.map = new HashMap<String, List<CustomBeanInfo>>();
    for (final String s : myText2Infos.keySet()) {
      final List<CustomBeanInfo> infos = myText2Infos.get(s);
      if (infos != null && !infos.isEmpty()) {
        bean.map.put(s, infos);
      }
    }
    bean.policies = myPolicies;
    return bean;
  }

  public void loadState(final MyBean state) {
    if (state.version == CURRENT_VERSION) {
      myText2Infos = state.map;
      myPolicies = state.policies;
    }
  }

  public ParseResult parseBeans(Collection<XmlTag> tags) {
    ParseResult result = ParseResult.EMPTY_PARSE_RESULT;
    for (final XmlTag tag : tags) {
      if (tag.isValid()) {
        result = result.merge(parseBean(tag));
      }
    }
    return result;
  }

  public ParseResult parseBean(XmlTag tag) {
    final String text = getIdealBeanText(tag);
    try {
      final Module module = ModuleUtil.findModuleForPsiElement(tag);
      if (module == null) return ParseResult.EMPTY_PARSE_RESULT;

      final ParseResult result = getCustomBeans(createTag(text, tag.getProject()), module);
      myText2Infos.put(text, result.beans == null ? Collections.<CustomBeanInfo>emptyList() : result.beans);
      return result;
    }
    catch (IncorrectOperationException e) {
      return new ParseResult(e);
    }
  }

  public static CustomBeanRegistry getInstance(Project project) {
    return ServiceManager.getService(project, CustomBeanRegistry.class);
  }

  @Nullable
  public List<CustomBeanInfo> getParseResult(final XmlTag tag) {
    final CustomBeanInfo policy = myPolicies.get(new MyQName(tag.getNamespace(), tag.getLocalName()));
    if (policy != null) {
      final CustomBeanInfo info = new CustomBeanInfo(policy);
      info.beanName = tag.getAttributeValue(policy.idAttribute);
      return Arrays.asList(info);
    }

    return myText2Infos.get(getIdealBeanText(tag));
  }

  @NotNull
  public static String getIdealBeanText(final XmlTag tag) {
    final Set<String> usedNamespaces = collectReferencedNamespaces(tag);

    String text = tag.getText();

    try {
      final XmlTag copy = createTag(text, tag.getProject());
      XmlTag parent = tag;
      while (parent != null) {
        for (final XmlAttribute attribute : parent.getAttributes()) {
          if (attribute.isNamespaceDeclaration()) {
            final String prefix = "xmlns".equals(attribute.getName()) ? "" : attribute.getLocalName();
            final String ns = copy.getNamespaceByPrefix(prefix);
            if (StringUtil.isEmpty(ns) && usedNamespaces.contains(attribute.getDisplayValue())) {
              copy.add(attribute);
            }
          }
        }
        parent = parent.getParentTag();
      }

      text = copy.getText();
      final Document document;
      try {
        document = JDOMUtil.loadDocument(text);
      }
      catch (IOException e) {
        return text;
      }
      catch (JDOMException e) {
        return text;
      }
      return JDOMUtil.writeDocument(document, "\n");
    }
    catch (IncorrectOperationException e) {
      LOG.error(e);
      return text;
    }
  }

  private static Set<String> collectReferencedNamespaces(final XmlTag tag) {
    final Set<String> usedNamespaces = new THashSet<String>();
    tag.accept(new XmlElementVisitor(){
      @Override
      public final void visitXmlTag(final XmlTag tag) {
        usedNamespaces.add(tag.getNamespace());
        for (final XmlAttribute attribute : tag.getAttributes()) {
          visitXmlAttribute(attribute);
        }
        for (final XmlTag xmlTag : tag.getSubTags()) {
          visitXmlTag(xmlTag);
        }
      }

      @Override
      public final void visitXmlAttribute(final XmlAttribute attribute) {
        usedNamespaces.add(attribute.getNamespace());
      }
    });
    return usedNamespaces;
  }

  private static XmlTag createTag(final String text, final Project project) throws IncorrectOperationException {
    return XmlElementFactory.getInstance(project).createTagFromText(text);
  }

  @NotNull
  public static XmlTag getActualSourceTag(final CustomBeanInfo info, XmlTag tag) {
    final List<Integer> path = info.path;
    for (Integer index : path) {
      final XmlTag parent = tag;
      final XmlTag[] subTags = parent.getSubTags();
      final int i = index.intValue();
      tag = subTags[i];
      if (tag == null) {
        LOG.error("parent: " + parent.getText() + "\nindex: " + i + "\nsubTags: " + Arrays.toString(subTags));
      }
    }
    return tag;
  }

  public void addBeanPolicy(@NotNull final String namespace, @NotNull final String localName, CustomBeanInfo info) {
    assert info.beanName == null;
    assert info.idAttribute != null;
    myPolicies.put(new MyQName(namespace, localName), info);
  }

  public static class MyBean {
    public int version = CURRENT_VERSION;

    public Map<String,List<CustomBeanInfo>> map = new THashMap<String, List<CustomBeanInfo>>();
    public Map<MyQName,CustomBeanInfo> policies = new THashMap<MyQName,CustomBeanInfo>();
  }

  public static class MyQName {
    public String namespace;
    public String localName;

    public MyQName() {
    }

    public MyQName(final String namespace, final String localName) {
      this.namespace = namespace;
      this.localName = localName;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (!(o instanceof MyQName)) return false;

      final MyQName myQName = (MyQName)o;

      if (!localName.equals(myQName.localName)) return false;
      if (!namespace.equals(myQName.namespace)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = namespace.hashCode();
      result = 31 * result + localName.hashCode();
      return result;
    }
  }

  private static void computeUrls(Module module, final PathsList list) {
    final File springPluginClassesLocation = new File(PathUtil.getJarPathForClass(CustomBeanWrapperImpl.class));
    if (springPluginClassesLocation.isFile()) {//build
      File customNsLocation = new File(springPluginClassesLocation.getParent(), "customNs");
      list.add(new File(customNsLocation, "customNs.jar").getAbsolutePath());
    }
    else {//development mode
      list.add(new File(springPluginClassesLocation.getParent(), "spring-customNs").getAbsolutePath());
    }
    JspSpiUtil.processClassPathItems(null, module, new Consumer<VirtualFile>() {
      public void consume(final VirtualFile file) {
        list.add(file);
      }
    });
    for (VirtualFile file : ModuleRootManager.getInstance(module).getFiles(OrderRootType.SOURCES)) {
      list.add(file);
    }
  }

  @NotNull
  private static ParseResult getCustomBeans(@NotNull XmlTag tag, @NotNull final Module module) {
    final Map<String, String> handlersToRun = findHandlersToRun(module, tag);
    final String namespace = tag.getNamespace();
    if (!handlersToRun.containsKey(namespace)) {
      return new ParseResult(SpringBundle.message("parse.no.namespace.handler", namespace));
    }

    final JavaParameters javaParameters = new JavaParameters();
    javaParameters.setJdk(ModuleRootManager.getInstance(module).getSdk());
    javaParameters.setMainClass("com.intellij.spring.model.xml.custom.CustomBeanParser");
    if (isDebug()) {
      javaParameters.getVMParametersList().addParametersString("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5239");
    }
    computeUrls(module, javaParameters.getClassPath());
    final Process process;
    try {
      process = CommandLineBuilder.createFromJavaParameters(javaParameters, true).createProcess();
    }
    catch (ExecutionException e) {
      return new ParseResult(e);
    }

    final OSProcessHandler handler = new OSProcessHandler(process, "");

    @NonNls PrintWriter writer = new PrintWriter(handler.getProcessInput());
    handler.startNotify();
    try {
      final int timeout = Math.max(getTimeout(), tag.getTextLength() * 150);
      writer.println(timeout);

      ParseResult result = invokeParser(writer, handler, tag, timeout);

      if (result.getStackTrace() != null && tag.getAttributeValue("id") == null) {
        try {
          tag.setAttribute("id", FAKE_ID);
        }
        catch (IncorrectOperationException e) {
          LOG.error(e);
        }
        final ParseResult result1 = invokeParser(writer, handler, tag, timeout);
        final List<CustomBeanInfo> list = result1.getBeans();
        if (list != null) {
          for (final CustomBeanInfo info : list) {
            if (FAKE_ID.equals(info.beanName) && info.path.isEmpty()) {
              info.beanName = null;
              info.idAttribute = "id";
            }
          }
          result = result1;
        }
      }

      List<CustomBeanInfo> infos = result.getBeans();
      if (infos != null) {
        guessIdAttributeNames(writer, handler, tag, infos, timeout);
      }
      return result;
    }
    catch (Throwable e) {
      return new ParseResult(e);
    }
    finally {
      writer.close();
      process.destroy();
    }
  }

  @NotNull
  private static ParseResult invokeParser(@NonNls final PrintWriter writer, final OSProcessHandler handler, final XmlTag tag,
                                          final int timeout) {
    final Ref<ParseResult> result = Ref.create(null);
    final Semaphore semaphore = new Semaphore();
    semaphore.down();

    final StringBuilder other = new StringBuilder();

    handler.addProcessListener(new ProcessAdapter() {
      StringBuilder sb = new StringBuilder();

      @Override
      public void onTextAvailable(final ProcessEvent event, final Key outputType) {
        try {
          if (outputType != ProcessOutputTypes.STDOUT) {
            other.append(event.getText());
            return;
          }

          sb.append(event.getText().replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n"));
          final int i = sb.indexOf("\n\n");
          if (i < 0) return;

          @NonNls String input = sb.substring(0, i);
          sb.delete(0, i + 2);
          String[] lines = input.split("\n");
          int k = 0;
          while (k < lines.length && !"exception".equals(lines[k]) && !"timeout".equals(lines[k]) && !"result".equals(lines[k])) k++;

          if (k >= lines.length) {
            setResult(new ParseResult("Internal error parsing bean; output:\n" + input));
            return;
          }

          @NonNls final String first = lines[k];
          if ("exception".equals(first)) {
            assert lines.length == k + 2;
            setResult(new ParseResult(StringUtil.unescapeStringCharacters(lines[k + 1]), true));
            return;
          }
          if ("timeout".equals(first)) {
            assert lines.length == k + 1;
            setResult(new ParseResult(SpringBundle.message("timeout.parsing.bean")));
            return;
          }
          if ("result".equals(first)) {
            List<CustomBeanInfo> list = new SmartList<CustomBeanInfo>();
            String nextLine = lines[k + 1];
            boolean hasInfras = "has_infrastructures".equals(nextLine);
            if (!hasInfras) {
              assert "no_infrastructures".equals(nextLine);
            }
            int j = k + 2;
            while (j < lines.length) {
              nextLine = lines[j++];
              assert "info".equals(nextLine);
              CustomBeanInfo info = new CustomBeanInfo();
              while (!"info_end".equals(lines[j])) {
                @NonNls final String prop = lines[j++];
                @NonNls final String propValue = StringUtil.unescapeStringCharacters(lines[j++]);
                if ("beanName".equals(prop)) {
                  info.beanName = propValue;
                } else if ("beanClassName".equals(prop)) {
                  info.beanClassName = propValue;
                } else if ("constructorArgumentCount".equals(prop)) {
                  info.constructorArgumentCount = Integer.parseInt(propValue);
                } else if ("factoryMethodName".equals(prop)) {
                  info.factoryMethodName = propValue;
                } else if ("factoryBeanName".equals(prop)) {
                  info.factoryBeanName = propValue;
                } else {
                  assert "path".equals(prop) : prop;
                  assert propValue.startsWith("x"); //otherwise string may be empty
                  final String separated = propValue.substring(1);
                  info.path = StringUtil.isEmpty(separated)
                              ? Collections.<Integer>emptyList()
                              : ContainerUtil.map(separated.split(";"), new Function<String, Integer>() {
                    public Integer fun(final String s) {
                      return Integer.parseInt(s);
                    }
                  });
                }
              }
              list.add(info);
              j++;
            }
            setResult(new ParseResult(list, hasInfras));
          }
        }
        catch (Throwable e) {
          setResult(new ParseResult(e));
        }
      }

      private void setResult(final ParseResult value) {
        result.set(value);
        handler.removeProcessListener(this);
        semaphore.up();
      }

      @Override
      public void processTerminated(final ProcessEvent event) {
        if (other.length() == 0 || sb.length() == 0) {
          setResult(new ParseResult(SpringBundle.message("process.unexpectedly.terminated", "")));
          return;
        }
        @NonNls final String output = ":\n\nSTDOUT:\n" + sb + "\n\nOTHER:\n" + other;
        setResult(new ParseResult(SpringBundle.message("process.unexpectedly.terminated", output)));
      }
    });

    writer.println("input");
    writer.println(StringUtil.escapeStringCharacters(tag.getText()));
    writer.flush();

    final boolean inTime = semaphore.waitFor(timeout);

    final ParseResult parseResult = result.get();
    if (parseResult == null) {
      if (inTime) {
        return new ParseResult(other.toString(), true);
      }

      return new ParseResult(SpringBundle.message("timeout.parsing.bean"));
    }
    return parseResult;
  }

  private static void guessIdAttributeNames(@NonNls PrintWriter writer, OSProcessHandler reader, XmlTag tag, final List<CustomBeanInfo> list,
                                            final int timeout)
      throws IncorrectOperationException {
    String[] fakeNames = new String[list.size()];
    String[] idAttrs = new String[list.size()];
    boolean hasFakeIds = false;
    for (int i = 0; i < list.size(); i++) {
      CustomBeanInfo info = list.get(i);
      if (info.idAttribute != null) continue;

      final XmlTag sourceTag = getActualSourceTag(info, tag);
      final String id = info.beanName;
      final XmlAttribute idAttr = id == null ? null : ContainerUtil.find(sourceTag.getAttributes(), new Condition<XmlAttribute>() {
        public boolean value(final XmlAttribute xmlAttribute) {
          return !xmlAttribute.isNamespaceDeclaration() && id.equals(xmlAttribute.getDisplayValue());
        }
      });
      if (idAttr != null) {
        String fakeName = FAKE_ID + i;
        fakeNames[i] = fakeName;
        idAttr.setValue(fakeName);
        idAttrs[i] = idAttr.getLocalName();
        hasFakeIds = true;
      }
    }

    if (hasFakeIds) {
      List<CustomBeanInfo> withFakes = invokeParser(writer, reader, tag, timeout).getBeans();
      if (withFakes != null && withFakes.size() == list.size()) {
        for (int i = 0; i < fakeNames.length; i++) {
          String name = fakeNames[i];
          if (name != null && name.equals(withFakes.get(i).beanName)) {
            list.get(i).idAttribute = idAttrs[i];
          }
        }
      }
    }
  }

  private static Map<String,String> findHandlersToRun(@NotNull final Module module, @NotNull final XmlTag tag) {
    if (SpringConstants.INSIDER_NAMESPACES.contains(tag.getNamespace())) return Collections.emptyMap();

    final Map<String, String> handlers = SpringSchemaProvider.getHandlers(module);
    if(handlers.isEmpty()) return Collections.emptyMap();

    final Set<String> referencedNamespaces = collectReferencedNamespaces(tag);

    final HashMap<String, String> handlersToRun = new HashMap<String, String>(referencedNamespaces.size());
    for (String namespace : handlers.keySet()) {
      if (referencedNamespaces.contains(namespace)) {
        handlersToRun.put(namespace, handlers.get(namespace));
      }
    }

    return handlersToRun;
  }



  public static class ParseResult {
    static final ParseResult EMPTY_PARSE_RESULT = new ParseResult(Collections.<CustomBeanInfo>emptyList(), false);

    @Nullable List<CustomBeanInfo> beans;
    boolean hasInfrastructures;
    @Nullable String errorMessage;
    @Nullable String stackTrace;

    private ParseResult(List<CustomBeanInfo> beans, boolean hasInfrastructures) {
      this.beans = beans;
      this.hasInfrastructures = hasInfrastructures;
    }

    private static String getStackTrace(Throwable e) {
      return StringUtil.getThrowableText(e);
    }


    private ParseResult(Throwable t) {
      this(getStackTrace(t), true);
    }

    private ParseResult(final String errorMessage) {
      this(errorMessage, false);
    }

    private ParseResult(final String errorMessage, boolean isStackTrace) {
      if (!isStackTrace) {
        this.errorMessage = errorMessage;
      } else {
        stackTrace = StringUtil.convertLineSeparators(errorMessage);
        int i = stackTrace.indexOf(CUSTOM_BEAN_PARSER);
        if (i >= 0) {
          i = stackTrace.lastIndexOf('\n', i);
          if (i >= 0) {
            stackTrace = stackTrace.substring(0, i);
          }
        }
      }
    }

    @Nullable
    public String getErrorMessage() {
      return errorMessage;
    }

    @Nullable
    public String getStackTrace() {
      return stackTrace;
    }

    @Nullable
    public List<CustomBeanInfo> getBeans() {
      return beans;
    }

    public boolean hasInfrastructureBeans() {
      return hasInfrastructures;
    }

    public boolean hasErrors() {
      return stackTrace != null || errorMessage != null;
    }

    public ParseResult merge(ParseResult with) {
      final ParseResult result = new ParseResult((List<CustomBeanInfo>)null, hasInfrastructures || with.hasInfrastructures);
      result.stackTrace = stackTrace == null ? with.stackTrace : stackTrace;
      result.errorMessage = errorMessage == null ? with.errorMessage : errorMessage;
      result.beans = beans == null ? with.beans : with.beans == null ? beans : ContainerUtil.concat(beans, with.beans);
      return result;
    }
  }

}


