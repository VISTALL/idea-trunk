package org.jetbrains.plugins.groovy.mvc.plugins;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

/**
 * User: Dmitry.Krasilschikov
 * Date: 31.08.2008
 */
public class MvcPluginDescriptor {
  private String myName;
  private String myRelease;
  private String myTitle;
  private String myAuthor;
  private String myUrl;
  private String myEmail;
  private String myZipRelease;

  private String myDescription;
  private LinkedHashSet<String> myVersions = new LinkedHashSet<String>();

  public static MvcPluginDescriptor[] EMPTY_ARRAY = new MvcPluginDescriptor[0];

  public String getName() {
    return myName;
  }

  public String getRelease() {
    return myRelease;
  }

  public String getTitle() {
    return myTitle;
  }

  public void setName(final String name) {
    myName = name;
  }

  public void setRelease(final String release) {
    myRelease = release;
  }

  public void setTitle(final String title) {
    myTitle = title;
  }

  public String getAuthor() {
    return myAuthor;
  }

  public void setAuthor(final String author) {
    myAuthor = author;
  }

  public String getUrl() {
    return myUrl;
  }

  public void setUrl(final String url) {
    myUrl = url;
  }

  public String getEmail() {
    return myEmail;
  }

  public void setEmail(final String email) {
    myEmail = email;
  }

  @NotNull
  public LinkedHashSet<String> getVersions() {
    return myVersions;
  }

  public void setVersions(@NotNull final LinkedHashSet<String> versions) {
    myVersions = versions;
  }

  public String getDescription() {
    return myDescription;
  }

  public void setDescription(final String description) {
    myDescription = description;
  }

  public String getZipRelease() {
    return myZipRelease;
  }

  public void setZipRelease(final String zipRelease) {
    myZipRelease = zipRelease;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final MvcPluginDescriptor that = (MvcPluginDescriptor)o;

    if (myName != null ? !myName.equals(that.myName) : that.myName != null) return false;
    if (myRelease != null ? !myRelease.equals(that.myRelease) : that.myRelease != null) return false;
    if (myTitle != null ? !myTitle.equals(that.myTitle) : that.myTitle != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myName != null ? myName.hashCode() : 0;
    result = 31 * result + (myRelease != null ? myRelease.hashCode() : 0);
    result = 31 * result + (myTitle != null ? myTitle.hashCode() : 0);
    return result;
  }
}
