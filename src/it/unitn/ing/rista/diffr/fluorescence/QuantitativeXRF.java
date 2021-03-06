/*
 * @(#)QualitativeXRF.java created May 15, 2009 Caen
 *
 * Copyright (c) 2009 Luca Lutterotti All Rights Reserved.
 *
 * This software is the research result of Luca Lutterotti and it is
 * provided as it is as confidential and proprietary information.
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with the author.
 *
 * THE AUTHOR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. THE AUTHOR SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */
package it.unitn.ing.rista.diffr.fluorescence;

import it.unitn.ing.rista.chemistry.AtomInfo;
import it.unitn.ing.rista.chemistry.XRayDataSqLite;
import it.unitn.ing.rista.diffr.*;
import it.unitn.ing.rista.diffr.detector.XRFDetector;
import it.unitn.ing.rista.diffr.geometry.GeometryXRFInstrument;
import it.unitn.ing.rista.diffr.radiation.XrayEbelTubeRadiation;
import it.unitn.ing.rista.util.*;

import java.util.Vector;

/**
 * The QuantitativeXRF is a class to
 *
 * @author Luca Lutterotti
 * @version $Revision: 1.00 $, $Date: May 15, 2009 7:03:18 PM $
 * @since JDK1.1
 */
public class QuantitativeXRF extends FluorescenceBase {

  public static String modelID = "Quantitative XRF";
  public static String descriptionID = "Perform quantitative fitting of XRF data";

  public QuantitativeXRF(XRDcat obj, String alabel) {
    super(obj, alabel);
    identifier = modelID;
    IDlabel = modelID;
    description = descriptionID;
  }

  public QuantitativeXRF(XRDcat afile) {
    this(afile, modelID);
  }

  public QuantitativeXRF() {
    identifier = modelID;
    IDlabel = modelID;
    description = descriptionID;
  }


	/**
	 * The method compute the fluorescence pattern using the
	 * fluorescence model by De Boer.
	 * D. K. G. de Boer, Phys. Review B, 44[2], 498, 1991.
	 * It uses the ElamDB. When the pattern is computed and it is added to the
	 * <code>DiffrDataFile</code> using the addtoFit method.
	 *
	 * @param adatafile
	 * @see DiffrDataFile#addtoFit
	 */

