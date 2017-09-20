/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.michalm.util.sim;

import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;

public class SimLauncher {
	public static void main(String[] args) {
		String dir;
		String cfgFile;

//		if (args.length == 1 && args[0].equals("test")) {// for testing
//			dir = "d:/PP-rad/taxi/mielec-2-peaks/2013_02/input/";
//			cfgFile = "siec-config.xml";
//			// dir = "d:\\PP-rad\\taxi\\poznan\\";
//			// cfgFile = "poznan-config.xml";
//		} else if (args.length == 2) {
			dir = "d:\\temp\\NY Bus\\";
			cfgFile = "config.xml";
//		} else {
//			throw new IllegalArgumentException("Incorrect program arguments: " + Arrays.toString(args));
//		}

		Controler controler = new Controler(new String[] { dir + cfgFile });
		controler.getConfig().controler().setOverwriteFileSetting(// OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles
				OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
		controler.run();
	}
}
