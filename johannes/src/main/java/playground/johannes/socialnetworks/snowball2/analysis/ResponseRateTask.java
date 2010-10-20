/* *********************************************************************** *
 * project: org.matsim.*
 * ResponseRateTask.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.johannes.socialnetworks.snowball2.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.matsim.contrib.sna.graph.Graph;
import org.matsim.contrib.sna.graph.analysis.AnalyzerTask;
import org.matsim.contrib.sna.snowball.SampledGraph;

import playground.johannes.socialnetworks.survey.ivt2009.analysis.ResponseRate;

/**
 * @author illenberger
 *
 */
public class ResponseRateTask extends AnalyzerTask {

	@Override
	public void analyze(Graph g, Map<String, Double> stats) {
		SampledGraph graph = (SampledGraph) g;
		
		stats.put("responseRate", ResponseRate.responseRate(graph.getVertices()));
		
		if(getOutputDirectory() != null) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(getOutputDirectory() + "responseRates.txt"));
				writer.write("iteration\tresponseRate");
				writer.newLine();
				double[] rate = ResponseRate.responseRatesAccumulated(graph.getVertices());
				for(int i = 0; i < rate.length; i++) {
					writer.write(String.valueOf(i));
					writer.write("\t");
					writer.write(String.valueOf(rate[i]));
					writer.newLine();
				}
				writer.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

}
