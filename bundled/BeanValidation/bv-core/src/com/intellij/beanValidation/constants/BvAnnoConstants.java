/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.beanValidation.constants;

import org.jetbrains.annotations.NonNls;

/**
 * @author Konstantin Bulenkov
 */
public final class BvAnnoConstants {
  private BvAnnoConstants() {}

  public static final @NonNls String CONSTRAINT = "javax.validation.Constraint";
  public static final @NonNls String CONSTRAINT_VALIDATOR = "javax.validation.ConstraintValidator";
  public static final @NonNls String VALIDATED_BY = "validatedBy";
  public static final @NonNls String CONSTRAINT_VALIDATOR_CONTEXT = "javax.validation.ConstraintValidatorContext";
  public static final @NonNls String SIZE = "javax.validation.constraints.Size";
  public static final @NonNls String LENGTH = "org.hibernate.validation.constraints.Length";


  public static final @NonNls String NOT_NULL = "javax.validation.constraints.NotNull";
  public static final @NonNls String NULL = "javax.validation.constraints.Null";
}
