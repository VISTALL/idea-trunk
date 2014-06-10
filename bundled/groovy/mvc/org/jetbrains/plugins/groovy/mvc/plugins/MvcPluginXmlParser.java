package org.jetbrains.plugins.groovy.mvc.plugins;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import groovy.util.Node;
import groovy.util.NodeList;
import groovy.util.XmlParser;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: Dmitry.Krasilschikov
 * Date: 08.10.2008
 */
public class MvcPluginXmlParser {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginXmlParser");

  private MvcPluginXmlParser() {
  }

  @Nullable
  public static MvcPluginDescriptor parse(VirtualFile file) {
    try {
      return parse(file.getInputStream());
    }
    catch (IOException e) {
      LOG.error(e);
    }
    return null;
  }

  public static MvcPluginDescriptor parse(InputStream inputStream) {
    MvcPluginDescriptor mvcPluginDescriptor = new MvcPluginDescriptor();

    Node plugin = null;
    try {
      plugin = new XmlParser().parse(inputStream);
    }
    catch (IOException e) {
      LOG.error(e);
    }
    catch (SAXException e) {
      LOG.error(e);
    }
    catch (ParserConfigurationException e) {
      LOG.error(e);
    }

    String name = (String)plugin.get("@name");
    String version = (String)plugin.get("@version");

    mvcPluginDescriptor.setName(name);
    mvcPluginDescriptor.setRelease(version);

    final Object authorList = plugin.get("author");
    if (authorList != null && authorList instanceof NodeList) {
      final NodeList list = (NodeList)authorList;

      if (list.size() > 0) {
        String author = ((Node)list.get(0)).text();
        if (!"Your name".equals(author)) {
          mvcPluginDescriptor.setAuthor(author);
        }
      }
    }

    final Object authorEmailList = plugin.get("authorEmail");
    if (authorEmailList != null && authorEmailList instanceof NodeList) {
      final NodeList list = (NodeList)authorEmailList;

      if (list.size() > 0) {
        String authorEmail = ((Node)list.get(0)).text();
        mvcPluginDescriptor.setEmail(authorEmail);
      }
    }


    final Object titleList = plugin.get("title");

    if (titleList != null && titleList instanceof NodeList) {
      final NodeList list = (NodeList)titleList;
      if (list.size() > 0) {
        String title = ((Node)list.get(0)).text();
        if (!"Plugin summary/headline".equals(title)) {
          mvcPluginDescriptor.setTitle(title);
        }
      }
    }


    final Object descriptionList = plugin.get("description");
    if (descriptionList != null && descriptionList instanceof NodeList) {
      final NodeList list = (NodeList)descriptionList;
      if (list.size() > 0) {
        String description = ((Node)list.get(0)).text();
        if (!"Brief description of the plugin.".equals(description)) {
          mvcPluginDescriptor.setDescription(description);
        }
      }
    }

    final Object documentationList = plugin.get("documentation");
    if (documentationList != null && documentationList instanceof NodeList) {
      final NodeList list = (NodeList)documentationList;
      if (list.size() > 0) {
        String documentation = ((Node)list.get(0)).text();
        mvcPluginDescriptor.setUrl(documentation);
      }
    }

    return mvcPluginDescriptor;
  }
}