	public void computeFluorescence(DiffrDataFile adatafile) {

		boolean checkSensitivity = MaudPreferences.getBoolean("xrf.sensitivityNoEnergy", false);
		Sample asample = adatafile.getDataFileSet().getSample();
		Instrument ainstrument = adatafile.getDataFileSet().getInstrument();
		Detector detector = ainstrument.getDetector();
		Geometry geometry = ainstrument.getGeometry();
		double incidentIntensity = ainstrument.getIntensityValue();
		double sampleLinearArea = detector.getGeometryCorrection(
				((GeometryXRFInstrument) geometry).getBeamOutCorrection(adatafile, asample));
//		incidentIntensity *= sampleLinearArea;

		double polarization = ainstrument.getGeometry().getPolarizationAmount();
		double polarizationAngle = ainstrument.getGeometry().getPolarizationAngle();
		double cos2polarization = MoreMath.cosd(polarizationAngle);
		cos2polarization *= cos2polarization;
		double s_factor = 0.5 - 0.5 * polarization * (1.0 - cos2polarization);
		double p_factor = 0.5 - 0.5 * polarization * cos2polarization;

		double[] xEnergy = adatafile.getXrangeInEnergy();
		int numberOfPoints = xEnergy.length;
		double maxEnergyInKeV = xEnergy[numberOfPoints - 1] * 0.001 * 1.1;
		double minEnergyInKeV = xEnergy[0] * 0.001 / 1.1;
//    System.out.println(xEnergy[0] + " " + xEnergy[numberOfPoints - 1]);

		int layersNumber = asample.numberOfLayers;

		double[] fluorescence = new double[numberOfPoints];

		double twothetadetector = detector.getThetaDetector(adatafile, 0);
		double[] incidentDiffracted = adatafile.getIncidentAndDiffractionAngles(adatafile.get2ThetaValue());
//		System.out.println(adatafile.getLabel() + ", incident beam angle: " + incidentDiffracted[0] * Constants.PITODEG + ", exiting beam angle: " + incidentDiffracted[2] * Constants.PITODEG + " " + adatafile.getTiltingAngle()[4]);
//	  incidentDiffracted[0] *= Constants.DEGTOPI;

//		double cosPhi2 = Math.cos(incidentDiffracted[0]);
		double sinPhii = Math.sin(incidentDiffracted[0]);
		double sinPhid = Math.sin(incidentDiffracted[2]);

		RadiationType radType = ainstrument.getRadiationType();
		int rad_lines = radType.getLinesCountForFluorescence();
		double[] energyInKeV = new double[rad_lines];
		double[] energy_intensity = new double[rad_lines];

		double[][] layerAbsorption = new double[layersNumber][rad_lines];
		double[][] overLayerAbsorption = new double[layersNumber][rad_lines];
		double[] layerDensity = new double[layersNumber];
		double[] layerThickness = new double[layersNumber];
		for (int j1 = 0; j1 < layersNumber; j1++) {
			Layer layer = asample.getlayer(j1);
			layerDensity[j1] = layer.getDensity();
			layerThickness[j1] = layer.getThicknessInCm();
		}
		for (int ej = 0; ej < rad_lines; ej++) {
			energyInKeV[ej] = Constants.ENERGY_LAMBDA / radType.getRadiationWavelengthForFluorescence(ej) * 0.001;
			energy_intensity[ej] = radType.getRadiationWeightForFluorescence(ej);
			layerAbsorption[0][ej] = -asample.getlayer(0).getAbsorption(energyInKeV[ej]) * layerDensity[0] / sinPhii;
			overLayerAbsorption[0][ej] = 0;
			for (int j1 = 1; j1 < layersNumber; j1++) {
				layerAbsorption[j1][ej] = -asample.getlayer(j1).getAbsorption(energyInKeV[ej]) * layerDensity[j1] / sinPhii;
				overLayerAbsorption[j1][ej] = overLayerAbsorption[j1 - 1][ej] + layerAbsorption[j1 - 1][ej] * layerThickness[j1 - 1];
//				System.out.println(overLayerAbsorption[j1][ej]);
			}
		}
//		int sub20 = radType.getSubdivision(); //MaudPreferences.getInteger("xrf_detector.energySubdivision", 20);

		Vector<FluorescenceLine> linesForAtom;
		int initialContent = 100;
		double source_intensity = ((XRFDetector) ainstrument.getDetector()).getSourceSpectrumIntensity();
		if (source_intensity > 0 && initialContent < rad_lines)
			initialContent = rad_lines;
		Vector<FluorescenceLine> fluorescenceLines = new Vector<FluorescenceLine>(initialContent, 100);
		for (int j1 = 0; j1 < layersNumber; j1++) {
			Layer layer = asample.getlayer(j1);
			Vector<AtomQuantity> chemicalComposition = layer.getChemicalComposition();
			for (int k = 0; k < chemicalComposition.size(); k++) {
				double atomsQuantities = chemicalComposition.elementAt(k).quantity_weight;
				if (atomsQuantities > 0) {
					int atomNumber = AtomInfo.retrieveAtomNumber(chemicalComposition.elementAt(k).label);

					if (checkSensitivity)
						linesForAtom = XRayDataSqLite.getFluorescenceLinesFor(     // remove NoSensitivity
							atomNumber, maxEnergyInKeV);
					else
						linesForAtom = XRayDataSqLite.getFluorescenceLinesNoSensitivityFor(     // remove NoSensitivity
								atomNumber, maxEnergyInKeV);

					for (int ij = 0; ij < linesForAtom.size(); ij++) {
						FluorescenceLine line = linesForAtom.elementAt(ij);
						double lineEnergyKeV = line.getEnergy(); // in KeV
						double lineInnerShellEnergyKeV = line.getCoreShellEnergy(); // in KeV
						double overLayerAbsorptionForLine = 0;
						for (int j2 = 0; j2 < j1; j2++) {
							double actualLayerAbs = -asample.getlayer(j2).getAbsorption(lineEnergyKeV) * layerDensity[j2] / sinPhid;
							overLayerAbsorptionForLine += actualLayerAbs * layerThickness[j2];
						}
						double actualLayerAbsorption = -asample.getlayer(j1).getAbsorption(lineEnergyKeV) * layerDensity[j1] / sinPhid;
//						System.out.println(actualLayerAbsorption + " " + asample.getlayer(j1).getAbsorption(lineEnergyKeV) + " " + layerDensity[j1] + " " + sinPhid);
						double totalIntensity = 0;
						for (int ej = 0; ej < rad_lines; ej++) {
							if (energyInKeV[ej] > lineInnerShellEnergyKeV && lineEnergyKeV <= energyInKeV[ej]) {
								double over_abs = overLayerAbsorptionForLine + overLayerAbsorption[j1][ej];
								if (!Double.isNaN(over_abs)) {
									if (over_abs > -Double.MAX_EXPONENT / 2 && over_abs < Double.MAX_EXPONENT / 2)
										over_abs = Math.exp(over_abs);
									else if (over_abs > 0)
										over_abs = Double.MAX_VALUE / 2;
									else
										over_abs = 0;
								} else
									over_abs = 0;

								double ab = (actualLayerAbsorption + layerAbsorption[j1][ej]);
								double abs = ab * layerThickness[j1];
								if (!Double.isNaN(abs) && abs != 0) {
									if (abs > -Double.MAX_EXPONENT / 2 && abs < Double.MAX_EXPONENT / 2)
										abs = -(1.0 - Math.exp(abs)) / ab;
									else
										abs = -1.0 / ab;
								} else
									abs = 0;

								double lineSensitivity = 1;
								if (!checkSensitivity)
									lineSensitivity = XRayDataSqLite.getSensitivity(atomNumber - 1, line.getCoreShellID(), energyInKeV[ej]);
//								System.out.println(line.getEnergy() + " " + line.getCoreShellID() + " " + lineSensitivity + " " + energy_intensity[ej]);
//								System.out.println(totalIntensity + " " + actualLayerAbsorption + " " + layerAbsorption[j1][ej] + " " + lineSensitivity + " " + over_abs + " " + abs + " " + energy_intensity[ej]);
								totalIntensity += lineSensitivity * over_abs * abs * energy_intensity[ej];
							}
						}
						totalIntensity *= layerDensity[j1];
						double detectorAbsorption = ((XRFDetector) detector).computeAbsorptionForLineWithEnergy(lineEnergyKeV);
						double detectorEfficiency = ((XRFDetector) detector).computeDetectorEfficiency(lineEnergyKeV);
						double areaCorrection = ((XRFDetector) detector).getAreaCorrection(sampleLinearArea);
//						if (lineEnergyKeV * 1000 > xEnergy[0] && lineEnergyKeV * 1000 < xEnergy[numberOfPoints - 1])
//						System.out.println(lineEnergyKeV + " " + line.getIntensity() + " " + atomsQuantities + " " + totalIntensity + " " + detectorAbsorption + " " +
//								detectorEfficiency + " " + areaCorrection + " " + getIntensityCorrection(atomNumber));
						line.multiplyIntensityBy(atomsQuantities * totalIntensity * detectorAbsorption *
								detectorEfficiency * areaCorrection * getIntensityCorrection(atomNumber));
						boolean addLine = true;
						for (int i = 0; i < fluorescenceLines.size() && addLine; i++) {
							FluorescenceLine lineExisting = fluorescenceLines.get(i);
							if (lineExisting.getEnergy() == line.getEnergy()) {
								addLine = false;
								lineExisting.setIntensity(lineExisting.getIntensity() + line.getIntensity());
							}
						}
						if (addLine) {
							fluorescenceLines.add(line);
						}

					}
				}
			}
		}

		if (((XRFDetector) ainstrument.getDetector()).getFiltersFluorescenceIntensityTotal() > 0) {

			for (int ej = 0; ej < rad_lines; ej++) {
				Vector<FluorescenceLine> filtersFluorescenceLines = ((XRFDetector) ainstrument.getDetector()).getFluorescenceLines(energyInKeV[ej]);
				for (int si = 0; si < filtersFluorescenceLines.size(); si++) {
					FluorescenceLine line = filtersFluorescenceLines.get(si);
					boolean addLine = true;
					for (int i = 0; i < fluorescenceLines.size() && addLine; i++) {
						FluorescenceLine lineExisting = fluorescenceLines.get(i);
//						lineExisting.setIntensity(lineExisting.getIntensity());
						if (lineExisting.getEnergy() == line.getEnergy()) {
							addLine = false;
							lineExisting.setIntensity(lineExisting.getIntensity() + line.getIntensity() * energy_intensity[ej]);
						}
					}
					if (addLine && line.getIntensity() > 0) {
						line.setIntensity(line.getIntensity() * energy_intensity[ej]);
						fluorescenceLines.add(line);
					}
				}
			}
		}
		if (ainstrument.getRadiationType() instanceof XrayEbelTubeRadiation) {
			XrayEbelTubeRadiation source = (XrayEbelTubeRadiation) ainstrument.getRadiationType();
			if (source.getFiltersFluorescenceIntensityTotal() > 0) {

				for (int ej = 0; ej < rad_lines; ej++) {
					Vector<FluorescenceLine> filtersFluorescenceLines = source.getFluorescenceLines(energyInKeV[ej]);
					for (int si = 0; si < filtersFluorescenceLines.size(); si++) {
						FluorescenceLine line = filtersFluorescenceLines.get(si);
						boolean addLine = true;
						for (int i = 0; i < fluorescenceLines.size(); i++) {
							FluorescenceLine lineExisting = fluorescenceLines.get(i);
//							lineExisting.setIntensity(lineExisting.getIntensity());
							if (lineExisting.getEnergy() == line.getEnergy()) {
								addLine = false;
								lineExisting.setIntensity(lineExisting.getIntensity() + line.getIntensity() * energy_intensity[ej]);
							}
						}
						if (addLine) {
							line.setIntensity(line.getIntensity() * energy_intensity[ej]);
							if (line.getIntensity() > 0)
								fluorescenceLines.add(line);
						}
					}
				}
			}
		}
		Vector<FluorescenceLine> sumLines = null;
		if (((XRFDetector) ainstrument.getDetector()).getSumPeaksIntensity() > 0)
			sumLines = ((XRFDetector) ainstrument.getDetector()).getPileUpPeaks(maxEnergyInKeV, fluorescenceLines);
		Vector<FluorescenceLine> escapeLines = null;
		if (((XRFDetector) ainstrument.getDetector()).getEscapePeaksIntensity() > 0)
			escapeLines = ((XRFDetector) ainstrument.getDetector()).getEscapePeaks(maxEnergyInKeV, fluorescenceLines);

		if (sumLines != null) {
			for (int si = 0; si < sumLines.size(); si++) {
				FluorescenceLine line = sumLines.get(si);
				boolean addLine = true;
				for (int i = 0; i < fluorescenceLines.size() && addLine; i++) {
					FluorescenceLine lineExisting = fluorescenceLines.get(i);
					if (lineExisting.getEnergy() == line.getEnergy()) {
						addLine = false;
						lineExisting.setIntensity(lineExisting.getIntensity() + line.getIntensity());
					}
				}
				if (addLine && line.getIntensity() > 0) {
//					line.setIntensity(line.getIntensity());
					fluorescenceLines.add(line);
				}
			}
		}

		if (escapeLines != null) {
			for (int si = 0; si < escapeLines.size(); si++) {
				FluorescenceLine line = escapeLines.get(si);
				boolean addLine = true;
				for (int i = 0; i < fluorescenceLines.size() && addLine; i++) {
					FluorescenceLine lineExisting = fluorescenceLines.get(i);
					if (lineExisting.getEnergy() == line.getEnergy()) {
						addLine = false;
						lineExisting.setIntensity(lineExisting.getIntensity() + line.getIntensity());
					}
				}
				if (addLine && line.getIntensity() > 0) {
//					line.setIntensity(line.getIntensity());
					fluorescenceLines.add(line);
				}
			}
		}

		if (source_intensity > 0) {
			double lastEnergyInKeV = 0;
			double lastEnergyIntensity = 0;
  		int sub20 = radType.getSubdivision(); //MaudPreferences.getInteger("xrf_detector.energySubdivision", 20);
			for (int ej = 0; ej < rad_lines; ej++) {
				if (radType instanceof XrayEbelTubeRadiation && ej >= ((XrayEbelTubeRadiation) radType).getNumberOfCharacteristicsLines()) {
					double stepEnergy = (energyInKeV[ej] - lastEnergyInKeV) / sub20;
					double stepIntEnergy = (energy_intensity[ej] - lastEnergyIntensity) / sub20;
					double subEnergy = lastEnergyInKeV;
					double subEnergyInt = lastEnergyIntensity;
					for (int sub_i = 0; sub_i < sub20; sub_i++) {
						subEnergy += stepEnergy;
						subEnergyInt += stepIntEnergy;
						FluorescenceLine transfertLine = new FluorescenceLine(subEnergy, -1, 0);
/*				for (int j1 = 0; j1 < layersNumber; j1++) {
					double abs = overLayerAbsorption[j1][ej] / sinPhii;
					if (Double.isNaN(abs))
						overLayerAbsorption[j1][ej] = 0;
					else
						overLayerAbsorption[j1][ej] = Math.exp(-abs);
				}*/
						double detectorAbsorption = ((XRFDetector) detector).computeAbsorptionForLineWithEnergy(subEnergy);
						double detectorEfficiency = ((XRFDetector) detector).computeDetectorEfficiency(subEnergy);
						double areaCorrection = ((XRFDetector) detector).getAreaCorrection(sampleLinearArea);
						transfertLine.setIntensity(transfertLine.getIntensity() * source_intensity * subEnergyInt *
								detectorAbsorption * detectorEfficiency * areaCorrection / sub20);
						if (transfertLine.getIntensity() > 0)
							fluorescenceLines.add(transfertLine);
					}
					lastEnergyInKeV = energyInKeV[ej];
					lastEnergyIntensity = energy_intensity[ej];
				} else {
					double diffractionIntensity = adatafile.getDiffractionIntensityForFluorescence(energyInKeV[ej], twothetadetector);
					FluorescenceLine transfertLine = new FluorescenceLine(energyInKeV[ej], -1, 0);
					double detectorAbsorption = ((XRFDetector) detector).computeAbsorptionForLineWithEnergy(energyInKeV[ej]);
					double detectorEfficiency = ((XRFDetector) detector).computeDetectorEfficiency(energyInKeV[ej]);
					double areaCorrection = ((XRFDetector) detector).getAreaCorrection(sampleLinearArea);
					transfertLine.setIntensity(transfertLine.getIntensity() * source_intensity * energy_intensity[ej] *
							diffractionIntensity * detectorAbsorption * detectorEfficiency * areaCorrection);
					if (transfertLine.getIntensity() > 0)
						fluorescenceLines.add(transfertLine);
				}
			}
		}

		for (int k = 0; k < fluorescenceLines.size(); k++) {
			FluorescenceLine line = fluorescenceLines.get(k);
			double[] broad = ainstrument.getInstrumentalBroadeningAt(line.getEnergy(), adatafile);
			line.setShape(broad[1], broad[0], broad[3], broad[2]);
			line.setEnergy(line.getEnergy() * 1000.0); // in eV
//      System.out.println(line.getEnergy() + " " + line.getIntensity());

			if (!getFilePar().isComputingDerivate() && Constants.testing)
				line.printToConsole();
			for (int i = 0; i < numberOfPoints; i++)
				fluorescence[i] += line.getIntensity(xEnergy[i]);
		}

		for (int i = 0; i < numberOfPoints; i++) {
			fluorescence[i] *= incidentIntensity;
			adatafile.addtoFit(i, fluorescence[i]);
//        System.out.println("Point: " + xEnergy[i] + ", intensity: " + fluorescence[i]);
		}
	}

/*	public FluorescenceLine[] getDiffractionLines() {
		Vector<FluorescenceLine> fluorescenceLines = new Vector<FluorescenceLine>(initialContent, 100);
		if (radType instanceof XrayEbelTubeRadiation && ej >= ((XrayEbelTubeRadiation) radType).getNumberOfCharacteristicsLines()) {
			double stepEnergy = (energyInKeV - lastEnergyInKeV) / sub20;
			double stepIntEnergy = (energy_intensity - lastEnergyIntensity) / sub20;
			double subEnergy = lastEnergyInKeV;
			double subEnergyInt = lastEnergyIntensity;
			for (int sub_i = 0; sub_i < sub20; sub_i++) {
				subEnergy += stepEnergy;
				subEnergyInt += stepIntEnergy;
				FluorescenceLine transfertLine = new FluorescenceLine(subEnergy, -1);
				double absorptionLineLambda = layer.getAbsorption(subEnergy) * density;
				double totalIntensity = Math.exp(-layer.getOverLayerAbsorption(subEnergy) * density / sinPhid)
						* subEnergyInt;
				double totalRe = 0.0;
				double mhuOverSinD = absorptionLineLambda / sinPhid;
//						System.out.println("Layer: " + j1 + ", atom: " + k + ", line: " + ij + ", energy " + lineEnergyKeV + ", intensity " + totalIntensity + ", mhuOverSinD " + mhuOverSinD);
				for (int m = 0; m < 2; m++) {
					double bm_mhu = bE[m] + mhuOverSinD;
//					  System.out.println(bm_mhu);
					double expm = -bm_mhu * thickness; //_or_zero;
					if (expm > -Double.MAX_EXPONENT / 2 && expm < Double.MAX_EXPONENT / 2)
						expm = Math.exp(expm);
					else {
						if (expm > 0)
							expm = Double.MAX_VALUE / 2;
						else
							expm = 0;
					}
					totalRe += AE[m] * ((1.0 - expm) / bm_mhu);
//							System.out.println("m bm be Ae: " + m + " " + bm_mhu + " " + bE[m] + " " + AE[m] + " " + expm + " " + totalRe);
				}
				double re1 = -thickness * (bE[2] + mhuOverSinD);
				double im1 = -thickness * bE[3];
				double[] res = MoreMath.complexExp(re1, im1);
				res[0] = 1.0 - res[0];
				res[1] = -res[1];
				res = MoreMath.complexDivide(res[0], res[1], bE[2] + mhuOverSinD, bE[3]);
				totalRe += MoreMath.complexMultiply(AE[2], AE[3], res[0], res[1])[0];
				totalRe *= density;
//  				  System.out.println(AE[2] + " " + AE[3] + " " + totalRe);
//						System.out.println("Layer: " + j + " " + chemicalComposition.elementAt(k).label + " " + lineEnergyKeV + " " + line.getIntensity() + " " + atomsQuantities + " " + totalRe + " " + totalIntensity + " " + detectorAbsorption + " " + detectorEfficiency + " " + getIntensityCorrection(atomNumber));
				double detectorAbsorption = ((XRFDetector) detector).computeAbsorptionForLineWithEnergy(subEnergy);
				double detectorEfficiency = ((XRFDetector) detector).computeDetectorEfficiency(subEnergy);
				double areaCorrection = ((XRFDetector) detector).getAreaCorrection(sampleLinearArea);
				transfertLine.setIntensity(transfertLine.getIntensity() * totalRe * totalIntensity * source_intensity *
						detectorAbsorption * detectorEfficiency * areaCorrection);
				boolean addLine = true;
				for (int i = 0; i < fluorescenceLines.size() && addLine; i++) {
					FluorescenceLine lineExisting = fluorescenceLines.get(i);
					if (lineExisting.getEnergy() == transfertLine.getEnergy()) {
						addLine = false;
						lineExisting.setIntensity(lineExisting.getIntensity() + transfertLine.getIntensity());
					}
				}
				if (addLine)
					fluorescenceLines.add(transfertLine);
			}
		} else {
			double subEnergy = energyInKeV;
			double subEnergyInt = energy_intensity;
			FluorescenceLine transfertLine = new FluorescenceLine(subEnergy, -1);
			double absorptionLineLambda = layer.getAbsorption(subEnergy) * density;
			double totalIntensity = Math.exp(-layer.getOverLayerAbsorption(subEnergy) * density / sinPhid)
					* subEnergyInt;
			double totalRe = 0.0;
			double mhuOverSinD = absorptionLineLambda / sinPhid;
//						System.out.println("Layer: " + j1 + ", atom: " + k + ", line: " + ij + ", energy " + lineEnergyKeV + ", intensity " + totalIntensity + ", mhuOverSinD " + mhuOverSinD);
			for (int m = 0; m < 2; m++) {
				double bm_mhu = bE[m] + mhuOverSinD;
//					  System.out.println(bm_mhu);
				double expm = -bm_mhu * thickness; //_or_zero;
				if (expm > -Double.MAX_EXPONENT / 2 && expm < Double.MAX_EXPONENT / 2)
					expm = Math.exp(expm);
				else {
					if (expm > 0)
						expm = Double.MAX_VALUE / 2;
					else
						expm = 0;
				}
				totalRe += AE[m] * ((1.0 - expm) / bm_mhu);
//							System.out.println("m bm be Ae: " + m + " " + bm_mhu + " " + bE[m] + " " + AE[m] + " " + expm + " " + totalRe);
			}
			double re1 = -thickness * (bE[2] + mhuOverSinD);
			double im1 = -thickness * bE[3];
			double[] res = MoreMath.complexExp(re1, im1);
			res[0] = 1.0 - res[0];
			res[1] = -res[1];
			res = MoreMath.complexDivide(res[0], res[1], bE[2] + mhuOverSinD, bE[3]);
			totalRe += MoreMath.complexMultiply(AE[2], AE[3], res[0], res[1])[0];
			totalRe *= density;
//  				  System.out.println(AE[2] + " " + AE[3] + " " + totalRe);
//						System.out.println("Layer: " + j + " " + chemicalComposition.elementAt(k).label + " " + lineEnergyKeV + " " + line.getIntensity() + " " + atomsQuantities + " " + totalRe + " " + totalIntensity + " " + detectorAbsorption + " " + detectorEfficiency + " " + getIntensityCorrection(atomNumber));
			double detectorAbsorption = ((XRFDetector) detector).computeAbsorptionForLineWithEnergy(subEnergy);
			double detectorEfficiency = ((XRFDetector) detector).computeDetectorEfficiency(subEnergy);
			double areaCorrection = ((XRFDetector) detector).getAreaCorrection(sampleLinearArea);
			transfertLine.setIntensity(transfertLine.getIntensity() * totalRe * totalIntensity * source_intensity *
					detectorAbsorption * detectorEfficiency * areaCorrection);
			boolean addLine = true;
			for (int i = 0; i < fluorescenceLines.size() && addLine; i++) {
				FluorescenceLine lineExisting = fluorescenceLines.get(i);
				if (lineExisting.getEnergy() == transfertLine.getEnergy()) {
					addLine = false;
					lineExisting.setIntensity(lineExisting.getIntensity() + transfertLine.getIntensity());
				}
			}
			if (addLine)
				fluorescenceLines.add(transfertLine);
		}
		return null;
	}*/
}
