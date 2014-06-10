package com.advancedtools.webservices.utils.ui;

/**
 * @author Maxim
 */
public class ValidationUtils {
  public static boolean validatePackageName(String packagePrefix) {
    if (packagePrefix != null) {
      String packagePrefixString = packagePrefix.trim();

      for (int i = packagePrefixString.length() - 1; i >= 0; --i) {
        char ch = packagePrefixString.charAt(i);

        if (!Character.isJavaIdentifierPart(ch) && (ch != '.' || i == packagePrefixString.length() - 1)) {
          return false;
        }
      }
    }

    return true;
  }

  public static boolean validateClassName(String classnameToCreate) {
    if (classnameToCreate != null) {
      classnameToCreate = classnameToCreate.trim();

      for (int i = classnameToCreate.length() - 1; i > 0; --i) {
        char ch = classnameToCreate.charAt(i);

        if (!Character.isJavaIdentifierPart(ch)) {
          return false;
        }
      }

      return classnameToCreate.length() > 0 && Character.isJavaIdentifierStart(classnameToCreate.charAt(0));
    }

    return false;
  }
}
