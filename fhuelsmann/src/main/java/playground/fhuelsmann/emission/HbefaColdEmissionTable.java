package playground.fhuelsmann.emission;
/* *********************************************************************** *
 * project: org.matsim.*
 * FhMain.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

 
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;


import playground.fhuelsmann.emission.objects.HbefaColdObject;

public class HbefaColdEmissionTable {
	
	private final  Map<String,Map<Integer,Map<Integer,HbefaColdObject>>> HbefaColdEmissionTable =
		new TreeMap<String,Map<Integer,Map<Integer,HbefaColdObject>>>();




	public Map<String,Map<Integer,Map<Integer,HbefaColdObject>>>  getHbefaColdTable() {
		return this.HbefaColdEmissionTable;
	}

	public void makeHbefaColdTable(String filename){
		try{

			FileInputStream fstream = new FileInputStream(filename);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line

			br.readLine();

			while ((strLine = br.readLine()) != null)   {

				//for all lines (whole text) we split the line to an array 

				String[] array = strLine.split(";");
				HbefaColdObject obj = new HbefaColdObject(
						array[0], //vehCat
						array[1], //component
						array[2], //parkingTime
						array[3], //distance
						Double.parseDouble(array[4]));//coldEF
						int distance = Integer.valueOf(array[3].split("-")[0]);
						int parkingTime = Integer.valueOf(array[2].split("-")[0]);
						if (this.HbefaColdEmissionTable.get(array[1]) != null){
							if(this.HbefaColdEmissionTable.get(array[1]).get(distance) != null){
								this.HbefaColdEmissionTable.get(array[1]).get(distance).put(parkingTime, obj);
								}else{
										Map<Integer,HbefaColdObject> tempParkingTime =
											new TreeMap<Integer,HbefaColdObject>();
										tempParkingTime.put(parkingTime, obj);
										this.HbefaColdEmissionTable.get(array[1]).put(distance, tempParkingTime);	  
										}
						}else{
							
							Map<Integer,HbefaColdObject> tempParkingTime =
								new TreeMap<Integer,HbefaColdObject>();
							
							tempParkingTime.put(parkingTime, obj);
							
							Map<Integer,Map<Integer,HbefaColdObject>> tempDistance =
								new TreeMap<Integer,Map<Integer,HbefaColdObject>>();
							
							tempDistance.put(parkingTime, tempParkingTime);
							this.HbefaColdEmissionTable.put(array[1], tempDistance);				
						
							
								}
							}
							
							
			
			
			//Close the input stream
			in.close();
		} catch (Exception e) {
			// Pass this exception upwards, because there is no way we can continue if we couldn't read this file!
			throw new RuntimeException(e);
		}
	}
}
