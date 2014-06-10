package com.intellij.seam.model.xml.pageflow;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public interface PageflowTransitionHolder extends SeamPageflowDomElement {
    @NotNull
    List<Transition> getTransitions();

    Transition addTransition();
}
