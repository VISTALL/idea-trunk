/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.factories;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.spring.factories.resolvers.*;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.util.containers.HashMap;
import com.intellij.util.xmlb.XmlSerializer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.EmptyVisitor;

import java.io.IOException;
import java.util.*;

/**
 * @author Serega Vasiliev, Taras Tielkes
 */
public class SpringFactoryBeansManager implements ApplicationComponent {
  private static final Key<CachedValue<Set<String>>> CACHED_OBJECT_TYPE = Key.create("CACHED_OBJECT_TYPE");

  @NonNls private static final String BEAN_FACTORY_CLASSNAME = "org.springframework.beans.factory.FactoryBean";

  private final Map<String, ObjectTypeResolver> mySpringFactories = new HashMap<String, ObjectTypeResolver>();
  private final ObjectTypeResolver[] myCustomResolvers = new ObjectTypeResolver[]{new TransactionProxyFactoryBeanTypeResolver(),
    new JndiObjectFactoryBeanTypeResolver(), new SpringEjbTypeResolver(), new ProxyFactoryBeanTypeResolver(),
    new ScopedProxyFactoryBeanTypeResolver(), new BeanReferenceFactoryBeanTypeResolver(), new UtilConstantTypeResolver()};

  @NonNls private static final String FACTORIES_RESOURCE_XML = "/resources/factories/factories.xml";
  @NonNls private static final String PROPERTY_NAME_DELIMITER = ",";

  public SpringFactoryBeansManager() {
    final FactoriesBean factoriesBean =
      XmlSerializer.deserialize(SpringFactoryBeansManager.class.getResource(FACTORIES_RESOURCE_XML), FactoriesBean.class);

    assert factoriesBean != null;
    assert factoriesBean.getFactories() != null;

    for (FactoryBeanInfo factoryBeanInfo : factoriesBean.getFactories()) {
      final String factory = factoryBeanInfo.getFactory();
      if (factory != null && factory.trim().length() > 0) {
        mySpringFactories.put(factory, getObjectTypeResolver(factoryBeanInfo));
      }
    }
  }

  @Nullable
  private ObjectTypeResolver getObjectTypeResolver(final FactoryBeanInfo factoryBeanInfo) {
    final String type = factoryBeanInfo.getObjectType();
    if (!StringUtil.isEmptyOrSpaces(type)) {
      return new SingleObjectTypeResolver(type);
    }

    final String delimitedNames = factoryBeanInfo.getPropertyNames();
    if (!StringUtil.isEmptyOrSpaces(delimitedNames)) {
      return new FactoryPropertiesDependentTypeResolver(StringUtil.split(delimitedNames, PROPERTY_NAME_DELIMITER));
    }

    final String factoryClass = factoryBeanInfo.getFactory();
    for (ObjectTypeResolver customResolver : myCustomResolvers) {
      if (customResolver.accept(factoryClass)) return customResolver;
    }

    return null;
  }

  public static boolean isBeanFactory(@NotNull PsiClass psiClass) {
    final Project project = psiClass.getProject();
    final PsiClass beanFactoryClass =
      JavaPsiFacade.getInstance(project).findClass(BEAN_FACTORY_CLASSNAME, GlobalSearchScope.allScope(project));

    return beanFactoryClass != null && psiClass.isInheritor(beanFactoryClass, true);
  }

  public boolean isProductKnown(@NotNull final PsiClass factoryClass, @NotNull final CommonSpringBean context) {
    return !getProductTypeClassNames(factoryClass, context).isEmpty();
  }

  @NotNull
  public PsiClass[] getProductTypes(final PsiClass factoryClass, @NotNull final CommonSpringBean context) {
    final Set<String> typeClassNames = getProductTypeClassNames(factoryClass, context);

    if (typeClassNames.isEmpty()) {
      return PsiClass.EMPTY_ARRAY;
    }
    else {
      final List<PsiClass> psiClasses = new ArrayList<PsiClass>(typeClassNames.size());
      final Project project = factoryClass.getProject();
      final PsiManager psiManager = PsiManager.getInstance(project);
      final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
      for (final String typeClassName : typeClassNames) {
        final PsiClass psiClass = JavaPsiFacade.getInstance(psiManager.getProject()).findClass(typeClassName, scope);
        if (psiClass != null) {
          psiClasses.add(psiClass);
        }
      }
      return psiClasses.toArray(PsiClass.EMPTY_ARRAY);
    }
  }

  public boolean canProduce(@NotNull PsiClass factory, @NotNull PsiClass requiredClass, @NotNull CommonSpringBean context) {
    final Set<String> typeNames = getProductTypeClassNames(factory, context);
    if (!typeNames.isEmpty()) {
      if (typeNames.contains(requiredClass.getQualifiedName())) return true;

      final Project project = factory.getProject();
      for (final String typeName : typeNames) {
        final PsiClass productClass = JavaPsiFacade.getInstance(project).findClass(typeName, GlobalSearchScope.allScope(project));
        if (productClass != null && productClass.isInheritor(requiredClass, true)) return true;
      }
    }
    return false;
  }

  public boolean canProduceAny(@NotNull PsiClass factory, @NotNull List<PsiClass> requiredClasses, @NotNull CommonSpringBean context) {
    for (final PsiClass requiredClass : requiredClasses) {
      if (requiredClass != null && canProduce(factory, requiredClass, context)) return true;
    }
    return false;
  }

