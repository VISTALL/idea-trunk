package com.intellij.eclipse.export.wizard;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.WorkbenchImages;
import org.osgi.framework.Bundle;

import java.net.URL;

public class ExporterImages {
  private static final String IMAGE_FILE_NAME = "icons/big.jpg";
  private static final String IMAGE_ID = "Export.Wizard.Banner";

  private static Bundle pluginBundle = Platform.getBundle("com.intellij.eclipse.export");

  static {
    URL url = pluginBundle.getEntry(IMAGE_FILE_NAME);
    ImageDescriptor desc = ImageDescriptor.createFromURL(url);
    WorkbenchImages.declareImage(IMAGE_ID, desc, true);
  }

  public static ImageDescriptor getImageDescriptor() {
    return WorkbenchImages.getImageDescriptor(ExporterImages.IMAGE_ID);
  }
}