/*
 * @(#)ReflectivityNone.java created 27/03/2000 Le Mans
 *
 * Copyright (c) 2000 Luca Lutterotti All Rights Reserved.
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

package it.unitn.ing.rista.diffr.reflectivity;

import java.lang.*;

import it.unitn.ing.rista.diffr.*;
import it.unitn.ing.rista.awt.*;
import it.unitn.ing.rista.util.*;

import java.awt.*;
import java.awt.*;
import java.awt.event.*;

/**
 *  The ReflectivityNone is a class to not compute reflectivity
 *
 *
 * @version $Revision: 1.4 $, $Date: 2004/08/12 09:36:07 $
 * @author Luca Lutterotti
 * @since JDK1.1
 */

public class ReflectivityNone extends Reflectivity {

  public ReflectivityNone(XRDcat aobj, String alabel) {
    super(aobj, alabel);
    identifier = "none reflectivity";
    IDlabel = "none reflectivity";
    description = "No reflectivity computation";
  }

  public ReflectivityNone(XRDcat aobj) {
    this(aobj, "none reflectivity");
  }

  public ReflectivityNone() {
    identifier = "none reflectivity";
    IDlabel = "none reflectivity";
    description = "No reflectivity computation";
  }

	public boolean needReflectivityStatistic() {
		return false;
	}

}
