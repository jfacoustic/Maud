package ec.gp;

import ec.*;
import ec.util.*;

import java.io.*;

/*
 * GPSpecies.java
 *
 * Created: Tue Aug 31 17:00:10 1999
 * By: Sean Luke
 */

/**
 * GPSpecies is a simple individual which is suitable as a species
 * for GP subpopulations.  GPSpecies' individuals must be GPIndividuals,
 * and often their pipelines are GPBreedingPipelines (at any rate,
 * the pipelines will have to return members of GPSpecies!).
 *
 <p><b>Default Base</b><br>
 gp.species

 *
 * @author Sean Luke
 * @version 1.0
 */

public class GPSpecies extends Species implements GPSpeciesForm {
  public static final String P_GPSPECIES = "species";

  public Parameter defaultBase() {
    return GPDefaults.base().push(P_GPSPECIES);
  }

  public void setup(final EvolutionState state, final Parameter base) {
    super.setup(state, base);

// check to make sure that our individual prototype is a GPIndividual
    if (!(i_prototype instanceof GPIndividual))
      state.output.fatal("The Individual class for the Species " + getClass().getName() + " is must be a subclass of ec.gp.GPIndividual.", base);
  }

  public Individual newIndividual(EvolutionState state, Subpopulation _population, Fitness _fitness) throws CloneNotSupportedException {
    GPIndividual newind = (GPIndividual) (i_prototype.protoClone());

// Initialize the trees
    for (int x = 0; x < newind.trees.length; x++)
      newind.trees[x].buildTree(state, 0);  // unthreaded, right?

// Set the fitness
    newind.fitness = _fitness;
    newind.evaluated = false;

    // Set the species to me
    newind.species = this;

// ...and we're ready!
    return newind;
  }

  public Individual newIndividual(final EvolutionState state,
                                  final Subpopulation _population,
                                  final Fitness _fitness,
                                  final LineNumberReader reader)
          throws IOException, CloneNotSupportedException {
    GPIndividual newind = (GPIndividual) (i_prototype.protoClone());

// Set the fitness -- must be done BEFORE loading!
    newind.fitness = _fitness;
    newind.evaluated = false; // for sanity's sake, though it's a useless line

    // Set the species to me
    newind.species = this;

// load that sucker
    newind.readIndividual(state, reader);

// and we're ready!
    return newind;
  }
}
