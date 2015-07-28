package ec.app.parity;

import ec.util.*;
import ec.*;
import ec.gp.*;

/*
 * ParityData.java
 *
 * Created: Wed Nov  3 18:32:13 1999
 * By: Sean Luke
 */

/**
 * @author Sean Luke
 * @version 1.0
 */

public class ParityData extends GPData {
  // return value -- should ALWAYS be either 1 or 0
  public int x;

  public GPData copyTo(final GPData gpd) {
    ((ParityData) gpd).x = x;
    return gpd;
  }
}
