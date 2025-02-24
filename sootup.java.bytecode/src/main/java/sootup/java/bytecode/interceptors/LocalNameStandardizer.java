package sootup.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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
import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.BodyUtils;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/jimple/toolkits/scalar/LocalNameStandardizer.java

/** @author Zun Wang */
public class LocalNameStandardizer implements BodyInterceptor {

  // TODO: ms: why not reuse LocalGenerator to dry?

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    // Get the order of all Locals' occurrences and store them into a map
    Map<Local, Integer> localToFirstOccurrence = new HashMap<>();
    int defsCount = 0;
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    for (Stmt stmt : stmtGraph) {
      Local def = null;
      final List<Value> defs = stmt.getDefs();
      if (!defs.isEmpty() && defs.get(0) instanceof Local) {
        def = (Local) defs.get(0);
      }
      if (def != null && !localToFirstOccurrence.containsKey(def)) {
        localToFirstOccurrence.put(def, defsCount);
        defsCount++;
      }
    }

    // Sort all locals in a list
    ArrayList<Local> localsList = new ArrayList<>(builder.getLocals());
    LocalComparator localComparator = new LocalComparator(localToFirstOccurrence);
    localsList.sort(localComparator);

    // Assign new name to each local
    int refCount = 0;
    int longCount = 0;
    int booleanCount = 0;
    int charCount = 0;
    int floatCount = 0;
    int doubleCount = 0;
    int errorCount = 0;
    int nullCount = 0;

    Map<Local, Local> localToNewLocal = new HashMap<>();
    for (Local local : localsList) {
      String prefix = "";
      boolean hasDollar = local.getName().startsWith("$");
      if (hasDollar) {
        prefix = "$";
      }
      Type type = local.getType();
      int index = localsList.indexOf(local);
      Local newLocal;

      if (type.equals(PrimitiveType.getByte())) {
        newLocal = local.withName(prefix + "b" + longCount);
        longCount++;
      } else if (type.equals(PrimitiveType.getShort())) {
        newLocal = local.withName(prefix + "s" + longCount);
        longCount++;
      } else if (type.equals(PrimitiveType.getInt())) {
        newLocal = local.withName(prefix + "i" + longCount);
        longCount++;
      } else if (type.equals(PrimitiveType.getLong())) {
        newLocal = local.withName(prefix + "l" + longCount);
        longCount++;
      } else if (type.equals(PrimitiveType.getFloat())) {
        newLocal = local.withName(prefix + "f" + floatCount);
        floatCount++;
      } else if (type.equals(PrimitiveType.getDouble())) {
        newLocal = local.withName(prefix + "d" + doubleCount);
        doubleCount++;
      } else if (type.equals(PrimitiveType.getChar())) {
        newLocal = local.withName(prefix + "c" + charCount);
        charCount++;
      } else if (type.equals(PrimitiveType.getBoolean())) {
        newLocal = local.withName(prefix + "z" + booleanCount);
        booleanCount++;
      } else if (type instanceof UnknownType) {
        newLocal = local.withName(prefix + "e" + errorCount);
        errorCount++;
      } else if (type instanceof NullType) {
        newLocal = local.withName(prefix + "n" + nullCount);
        nullCount++;
      } else {
        newLocal = local.withName(prefix + "r" + refCount);
        refCount++;
      }
      localsList.set(index, newLocal);
      localToNewLocal.put(local, newLocal);
    }

    Set<Local> sortedLocals = new LinkedHashSet<>(localsList);
    builder.setLocals(sortedLocals);

    // modify locals in stmtGraph with new locals
    for (Stmt stmt : stmtGraph) {
      Stmt newStmt = stmt;
      if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
        Local def = (Local) stmt.getDefs().get(0);
        Local newLocal = localToNewLocal.get(def);
        newStmt = BodyUtils.withNewDef(newStmt, newLocal);
      }
      for (Value use : stmt.getUses()) {
        if (use instanceof Local) {
          Local newLocal = localToNewLocal.get(use);
          newStmt = BodyUtils.withNewUse(newStmt, use, newLocal);
        }
      }
      if (!stmt.equals(newStmt)) {
        stmtGraph.replaceNode(stmt, newStmt);
      }
    }
  }

  private static class LocalComparator implements Comparator<Local> {

    Map<Local, Integer> localToFirstOccurence;

    public LocalComparator(Map<Local, Integer> localToInteger) {
      this.localToFirstOccurence = localToInteger;
    }

    @Override
    public int compare(Local local1, Local local2) {
      int result = local1.getType().toString().compareTo(local2.getType().toString());
      if (result == 0) {
        result =
            Integer.compare(localToFirstOccurence.get(local1), localToFirstOccurence.get(local2));
      }
      return result;
    }
  }
}
