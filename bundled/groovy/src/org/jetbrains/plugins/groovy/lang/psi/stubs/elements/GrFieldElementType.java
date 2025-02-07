package org.jetbrains.plugins.groovy.lang.psi.stubs.elements;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.StringRef;
import org.jetbrains.annotations.Nullable;
import static org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes.ENUM_CONSTANT;
import static org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes.FIELD;
import org.jetbrains.plugins.groovy.lang.psi.GrStubElementType;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.GrModifierList;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.annotation.GrAnnotation;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.GrFieldImpl;
import org.jetbrains.plugins.groovy.lang.psi.stubs.GrFieldStub;
import org.jetbrains.plugins.groovy.lang.psi.stubs.impl.GrFieldStubImpl;
import org.jetbrains.plugins.groovy.lang.psi.stubs.index.GrAnnotatedMemberIndex;
import org.jetbrains.plugins.groovy.lang.psi.stubs.index.GrFieldNameIndex;

import java.io.IOException;
import java.util.*;

/**
 * @author ilyas
 */
public class GrFieldElementType extends GrStubElementType<GrFieldStub, GrField> {

  public GrFieldElementType() {
    super("field");
  }

  public PsiElement createElement(ASTNode node) {
    return new GrFieldImpl(node);
  }

  public GrField createPsi(GrFieldStub stub) {
    return new GrFieldImpl(stub);
  }

  public GrFieldStub createStub(GrField psi, StubElement parentStub) {
    final GrModifierList modifiers = psi.getModifierList();
    String[] annNames;
    if (modifiers == null) {
      annNames = new String[0];
    }
    else {
      final GrAnnotation[] annotations = modifiers.getAnnotations();
      annNames = ContainerUtil.map(annotations, new Function<GrAnnotation, String>() {
        @Nullable
        public String fun(final GrAnnotation grAnnotation) {
          final GrCodeReferenceElement element = grAnnotation.getClassReference();
          if (element == null) return null;
          return element.getReferenceName();
        }
      }, new String[annotations.length]);
    }

    Set<String>[] namedParametersArray = new Set[0];
    if (psi instanceof GrFieldImpl){
      namedParametersArray = ((GrFieldImpl)psi).getNamedParametersArray();
    }

    return new GrFieldStubImpl(parentStub, StringRef.fromString(psi.getName()), false, annNames, namedParametersArray, FIELD);
  }

  public void serialize(GrFieldStub stub, StubOutputStream dataStream) throws IOException {
    serializeFieldStub(stub, dataStream);
  }

  public GrFieldStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
    return deserializeFieldStub(dataStream, parentStub);
  }

  public void indexStub(GrFieldStub stub, IndexSink sink) {
    indexFieldStub(stub, sink);
  }

  /*
   * ****************************************************************************************************************
   */

  static void serializeFieldStub(GrFieldStub stub, StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName());
    final String[] annotations = stub.getAnnotations();
    dataStream.writeByte(annotations.length);
    for (String s : annotations) {
      dataStream.writeName(s);
    }

    final Set<String>[] namedParameters = stub.getNamedParameters();

    dataStream.writeByte(namedParameters.length);
    for (Set<String> namedParameterSet : namedParameters) {
      dataStream.writeByte(namedParameterSet.size());
      for (String namepParameter : namedParameterSet) {
        dataStream.writeUTF(namepParameter);
      }
    }
    dataStream.writeBoolean(stub.isEnumConstant());
  }

  static GrFieldStub deserializeFieldStub(StubInputStream dataStream, StubElement parentStub) throws IOException {
    StringRef ref = dataStream.readName();
    final byte b = dataStream.readByte();
    final String[] annNames = new String[b];
    for (int i = 0; i < b; i++) {
      annNames[i] = dataStream.readName().toString();
    }

    //named parameters
    final byte namedParametersSetNumber = dataStream.readByte();
    final List<Set<String>> namedParametersSets = new ArrayList<Set<String>>();

    for (int i = 0; i < namedParametersSetNumber; i++) {
      final byte curNamedParameterSetSize = dataStream.readByte();
      final String[] namedParameterSetArray = new String[curNamedParameterSetSize];

      for (int j = 0; j < curNamedParameterSetSize; j++) {
        namedParameterSetArray[j] = dataStream.readUTF();
      }
      Set<String> curSet = new HashSet<String>();
      curSet.addAll(Arrays.asList(namedParameterSetArray));
      namedParametersSets.add(curSet);
    }

    boolean isEnumConstant = dataStream.readBoolean();
    return new GrFieldStubImpl(parentStub, ref, isEnumConstant, annNames, namedParametersSets.toArray(new HashSet[0]),
                               isEnumConstant ? ENUM_CONSTANT : FIELD);
  }

  static void indexFieldStub(GrFieldStub stub, IndexSink sink) {
    String name = stub.getName();
    if (name != null) {
      sink.occurrence(GrFieldNameIndex.KEY, name);
    }
    for (String annName : stub.getAnnotations()) {
      if (annName != null) {
        sink.occurrence(GrAnnotatedMemberIndex.KEY, annName);
      }
    }
  }
}
