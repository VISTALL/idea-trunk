package org.jetbrains.android.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.libraries.Library;
import com.android.sdklib.IAndroidTarget;
import org.jdom.Element;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.android.sdk.AndroidSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene.Kudelevsky
 */
public class AndroidFacetConfiguration implements FacetConfiguration {
  public String PLATFORM_NAME = "";

  private AndroidPlatform myAndroidPlatform;
  private AndroidFacet myFacet = null;

  @Nullable
  public AndroidPlatform getAndroidPlatform() {
    if (myAndroidPlatform == null) {
      Library library = LibraryTablesRegistrar.getInstance().getLibraryTable().getLibraryByName(PLATFORM_NAME);
      if (library != null) {
        myAndroidPlatform = AndroidPlatform.parse(library, null, null);
      }
    }
    return myAndroidPlatform;
  }

  @Nullable
  public AndroidSdk getAndroidSdk() {
    return myAndroidPlatform != null ? myAndroidPlatform.getSdk() : null;
  }

  @Nullable
  public IAndroidTarget getAndroidTarget() {
    return myAndroidPlatform != null ? myAndroidPlatform.getTarget() : null;
  }

  @Nullable
  public String getSdkPath() {
    AndroidSdk sdk = getAndroidSdk();
    return sdk != null ? sdk.getLocation() : null;
  }

  @Nullable
  public AndroidFacet getFacet() {
    return myFacet;
  }

  public void setFacet(@NotNull AndroidFacet facet) {
    this.myFacet = facet;
    facet.androidPlatformChanged();
  }

  public void setAndroidPlatform(@Nullable AndroidPlatform platform) {
    myAndroidPlatform = platform;
    PLATFORM_NAME = platform != null ? platform.getName() : "";
    if (myFacet != null) {
      myFacet.androidPlatformChanged();
    }
  }

  public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
    return new FacetEditorTab[]{new AndroidFacetEditorTab(editorContext, this)};
  }

  public void readExternal(Element element) throws InvalidDataException {
    DefaultJDOMExternalizer.readExternal(this, element);
    myAndroidPlatform = null;
  }

  public void writeExternal(Element element) throws WriteExternalException {
    DefaultJDOMExternalizer.writeExternal(this, element);
  }
}
