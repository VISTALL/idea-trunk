package com.intellij.coldFusion.model.psi;

public interface CfmlCallable {
    CfmlCallable[] EMPTY_ARRAY = new CfmlCallable[0];

    CfmlVariable[] getParameters();
}