  public boolean isFactoryRegistered(@NotNull final PsiClass factoryClass) {
      return  mySpringFactories.containsKey(factoryClass.getQualifiedName());
  }
  
  @NotNull
  public Set<String> getProductTypeClassNames(@NotNull final PsiClass factoryClass, @NotNull final CommonSpringBean context) {
    final String qualifiedName = factoryClass.getQualifiedName();
    final ObjectTypeResolver typeResolver = mySpringFactories.get(qualifiedName);
    if (typeResolver != null) {
      return typeResolver.getObjectType(context);
    }

    final PsiManager psiManager = PsiManager.getInstance(factoryClass.getProject());
    for (String factoryClassName : mySpringFactories.keySet()) {
      final PsiClass psiClass = JavaPsiFacade.getInstance(psiManager.getProject())
        .findClass(factoryClassName, GlobalSearchScope.allScope(factoryClass.getProject()));
      if (psiClass != null && factoryClass.isInheritor(psiClass, false)) {
        final ObjectTypeResolver resolver = mySpringFactories.get(factoryClassName);
        if (resolver != null) {
          return resolver.getObjectType(context);
        }
      }
    }

    return guessObjectType(factoryClass);
  }

  private static Set<String> guessObjectType(final PsiClass factoryClass) {
    CachedValue<Set<String>> cachedValue = factoryClass.getUserData(CACHED_OBJECT_TYPE);
    if (cachedValue == null) {
      factoryClass.putUserData(CACHED_OBJECT_TYPE, cachedValue = factoryClass.getManager().getCachedValuesManager().createCachedValue(new CachedValueProvider<Set<String>>() {
        public Result<Set<String>> compute() {
          return new Result<Set<String>>(doGuessObjectType(factoryClass), factoryClass);
        }
      }, false));
    }

    return cachedValue.getValue();
  }

  @Nullable
  private static PsiMethod getProductTypeMethod(final PsiClass factoryClass) {
    for (final PsiMethod psiMethod : factoryClass.findMethodsByName("getObjectType", true)) {
      if (psiMethod.getParameterList().getParameters().length == 0) {
        return psiMethod;
      }
    }
    return null;
  }

  private static Set<String> doGuessObjectType(final PsiClass factoryClass) {
    final PsiMethod method = getProductTypeMethod(factoryClass);
    if (method == null) return Collections.emptySet();

    if (method instanceof PsiCompiledElement) {
      final VirtualFile file = method.getContainingFile().getVirtualFile();
      if (file != null) {
        final FactoryBeanObjectTypeReader reader = new FactoryBeanObjectTypeReader();
        try {
          new ClassReader(file.contentsToByteArray()).accept(reader, ClassReader.SKIP_DEBUG);
        }
        catch (IOException e) {
        }
        final String qName = reader.getResultQName();
        if (qName != null) return Collections.singleton(qName);
      }
    }

    final PsiCodeBlock body = method.getBody();
    if (body != null) {
      final PsiStatement[] statements = body.getStatements();
      if (statements.length == 1 && statements[0] instanceof PsiReturnStatement) {
        final PsiExpression value = ((PsiReturnStatement)statements[0]).getReturnValue();
        if (value instanceof PsiClassObjectAccessExpression) {
          final String s = ((PsiClassObjectAccessExpression)value).getOperand().getType().getCanonicalText();
          if (s != null) {
            return Collections.singleton(s);
          }
        }
      }
    }
    return Collections.emptySet();
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return SpringFactoryBeansManager.class.getName();
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public static SpringFactoryBeansManager getInstance() {
    return ApplicationManager.getApplication().getComponent(SpringFactoryBeansManager.class);
  }

  public void registerFactory(String className, ObjectTypeResolver reslover) {
    mySpringFactories.put(className, reslover);
  }
  
  public void unregisterFactory(String className) {
    mySpringFactories.remove(className);
  }

  private static class FactoryBeanObjectTypeReader extends EmptyVisitor {
    private String myResultQName;

    public String getResultQName() {
      return myResultQName;
    }

    public MethodVisitor visitMethod(final int access, @NonNls final String name, final String desc, final String signature,
                                     final String[] exceptions) {
      if ("getObjectType".equals(name) && (signature == null || signature.startsWith("()"))) {
        return new EmptyVisitor(){
          private String qname;
          private int number = 0;

          @Override public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
            if ((number == 0 || number == 7) && opcode == Opcodes.GETSTATIC || number == 5 && opcode == Opcodes.PUTSTATIC) {
              number++;
            }

          }

          @Override public void visitJumpInsn(final int opcode, final Label label) {
            if (number == 1 && opcode == Opcodes.IFNONNULL || number == 6 && opcode == Opcodes.GOTO) {
              number++;
            }
          }

          @Override public void visitLdcInsn(final Object cst) {
            if (number == 2 && cst instanceof String) {
              number++;
              qname = (String)cst;
            }
            else if (number == 0 && cst instanceof Type) {
              number++;
              qname = ((Type)cst).getClassName();
            }
          }

          @Override public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
            if (number != 3 || opcode != Opcodes.INVOKESTATIC || !"class$".equals(name)) return;
            number++;
          }

          @Override public void visitInsn(final int opcode) {
            if (number == 4 && opcode == Opcodes.DUP) {
              number++;
            }
            if ((number == 8 || number == 1) && opcode == Opcodes.ARETURN) {
              if (myResultQName == null) {
                myResultQName = qname;
              }
              number++;
            }
          }

        };
      }
      return super.visitMethod(access, name, desc, signature, exceptions);
    }
  }
}
