/*
 * @(#)XRFDetector.java created 29/5/2013 Caen
 *
 * Copyright (c) 2013 Luca Lutterotti All Rights Reserved.
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

package it.unitn.ing.rista.diffr.detector;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.*;
import java.util.Vector;

import it.unitn.ing.rista.awt.JOptionsDialog;
import it.unitn.ing.rista.awt.JSubordinateLoopListPane;
import it.unitn.ing.rista.chemistry.AtomInfo;
import it.unitn.ing.rista.chemistry.XRayDataSqLite;
import it.unitn.ing.rista.diffr.*;
import it.unitn.ing.rista.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 *  The XRFDetector is a class
 *
 *
 * @version $Revision: 1.0 $, $Date: 2013/05/29 15:37:26 $
 * @author Luca Lutterotti
 * @since JDK1.1
 */

public class XRFDetector extends Detector {

	public static String[] diclistc = {"_instrument_detector_2theta",
			"_instrument_detector_eta",
			"_instrument_detector_semiconductor_thickness",
			"_instrument_detector_semiconductor_density",
			"_instrument_detector_window_area",
			"_instrument_detector_area_correction_exp",
			"_instrument_detector_escape_peaks_intensity",
			"_instrument_detector_sum_peaks_intensity",
			"_instrument_detector_source_spectrum_intensity",
			"_instrument_detector_semiconductor_material",
			"_instrument_filter_material"
	};
	public static String[] diclistcrm = {"_instrument_detector_2theta",
			"_instrument_detector_eta",
			"_instrument_detector_semiconductor_thickness",
			"_instrument_detector_semiconductor_density",
			"_instrument_detector_window_area",
			"_instrument_detector_area_correction_exp",
			"_instrument_detector_escape_peaks_intensity",
			"_instrument_detector_sum_peaks_intensity",
			"_instrument_detector_source_spectrum_intensity",
			"_instrument_detector_semiconductor_material",
			"_instrument_filter_material"
	};

	public static String[] classlistc = {"superclass:it.unitn.ing.rista.diffr.CompositionElement",
			"superclass:it.unitn.ing.rista.diffr.AbsorptionWindow"};

	public static String[] classlistcs = {};

	private static int detector_2theta_id = 0;
	private static int detector_eta_id = 1;
	private static int semiconductor_thickness_id = 2;
	private static int semiconductor_density_id = 3;
	private static int window_area_id = 4;
	private static int window_area_correction_id = 5;
	private static int escape_peaks_id = 6;
	private static int sum_peaks_id = 7;
	private static int source_spectrum_id = 8;
	private static int semiconductor_composition_id = 0;
	private static int filter_material_id = 1;
	int integrationPointsNumber = 100;

	double totalSemiconductorAtomFraction = 1;

	public XRFDetector(XRDcat aobj, String alabel) {
		super(aobj, alabel);
		initXRD();
		identifier = "XRF Detector";
		IDlabel = "XRF Detector";
		description = "XRF Detector";
	}

	public XRFDetector(XRDcat aobj) {
		this(aobj, "XRF Detector");
	}

	public XRFDetector() {
		identifier = "XRF Detector";
		IDlabel = "XRF Detector";
		description = "XRF Detector";
	}

