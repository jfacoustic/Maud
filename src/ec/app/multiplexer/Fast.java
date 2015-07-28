package ec.app.multiplexer;

/*
 * Fast.java
 *
 * Created: Tue Mar 21 18:53:53 2000
 * By: Sean Luke
 */

/**
 * Fast contains lots of magic numbers for speeding up Multiplexer,
 * basically arrays of bitfields representing the various on/off bit
 * values for all of the scoreboard permutations of 3, 6, and 11-multiplexer.
 * Do not bother yourselves with the man behind the curtain!
 *
 * @author Sean Luke
 * @version 1.0
 */

public class Fast {
  /** 3-Multiplexer has 3 boolean variables (A0, D0, D1) */
  public static final int M_3_OUTPUT = 3;
  /** 3-Multiplexer has 8 permutations of its 3 boolean variables */
  public static final int M_3_SIZE = 8;
  /** 3-Multiplexer bitfield values for the 3 boolean variables and 1 output variable, stored as bytes (8 bits used) */
  public static final byte M_3 [/*4*/] =
          {85, 51, 15, 39};
  /** 3-Multiplexer names for the 3 boolean variables and 1 output variable */
  public static final String M_3_NAMES[/*4*/] =
          {"A0", "D0", "D1", "Output"};

  /** 6-Multiplexer has 6 boolean variables (A0, A1, D0, D1, D2, D3) */
  public static final int M_6_OUTPUT = 6;
  /** 6-Multiplexer has 64 permutations of its 6 boolean variables */
  public static final int M_6_SIZE = 64;
  /** 6-Multiplexer bitfield values for the 6 boolean variables and 1 output variable, stored as longs (64 bits used) */
  public static final long M_6[/*7*/] =
          {6148914691236517205L, 3689348814741910323L, 1085102592571150095L, 71777214294589695L, 281470681808895L, 4294967295L, 597899502893742975L};
  /** 6-Multiplexer names for the 6 boolean variables and 1 output variable */
  public static final String M_6_NAMES[/*7*/] =
          {"A0", "A1", "D0", "D1", "D2", "D3", "Output"};

  /** 11-Multiplexer has 11 boolean variables (A0, A1, A2, D0, D1, D2, D3, D4, D5, D6, D7) */
  public static final int M_11_OUTPUT = 11;
  /** 11-Multiplexer has 2048 permutations of its 11 boolean variables */
  public static final int M_11_SIZE = 2048;
  /** 11-Multiplexer bitfield values for the 11 boolean variables and 1 output variable, where each of the 12 variable slots is an array of 32 longs (32 x 64 bits = 2048) which together comprise the big long 2048-bit permutation vector for that variable. */
  public static final long M_11[/*12*/][/*32*/] =
          {
            {6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L, 6148914691236517205L},

            {3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L, 3689348814741910323L},

            {1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L, 1085102592571150095L},

            {71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L, 71777214294589695L},

            {281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L, 281470681808895L},

            {4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L, 4294967295L},

            {0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L, 0L, -1L},

            {0L, 0L, -1L, -1L, 0L, 0L, -1L, -1L, 0L, 0L, -1L, -1L, 0L, 0L, -1L, -1L, 0L, 0L, -1L, -1L, 0L, 0L, -1L, -1L, 0L, 0L, -1L, -1L, 0L, 0L, -1L, -1L},

            {0L, 0L, 0L, 0L, -1L, -1L, -1L, -1L, 0L, 0L, 0L, 0L, -1L, -1L, -1L, -1L, 0L, 0L, 0L, 0L, -1L, -1L, -1L, -1L, 0L, 0L, 0L, 0L, -1L, -1L, -1L, -1L},

            {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L},

            {0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L, -1L},

            {36099990944243936L, 1193542756353470704L, 614821373648857320L, 1772264139058084088L, 325460682296550628L, 1482903447705777396L, 904182065001164012L, 2061624830410390780L, 180780336620397282L, 1338223102029624050L, 759501719325010666L, 1916944484734237434L, 470141027972703974L, 1627583793381930742L, 1048862410677317358L, 2206305176086544126L, 108440163782320609L, 1265882929191547377L, 687161546486933993L, 1844604311896160761L, 397800855134627301L, 1555243620543854069L, 976522237839240685L, 2133965003248467453L, 253120509458473955L, 1410563274867700723L, 831841892163087339L, 1989284657572314107L, 542481200810780647L, 1699923966220007415L, 1121202583515394031L, 2278645348924620799L}

          };

  /** 11-Multiplexer names for the 11 boolean variables and 1 output variable */
  public static final String M_11_NAMES[/*12*/] =
          {"A0", "A1", "A2", "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "Output"};
}
