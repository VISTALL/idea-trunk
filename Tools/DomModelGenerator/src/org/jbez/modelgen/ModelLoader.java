package org.jbez.modelgen;

import org.apache.xerces.xni.parser.XMLEntityResolver;

import java.io.File;
import java.util.Collection;

public interface ModelLoader {
  void loadModel(ModelDesc model, Collection<File> files, XMLEntityResolver resolver) throws Exception;
}
