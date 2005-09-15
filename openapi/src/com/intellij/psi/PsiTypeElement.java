/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.psi;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the occurrence of a type in Java source code, for example, as a return
 * type of the method or the type of a method parameter.
 */
public interface PsiTypeElement extends PsiElement {
  PsiTypeElement[] EMPTY_ARRAY = new PsiTypeElement[0];
  @NotNull PsiType getType();
  PsiJavaCodeReferenceElement getInnermostComponentReferenceElement();
}
