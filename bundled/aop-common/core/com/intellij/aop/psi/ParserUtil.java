/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.aop.AopBundle;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class ParserUtil {
  protected static ParsingCommand sequence(final ParsingCommand... commands) {
    return new ParsingCommand() {
      public boolean perform(final PsiBuilder builder) {
        for (final ParsingCommand command : commands) {
          if (!command.perform(builder)) return false;
        }
        return true;
      }
    };
  }

  protected static PsiBuilder.Marker doAndPrecedeMarker(PsiBuilder.Marker marker, final IElementType type) {
    final PsiBuilder.Marker preceding = marker.precede();
    marker.done(type);
    return preceding;
  }

  protected static ParsingCommand token(final String text) {
    return new ParsingCommand() {
      public boolean perform(final PsiBuilder builder) {
        if (text.equals(builder.getTokenText())) {
          builder.advanceLexer();
          return true;
        }
        builder.error(AopBundle.message("error.0.expected", text));
        return false;
      }
    };
  }

  protected static ParsingCommand token(final AopElementType tokenType, final @NonNls @Nullable String expected) {
    return new ParsingCommand() {
      public boolean perform(final PsiBuilder builder) {
        if (tokenType.equals(builder.getTokenType())) {
          builder.advanceLexer();
          return true;
        }
        builder.error(AopBundle.message("error.0.expected", expected));
        return false;
      }
    };
  }

  protected static ParsingCommand token(final TokenSet set, final @NonNls String expected) {
    return new ParsingCommand() {
      public boolean perform(final PsiBuilder builder) {
        if (set.contains(builder.getTokenType())) {
          builder.advanceLexer();
          return true;
        }
        builder.error(AopBundle.message("error.0.expected", expected));
        return false;
      }
    };
  }

  protected static boolean dropMarker(final PsiBuilder.Marker marker) {
    marker.drop();
    return false;
  }

  protected static boolean doMarker(ParsingCommand command, final PsiBuilder.Marker marker, IElementType type, final PsiBuilder builder) {
    return doMarker(command.perform(builder), marker, type);
  }

  protected static boolean doMarker(final boolean result, final PsiBuilder.Marker marker, final IElementType type) {
    marker.done(type);
    return result;
  }

  protected static ParsingCommand condition(final IElementType type, final ParsingCommand then, final ParsingCommand elze) {
    return new ParsingCommand() {
      public boolean perform(final PsiBuilder builder) {
        return builder.getTokenType() == type ? then.perform(builder) : elze.perform(builder);
      }
    };
  }

  protected static ParsingCommand or(final ParsingCommand... commands) {
    return or(commands.length - 1, commands);
  }

  private static ParsingCommand or(final int main, final ParsingCommand... commands) {
    return new ParsingCommand() {
      public boolean perform(final PsiBuilder myBuilder) {
        for (final ParsingCommand command : commands) {
          final PsiBuilder.Marker marker = myBuilder.mark();
          if (command.perform(myBuilder)) {
            marker.drop();
            return true;
          } else {
            marker.rollbackTo();
          }
        }
        return commands[main].perform(myBuilder);
      }
    };
  }

  protected static ParsingCommand parseList(@NotNull final ParsingCommand member, @Nullable final ParsingCommand separator, @Nullable final IElementType endType, final boolean canBeEmpty) {
    return new ParsingCommand() {
      public boolean perform(final PsiBuilder builder) {
        if (canBeEmpty) {
          if (!tryParse(member, builder, endType)) return true;
          if (!tryParse(separator, builder, null)) return true;
        }

        while (true) {
          if (separator == null && !tryParse(member, builder, endType) || separator != null && !member.perform(builder)) return separator == null;
          if (!tryParse(separator, builder, null)) return true;
        }
      }
    };
  }

  protected static boolean tryParse(@Nullable final ParsingCommand command, final PsiBuilder builder, @Nullable IElementType endType) {
    if (command == null) return true;

    boolean canBeEnd = endType == null || builder.getTokenType() == endType;

    final PsiBuilder.Marker beforeSeparator = builder.mark();
    //final int offset = builder.getCurrentOffset();
    if (!command.perform(builder) && canBeEnd) {
      beforeSeparator.rollbackTo();
      return false;
    }
    beforeSeparator.drop();
    return true;
  }

  protected static ParsingCommand optional(final ParsingCommand command) {
    return or(command, ParsingCommand.TRUE);
  }

  protected static ParsingCommand handleError(final ParsingCommand command, final String message) {
    return new ParsingCommand() {
      public boolean perform(final PsiBuilder builder) {
        final int offset = builder.getCurrentOffset();
        final PsiBuilder.Marker marker = builder.mark();
        final boolean result = command.perform(builder);
        if (offset == builder.getCurrentOffset()) {
          marker.rollbackTo();
        } else {
          marker.drop();
        }

        if (!result) {
          builder.error(message);
        }
        return result;
      }
    };
  }

  protected static ParsingCommand parseBinary(final AopElementType exprType, final IElementType separatorType, final ParsingCommand lower) {
    return new ParsingCommand() {
      public boolean perform(final PsiBuilder builder) {
        PsiBuilder.Marker expr = builder.mark();
        if (!lower.perform(builder)) return dropMarker(expr);

        while (separatorType == builder.getTokenType()) {
          builder.advanceLexer();
          if (!lower.perform(builder)) return dropMarker(expr);
          expr = doAndPrecedeMarker(expr, exprType);
        }
        expr.drop();
        return true;
      }
    };
  }

  protected static ParsingCommand wrap(final ParsingCommand inner, final IElementType type) {
    return new ParsingCommand() {
      public boolean perform(final PsiBuilder builder) {
        return doMarker(inner, builder.mark(), type, builder);
      }
    };
  }


  protected static abstract class ParsingCommand {
    public static ParsingCommand TRUE = new ParsingCommand() {
      public boolean perform(final PsiBuilder builder) {
        return true;
      }
    };

    /*@NonNls private final String myToString;

    public ParsingCommand() {
      myToString = "Created at: " + new Throwable().fillInStackTrace().getStackTrace()[2];
    }

    public String toString() {
      return myToString;
    }*/

    public abstract boolean perform(final PsiBuilder builder);
  }
  
}
