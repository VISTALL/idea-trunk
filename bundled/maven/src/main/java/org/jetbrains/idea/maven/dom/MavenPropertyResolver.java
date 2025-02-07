package org.jetbrains.idea.maven.dom;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.beanutils.BeanAccessLanguageException;
import org.apache.commons.beanutils.BeanUtils;
import org.jetbrains.idea.maven.dom.model.MavenDomProfile;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.dom.model.MavenDomProperties;
import org.jetbrains.idea.maven.embedder.MavenEmbedderFactory;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenPropertyResolver {
  public static final Pattern PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

  public static String resolve(Module module,
                               String text,
                               Properties additionalProperties,
                               String propertyEscapeString,
                               String escapedCharacters) {
    MavenProjectsManager manager = MavenProjectsManager.getInstance(module.getProject());
    MavenProject mavenProject = manager.findProject(module);
    if (mavenProject == null) return text;
    return doResolve(text, mavenProject, additionalProperties, propertyEscapeString, escapedCharacters, new Stack<String>());
  }

  public static String resolve(String text, MavenDomProjectModel projectDom) {
    MavenProject mavenProject = MavenDomUtil.findProject(projectDom);
    if (mavenProject == null) return text;
    return doResolve(text, mavenProject, collectPropertiesFromDOM(mavenProject, projectDom), null, null, new Stack<String>());
  }

  private static Properties collectPropertiesFromDOM(MavenProject project, MavenDomProjectModel projectDom) {
    Properties result = new Properties();

    collectPropertiesFromDOM(projectDom.getProperties(), result);

    List<String> activePropfiles = project.getActiveProfilesIds();
    for (MavenDomProfile each : projectDom.getProfiles().getProfiles()) {
      XmlTag idTag = each.getId().getXmlTag();
      if (idTag == null || !activePropfiles.contains(idTag.getValue().getText())) continue;
      collectPropertiesFromDOM(each.getProperties(), result);
    }

    return result;
  }

  private static void collectPropertiesFromDOM(MavenDomProperties props, Properties result) {
    XmlTag propsTag = props.getXmlTag();
    if (propsTag != null) {
      for (XmlTag each : propsTag.getSubTags()) {
        result.setProperty(each.getName(), each.getValue().getText());
      }
    }
  }

  private static String doResolve(String text,
                                  MavenProject project,
                                  Properties additionalProperties,
                                  String escapeString,
                                  String escapedCharacters,
                                  Stack<String> resolutionStack) {
    Matcher matcher = PATTERN.matcher(text);

    StringBuffer buff = new StringBuffer();
    StringBuffer dummy = new StringBuffer();
    int last = 0;
    while (matcher.find()) {
      String propText = matcher.group();
      String propName = matcher.group(1);

      int tempLast = last;
      last = matcher.start() + propText.length();

      if (escapeString != null) {
        int pos = matcher.start();
        if (pos > escapeString.length() && text.substring(pos - escapeString.length(), pos).equals(escapeString)) {
          buff.append(text.substring(tempLast, pos - escapeString.length()));
          buff.append(propText);
          matcher.appendReplacement(dummy, "");
          continue;
        }
      }

      String resolved = doResolveProperty(propName, project, additionalProperties);
      if (resolved == null) resolved = propText;
      if (!resolved.equals(propText) && !resolutionStack.contains(propName)) {
        resolutionStack.push(propName);
        resolved = doResolve(resolved, project, additionalProperties, escapeString, escapedCharacters, resolutionStack);
        resolutionStack.pop();
      }
      matcher.appendReplacement(buff, Matcher.quoteReplacement(escapeCharacters(resolved, escapedCharacters)));
    }
    matcher.appendTail(buff);

    return buff.toString();
  }

  private static String escapeCharacters(String text, String escapedCharacters) {
    if (StringUtil.isEmpty(escapedCharacters)) return text;

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (escapedCharacters.indexOf(ch) != -1) {
        builder.append('\\');
      }
      builder.append(ch);
    }
    return builder.toString();
  }

  private static String doResolveProperty(String propName, MavenProject project, Properties additionalProperties) {
    String result;

    result = MavenEmbedderFactory.collectSystemProperties().getProperty(propName);
    if (result != null) return result;

    if (propName.startsWith("project.") || propName.startsWith("pom.")) {
      if (propName.startsWith("pom.")) {
        propName = propName.substring("pom.".length());
      }
      else {
        propName = propName.substring("project.".length());
      }
    }

    if (propName.equals("basedir")) return project.getDirectory();

    try {
      result = BeanUtils.getNestedProperty(project.getMavenModel(), propName);
    }
    catch (IllegalAccessException e) {
    }
    catch (BeanAccessLanguageException e) {
    }
    catch (InvocationTargetException e) {
    }
    catch (NoSuchMethodException e) {
    }
    catch (IllegalArgumentException e) {
    }
    if (result != null) return result;

    result = additionalProperties.getProperty(propName);
    if (result != null) return result;

    result = project.getProperties().getProperty(propName);
    if (result != null) return result;

    return null;
  }
}
