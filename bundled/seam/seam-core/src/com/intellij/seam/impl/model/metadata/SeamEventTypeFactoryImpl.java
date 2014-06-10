package com.intellij.seam.impl.model.metadata;

import com.intellij.openapi.project.Project;
import com.intellij.seam.model.metadata.SeamEventType;
import com.intellij.seam.model.metadata.SeamEventTypeFactory;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiFile;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SeamEventTypeFactoryImpl extends SeamEventTypeFactory {

  private final Map<String, SeamEventType> myEvents = new THashMap<String, SeamEventType>();

  private final PsiFile myDummyFile;

  public SeamEventTypeFactoryImpl(final Project project) {
    myDummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.java", "");
  }

  @NotNull
  public SeamEventType getOrCreateEventType(final String eventType) {
    if (!myEvents.containsKey(eventType)) {
       myEvents.put(eventType, new SeamEventType(eventType, myDummyFile));
    }
    return myEvents.get(eventType);
  }

  public void dispose() {
      myEvents.clear();
  }
}
