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

package playground.anhorni.csestimation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.geometry.CoordImpl;

import playground.anhorni.surprice.MultiDayControler;

public class Controler {

	public static ArrayList<String> modes = new ArrayList<String>(Arrays.asList("car", "pt", "bike", "walk", ""));
	public static ArrayList<String> frequency = new ArrayList<String>(Arrays.asList("VeryOften", "Often", "OnceAWhile", "Seldom", "Never", "NULL", ""));
	private ArrayList<Person> population = new ArrayList<Person>();
	
	private final static Logger log = Logger.getLogger(Controler.class);
	
	
	public static void main(String[] args) {
		Controler c = new Controler();
		String personFile = args[0];
		String personShopsFile = args[1];
		String addedShopsFile = args[2];
		c.run(personFile, personShopsFile, addedShopsFile);
	}
	
	public void run(String personFile, String personShopsFile, String addedShopsFile) {
		this.readDumpedPersons(personFile);
		log.info(this.population.size() + " persons created");
		this.readDumpedPersonShops(personShopsFile);
		this.readDumpedAddedShops(addedShopsFile);	
		log.info("finished .......................................");
	}
	
	
// 0: username,		1: age,		2: sex,		3: income,	4: hhsize,	5: shoppingPerson,	6: purchasesPerMonth,	
// 7: H_Street,		8: H_nbr,	9: H_PLZ,	10: H_city, 11: H_Lat, 12: H_Lng,
// 13: fCar, 		14: fPt, 	15: fBike,	16: fWalk,	17: job, 
// 18: W_Street,	19: W_nbr,	20: W_PLZ,	21: W_city, 22: W_Lat, 23: W_Lng,			24: noAddressWork, 
// 25: mode,		26: fHome,	27: fWork,	28: fInter,	29: fOther

	private void readDumpedPersons(String file) {		
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String curr_line;
			while ((curr_line = br.readLine()) != null) {
				String[] entrs = curr_line.split(";", -1);
				Id id = new IdImpl(entrs[0].trim());
				
				Person person = new Person(id);
				this.population.add(person);
				
				person.setAge(Integer.parseInt(entrs[1].trim()));
				person.setSex(entrs[2].trim());
				// TODO: hh income -99 = AHV
				if (!entrs[3].trim().equals("")) {
					person.setHhIncome(Integer.parseInt(entrs[3].trim()));
				}
				else {
					person.setHhIncome(-1);
				}
				person.setHhSize(Integer.parseInt(entrs[4].trim()));
				person.setNbrPersonShoppingTripsMonth(Integer.parseInt(entrs[5].trim()));
				person.setNbrShoppingTripsMonth(entrs[6].trim());

				Location hlocation = new Location();
				person.setHomeLocation(hlocation);
				hlocation.setCity(entrs[10].trim());
				hlocation.setCoord(new CoordImpl(Double.parseDouble(entrs[11].trim()), Double.parseDouble(entrs[12].trim()))); // TODO: prüfen bei NULL
				
				person.setModesForShopping(Controler.modes.indexOf("car"), Controler.frequency.indexOf(entrs[13].trim().replaceAll("car", "")));
				person.setModesForShopping(Controler.modes.indexOf("pt"), Controler.frequency.indexOf(entrs[14].trim().replaceAll("pt", "")));
				person.setModesForShopping(Controler.modes.indexOf("bike"), Controler.frequency.indexOf(entrs[15].trim().substring(1)));
				person.setModesForShopping(Controler.modes.indexOf("walk"), Controler.frequency.indexOf(entrs[16].trim().substring(1)));
				
				if (entrs[17].trim().equals("yes")) {
					Location wlocation = new Location();
					person.setWorkLocation(wlocation);
					wlocation.setCity(entrs[21].trim());
					wlocation.setCoord(new CoordImpl(Double.parseDouble(entrs[22].trim()), Double.parseDouble(entrs[23].trim())));
				
					if (entrs[25].trim().contains("car")) person.setModeForWorking(Controler.modes.indexOf("car"), true);
					if (entrs[25].trim().contains("pt")) person.setModeForWorking(Controler.modes.indexOf("pt"), true);
					if (entrs[25].trim().contains("bike")) person.setModeForWorking(Controler.modes.indexOf("bike"), true);
					if (entrs[25].trim().contains("walk")) person.setModeForWorking(Controler.modes.indexOf("walk"), true);		
					
					person.setAreaToShop(0, Controler.frequency.indexOf(entrs[26].trim().replaceAll("home", "")));
					person.setAreaToShop(1, Controler.frequency.indexOf(entrs[27].trim().replaceAll("work", "")));
					person.setAreaToShop(2, Controler.frequency.indexOf(entrs[28].trim().replaceAll("inter", "")));
					person.setAreaToShop(3, Controler.frequency.indexOf(entrs[29].trim().replaceAll("other", "")));
				}				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 		
	}
	
	private void readDumpedPersonShops(String file) {		
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String curr_line;
			while ((curr_line = br.readLine()) != null) {
				String[] entrs = curr_line.split("\t", -1);

				Id id = new IdImpl(entrs[0].trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 		
	}
	
	private void readDumpedAddedShops(String file) {		
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String curr_line;
			while ((curr_line = br.readLine()) != null) {
				String[] entrs = curr_line.split("\t", -1);

				Id id = new IdImpl(entrs[0].trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 		
	}
}
