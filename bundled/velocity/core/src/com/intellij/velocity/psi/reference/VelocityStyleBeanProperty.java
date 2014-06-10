package com.intellij.velocity.psi.reference;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VelocityStyleBeanProperty extends BeanProperty {
    private String myName;

    protected VelocityStyleBeanProperty(@NotNull PsiMethod method, @NotNull String name) {
        super(method);
        myName = name;
    }

    @NotNull
    @Override
    public String getName() {
        return myName;
    }

    @Override
    public PsiMethod getGetter() {
        final PsiMethod method = getMethod();
        if (VelocityNamingUtil.isPropertyGetter(method)) {
            return method;
        }
        return VelocityNamingUtil.findPropertyGetter(method.getContainingClass(), getName());
    }

    @Override
    public PsiMethod getSetter() {
        final PsiMethod method = getMethod();
        if (VelocityNamingUtil.isPropertySetter(method)) {
            return method;
        }
        return VelocityNamingUtil.findPropertySetter(method.getContainingClass(), getName());
    }

    @Override
    public void setName(String newName) throws IncorrectOperationException {
        super.setName(newName);
        myName = newName;
    }

  @Override
  @NotNull
  public PsiType getPropertyType() {
    PsiType type = VelocityNamingUtil.getPropertyType(getMethod());
    assert type != null;
    return type;
  }

    @Nullable
    public static BeanProperty createVelocityStyleBeanProperty(@NotNull PsiMethod method, @Nullable String name) {
        if(name == null) {
            return null;
        }
        return VelocityNamingUtil.isPropertyAccessor(method) || VelocityNamingUtil.isGetByStringOrByObjectMethod(method)
                ? new VelocityStyleBeanProperty(method, name)
                : null;
    }


}
