package com.sixrr.ejbmetrics;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.util.PsiTreeUtil;

public class PackageUtil {

    public static String calculatePackageName(PsiClass aClass) {
        final PsiJavaFile file = PsiTreeUtil.getParentOfType(aClass, PsiJavaFile.class);
        if (file == null) {
            return "";
        }
        return file.getPackageName();
    }

    public static PsiPackage findPackage(PsiClass referencedClass) {
        final String referencedPackageName = calculatePackageName(referencedClass);
        return referencedClass.getManager().findPackage(referencedPackageName);
    }
}
