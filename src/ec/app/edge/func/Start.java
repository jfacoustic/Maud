package ec.app.edge.func;

import ec.*;
import ec.app.edge.*;
import ec.gp.*;
import ec.util.*;

/*
 * Start.java
 *
 * Created: Wed Nov  3 18:26:37 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0
 */

public class Start extends GPNode {
  public String toString() {
    return "s";
  }

  public void checkConstraints(final EvolutionState state,
                               final int tree,
                               final GPIndividual typicalIndividual,
                               final Parameter individualBase) {
    super.checkConstraints(state, tree, typicalIndividual, individualBase);
    if (children.length != 1)
      state.output.error("Incorrect number of children for node " +
              toStringForError() + " at " +
              individualBase);
  }

  public void eval(final EvolutionState state,
                   final int thread,
                   final GPData input,
                   final ADFStack stack,
                   final GPIndividual individual,
                   final Problem problem) {
    int edge = ((EdgeData) (input)).edge;
    Edge prob = (Edge) problem;

    prob.start[prob.to[edge]] = true;

// pass the edge down

    children[0].eval(state, thread, input, stack, individual, problem);
  }
}



