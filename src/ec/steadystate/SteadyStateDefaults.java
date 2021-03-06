package ec.steadystate;

import ec.*;
import ec.util.*;

/*
 * SteadyStateDefaults.java
 *
 * Created: Thu Jan 20 16:49:57 2000
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0
 */

public final class SteadyStateDefaults implements DefaultsForm {
  public static final String P_STEADYSTATE = "steady";

  /** Returns the default base. */
  public static final Parameter base() {
    return new Parameter(P_STEADYSTATE);
  }
}
