// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartAbstractOperatorDeclaration extends DartOperator {

  @Nullable
  DartFormalParameterList getFormalParameterList();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartReturnType getReturnType();

  @Nullable
  DartUserDefinableOperator getUserDefinableOperator();

}