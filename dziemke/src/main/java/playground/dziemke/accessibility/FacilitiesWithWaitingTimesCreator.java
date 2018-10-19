package playground.dziemke.accessibility;

import java.io.BufferedReader;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.FacilitiesWriter;

public class FacilitiesWithWaitingTimesCreator {
	private static final Logger LOG = Logger.getLogger(FacilitiesWithWaitingTimesCreator.class);

	public static void main(String[] args) {
		String waitingTimesFile = "../../shared-svn/projects/accessibility_berlin/av/waittimes_500_access_grid/merged_10.csv";
		String facilitiesFile = "../../shared-svn/projects/accessibility_berlin/av/waittimes_500_access_grid/facilities.xml";
		double waitingTimeIfNotReported = 30*60; // <--- Adjust
		double additionalTimeOffset = 10*60; // <--- Adjust
		
		FacilitiesWriter facilitiesWriter = new FacilitiesWriter(createActivityFacilitiesWithWaitingTime(
				waitingTimesFile, waitingTimeIfNotReported, additionalTimeOffset));
		facilitiesWriter.write(facilitiesFile);
	}
	
	public static ActivityFacilities createActivityFacilitiesWithWaitingTime(String waitingTimesFile, double waitingTimeIfNotReported, double additionalTimeOffset) {
		ActivityFacilities activityFacilities = FacilitiesUtils.createActivityFacilities() ;
		int columnIndex = 15;
		
		LOG.info("Read column 8");
		BufferedReader reader = null;
		try {
			reader = IOUtils.getBufferedReader(waitingTimesFile);
			String header = reader.readLine(); // header
			String[] headerArray = header.split(";");
			if (!headerArray[columnIndex].equals("8")) {
				throw new RuntimeException("wrong column index");
			}
			String line = reader.readLine(); // first line
			int facId = 1;
			
			while (line != null) {
				String[] s = line.split(";");
				
				// create activityFacility
				double coordX = Double.valueOf(s[0]);
				double coordY = Double.valueOf(s[1]);
				ActivityFacility actFac = activityFacilities.getFactory().createActivityFacility(Id.create(facId, ActivityFacility.class), 
						new Coord(coordX, coordY));
				
				double waitingTime;
				if (s.length <= columnIndex ) {
					waitingTime = waitingTimeIfNotReported;
				} else {
					waitingTime = Double.valueOf(s[columnIndex]);
				}
				
				if (waitingTime == 0) {
					waitingTime = waitingTimeIfNotReported;
				}
				
				actFac.getAttributes().putAttribute("waitingTime", waitingTime);
//				actFac.getAttributes().putAttribute("waitingTime", waitingTime + additionalTimeOffset);
//				actFac.getAttributes().putAttribute("waitingTime", additionalTimeOffset);
				
				activityFacilities.addActivityFacility(actFac);
				
				line = reader.readLine();
				facId++;
			}
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return activityFacilities;
	}
}  