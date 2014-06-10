package com.sixrr.ejbmetrics;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.openapi.project.Project;
import com.intellij.codeInsight.AnnotationUtil;

public class EJBUtil {
    public static boolean isSessionBean(PsiClass psiClass) {
        return isSubClass(psiClass, "javax.ejb.SessionBean") ||
                hasAnnotation(psiClass, "javax.ejb.Stateful") || hasAnnotation(psiClass, "javax.ejb.Stateless") ;
    }

    public static boolean isEntityBean(PsiClass psiClass) {
        return isSubClass(psiClass, "javax.ejb.EntityBean") || hasAnnotation(psiClass, "javax.ejb.Entity");
    }

    public static boolean isMessageDrivenBean(PsiClass psiClass) {
        return isSubClass(psiClass, "javax.ejb.MessageDrivenBean") || hasAnnotation(psiClass, "javax.ejb.MessageDriven");
    }

    private static boolean isSubClass(PsiClass psiClass, String baseClass) {
        final PsiManager manager = psiClass.getManager();
        final Project project = manager.getProject();
        final GlobalSearchScope globalScope = GlobalSearchScope.allScope(project);
        final PsiClass sessionBeanClass = manager.findClass(baseClass, globalScope);
        return InheritanceUtil.isInheritorOrSelf(psiClass, sessionBeanClass, true);
    }

    private static boolean hasAnnotation(PsiClass psiClass, String annotationClass) {
        return AnnotationUtil.isAnnotated(psiClass, annotationClass, true);
    }
}
