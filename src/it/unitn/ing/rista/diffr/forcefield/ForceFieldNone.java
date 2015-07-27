/*
 * @(#)ForceFieldNone.java created 18/10/2002 Mesiano
 *
 * Copyright (c) 2002 Luca Lutterotti All Rights Reserved.
 *
 * This software is the research result of Luca Lutterotti and it is
 * provided as it is as confidential and proprietary information.
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with Luca Lutterotti.
 *
 * THE AUTHOR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. THE AUTHOR SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */

package it.unitn.ing.rista.diffr.forcefield;

import it.unitn.ing.rista.diffr.*;

import java.util.*;

/**
 *  The ForceFieldNone is a fake method for determining potential energy
 *
 *
 * @version $Revision: 1.6 $, $Date: 2005/05/06 18:07:26 $
 * @author Luca Lutterotti
 * @since JDK1.1
 */


public class ForceFieldNone extends ForceField {

	protected static String[] diclistc = {};
  protected static String[] diclistcrm = {};

	protected static String[] classlistc = {};

	protected static String[] classlistcs = {};

	public ForceFieldNone(XRDcat aobj, String alabel) {
		super(aobj, alabel);
		initXRD();
		identifier = "No force field";
		IDlabel = "No force field";
		description = "Force Field has no effect";
	}

	public ForceFieldNone(XRDcat aobj) {
		this(aobj, "Force Field has no effect");
	}

	public ForceFieldNone() {
		identifier = "No force field";
		IDlabel = "No force field";
		description = "Force Field has no effect";
	}

	public void initConstant() {
		Nstring = 0;
		Nstringloop = 0;
		Nparameter = 0;
		Nparameterloop = 0;
		Nsubordinate = 0;
		Nsubordinateloop = 0;
	}

	public void initDictionary() {
		for (int i = 0; i < totsubordinateloop; i++)
			diclist[i] = diclistc[i];
    System.arraycopy(diclistcrm, 0, diclistRealMeaning, 0, totsubordinateloop);
		for (int i = 0; i < totsubordinateloop - totsubordinate; i++)
			classlist[i] = classlistc[i];
		for (int i = 0; i < totsubordinate - totparameterloop; i++)
			classlists[i] = classlistcs[i];
	}

}

