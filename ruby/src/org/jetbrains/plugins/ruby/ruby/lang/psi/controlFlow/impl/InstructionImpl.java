package org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.impl;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.CallEnvironment;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.CallInstruction;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.Instruction;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RLiteral;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;

import java.util.ArrayList;
import java.util.Stack;

class InstructionImpl implements Instruction, Cloneable {
    ArrayList<InstructionImpl> myPred = new ArrayList<InstructionImpl>();
    ArrayList<InstructionImpl> mySucc = new ArrayList<InstructionImpl>();

    final PsiElement myPsiElement;
    private final int myNumber;

    @Nullable
    public PsiElement getElement() {
        return myPsiElement;
    }

    InstructionImpl(PsiElement element, int num) {
        myPsiElement = element;
        myNumber = num;
    }

    public Iterable<? extends Instruction> allSucc() {
        return mySucc;
    }

    public Iterable<? extends Instruction> allPred() {
        return myPred;
    }

    protected Stack<CallInstruction> getStack(CallEnvironment env, InstructionImpl instruction) {
      return env.callStack(instruction);
    }

    public Iterable<? extends Instruction> succ(CallEnvironment env) {
      final Stack<CallInstruction> stack = getStack(env, this);
      for (InstructionImpl instruction : mySucc) {
        env.update(stack, instruction);
      }

      return mySucc;
    }

    public Iterable<? extends Instruction> pred(CallEnvironment env) {
      final Stack<CallInstruction> stack = getStack(env, this);
      for (InstructionImpl instruction : myPred) {
        env.update(stack, instruction);
      }

      return myPred;
    }

    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(myNumber);
        builder.append("(");
        for (int i = 0; i < mySucc.size(); i++) {
            if (i > 0) builder.append(',');
            builder.append(mySucc.get(i).myNumber);
        }
        builder.append(") ").append(getElementPresentation());
        return builder.toString();
    }

    protected String getElementPresentation() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("element: ").append(myPsiElement);
        if (myPsiElement instanceof RIdentifier || myPsiElement instanceof RLiteral){
            buffer.append(" ").append(myPsiElement.getText());
        }
        return  buffer.toString();
    }

    public int num() {
        return myNumber;
    }
}