	public void initConstant() {
		Nstring = 0;
		Nstringloop = 0;
		Nparameter = 9;
		Nparameterloop = 0;
		Nsubordinate = 0;
		Nsubordinateloop = 2;
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

	public void initParameters() {
		super.initParameters();
		parameterField[detector_2theta_id] = new Parameter(this, getParameterString(detector_2theta_id), 90.0,
				ParameterPreferences.getDouble(getParameterString(detector_2theta_id) + ".min", 0.0),
				ParameterPreferences.getDouble(getParameterString(detector_2theta_id) + ".max", 360.0));
		parameterField[detector_eta_id] = new Parameter(this, getParameterString(detector_eta_id), 0.0,
				ParameterPreferences.getDouble(getParameterString(detector_eta_id) + ".min", 0.0),
				ParameterPreferences.getDouble(getParameterString(detector_eta_id) + ".max", 180.0));
		parameterField[semiconductor_thickness_id] = new Parameter(this, getParameterString(semiconductor_thickness_id), 0.05,
				ParameterPreferences.getDouble(getParameterString(semiconductor_thickness_id) + ".min", 0.01),
				ParameterPreferences.getDouble(getParameterString(semiconductor_thickness_id) + ".max", 1));
		parameterField[semiconductor_density_id] = new Parameter(this, getParameterString(semiconductor_density_id), 1.85,
				ParameterPreferences.getDouble(getParameterString(semiconductor_density_id) + ".min", 1),
				ParameterPreferences.getDouble(getParameterString(semiconductor_density_id) + ".max", 10));
		parameterField[window_area_id] = new Parameter(this, getParameterString(window_area_id), 0.25,
				ParameterPreferences.getDouble(getParameterString(window_area_id) + ".min", 0.01),
				ParameterPreferences.getDouble(getParameterString(window_area_id) + ".max", 10));
		parameterField[window_area_correction_id] = new Parameter(this, getParameterString(window_area_correction_id), 1,
				ParameterPreferences.getDouble(getParameterString(window_area_correction_id) + ".min", 0.1),
				ParameterPreferences.getDouble(getParameterString(window_area_correction_id) + ".max", 10));
		parameterField[escape_peaks_id] = new Parameter(this, getParameterString(escape_peaks_id), 1E-3,
				ParameterPreferences.getDouble(getParameterString(escape_peaks_id) + ".min", 1.0E-4),
				ParameterPreferences.getDouble(getParameterString(escape_peaks_id) + ".max", 1.0E-2));
		parameterField[sum_peaks_id] = new Parameter(this, getParameterString(sum_peaks_id), 0,
				ParameterPreferences.getDouble(getParameterString(sum_peaks_id) + ".min", 0),
				ParameterPreferences.getDouble(getParameterString(sum_peaks_id) + ".max", 1.0E-2));
		parameterField[source_spectrum_id] = new Parameter(this, getParameterString(source_spectrum_id), 0,
				ParameterPreferences.getDouble(getParameterString(source_spectrum_id) + ".min", 0),
				ParameterPreferences.getDouble(getParameterString(source_spectrum_id) + ".max", 100000));
	}

	public void updateStringtoDoubleBuffering(boolean firstLoading) {
		super.updateStringtoDoubleBuffering(firstLoading);

		double totalFraction = 0;
		for (int i = 0; i < subordinateloopField[semiconductor_composition_id].size(); i++)
			totalFraction += ((CompositionElement) subordinateloopField[semiconductor_composition_id].elementAt(i)).getParameterValue(0);
		totalSemiconductorAtomFraction = totalFraction;

		integrationPointsNumber = MaudPreferences.getInteger("fluorescenceDetector.numberOfIntegrationPoints", 1);
	}

	public double getThetaDetector(DiffrDataFile datafile, double twotheta) {
		return getParameterValue(detector_2theta_id) + datafile.get2ThetaValue();
	}

	public double getEtaDetector(DiffrDataFile datafile) {
		return getParameterValue(detector_eta_id);
	}

	public double getSemiconductorThickness() {
		return getParameterValue(semiconductor_thickness_id);
	}

	public double getSemiconductorDensity() {
		return getParameterValue(semiconductor_density_id);
	}

	public double getEscapePeaksIntensity() {
		return Math.abs(getParameterValue(escape_peaks_id));
	}

	public double getWindowArea() {
		return Math.abs(getParameterValue(escape_peaks_id));
	}

	public double getSumPeaksIntensity() {
		return Math.abs(getParameterValue(sum_peaks_id));
	}

	public double getSourceSpectrumIntensity() {
		return Math.abs(getParameterValue(source_spectrum_id));
	}

	public double getGeometryCorrection(double beamOutCorrection) {

//		System.out.print(beamOutCorrection);
/*    first approximation
		if (A > w0) {
			double bs_over_h = (b0 + s0) * 0.25 / h0;
			double correction =  w0 + (1.0 + bs_over_h) * (A - w0) - bs_over_h * 0.5 / w0 * (A * A - w0 * w0);

//			System.out.println(" " + correction);
			return correction;
		}*/
//		System.out.println();
		return beamOutCorrection;
	}

	public double getFiltersFluorescenceIntensityTotal() {
		double totalFluorescence = 0;
		for (int i = 0; i < getList(filter_material_id).size(); i++) {
			AbsorptionWindow absorptionWindow = (AbsorptionWindow) getList(filter_material_id).elementAt(i);
			totalFluorescence += absorptionWindow.getFluorescenceIntensity();
		}
		return totalFluorescence;
	}

	public Vector<FluorescenceLine> getFluorescenceLines(double energyInKeV) {
		Vector<FluorescenceLine> allLines = new Vector<FluorescenceLine>();
		for (int i = 0; i < getList(filter_material_id).size(); i++) {
			AbsorptionWindow absorptionWindow = (AbsorptionWindow) getList(filter_material_id).elementAt(i);
			allLines.addAll(absorptionWindow.getFluorescenceLines(energyInKeV));
		}
		return allLines;
	}

	public Vector<FluorescenceLine> getEscapePeaks(double energyInKeV, Vector<FluorescenceLine> fluorescenceLines) {
		double minIntensityEscapePeaks = MaudPreferences.getDouble("fluorescence.escapePeaksIntensityRatioToConsider", 0.01);
//		boolean testEscape = true;
//		if (Constants.testing)
//			testEscape = MaudPreferences.getBoolean("fluorescence.escapePeaksIntensityTest", false);
		double intensityRatio = getEscapePeaksIntensity();
		Vector<FluorescenceLine> allLines = new Vector<FluorescenceLine>();
		double totalFraction = getTotalSemiconductorAtomFraction();
		for (int i = 0; i < subordinateloopField[semiconductor_composition_id].size(); i++) {
			String atomLabel = ((CompositionElement) subordinateloopField[semiconductor_composition_id].elementAt(i)).getString(0);
			double atomFraction = ((CompositionElement) subordinateloopField[semiconductor_composition_id].elementAt(i)).getParameterValue(0) / totalFraction;
			int atomNumber = AtomInfo.retrieveAtomNumber(atomLabel);
			Vector<FluorescenceLine> linesForAtom = XRayDataSqLite.getFluorescenceLinesFor(
					atomNumber, energyInKeV);
			for (int j = 0; j < linesForAtom.size(); j++) {
				FluorescenceLine line = linesForAtom.elementAt(j);
				line.setIntensity(line.getIntensity() * atomFraction);
			}
			allLines.addAll(linesForAtom);
		}
		double maxIntensity = 0;
		for (int i = 0; i < allLines.size(); i++) {
			FluorescenceLine line = allLines.get(i);
			if (line.getIntensity() > maxIntensity)
				maxIntensity = line.getIntensity();
		}
		maxIntensity *= minIntensityEscapePeaks;
		Vector<FluorescenceLine> linesForSubtraction = new Vector<FluorescenceLine>();
		for (int i = 0; i < allLines.size(); i++) {
			FluorescenceLine line = allLines.get(i);
			if (line.getIntensity() > maxIntensity)
				linesForSubtraction.add(line);
		}
		allLines.removeAllElements();
		for (int i = 0; i < linesForSubtraction.size(); i++) {
			FluorescenceLine lineSemiconductor = linesForSubtraction.get(i);
			double lineIntensity = lineSemiconductor.getIntensity();
			double lineEnergy = lineSemiconductor.getEnergy();
			lineSemiconductor.setIntensity(0);
//			System.out.println("Semiconductor line: " + lineEnergy + " " + lineIntensity);
			for (int j = 0; j < fluorescenceLines.size(); j++) {
				FluorescenceLine line = fluorescenceLines.get(j);
				if (line.getEnergy() - lineEnergy > 0) {
					FluorescenceLine newLine = new FluorescenceLine(line);
					newLine.setEnergy(line.getEnergy() - lineEnergy);
					double intensityEscapePeak = line.getIntensity() * intensityRatio * lineIntensity;
					newLine.setIntensity(intensityEscapePeak);
/*					if (testEscape) {
						line.setIntensity(line.getIntensity() - intensityEscapePeak);
						lineSemiconductor.setIntensity(lineSemiconductor.getIntensity() + intensityEscapePeak);
					}*/
//					System.out.println("Adding new line: " + newLine.getEnergy() + " " + newLine.getIntensity());
					allLines.add(newLine);
				}
			}
//			if (testEscape)
//				allLines.add(lineSemiconductor);
		}

		return allLines;
	}

	public Vector<FluorescenceLine> getPileUpPeaks(double maxEnergyInKeV, Vector<FluorescenceLine> fluorescenceLines) {
		double minIntensitySumPeaks = MaudPreferences.getDouble("fluorescence.pileUpPeaksIntensityRatioToConsider", 0.01);
		double intensityRatio = getSumPeaksIntensity();
		Vector<FluorescenceLine> allLines = new Vector<FluorescenceLine>();
		double maxIntensity = 0;
		double[] intensities = new double[fluorescenceLines.size()];
		for (int i = 0; i < fluorescenceLines.size(); i++) {
			FluorescenceLine line = fluorescenceLines.get(i);
			if ((intensities[i] = line.getIntensity()) > maxIntensity)
				maxIntensity = intensities[i];
		}
		maxIntensity *= minIntensitySumPeaks;
		for (int i = 0; i < fluorescenceLines.size(); i++) {
			FluorescenceLine firstLine = fluorescenceLines.get(i);
			if (intensities[i] > maxIntensity) {
				double firstLineIntensityRatio = intensities[i] * intensityRatio;
				double firstLineEnergy = firstLine.getEnergy();
				for (int j = 0; j < fluorescenceLines.size(); j++) {
					FluorescenceLine secondLine = fluorescenceLines.get(j);
					if (intensities[j]  > maxIntensity) {
						double sumEnergy = firstLineEnergy + secondLine.getEnergy();
						if (sumEnergy < maxEnergyInKeV) {
							FluorescenceLine newLine = new FluorescenceLine(sumEnergy, -1, 0);
							double totalSumIntensity = firstLineIntensityRatio * intensities[j];
							newLine.setIntensity(totalSumIntensity);
							firstLine.setIntensity(firstLine.getIntensity() - totalSumIntensity);
							secondLine.setIntensity(secondLine.getIntensity() - totalSumIntensity);
							allLines.add(newLine);
						}
					}
				}
			}
		}

		return allLines;
	}

	public double computeAbsorptionForLineWithEnergy(double energyInKeV) {
		double integral = 1.0;
		for (int i = 0; i < getList(filter_material_id).size(); i++) {
			AbsorptionWindow absorptionWindow = (AbsorptionWindow) getList(filter_material_id).elementAt(i);
			integral *= absorptionWindow.computeAbsorptionForLineWithEnergy(energyInKeV);
		}
		return integral;
	}

	public double computeDetectorEfficiency(double energyInKeV) {
		double absorption = 0;
		double totalFraction = getTotalSemiconductorAtomFraction();
		for (int i = 0; i < subordinateloopField[semiconductor_composition_id].size(); i++) {
			String atomLabel = ((CompositionElement) subordinateloopField[semiconductor_composition_id].elementAt(i)).getString(0);
			double atomFraction = ((CompositionElement) subordinateloopField[semiconductor_composition_id].elementAt(i)).getParameterValue(0) / totalFraction;
			int atomNumber = AtomInfo.retrieveAtomNumber(atomLabel);
			absorption += atomFraction * XRayDataSqLite.getTotalAbsorptionForAtomAndEnergy(atomNumber, energyInKeV);
		}
		absorption *= getSemiconductorDensity() * getSemiconductorThickness(); // linear absorption
		absorption = Math.abs(absorption);

		double integral = 1.0;
/*		if (integrationPointsNumber > 1) {
			double h0 = getDistanceFromSample();
			double integralUpperLimit = Math.atan(linearArea / h0 * 0.5);
			double integrationStep = integralUpperLimit / (integrationPointsNumber - 1);
			if (absorption < 1E40) {
				for (int i = 0; i < integrationPointsNumber; i++) {
					double phi = integrationStep * i;
					double tanphi = Math.tan(phi);
					integral += (1.0 + tanphi * tanphi) * Math.exp(- absorption / Math.cos(phi));
				}
				integral *= 2.0 * h0 * integrationStep;
//				System.out.println(energyInKeV + " " + linearArea + " " + integralUpperLimit + " " + absorption + " " +
//						integral / (linearArea * Math.exp(- absorption)));
			}
		} else {*/
			if (absorption < 1E40)
				integral = 1.0 - Math.exp(- absorption);
//		}

		return integral; // * areaCorrection;
	}

	public double getAreaCorrection(double linearArea) {
		double exponent = getParameterValue(window_area_correction_id);
		double areaCorrection = Math.pow(linearArea, exponent);
		return areaCorrection;
	}

	public double getTotalSemiconductorAtomFraction() {
		return totalSemiconductorAtomFraction;
	}

	public JOptionsDialog getOptionsDialog(Frame parent) {
		JOptionsDialog adialog = new JXRFDetectorOptionsD(parent, this);
		return adialog;
	}

	public class JXRFDetectorOptionsD extends JOptionsDialog {

		JTextField theta2TF;
		JTextField etaTF;
		JTextField escapeIntensityTF;
		JTextField sumIntensityTF;
		JTextField semiconductorThicknessTF;
		JTextField semiconductorDensityTF;
		JTextField windowGapTF;
		JTextField windowAreaTF;
		JTextField spectrumIntensityTF;
		JButton addSemiB = new JButton("Add element");
		JButton removeSemiB = new JButton("Remove element");
		elementTableModel semiconductorCompositionModel = null;
		JTable semiconductorCompositionTable = null;
		JSubordinateLoopListPane absorptionMaterialPanel;

		public JXRFDetectorOptionsD(Frame parent, XRDcat obj) {

			super(parent, obj);

			principalPanel.setLayout(new BorderLayout(3, 3));
			JPanel jp1 = new JPanel();
			principalPanel.add(BorderLayout.CENTER, jp1);
			jp1.setLayout(new GridLayout(0, 2, 6, 6));
			jp1.add(new JLabel("2Theta position (degrees):"));
			theta2TF = new JTextField(Constants.FLOAT_FIELD);
			theta2TF.setToolTipText("Insert the 2Theta position of the detector in degrees");
			jp1.add(theta2TF);
			jp1.add(new JLabel("Eta position (degrees):"));
			etaTF = new JTextField(Constants.FLOAT_FIELD);
			etaTF.setToolTipText("Insert the 2Theta position of the detector in degrees");
			jp1.add(etaTF);
			jp1.add(new JLabel("Intensity escape lines:"));
			escapeIntensityTF = new JTextField(Constants.FLOAT_FIELD);
			escapeIntensityTF.setToolTipText("Intensity/probability of the escape peaks");
			jp1.add(escapeIntensityTF);
			jp1.add(new JLabel("Intensity pile-up lines:"));
			sumIntensityTF = new JTextField(Constants.FLOAT_FIELD);
			sumIntensityTF.setToolTipText("Intensity/probability of the pile-up lines");
			jp1.add(sumIntensityTF);
			jp1.add(new JLabel("Semiconductor(Si) thickness:"));
			semiconductorThicknessTF = new JTextField(Constants.FLOAT_FIELD);
			semiconductorThicknessTF.setToolTipText("Insert the thickness in cm of the semiconductor");
			jp1.add(semiconductorThicknessTF);
			jp1.add(new JLabel("Semiconductor density:"));
			semiconductorDensityTF = new JTextField(Constants.FLOAT_FIELD);
			semiconductorDensityTF.setToolTipText("Insert the semiconductor density in g/cm3");
			jp1.add(semiconductorDensityTF);
			jp1.add(new JLabel("Window area:"));
			windowAreaTF = new JTextField(Constants.FLOAT_FIELD);
			windowAreaTF.setToolTipText("Insert the area of the window in cm2");
			jp1.add(windowAreaTF);
			jp1.add(new JLabel("Area correction exponent:"));
			windowGapTF = new JTextField(Constants.FLOAT_FIELD);
			windowGapTF.setToolTipText("Insert the exponent for the area correction (for testing only)");
			jp1.add(windowGapTF);
			jp1.add(new JLabel("Tube spectrum intensity:"));
			spectrumIntensityTF = new JTextField(Constants.FLOAT_FIELD);
			spectrumIntensityTF.setToolTipText("Insert the intensity of tube spectrum");
			jp1.add(spectrumIntensityTF);

			JTabbedPane jpT = new JTabbedPane();
			principalPanel.add(BorderLayout.EAST, jpT);

			jp1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
			jpT.addTab("Semiconductor composition", jp1);
			semiconductorCompositionModel = new elementTableModel(semiconductor_composition_id);
			semiconductorCompositionTable = new JTable(semiconductorCompositionModel);
			JScrollPane semiconductorCompositionScrollpane = new JScrollPane(semiconductorCompositionTable);
			JPanel jPanel = new JPanel(new BorderLayout(3, 3));
			jp1.add(jPanel);
			jPanel.add(BorderLayout.CENTER, semiconductorCompositionScrollpane);
			JPanel jp2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
			jPanel.add(BorderLayout.EAST, jp2);
			JPanel jp3 = new JPanel(new GridLayout(0, 1, 3, 3));
			jp2.add(jp3);
			jp3.add(addSemiB);
			jp3.add(removeSemiB);
			addSemiB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					semiconductorCompositionModel.add();
				}
			});
			removeSemiB.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					int index = semiconductorCompositionTable.getSelectedRow();
					semiconductorCompositionModel.remove(index);
				}
			});

