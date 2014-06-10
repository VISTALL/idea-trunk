package com.intellij.velocity.psi.reference;

import static com.intellij.openapi.util.text.StringUtil.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VelocityNamingUtil {
    private VelocityNamingUtil() {
    }

  @SuppressWarnings({"HardCodedStringLiteral"})
    public static boolean isWaitOrNotifyOfObject(@NotNull PsiMethod method) {
    PsiClass psiClass = method.getContainingClass();
    if (psiClass == null || !CommonClassNames.JAVA_LANG_OBJECT.equals(psiClass.getQualifiedName())) {
            return false;
        }
        String name = method.getName();
        return "wait".equals(name) || "notify".equals(name) || "notifyAll".equals(name);
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    public static boolean isPropertyGetter(@NotNull PsiMethod method) {
        if (method.isConstructor()) {
            return false;
        }
        String methodName = method.getName();
        PsiType returnType = method.getReturnType();
        if (methodName.startsWith("get") && methodName.length() > "get".length()) {
            if (returnType == null || PsiType.VOID.equals(returnType)) return false;
        } else if (methodName.startsWith("is")) {
            if (returnType != PsiType.BOOLEAN) return false;
        } else return false;
        return method.getParameterList().getParametersCount() == 0;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public static boolean isGetByStringOrByObjectMethod(@NotNull PsiMethod method) {
        String methodName = method.getName();
        if (!"get".equals(methodName)) {
            return false;
        }
        PsiParameterList parameterList = method.getParameterList();
        if (parameterList.getParametersCount() != 1) {
            return false;
        }
        PsiParameter parameter = parameterList.getParameters()[0];
        final PsiElementFactory factory = JavaPsiFacade.getInstance(method.getProject()).getElementFactory();
        PsiClassType javaLangString = factory.createTypeByFQClassName(CommonClassNames.JAVA_LANG_STRING, method.getResolveScope());
        if (parameter.getType().isAssignableFrom(javaLangString)) {
            return true;
        }
        PsiClassType javaLangObject = factory.createTypeByFQClassName(CommonClassNames.JAVA_LANG_OBJECT, method.getResolveScope());
        return parameter.getType().isAssignableFrom(javaLangObject);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public static boolean isPropertySetter(@NotNull PsiMethod method) {
        if (method.isConstructor()) {
            return false;
        }
        String methodName = method.getName();
        return methodName.startsWith("set") &&
                methodName.length() > "set".length() &&
                method.getParameterList().getParametersCount() == 1 &&
                (method.getReturnType() == null || PsiType.VOID.equals(method.getReturnType()));
    }

    public static boolean isPropertyAccessor(PsiMethod method) {
        return isPropertyGetter(method) || isPropertySetter(method);
    }

    public static String getPropertyNameFromAccessor(@NotNull PsiMethod accessor, boolean firstCharInLowerCase) {
        if (isPropertySetter(accessor)) {
            return adjustFirstCharCase(getPropertyNameFromSetter(accessor), firstCharInLowerCase);
        }
        if (isPropertyGetter(accessor)) {
            return adjustFirstCharCase(getPropertyNameFromGetter(accessor), firstCharInLowerCase);
        }
        return null;
    }

    public static String getPropertyNameFromAccessor(@NotNull PsiMethod accessor) {
        if (isPropertySetter(accessor)) {
            return getPropertyNameFromSetter(accessor);
        }
        if (isPropertyGetter(accessor)) {
            return getPropertyNameFromGetter(accessor);
        }
        return null;
    }

    public static String adjustFirstCharCase(@NotNull String name, boolean lowerCase) {
        char chars[] = name.toCharArray();
        chars[0] = lowerCase ? toLowerCase(chars[0]) : toUpperCase(chars[0]);
        return new String(chars);
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    public static String getPropertyName(String methodName, boolean firstCharInLowerCase) {
        String propName;
        if (methodName.startsWith("get")) {
            propName = methodName.substring("get".length());
        } else if (methodName.startsWith("set")) {
            propName = methodName.substring("set".length());
        } else if (methodName.startsWith("is")) {
            propName = methodName.substring("is".length());
        } else {
            return null;
        }
        return adjustFirstCharCase(propName, firstCharInLowerCase);
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    private static String getPropertyNameFromGetter(@NotNull PsiMethod getter) {
        String methodName = getter.getName();
        int prefixLength = methodName.startsWith("get") ? "get".length() : "is".length();
        return methodName.substring(prefixLength);
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    private static String getPropertyNameFromSetter(@NotNull PsiMethod setter) {
        return setter.getName().substring("set".length());
    }


    @Nullable
    public static PsiMethod findPropertyGetter(final PsiClass aClass, final String propertyName) {
        return findPropertyAccessor(aClass, propertyName, new PropertyNameExtractor() {
            public String extractPropertyName(@NotNull PsiMethod method) {
                return isPropertyGetter(method) ? getPropertyNameFromGetter(method) : null;
            }
        });
    }

    @Nullable
    public static PsiMethod findPropertySetter(final PsiClass aClass, final String propertyName) {
        return findPropertyAccessor(aClass, propertyName, new PropertyNameExtractor() {
            public String extractPropertyName(@NotNull PsiMethod method) {
                return isPropertySetter(method) ? getPropertyNameFromSetter(method) : null;
            }
        });
    }

    @Nullable
    private static PsiMethod findPropertyAccessor(final PsiClass aClass, final String propertyName, PropertyNameExtractor extractor) {
        if (aClass == null) {
            return null;
        }
        PsiMethod found = null;
        String propertyNameWithoutFirstChar = propertyName.substring(1);
        for (PsiMethod method : aClass.getAllMethods()) {
            String wouldBePropertyName = extractor.extractPropertyName(method);
            if (wouldBePropertyName == null || !propertyNameWithoutFirstChar.equals(wouldBePropertyName.substring(1))) {
                continue;
            }
            if (wouldBePropertyName.charAt(0) == propertyName.charAt(0)) {
                return method;
            }
            if (toLowerCase(wouldBePropertyName.charAt(0)) == toLowerCase(propertyName.charAt(0))) {
                found = method;
            }
        }
        return found;
    }

    @Nullable
    public static PsiType getPropertyType(PsiMethod psiMethod) {
        if (isPropertyGetter(psiMethod)) {
            return psiMethod.getReturnType();
        }
        else if (isPropertySetter(psiMethod)) {
            return psiMethod.getParameterList().getParameters()[0].getType();
        }
        return null;
    }

    private interface PropertyNameExtractor {
        @Nullable
        String extractPropertyName(@NotNull PsiMethod method);
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    public static String[] suggestGetterNames(String propertyName) {
        final String upperCaseName = adjustFirstCharCase(propertyName, false);
        final String lowerCaseName = adjustFirstCharCase(propertyName, true);
        return new String[]{"is" + upperCaseName, "get" + upperCaseName, "is" + lowerCaseName, "get" + lowerCaseName};
    }

    @SuppressWarnings({"HardCodedStringLiteral"})
    public static String[] suggestSetterNames(String propertyName) {
        final String upperCaseName = adjustFirstCharCase(propertyName, false);
        final String lowerCaseName = adjustFirstCharCase(propertyName, true);
        return new String[]{"set" + upperCaseName, "set" + lowerCaseName};
    }
}
