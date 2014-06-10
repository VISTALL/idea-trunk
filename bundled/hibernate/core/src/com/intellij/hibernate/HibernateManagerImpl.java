package com.intellij.hibernate;

import com.intellij.util.xml.ConverterManager;

/**
 * @author Gregory.Shrago
 */
public class HibernateManagerImpl extends HibernateManager {
  private final HibernateConvertersRegistry myConvertersRegistry;

  public HibernateManagerImpl(ConverterManager converterManager) {
    myConvertersRegistry = new HibernateConvertersRegistry(converterManager);
  }

  public HibernateConvertersRegistry getConvertersRegistry() {
    return myConvertersRegistry;
  }
}
