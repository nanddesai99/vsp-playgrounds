/* *********************************************************************** *
 * project: org.matsim.*
 * PreprocessedModelRunnerConfigGroup.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
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
package playground.thibautd.initialdemandgeneration.socnetgensimulated.framework;

import org.matsim.core.config.experimental.ReflectiveConfigGroup;

/**
 * @author thibautd
 */
public class PreprocessedModelRunnerConfigGroup extends ReflectiveConfigGroup {
	public static final String GROUP_NAME = "preprocessedRunner";

	private double lowestStoredPrimary = 0;
	private double lowestStoredSecondary = 0;

	private int randomSeed = 20150116;

	private double primarySampleRate = 0.1;
	private double secondarySampleRate = 0.1;

	private int maxSizePrimary = 200;
	private int maxSizeSecondary = 500;

	private int nThreads = 1;

	public PreprocessedModelRunnerConfigGroup() {
		super( GROUP_NAME );
	}

	@StringGetter( "lowestStoredPrimary" )
	public double getLowestStoredPrimary() {
		return lowestStoredPrimary;
	}

	@StringSetter( "lowestStoredPrimary" )
	public void setLowestStoredPrimary( double lowestStoredPrimary ) {
		this.lowestStoredPrimary = lowestStoredPrimary;
	}

	@StringGetter( "lowestStoredSecondary" )
	public double getLowestStoredSecondary() {
		return lowestStoredSecondary;
	}

	@StringSetter( "lowestStoredSecondary" )
	public void setLowestStoredSecondary( double lowestStoredSecondary ) {
		this.lowestStoredSecondary = lowestStoredSecondary;
	}

	@StringGetter( "randomSeed" )
	public int getRandomSeed() {
		return randomSeed;
	}

	@StringSetter( "randomSeed" )
	public void setRandomSeed( int randomSeed ) {
		this.randomSeed = randomSeed;
	}

	@StringGetter( "primarySampleRate" )
	public double getPrimarySampleRate() {
		return primarySampleRate;
	}

	@StringSetter( "primarySampleRate" )
	public void setPrimarySampleRate( double primarySampleRate ) {
		this.primarySampleRate = primarySampleRate;
	}

	@StringGetter( "secondarySampleRate" )
	public double getSecondarySampleRate() {
		return secondarySampleRate;
	}

	@StringSetter( "secondarySampleRate" )
	public void setSecondarySampleRate( double secondarySampleRate ) {
		this.secondarySampleRate = secondarySampleRate;
	}

	@StringGetter( "maxSizePrimary" )
	public int getMaxSizePrimary() {
		return maxSizePrimary;
	}

	@StringSetter( "maxSizePrimary" )
	public void setMaxSizePrimary( int maxSizePrimary ) {
		this.maxSizePrimary = maxSizePrimary;
	}

	@StringGetter( "maxSizeSecondary" )
	public int getMaxSizeSecondary() {
		return maxSizeSecondary;
	}

	@StringSetter( "maxSizeSecondary" )
	public void setMaxSizeSecondary( int maxSizeSecondary ) {
		this.maxSizeSecondary = maxSizeSecondary;
	}

	@StringGetter( "nThreads" )
	public int getNThreads() {
		return nThreads;
	}

	@StringSetter( "nThreads" )
	public void setNThreads( int nThreads ) {
		this.nThreads = nThreads;
	}
}
