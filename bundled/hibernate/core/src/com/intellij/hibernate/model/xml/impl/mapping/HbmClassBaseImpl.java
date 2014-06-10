package com.intellij.hibernate.model.xml.impl.mapping;

import com.intellij.hibernate.model.xml.mapping.*;
import com.intellij.javaee.model.common.CommonModelElement;
import com.intellij.javaee.model.common.CommonModelElementWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.persistence.facet.PersistencePackageDefaults;
import com.intellij.persistence.model.PersistenceInheritanceType;
import com.intellij.persistence.model.PersistentAttribute;
import com.intellij.persistence.model.PersistentEntity;
import com.intellij.persistence.model.TableInfoProvider;
import com.intellij.persistence.model.helpers.PersistentEntityModelHelper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PropertyMemberType;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author Gregory.Shrago
 */
public abstract class HbmClassBaseImpl extends HbmPersistentObjectBaseImpl implements HbmClassBase {

  private final PersistentEntityModelHelper helper = new MyHelper();

  @NotNull
  public PersistentEntityModelHelper getObjectModelHelper() {
    return helper;
  }

  public GenericValue<PsiClass> getIdClassValue() {
    if (this instanceof HbmClass) {
      return ((HbmClass)this).getCompositeId().getClazz();
    }
    return ReadOnlyGenericValue.getInstance(null);
  }

  public GenericValue<String> getName() {
    return getEntityName();
  }

  public class MyTable extends CommonModelElementWrapper implements TableInfoProvider {
    private final HbmTableInfoProvider myDelegate;

    public MyTable(HbmTableInfoProvider delegate) {
      myDelegate = delegate;
    }

    public HbmTableInfoProvider getDelegate() {
      return myDelegate;
    }

    public GenericValue<String> getTableName() {
      final GenericAttributeValue<String> value = getDelegate().getTableName();
      if (StringUtil.isEmpty(value.getStringValue())) {
        final HbmClassBase object = myDelegate.getParentOfType(HbmClassBase.class, false);
        final String className;
        if (object != null && StringUtil.isNotEmpty(className = object.getClazz().getStringValue())) {
          return ReadOnlyGenericValue.getInstance(StringUtil.getShortName(className));
        }
      }
      return value;
    }

    public GenericValue<String> getCatalog() {
      return getCurrentValueOrDefault(getDelegate().getCatalog(), new DefaultsProcessor<String>() {
        public GenericValue<String> processMappings(final HbmHibernateMapping mappings) {
          return mappings.getCatalog();
        }

        public String processUnitDefaults(final PersistencePackageDefaults defaults) {
          return defaults.getCatalog();
        }
      });
    }

    public GenericValue<String> getSchema() {
      return getCurrentValueOrDefault(getDelegate().getSchema(), new DefaultsProcessor<String>() {
        public GenericValue<String> processMappings(final HbmHibernateMapping mappings) {
          return mappings.getSchema();
        }

        public String processUnitDefaults(final PersistencePackageDefaults defaults) {
          return defaults.getSchema();
        }
      });
    }
  }

  public class MyHelper extends CommonModelElementWrapper implements PersistentEntityModelHelper {

    @PrimaryKey
    public GenericValue<PsiClass> getClazz() {
      return HbmClassBaseImpl.this.getClazz();
    }

    public TableInfoProvider getTable() {
      final HbmClassBaseImpl entity = HbmClassBaseImpl.this;
      if (entity instanceof HbmSubclass) {
        final HbmClass superclass = entity.getParentOfType(HbmClass.class, true);
        if (superclass != null) {
          return superclass.getObjectModelHelper().getTable();
        }
      }
      return new MyTable(entity);
    }

    public List<? extends TableInfoProvider> getSecondaryTables() {
      final List<HbmJoin> hbmJoins;
      if (HbmClassBaseImpl.this instanceof HbmClass) {
        hbmJoins = ((HbmClass)HbmClassBaseImpl.this).getJoins();
      }
      else if (HbmClassBaseImpl.this instanceof HbmSubclass) {
        hbmJoins = ((HbmSubclass)HbmClassBaseImpl.this).getJoins();
      }
      else {
        return Collections.emptyList();
      }
      return ContainerUtil.map(hbmJoins, new Function<HbmJoin, TableInfoProvider>() {
        public TableInfoProvider fun(final HbmJoin join) {
          return new MyTable(join);
        }
      });
    }

    @Nullable
    public PersistenceInheritanceType getInheritanceType(final PersistentEntity descendant) {
      final HbmClassBaseImpl thisEntity = HbmClassBaseImpl.this;
      if (thisEntity instanceof HbmSubclass) return PersistenceInheritanceType.SINGLE_TABLE;
      if (thisEntity instanceof HbmUnionSubclass) return PersistenceInheritanceType.TABLE_PER_CLASS;
      if (thisEntity instanceof HbmJoinedSubclass) return PersistenceInheritanceType.JOINED;
      final HbmClassBase hbmDescendant = ModelMergerUtil.getImplementation(descendant, HbmClassBase.class);
      if (hbmDescendant == null) return null;
      if (descendant instanceof HbmSubclass) return PersistenceInheritanceType.SINGLE_TABLE;
      if (descendant instanceof HbmUnionSubclass) return PersistenceInheritanceType.TABLE_PER_CLASS;
      if (descendant instanceof HbmJoinedSubclass) return PersistenceInheritanceType.JOINED;
      return null;
    }

    @NotNull
    public List<? extends PersistentAttribute> getAttributes() {
      return HbmClassBaseImpl.this.getAttributes();
    }

    public PropertyMemberType getDefaultAccessMode() {
      return HbmClassBaseImpl.this.getDefaultAccessMode();
    }

    public boolean isAccessModeFixed() {
      return false;
    }

    public List<HbmQuery> getNamedQueries() {
      return getQueries();
    }

    public List<HbmSqlQuery> getNamedNativeQueries() {
      return getSqlQueries();
    }

    public CommonModelElement getDelegate() {
      return HbmClassBaseImpl.this;
    }
  }
}
