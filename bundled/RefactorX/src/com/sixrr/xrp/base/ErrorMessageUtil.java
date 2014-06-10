package com.sixrr.xrp.base;

import com.intellij.openapi.project.Project;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ErrorMessageUtil {
    public static void showErrorMessage(String refactoringName, String message, String helpID, Project project) {
        try {
            Class<?> rmu = Class.forName("com.intellij.refactoring.util.RefactoringMessageUtil");
            Method[] methods = rmu.getMethods();
            for (Method method : methods) {
                if(method.getName().equals("showErrorMessage"))
                {
                    method.invoke(null, refactoringName, message, helpID, project);
                    return;
                }
            }
        } catch (ClassNotFoundException e) {

        } catch (IllegalAccessException e) {

        } catch (InvocationTargetException e) {
        }
         try {
            Class<?> rmu = Class.forName("com.intellij.refactoring.util.CommonRefactoringUtil");
            Method[] methods = rmu.getMethods();
            for (Method method : methods) {
                if(method.getName().equals("showErrorMessage"))
                {
                    method.invoke(null, refactoringName, message, helpID, project);
                    return;
                }
            }
        } catch (ClassNotFoundException e) {

        } catch (IllegalAccessException e) {

        } catch (InvocationTargetException e) {
        }
    }
}