//			jp1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
			absorptionMaterialPanel = new JSubordinateLoopListPane(this, "List of windows and filters");
			jpT.addTab("Filters and windows", absorptionMaterialPanel);

			setTitle("XRF detector data");
			initParameters();
			pack();
		}

		public void initParameters() {
			theta2TF.setText(getParameterValueAsString(detector_2theta_id));
			addComponenttolist(theta2TF, getParameter(detector_2theta_id));
			etaTF.setText(getParameterValueAsString(detector_eta_id));
			addComponenttolist(etaTF, getParameter(detector_eta_id));
			escapeIntensityTF.setText(getParameterValueAsString(escape_peaks_id));
			addComponenttolist(escapeIntensityTF, getParameter(escape_peaks_id));
			sumIntensityTF.setText(getParameterValueAsString(sum_peaks_id));
			addComponenttolist(sumIntensityTF, getParameter(sum_peaks_id));
			windowAreaTF.setText(getParameterValueAsString(window_area_id));
			addComponenttolist(windowAreaTF, getParameter(window_area_id));
			windowGapTF.setText(getParameterValueAsString(window_area_correction_id));
			addComponenttolist(windowGapTF, getParameter(window_area_correction_id));
			spectrumIntensityTF.setText(getParameterValueAsString(source_spectrum_id));
			addComponenttolist(spectrumIntensityTF, getParameter(source_spectrum_id));
			semiconductorThicknessTF.setText(getParameterValueAsString(semiconductor_thickness_id));
			addComponenttolist(semiconductorThicknessTF, getParameter(semiconductor_thickness_id));
			semiconductorDensityTF.setText(getParameterValueAsString(semiconductor_density_id));
			addComponenttolist(semiconductorDensityTF, getParameter(semiconductor_density_id));

			absorptionMaterialPanel.setList(XRFDetector.this, filter_material_id);
		}

		/**
		 * This method is automatically called when the user press the close button on the dialog.
		 */
		public void retrieveParameters() {
			getParameter(detector_2theta_id).setValue(theta2TF.getText());
			getParameter(detector_eta_id).setValue(etaTF.getText());
			getParameter(escape_peaks_id).setValue(escapeIntensityTF.getText());
			getParameter(sum_peaks_id).setValue(sumIntensityTF.getText());
			getParameter(window_area_correction_id).setValue(windowGapTF.getText());
			getParameter(window_area_id).setValue(windowAreaTF.getText());
			getParameter(source_spectrum_id).setValue(spectrumIntensityTF.getText());
			getParameter(semiconductor_density_id).setValue(semiconductorDensityTF.getText());
			getParameter(semiconductor_thickness_id).setValue(semiconductorThicknessTF.getText());
		}
	}

	public class elementTableModel extends AbstractTableModel {

		/**
		 * Column labels for the table
		 */
		private String columns[] = {"Atom", "Quantity"};

		/**
		 * Number of columns in the table, stored for speed enhancement.
		 */
		private int numColumns = columns.length;

		/**
		 * The object storing the preferences
		 */
		int subordinateListIndex = -1;

		/**
		 * Sets up the subordinate object loop where to retrieve the fields and
		 * values to generate the table.
		 *
		 * @param listIndex				index of the subordinateLoop
		 * @see                 XRDcat
		 */

		public elementTableModel(int listIndex) {
			subordinateListIndex = listIndex;
		}

		public void add() {
			int size = getRowCount();
			CompositionElement element = new CompositionElement(XRFDetector.this);
			element.setString(0, "Si");
			addsubordinateloopField(subordinateListIndex, element);
			fireTableRowsInserted(size, size);
		}

		public void remove(int index) {
			if (index >= 0 && index < getRowCount()) {
				subordinateloopField[subordinateListIndex].removeItemAt(index);
				fireTableRowsDeleted(index, index);
			}
		}

		/**
		 * @return the column number, equal to the number fields in the Hashtable
		 */

		public int getColumnCount() {
			return numColumns;
		}

		/**
		 * @return the row number
		 */

		public int getRowCount() {
			return subordinateloopField[subordinateListIndex].size();
		}

		/**
		 * Gives the content to be displayed at position row x column.
		 * It is retrieved directly from the Hashtable.
		 * In any row the elements are:
		 *
		 *  field 	value
		 *
		 * @param  row		the row position
		 * @param  column	the column position
		 * @return the element at position row x column
		 *
		 */

		public Object getValueAt(int row, int column) {
			switch (column) {
				case 0:
					return ((CompositionElement) subordinateloopField[subordinateListIndex].elementAt(row)).getString(0);
				case 1:
					return ((CompositionElement) subordinateloopField[subordinateListIndex].elementAt(row)).getParameter(0).getValue();
				default:
				{
					return null;
				}
			}
		}

		public void setValueAt(Object aValue, int row, int column) {
			switch (column) {
				case 0:
					((CompositionElement) subordinateloopField[subordinateListIndex].elementAt(row)).setString(0, (String) aValue);
					break;
				case 1:
					String sValue = (String) aValue;
					double dValue = Double.valueOf(sValue);
					((CompositionElement) subordinateloopField[subordinateListIndex].elementAt(row)).getParameter(0).setValue(dValue);
					break;
				default:
				{
				}
			}
			fireTableCellUpdated(row, column);
		}

		/**
		 * Gets the label of the <code>columnIndex</code> column
		 *
		 * @param columnIndex the column number
		 * @return the appropriate label for the column
		 */

		public String getColumnName(int columnIndex) {
			return columns[columnIndex];
		}

		// Types of the columns.
//	public Class[] cTypes = {String.class, String.class};

	/*public Class getColumnClass(int column) {
		return cTypes[column];
	}*/

		public Class getColumnClass(int c) {
			switch (c) {
				case 0:
					return String.class;
				case 1:
					return String.class;
				default:
				{
				}
			}
			return null;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0:
					return true;
				case 1:
					return true;
				default:
				{
				}
			}

			return false;
		}

	}

}
