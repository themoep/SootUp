package de.upb.swt.soot.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Etienne Gagnon, Linghui Luo and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import afu.org.checkerframework.checker.nullness.qual.Nullable;
import de.upb.swt.soot.core.jimple.common.constant.*;
import javax.annotation.Nonnull;

public abstract class AbstractConstantVisitor<V> implements ConstantVisitor {
  private V result;

  @Override
  public void caseDoubleConstant(@Nonnull DoubleConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseFloatConstant(@Nonnull FloatConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseIntConstant(@Nonnull IntConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseLongConstant(@Nonnull LongConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseNullConstant(@Nonnull NullConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseStringConstant(@Nonnull StringConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseClassConstant(@Nonnull ClassConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseMethodHandle(@Nonnull MethodHandle v) {
    defaultCase(v);
  }

  @Override
  public void defaultCase(@Nonnull Constant v) {}

  protected void setResult(V result) {
    this.result = result;
  }

  @Nullable
  public V getResult() {
    return result;
  }
}
