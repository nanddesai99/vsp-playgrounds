package playground.sergioo.workplaceCapacities;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.EigenDecomposition;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.stat.clustering.Cluster;
import org.apache.commons.math.stat.clustering.KMeansPlusPlusClusterer;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.facilities.ActivityFacilities;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.facilities.ActivityFacilityImpl;
import org.matsim.core.facilities.ActivityOption;
import org.matsim.core.facilities.ActivityOptionImpl;
import org.matsim.core.facilities.FacilitiesWriter;
import org.matsim.core.facilities.OpeningTime;
import org.matsim.core.facilities.OpeningTimeImpl;
import org.matsim.core.facilities.OpeningTime.DayType;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.router.AStarLandmarks;
import org.matsim.core.router.util.PreProcessLandmarks;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.vehicles.Vehicle;

import others.sergioo.util.algebra.Matrix1DImpl;
import others.sergioo.util.algebra.Matrix2DImpl;
import others.sergioo.util.algebra.Matrix3DImpl;
import others.sergioo.util.algebra.MatrixND;
import others.sergioo.util.algebra.PointND;
import others.sergioo.util.dataBase.DataBaseAdmin;
import others.sergioo.util.dataBase.NoConnectionException;
import playground.sergioo.Visualizer2D.NetworkVisualizer.SimpleNetworkWindow;
import playground.sergioo.Visualizer2D.NetworkVisualizer.NetworkPainters.NetworkPainter;
import playground.sergioo.workplaceCapacities.gui.ClustersWindow;
import playground.sergioo.workplaceCapacities.gui.WeigthsNetworkWindow;
import playground.sergioo.workplaceCapacities.gui.WorkersAreaPainter;
import playground.sergioo.workplaceCapacities.hits.PersonSchedule;
import playground.sergioo.workplaceCapacities.hits.PointPerson;
import playground.sergioo.workplaceCapacities.hits.Trip;

public class MainWorkplaceCapacities {

	//Constants
	private static final String NETWORK_FILE = "./data/currentSimulation/singapore2.xml";
	private static final String LINKS_MAP_FILE = "./data/facilities/auxiliar/links.map";
	private static final String NEAREST_LINKS_MAP_FILE = "./data/facilities/auxiliar/nearestLinks.map";
	private static final String QUANTITIES_MAP_FILE = "./data/facilities/auxiliar/quantities.map";
	private static final String WEIGHTS_MAP_FILE = "./data/facilities/auxiliar/weightsMap.map";
	private static final String WEIGHTS2_MAP_FILE = "./data/facilities/auxiliar/weights.map";
	private static final String CLUSTERS_FILE = "./data/facilities/auxiliar/clusters.map";
	private static final String AREAS_MAP_FILE = "./data/facilities/auxiliar/areas.map";
	private static final String CAPACITIES_FILE = "./data/facilities/auxiliar/capacities.map";
	private static final String SOLUTION_FILE = "./data/facilities/solution.dat";
	private static final String WORK_FACILITIES_FILEO = "./data/facilities/workFacilitiesO.xml";
	private static final String WORK_FACILITIES_FILE = "./data/facilities/workFacilities.xml";
	private static final double WALKING_SPEED = 4/3.6;
	private static final double PRIVATE_BUS_SPEED = 16/3.6;
	private static final double MAX_TRAVEL_TIME = 15*60;
	private static final String SEPARATOR = ";;;";
	private static final int NUM_NEAR = 3;
	private static final String TRAVEL_TIMES_FILE = "./data/facilities/travelTimes.dat";
	private static final String INPUT_FILE = "./data/facilities/input.dat";
	private static final String OUTPUT_FILE = "./data/facilities/output.dat";
	
	//Static attributes
	private static int SIZE = 10;
	private static int NUM_ITERATIONS = 100;
	private static List<Cluster<PointPerson>> clusters;
	private static SortedMap<Id, MPAreaData> dataMPAreas = new TreeMap<Id, MPAreaData>();
	private static SortedMap<String, Coord> stopsBase = new TreeMap<String, Coord>();
	private static Network network;
	private static List<List<Double>> travelTimes;
	private static List<Double> maximumAreaCapacities;
	private static List<List<Double>> stopScheduleCapacities;
	//private static Coord downLeft = new CoordImpl(103.83355, 1.2814);
	//private static Coord upRight = new CoordImpl(103.8513, 1.2985);
	private static Coord downLeft = new CoordImpl(-Double.MAX_VALUE, -Double.MAX_VALUE);
	private static Coord upRight = new CoordImpl(Double.MAX_VALUE, Double.MAX_VALUE);
	private static HashMap<String, Double> workerAreas;
	
	
	//Attributes
	
	//Main
	/**
	 * @param args
	 * @throws NoConnectionException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 * @throws BadStopException 
	 */
	public static void main(String[] args) throws BadStopException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, NoConnectionException {
		if(args.length==2) {
			SIZE = Integer.parseInt(args[0]);
			NUM_ITERATIONS = Integer.parseInt(args[1]);
		}
		loadData();
		//calculateOptimizationParameters();
		boolean exception = true;
		while(exception) {
			System.out.println("Run the solver and press Enter when the file is copied in the folder");
			new BufferedReader(new InputStreamReader(System.in)).readLine();
			try {
				fitCapacities2();
			}
			catch (Exception e) {
				continue;
			}
			exception = false;
		}
		ActivityFacilitiesImpl facilities = capacitiesToBuildings();
		new FacilitiesWriter(facilities).write(WORK_FACILITIES_FILEO);
	}

	private static void loadData() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, NoConnectionException {
		DataBaseAdmin dataBaseAux  = new DataBaseAdmin(new File("./data/facilities/DataBaseAuxiliar.properties"));
		ResultSet stopsResult = dataBaseAux.executeQuery("SELECT * FROM stops");
		while(stopsResult.next())
			stopsBase.put(stopsResult.getString(1), new CoordImpl(stopsResult.getDouble(3), stopsResult.getDouble(2)));
		stopsResult.close();
		System.out.println("Stops done!");
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CLUSTERS_FILE));
			clusters = (List<Cluster<PointPerson>>) ois.readObject();
			ois.close();
		} catch(EOFException e) {
			clusters = clusterWorkActivities(getWorkActivityTimes());
		}
		new ClustersWindow("Work times cluster PCA back: "+getClustersDeviations(clusters)+" "+getWeightedClustersDeviations(clusters), clusters).setVisible(true);
		System.out.println("Clustering done!");
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		scenario.getConfig().scenario().setUseTransit(true);
		new MatsimNetworkReader(scenario).readFile(NETWORK_FILE);
		network = scenario.getNetwork();
		setMPAreas();
		workerAreas = new HashMap<String, Double>();
		ResultSet typesResult = dataBaseAux.executeQuery("SELECT * FROM building_types");
		while(typesResult.next())
			workerAreas.put(typesResult.getString(1), typesResult.getDouble(2));
		typesResult.close();
		System.out.println("Types done!");
		dataBaseAux.close();
	}

	//Methods
	private static void calculateOptimizationParameters() throws BadStopException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, NoConnectionException {
		System.out.println("Process starts with "+SIZE+" clusters and "+NUM_ITERATIONS+" iterations.");
		/*CoordinateTransformation coordinateTransformation = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.WGS84_UTM48N);
		downLeft=coordinateTransformation.transform(downLeft);
		upRight=coordinateTransformation.transform(upRight);*/
		Map<Id, Double> stopCapacities = new HashMap<Id, Double>();
		Map<String, Double> quantitiesMap;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(QUANTITIES_MAP_FILE));
			quantitiesMap = (Map<String, Double>) ois.readObject();
			ois.close();
		} catch (EOFException e) {
			quantitiesMap = calculateStopClustersQuantities(stopCapacities);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(QUANTITIES_MAP_FILE));
			oos.writeObject(quantitiesMap);
			oos.close();
		}
		MatrixND<Double> quantities = new Matrix2DImpl(new int[]{clusters.size(),stopsBase.size()});
		stopScheduleCapacities = new ArrayList<List<Double>>();
		Iterator<String> stopIdsI = stopsBase.keySet().iterator();
		Iterator<Coord> stopsI = stopsBase.values().iterator();
		for(int s=0; s<quantities.getDimension(1); s++) {
			String stopId = stopIdsI.next();
			Coord stopCoord = stopsI.next();
			boolean inStop = stopCoord.getX()>downLeft.getX() && stopCoord.getX()<upRight.getX() && stopCoord.getY()>downLeft.getY() && stopCoord.getY()<upRight.getY();
			if(inStop)
				stopScheduleCapacities.add(new ArrayList<Double>());
			for(int c=0; c<quantities.getDimension(0); c++) {
				Double quantity = quantitiesMap.get(stopId+SEPARATOR+c);
				if(quantity==null)
					quantity = 0.0;
				quantities.setElement(new int[]{c,s}, quantity);
				if(inStop)
					stopScheduleCapacities.get(stopScheduleCapacities.size()-1).add(quantity);
			}
		}
		System.out.println("Quantities done!");
		Map<Tuple<Id, Id>,Tuple<Boolean,Double>> weightsMap;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(WEIGHTS_MAP_FILE));
			weightsMap = (Map<Tuple<Id, Id>,Tuple<Boolean,Double>>) ois.readObject();
			ois.close();
			try {
				ois = new ObjectInputStream(new FileInputStream(TRAVEL_TIMES_FILE));
				travelTimes = (List<List<Double>>) ois.readObject();
				ois.close();
			} catch(EOFException e) {
				e.printStackTrace();
			}
		} catch(EOFException e){
			weightsMap= calculateAreaStopWeights(stopsBase, stopCapacities, workerAreas, network);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(WEIGHTS_MAP_FILE));
			oos.writeObject(weightsMap);
			oos.close();
			oos = new ObjectOutputStream(new FileOutputStream(TRAVEL_TIMES_FILE));
			oos.writeObject(travelTimes);
			oos.close();
		}
		new WeigthsNetworkWindow("Weights", new NetworkPainter(network), weightsMap, dataMPAreas, stopsBase).setVisible(true);
		System.out.println("Travel times done!");
		Matrix2DImpl weights = new Matrix2DImpl(new int[]{dataMPAreas.size(),stopsBase.size()});
		Iterator<Id> mPAreaI = dataMPAreas.keySet().iterator();
		for(int f=0; f<weights.getDimension(0); f++) {
			Id mPAreaId = mPAreaI.next();
			stopIdsI = stopsBase.keySet().iterator();
			for(int s=0; s<weights.getDimension(1); s++) {
				Id sId = new IdImpl(stopIdsI.next());
				Tuple<Boolean, Double> weight = weightsMap.get(new Tuple<Id, Id>(sId, mPAreaId));
				if(weight==null)
					weights.setElement(f, s, 0.0);
				else
					weights.setElement(f, s, weight.getSecond());
			}
		}
		System.out.println("Weights done!");
		Matrix2DImpl proportions = new Matrix2DImpl(new int[]{dataMPAreas.size(),clusters.size()});
		Map<String, List<Double>> proportionsMap = calculateTypeBuildingOptionWeights(clusters);
		mPAreaI = dataMPAreas.keySet().iterator();
		for(int f=0; f<proportions.getDimension(0); f++) {
			Id mPAreaId = mPAreaI.next();
			for(int c=0; c<proportions.getDimension(1); c++)
				proportions.setElement(f, c, proportionsMap.get(dataMPAreas.get(mPAreaId).getType()).get(c));
		}
		System.out.println("Proportions done!");
		MatrixND<Double> maxs = new Matrix1DImpl(new int[]{dataMPAreas.size()}, 60.0);
		mPAreaI = dataMPAreas.keySet().iterator();
		maximumAreaCapacities = new ArrayList<Double>();
		for(int f=0; f<maxs.getDimension(0); f++) {
			Id mPId = mPAreaI.next();
			MPAreaData dataMPArea = dataMPAreas.get(mPId);
			double max = (dataMPArea.getMaxArea()/workerAreas.get(dataMPArea.getType()))*dataMPArea.getModeShare()/(1+dataMPArea.getModeShare());
			maxs.setElement(new int[]{f}, max);
			Coord areaCoord = dataMPArea.getCoord();
			if(areaCoord.getX()>downLeft.getX() && areaCoord.getX()<upRight.getX() && areaCoord.getY()>downLeft.getY() && areaCoord.getY()<upRight.getY())
				maximumAreaCapacities.add(max);
		}
		System.out.println("Max areas done!");
		writeOptimizationParameters();
	}
	private static Map<String, PointPerson> getWorkActivityTimes() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, NoConnectionException {
		DataBaseAdmin dataBaseHits  = new DataBaseAdmin(new File("./data/hits/DataBase.properties"));
		Map<String, PersonSchedule> times;
		ResultSet timesResult = dataBaseHits.executeQuery("SELECT pax_idx,trip_id,t6_purpose,t3_starttime,t4_endtime,p6_occup,t5_placetype FROM hits.hitsshort");
		times = new HashMap<String, PersonSchedule>();
		while(timesResult.next()) {
			PersonSchedule timesPerson = times.get(timesResult.getString(1));
			if(timesPerson==null) {
				timesPerson = new PersonSchedule(timesResult.getString(1), timesResult.getString(6));
				times.put(timesResult.getString(1), timesPerson);
			}
			if(timesResult.getInt(2)!=0) {
				Iterator<Entry<Integer, Trip>> timesPersonI = timesPerson.getTrips().entrySet().iterator();
				Entry<Integer, Trip> last = null;
				while(timesPersonI.hasNext())
					last = timesPersonI.next();
				if(last==null || last.getKey()!=timesResult.getInt(2)) {
					int startTime = (timesResult.getInt(4)%100)*60+(timesResult.getInt(4)/100)*3600;
					int endTime = (timesResult.getInt(5)%100)*60+(timesResult.getInt(5)/100)*3600;
					if(last!=null && last.getKey()<timesResult.getInt(2) && last.getValue().getEndTime()>startTime) {
						startTime += 12*3600;
						endTime += 12*3600;
					}
					if(last!=null && last.getKey()<timesResult.getInt(2) && last.getValue().getEndTime()>startTime) {
						startTime += 12*3600;
						endTime += 12*3600;
					}
					timesPerson.getTrips().put(timesResult.getInt(2), new Trip(timesResult.getString(3), startTime, endTime, timesResult.getString(7)));
				}
			}
		}
		timesResult.close();
		Map<String, PointPerson> points = new HashMap<String, PointPerson>();
		for(PersonSchedule timesPerson:times.values()) {
			SortedMap<Integer, Trip> tripsPerson = timesPerson.getTrips();
			boolean startTimeSaved=false;
			double startTime=-1, endTime=-1;
			String placeType=null;
			if(tripsPerson.size()>0) {
				for(int i = tripsPerson.keySet().iterator().next(); i<=tripsPerson.size(); i ++) {
					if(!startTimeSaved && tripsPerson.get(i).getPurpose()!=null && tripsPerson.get(i).getPurpose().equals("work")) {
						startTime = tripsPerson.get(i).getEndTime();
						startTimeSaved = true;
					}
					if(i>tripsPerson.keySet().iterator().next() && tripsPerson.get(i-1).getPurpose().equals("work")) {
						endTime = tripsPerson.get(i).getStartTime();
						placeType = tripsPerson.get(i-1).getPlaceType();
					}
				}
			}
			if(startTime!=-1 && endTime!=-1 && endTime-startTime>=7*3600 && endTime-startTime<=16*3600)
				if(startTime>24*3600)
					points.put(timesPerson.getId(), new PointPerson(timesPerson.getId(), timesPerson.getOccupation(), new Double[]{startTime-24*3600, endTime-24*3600-(startTime-24*3600)}, placeType));
				else
					points.put(timesPerson.getId(), new PointPerson(timesPerson.getId(), timesPerson.getOccupation(), new Double[]{startTime, endTime-startTime}, placeType));
		}
		Map<String, Double> weights;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(WEIGHTS2_MAP_FILE));
			weights = (Map<String, Double>) ois.readObject();
			ois.close();
		} catch (EOFException e) {
			weights = new HashMap<String, Double>();
			ResultSet weightsR = dataBaseHits.executeQuery("SELECT pax_idx,hipf10  FROM hits.hitsshort_geo_hipf");
			while(weightsR.next())
				weights.put(weightsR.getString(1), weightsR.getDouble(2));
			for(PointPerson pointPerson:points.values()) {
				if(weights.get(pointPerson.getId())!=null)
					pointPerson.setWeight(weights.get(pointPerson.getId()));
				else
					pointPerson.setWeight(100);
			}
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(WEIGHTS2_MAP_FILE));
			oos.writeObject(weights);
			oos.close();
		}
		dataBaseHits.close();
		return points;
	}
	private static List<Cluster<PointPerson>> clusterWorkActivities(Map<String,PointPerson> points) throws FileNotFoundException, IOException, ClassNotFoundException {
		List<Cluster<PointPerson>> clusters = null;
		Set<PointPerson> pointsC = getPCATransformation(points.values());
		Random r = new Random();
		clusters = new KMeansPlusPlusClusterer<PointPerson>(r).cluster(pointsC, SIZE, 1000);
		new ClustersWindow("Work times cluster PCA: "+getClustersDeviations(clusters)+" "+getWeightedClustersDeviations(clusters), clusters).setVisible(true);
		for(Cluster<PointPerson> cluster:clusters)
			for(PointPerson pointPersonT:cluster.getPoints()) {
				PointPerson pointPerson = points.get(pointPersonT.getId());
				for(int d=0; d<pointPersonT.getDimension(); d++)
					pointPersonT.setElement(d, pointPerson.getElement(d));
			}
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CLUSTERS_FILE));
		oos.writeObject(clusters);
		oos.close();
		return clusters;
	}
	private static double getClustersDeviations(List<Cluster<PointPerson>> clusters) {
		double deviation = 0;
		for(Cluster<PointPerson> cluster:clusters)
			for(PointPerson pointPerson:cluster.getPoints())
				deviation += cluster.getCenter().distanceFrom(pointPerson);
		return deviation;
	}
	private static double getWeightedClustersDeviations(List<Cluster<PointPerson>> clusters) {
		double deviation = 0, totalWeight=0;
		for(Cluster<PointPerson> cluster:clusters) {
			for(PointPerson pointPerson:cluster.getPoints()) {
				deviation += cluster.getCenter().distanceFrom(pointPerson)*pointPerson.getWeight();
				totalWeight = pointPerson.getWeight();
			}
		}
		return deviation/totalWeight;
	}
	private static Set<PointPerson> getPCATransformation(Collection<PointPerson> points) {
		RealMatrix pointsM = new Array2DRowRealMatrix(points.iterator().next().getDimension(), points.size());
		int k=0;
		for(PointND<Double> point:points) {
			for(int f=0; f<point.getDimension(); f++)
				pointsM.setEntry(f, k, point.getElement(f));
			k++;
		}
		RealMatrix means = new Array2DRowRealMatrix(pointsM.getRowDimension(), 1);
		for(int r=0; r<means.getRowDimension(); r++) {
			double mean = 0;
			for(int c=0; c<pointsM.getColumnDimension(); c++)
				mean+=pointsM.getEntry(r,c)/pointsM.getColumnDimension();
			means.setEntry(r, 0, mean);
		}
		RealMatrix deviations = new Array2DRowRealMatrix(pointsM.getRowDimension(), pointsM.getColumnDimension());
		for(int r=0; r<deviations.getRowDimension(); r++)
			for(int c=0; c<deviations.getColumnDimension(); c++)
				deviations.setEntry(r, c, pointsM.getEntry(r, c)-means.getEntry(r, 0));
		RealMatrix covariance = deviations.multiply(deviations.transpose()).scalarMultiply(1/(double)pointsM.getColumnDimension());
		EigenDecomposition eigenDecomposition = new EigenDecompositionImpl(covariance, 0);
		RealMatrix eigenVectorsT = eigenDecomposition.getVT();
		RealVector eigenValues = new ArrayRealVector(eigenDecomposition.getD().getRowDimension());
		for(int r=0; r<eigenDecomposition.getD().getRowDimension(); r++)
			eigenValues.setEntry(r, eigenDecomposition.getD().getEntry(r, r));
		for(int i=0; i<eigenValues.getDimension(); i++) {
			for(int j=i+1; j<eigenValues.getDimension(); j++)
				if(eigenValues.getEntry(i)<eigenValues.getEntry(j)) {
					double tempValue = eigenValues.getEntry(i);
					eigenValues.setEntry(i, eigenValues.getEntry(j));
					eigenValues.setEntry(j, tempValue);
					RealVector tempVector = eigenVectorsT.getRowVector(i);
					eigenVectorsT.setRowVector(i, eigenVectorsT.getRowVector(j));
					eigenVectorsT.setRowVector(j, tempVector);
				}
			eigenVectorsT.setRowVector(i,eigenVectorsT.getRowVector(i).mapMultiply(Math.sqrt(1/eigenValues.getEntry(i))));
		}
		RealVector standardDeviations = new ArrayRealVector(pointsM.getRowDimension());
		for(int r=0; r<covariance.getRowDimension(); r++)
			standardDeviations.setEntry(r, Math.sqrt(covariance.getEntry(r, r)));
		double zValue = standardDeviations.dotProduct(new ArrayRealVector(pointsM.getRowDimension(), 1));
		RealMatrix zScore = deviations.scalarMultiply(1/zValue);
		pointsM = eigenVectorsT.multiply(zScore);
		Set<PointPerson> pointsC = new HashSet<PointPerson>();
		k=0;
		for(PointPerson point:points) {
			PointPerson pointC = new PointPerson(point.getId(), point.getOccupation(), new Double[]{pointsM.getEntry(0, k), pointsM.getEntry(1, k)}, point.getPlaceType());
			pointC.setWeight(point.getWeight());
			pointsC.add(pointC);
			k++;
		}
		return pointsC;
	}
	private static Map<String, Double> calculateStopClustersQuantities(Map<Id, Double> stopCapacities) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, NoConnectionException {
		List<PointPerson> centers = new ArrayList<PointPerson>();
		for(int c=0; c<clusters.size(); c++)
			centers.add(clusters.get(c).getPoints().get(0).centroidOf(clusters.get(c).getPoints()));
		DataBaseAdmin dataBaseAux  = new DataBaseAdmin(new File("./data/facilities/DataBaseAuxiliar.properties"));
		Map<String, Double> quantities = new HashMap<String, Double>();
		Map<String, Integer> users = new HashMap<String, Integer>();
		ResultSet tripsResult = dataBaseAux.executeQuery("SELECT * FROM DCM_work_activities");
		int ts=0;
		while(tripsResult.next()) {
			Id stopId = new IdImpl(tripsResult.getString(5));
			Double quantity = stopCapacities.get(stopId);
			if(quantity==null)
				quantity = 0.0;
			stopCapacities.put(stopId, quantity+1);
			Integer num = users.get(tripsResult.getString(2));
			if(num==null)
				num = 0;
			users.put(tripsResult.getString(2), ++num);
			int nearestCluster = 0;
			PointPerson time = new PointPerson(tripsResult.getString(2)+"_"+num, "", new Double[]{(double) tripsResult.getInt(8), (double) (tripsResult.getInt(12)-tripsResult.getInt(8))}, "");
			for(int c=0; c<clusters.size(); c++)
				if(centers.get(c).distanceFrom(time)<centers.get(nearestCluster).distanceFrom(time))
					nearestCluster = c;
			String key = tripsResult.getString(5)+SEPARATOR+nearestCluster;
			quantity = quantities.get(key);
			if(quantity==null)
				quantity = 0.0;
			quantities.put(key, quantity+1);
			ts++;
		}
		System.out.println(ts);
		tripsResult.close();
		dataBaseAux.close();
		return quantities;
	}
	private static void setMPAreas() throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, NoConnectionException {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(AREAS_MAP_FILE));
			dataMPAreas = (SortedMap<Id, MPAreaData>)ois.readObject();
			ois.close();
		} catch(EOFException e) {
			DataBaseAdmin dataBaseAuxiliar  = new DataBaseAdmin(new File("./data/facilities/DataBaseAuxiliar.properties"));
			CoordinateTransformation coordinateTransformation = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84_SVY21, TransformationFactory.WGS84_UTM48N);
			ResultSet mPAreasR = dataBaseAuxiliar.executeQuery("SELECT * FROM masterplan_areas WHERE use_for_generation = 1");
			while(mPAreasR.next()) {
				ResultSet mPAreasR2 = dataBaseAuxiliar.executeQuery("SELECT ZoneID,`Pu/Pr` FROM DCM_mplan_zones_modeshares WHERE objectID="+mPAreasR.getInt(1));
				mPAreasR2.next();
				dataMPAreas.put(new IdImpl(mPAreasR.getString(1)), new MPAreaData(new IdImpl(mPAreasR.getString(1)), coordinateTransformation.transform(new CoordImpl(mPAreasR.getDouble(6), mPAreasR.getDouble(7))), mPAreasR.getString(2), mPAreasR.getDouble(5), new IdImpl(mPAreasR2.getInt(1)), mPAreasR2.getDouble(2)));
			}
			mPAreasR.close();
			dataBaseAuxiliar.close();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(AREAS_MAP_FILE));
			oos.writeObject(dataMPAreas);
			oos.close();
		}
		System.out.println("Areas done!");
		//Find nearest good links
		Map<String, String> nearestLinks;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(NEAREST_LINKS_MAP_FILE));
			nearestLinks = (Map<String, String>) ois.readObject();
			ois.close();
		} catch(EOFException e) {
			nearestLinks = new HashMap<String, String>();
			for(MPAreaData mPArea:dataMPAreas.values()) {
				Link nearestLink =null;
				double nearestDistance = Double.MAX_VALUE;
				for(Link link:network.getLinks().values())
					if(link.getAllowedModes().contains("car")) {
						double distance = CoordUtils.distancePointLinesegment(link.getFromNode().getCoord(), link.getToNode().getCoord(), mPArea.getCoord());
						if(distance<nearestDistance) {
							nearestDistance = distance;
							nearestLink = link;
						}
					}
				nearestLinks.put(mPArea.getId().toString(), nearestLink.getId().toString());
			}
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NEAREST_LINKS_MAP_FILE));
			oos.writeObject(nearestLinks);
			oos.close();
		}
		for(MPAreaData mPArea:dataMPAreas.values())
			mPArea.setLinkId(new IdImpl(nearestLinks.get(mPArea.getId().toString())));
	}
	private static Map<Tuple<Id, Id>, Tuple<Boolean, Double>> calculateAreaStopWeights(SortedMap<String, Coord> stopsBase, Map<Id, Double> stopsCapacities, Map<String, Double> workerAreas, Network network) throws BadStopException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, NoConnectionException {
		List<Map<String, Id>> linksStops;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(LINKS_MAP_FILE));
			linksStops = (List<Map<String, Id>>) ois.readObject();
			ois.close();
		} catch (EOFException e) {
			linksStops = new ArrayList<Map<String,Id>>();
			for(int n=0; n<NUM_NEAR; n++) {
				linksStops.add(new HashMap<String, Id>());
				for(Entry<String, Coord> stopBase: stopsBase.entrySet()) {
					Id nearest = network.getLinks().values().iterator().next().getId();
					double nearestDistance = CoordUtils.calcDistance(network.getLinks().get(nearest).getCoord(), stopBase.getValue());
					for(Link link:network.getLinks().values())
						if(link.getAllowedModes().contains("car")) {
							boolean selected = false;
							for(int p=0; p<n; p++)
								if(linksStops.get(p).get(stopBase.getKey()).equals(link.getId()))
									selected=true;
							if(!selected && CoordUtils.calcDistance(link.getToNode().getCoord(), stopBase.getValue())<nearestDistance) {
								nearest = link.getId();
								nearestDistance = CoordUtils.calcDistance(network.getLinks().get(nearest).getCoord(), stopBase.getValue());
							}
						}
					linksStops.get(n).put(stopBase.getKey(), nearest);
				}
			}
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LINKS_MAP_FILE));
			oos.writeObject(linksStops);
			oos.close();
		}
		//Compute stops facilities weights
		Map<Tuple<Id, Id>,Tuple<Boolean,Double>> weights;
		TravelDisutility travelMinCost = new TravelDisutility() {
			@Override
			public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
				return getLinkMinimumTravelDisutility(link);
			}
			@Override
			public double getLinkMinimumTravelDisutility(Link link) {
				return link.getLength()/WALKING_SPEED;
			}
		};
		TravelTime timeFunction = new TravelTime() {	
			@Override
			public double getLinkTravelTime(Link link, double time) {
				return link.getLength()/WALKING_SPEED;
			}
		};
		PreProcessLandmarks preProcessData = new PreProcessLandmarks(travelMinCost);
		preProcessData.run(network);
		AStarLandmarks aStarLandmarks = new AStarLandmarks(network, preProcessData, timeFunction);
		weights = new HashMap<Tuple<Id,Id>, Tuple<Boolean,Double>>();
		Collection<Id> removeStops = new ArrayList<Id>(); 
		travelTimes = new ArrayList<List<Double>>();
		int s=0;
		for(Entry<String, Coord> stop: stopsBase.entrySet()) {
			String stopKey = stop.getKey();
			Coord stopCoord = stop.getValue();
			boolean inStop = stopCoord.getX()>downLeft.getX() && stopCoord.getX()<upRight.getX() && stopCoord.getY()>downLeft.getY() && stopCoord.getY()<upRight.getY();
			if(inStop)
				travelTimes.add(new ArrayList<Double>());
			double maxTimeFromStop = 0;
			Collection<Id> linkIds = new ArrayList<Id>();
			for(int n=0; n<NUM_NEAR; n++)
				if(CoordUtils.calcDistance(network.getLinks().get(linksStops.get(n).get(stopKey)).getToNode().getCoord(), stop.getValue())<MAX_TRAVEL_TIME*WALKING_SPEED/5)
					linkIds.add(linksStops.get(n).get(stopKey));
			if(linkIds.size()==0)
				System.out.println();
			Id stopId = new IdImpl(stopKey);
			double maxCapacityNearFacilities = 0;
			int w=0;
			for(MPAreaData mPArea:dataMPAreas.values()) {
				Coord areaCoord = mPArea.getCoord();
				boolean inArea = areaCoord.getX()>downLeft.getX() && areaCoord.getX()<upRight.getX() && areaCoord.getY()>downLeft.getY() && areaCoord.getY()<upRight.getY();
				if(inStop && inArea) {
					travelTimes.get(travelTimes.size()-1).add(36000.0);
					w++;
				}
				if(CoordUtils.calcDistance(stopsBase.get(stopKey), mPArea.getCoord())<MAX_TRAVEL_TIME*WALKING_SPEED) {
					double walkingTime = Double.MAX_VALUE;
					for(Id linkId:linkIds) {
						double walkingTimeA=aStarLandmarks.calcLeastCostPath(network.getLinks().get(linkId).getToNode(), network.getLinks().get(mPArea.getLinkId()).getFromNode(), 0, null, null).travelCost;
						if(walkingTimeA<walkingTime)
							walkingTime = walkingTimeA;
					}
					if(walkingTime<=MAX_TRAVEL_TIME) {
						weights.put(new Tuple<Id, Id>(stopId, mPArea.getId()), new Tuple<Boolean, Double>(true, walkingTime));
						if(inStop && inArea) {
							travelTimes.get(travelTimes.size()-1).set(w-1, walkingTime);
							mPArea.addTravelTime(stopId, walkingTime);
						}
						if(walkingTime>maxTimeFromStop)
							maxTimeFromStop = walkingTime;
						MPAreaData dataMPArea = dataMPAreas.get(mPArea.getId());
						maxCapacityNearFacilities += (dataMPArea.getMaxArea()/workerAreas.get(dataMPArea.getType()))*dataMPArea.getModeShare()/(1+dataMPArea.getModeShare());
					}
				}
			}
			if(stopsCapacities.get(stopId)>maxCapacityNearFacilities) {
				double maxCapacityNear2Facilities = maxCapacityNearFacilities;
				w=0;
				for(MPAreaData mPArea:dataMPAreas.values()) {
					Coord areaCoord = mPArea.getCoord();
					boolean inArea = areaCoord.getX()>downLeft.getX() && areaCoord.getX()<upRight.getX() && areaCoord.getY()>downLeft.getY() && areaCoord.getY()<upRight.getY();
					if(inStop && inArea)
						w++;
					if(CoordUtils.calcDistance(stopsBase.get(stopKey), mPArea.getCoord())<(MAX_TRAVEL_TIME*2/3)*PRIVATE_BUS_SPEED) {
						double walkingTime = Double.MAX_VALUE;
						for(Id linkId:linkIds) {
							double walkingTimeA=aStarLandmarks.calcLeastCostPath(network.getLinks().get(linkId).getToNode(), network.getLinks().get(mPArea.getLinkId()).getFromNode(), 0, null, null).travelCost;
							if(walkingTimeA<walkingTime)
								walkingTime = walkingTimeA;
						}
						double privateBusTime = Double.MAX_VALUE;
						for(Id linkId:linkIds) {
							double privateBusTimeA=aStarLandmarks.calcLeastCostPath(network.getLinks().get(linkId).getToNode(), network.getLinks().get(mPArea.getLinkId()).getFromNode(), 0, null, null).travelCost*WALKING_SPEED/PRIVATE_BUS_SPEED;
							if(privateBusTimeA<privateBusTime)
								privateBusTime = privateBusTimeA;
						}
						if(walkingTime>MAX_TRAVEL_TIME && privateBusTime<=(MAX_TRAVEL_TIME*2/3)) {
							weights.put(new Tuple<Id, Id>(stopId, mPArea.getId()), new Tuple<Boolean, Double>(false, privateBusTime));
							if(inStop && inArea) {
								travelTimes.get(travelTimes.size()-1).set(w-1, privateBusTime);
								mPArea.addTravelTime(stopId, privateBusTime);
							}
							if(privateBusTime>maxTimeFromStop)
								maxTimeFromStop = privateBusTime;
							MPAreaData dataMPArea = dataMPAreas.get(mPArea.getId());
							maxCapacityNear2Facilities += (dataMPArea.getMaxArea()/workerAreas.get(dataMPArea.getType()))*dataMPArea.getModeShare()/(1+dataMPArea.getModeShare());
						}
					}
				}
				if(stopsCapacities.get(stopId)>maxCapacityNear2Facilities) {
					System.out.println("Far" + stopId);
					double maxCapacityNear3Facilities = maxCapacityNear2Facilities;
					w=0;
					for(MPAreaData mPArea:dataMPAreas.values()) {
						Coord areaCoord = mPArea.getCoord();
						boolean inArea = areaCoord.getX()>downLeft.getX() && areaCoord.getX()<upRight.getX() && areaCoord.getY()>downLeft.getY() && areaCoord.getY()<upRight.getY();
						if(inStop && inArea)
							w++;
						if(CoordUtils.calcDistance(stopsBase.get(stopKey), mPArea.getCoord())<MAX_TRAVEL_TIME*PRIVATE_BUS_SPEED) {
							double privateBusTime = Double.MAX_VALUE;
							for(Id linkId:linkIds) {
								double privateBusTimeA=aStarLandmarks.calcLeastCostPath(network.getLinks().get(linkId).getToNode(), network.getLinks().get(mPArea.getLinkId()).getFromNode(), 0, null, null).travelCost*WALKING_SPEED/PRIVATE_BUS_SPEED;
								if(privateBusTimeA<privateBusTime)
									privateBusTime = privateBusTimeA;
							}
							if(privateBusTime>(MAX_TRAVEL_TIME*2/3) && privateBusTime<=MAX_TRAVEL_TIME) {
								weights.put(new Tuple<Id, Id>(stopId, mPArea.getId()), new Tuple<Boolean, Double>(false, privateBusTime));
								if(inStop && inArea) {
									travelTimes.get(travelTimes.size()-1).set(w-1, privateBusTime);
									mPArea.addTravelTime(stopId, privateBusTime);
								}
								if(privateBusTime>maxTimeFromStop)
									maxTimeFromStop = privateBusTime;
								MPAreaData dataMPArea = dataMPAreas.get(mPArea.getId());
								maxCapacityNear3Facilities += (dataMPArea.getMaxArea()/workerAreas.get(dataMPArea.getType()))*dataMPArea.getModeShare()/(1+dataMPArea.getModeShare());
							}
						}
					}
					if(stopsCapacities.get(stopId)>maxCapacityNear3Facilities) {
						System.out.println("Very far" + stopId);
						removeStops.add(stopId);
					}
				}
			}
			double totalTimeFromStop = 0;
			maxTimeFromStop++;
			for(Entry<Tuple<Id, Id>,Tuple<Boolean, Double>> weight:weights.entrySet())
				if(weight.getKey().getFirst().equals(stopId)) {
					double correctWeight = maxTimeFromStop-weight.getValue().getSecond();
					weights.put(weight.getKey(), new Tuple<Boolean, Double>(weight.getValue().getFirst(), correctWeight));
					totalTimeFromStop += correctWeight;
				}
			if(totalTimeFromStop!=0)
				for(Entry<Tuple<Id, Id>,Tuple<Boolean, Double>> weight:weights.entrySet())
					if(weight.getKey().getFirst().equals(stopId))
						weights.put(weight.getKey(), new Tuple<Boolean, Double>(weight.getValue().getFirst(),weight.getValue().getSecond()/totalTimeFromStop));
			System.out.println(s+++" of "+stopsBase.size());
		}
		int num=0;
		for(Id stopId:removeStops) {
			stopsBase.remove(stopId.toString());
			num+=stopsCapacities.get(stopId);
			stopsCapacities.remove(stopId);
		}
		System.out.println(num+" workers lost.");
		return weights;
	}
	private static Map<String, List<Double>> calculateTypeBuildingOptionWeights(List<Cluster<PointPerson>> clusters) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, NoConnectionException {
		DataBaseAdmin dataBaseAux  = new DataBaseAdmin(new File("./data/facilities/DataBaseAuxiliar.properties"));
		Map<String, List<String>> mPTypesAll = new HashMap<String, List<String>>();
		Map<String, List<Double>> proportions = new HashMap<String, List<Double>>();
		Map<String, Double> proportionsT = new HashMap<String, Double>();
		ResultSet mPTypesResult = dataBaseAux.executeQuery("SELECT DISTINCT mp_type FROM mp_types_X_hits_types");
		while(mPTypesResult.next()) {
			List<Double> list=new ArrayList<Double>();
			for(int i=0; i<clusters.size(); i++)
				list.add(0.0);
			proportions.put(mPTypesResult.getString(1), list);
			proportionsT.put(mPTypesResult.getString(1), 0.0);
		}
		for(int c=0; c<clusters.size(); c++)
			for(PointPerson person:clusters.get(c).getPoints()) {
				String type = person.getPlaceType();
				List<String> mPTypes = mPTypesAll.get(type);
				if(mPTypes==null) {
					mPTypes = getMPTypes(type, dataBaseAux);
					mPTypesAll.put(type, mPTypes);
				}
				for(String mPType:mPTypes) {
					List<Double> list = proportions.get(mPType);
					list.set(c, list.get(c)+1);
					proportionsT.put(mPType,proportionsT.get(mPType)+1);
				}
			}
		for(String key:proportions.keySet())
			for(int c=0; c<clusters.size(); c++) {
				Double proportion = proportions.get(key).get(c)/proportionsT.get(key);
				if(proportion.isNaN())
					proportion=0.0;
				proportions.get(key).set(c, proportion);
			}
		dataBaseAux.close();
		return proportions;
	}
	private static List<String> getMPTypes(String placeType, DataBaseAdmin dataBaseAux) throws SQLException, NoConnectionException {
		ResultSet mPTypesResult = dataBaseAux.executeQuery("SELECT mp_type FROM mp_types_X_hits_types WHERE hits_type='"+placeType+"'");
		List<String> mpTypes = new ArrayList<String>();
		while(mPTypesResult.next())
			mpTypes.add(mPTypesResult.getString(1));
		mPTypesResult.close();
		return mpTypes;
	}
	private static void writeOptimizationParameters() throws FileNotFoundException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INPUT_FILE));
		double[][] travelTimesM = new double[travelTimes.size()][travelTimes.get(0).size()];
		for(int s=0; s<travelTimesM.length; s++)
			for(int w=0; w<travelTimesM[s].length; w++)
				travelTimesM[s][w]=travelTimes.get(s).get(w);
		oos.writeObject(travelTimesM);
		double[] maximumAreaCapacitiesM = new double[maximumAreaCapacities.size()];
		for(int w=0; w<maximumAreaCapacitiesM.length; w++)
			maximumAreaCapacitiesM[w]=maximumAreaCapacities.get(w);
		oos.writeObject(maximumAreaCapacitiesM);
		double[][] stopScheduleCapacitiesM = new double[stopScheduleCapacities.size()][stopScheduleCapacities.get(0).size()];
		for(int s=0; s<stopScheduleCapacitiesM.length; s++)
			for(int c=0; c<stopScheduleCapacitiesM[s].length; c++)
				stopScheduleCapacitiesM[s][c]=stopScheduleCapacities.get(s).get(c);
		oos.writeObject(stopScheduleCapacitiesM);
		oos.close();
	}
	private static void writeOptimizationParameters2(int numRegions) throws FileNotFoundException, IOException {
		List<double[][]> travelTimes = new ArrayList<double[][]>();
		List<double[]> maximumAreaCapacities = new ArrayList<double[]>();
		List<double[][]> stopScheduleCapacities = new ArrayList<double[][]>();
		Set<StopCoord> pointsC = new HashSet<StopCoord>();
		for(Entry<String,Coord> stop:stopsBase.entrySet())
			pointsC.add(new StopCoord(stop.getValue().getX(), stop.getValue().getY(), new IdImpl(stop.getKey())));
		Random r = new Random();
		List<Cluster<StopCoord>> clusters = new KMeansPlusPlusClusterer<StopCoord>(r).cluster(pointsC, numRegions, 1000);
		for(int n=0; n<numRegions; n++) {
			double[][] tts = new double[clusters.get(n).getPoints().size()][1];
			for(StopCoord stop:clusters.get(n).getPoints()) {
				for(MPAreaData mPArea:dataMPAreas.values()) {
					Double tt = mPArea.getTravelTime(stop.getId());
					int s=0;
					int w=0;
					if(tt!=null)
						tts[s][w]=tt;
				}
			}
			travelTimes.add(tts);
			maximumAreaCapacities.add(new double[1]);
			stopScheduleCapacities.add(new double[clusters.get(n).getPoints().size()][SIZE]);
		}
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INPUT_FILE));
		oos.writeObject(travelTimes);
		oos.writeObject(maximumAreaCapacities);
		oos.writeObject(stopScheduleCapacities);
		oos.close();
	}
	private static void fitCapacities(FittingCapacities fittingCapacities) throws FileNotFoundException, IOException, ClassNotFoundException {
		MatrixND<Double> capacities = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CAPACITIES_FILE));
			capacities = (MatrixND<Double>) ois.readObject();
			ois.close();
		} catch (Exception e2) {
			Runtime.getRuntime().gc();
			capacities = fittingCapacities.run(NUM_ITERATIONS);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CAPACITIES_FILE));
			oos.writeObject(capacities);
			oos.close();
			System.out.println("Matrix written!");
		}
		Matrix3DImpl matrix = (Matrix3DImpl)capacities;
		ActivityFacilityImpl fac = new ActivityFacilitiesImpl().createFacility(new IdImpl("dummy"), new CoordImpl(0,0));
		for(int o=0; o<matrix.getDimension(1); o++) {
			double[] center = new double[]{0, 0};
			for(PointPerson pointPerson:clusters.get(o).getPoints())
				for(int i=0; i<2; i++)
					center[i] += pointPerson.getElement(i);
			for(int i=0; i<2; i++)
				center[i] /= clusters.get(o).getPoints().size();
			int minStart = ((int) Math.round(((center[0]%3600.0)/60)/15))*15;
			int minDuration = ((int) Math.round(((center[1]%3600.0)/60)/15))*15;
			NumberFormat numberFormat = new DecimalFormat("00");
			String optionText = "w_"+numberFormat.format(Math.floor(center[0]/3600))+numberFormat.format(minStart)+"_"+numberFormat.format(Math.floor(center[1]/3600))+numberFormat.format(minDuration);
			OpeningTime openingTime = new OpeningTimeImpl(DayType.wkday, Math.round(center[0]/900)*900, Math.round((center[0]+center[1])/900)*900);
			Iterator<MPAreaData> mPAreaI = dataMPAreas.values().iterator();
			for(int f=0; f<matrix.getDimension(0); f++) {
				MPAreaData mPArea = mPAreaI.next();
				double pTCapacityFO = 0;
				for(int s=0; s<matrix.getDimension(2); s++)
					pTCapacityFO += matrix.getElement(f, o, s);
				if(pTCapacityFO>0) {
					ActivityOption activityOption = new ActivityOptionImpl(optionText, fac);
					activityOption.setCapacity(pTCapacityFO/mPArea.getModeShare());
					activityOption.addOpeningTime(openingTime);
					mPArea.putActivityOption(activityOption);
				}
			}
		}
	}
	private static void fitCapacities2() throws FileNotFoundException, IOException, ClassNotFoundException {
		if(dataMPAreas.size()==0) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(AREAS_MAP_FILE));
				dataMPAreas = (SortedMap<Id, MPAreaData>)ois.readObject();
				ois.close();
			} catch(EOFException e) {
				
			}
			System.out.println("Areas done!");
		}
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(OUTPUT_FILE));
		double[][] matrixCapacities = (double[][]) ois.readObject();
		ois.close();
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		scenario.getConfig().scenario().setUseTransit(true);
		new MatsimNetworkReader(scenario).readFile(NETWORK_FILE);
		Network network = scenario.getNetwork();
		WorkersAreaPainter workersPainter = new WorkersAreaPainter(network);
		workersPainter.setData(matrixCapacities, dataMPAreas, stopsBase.values());
		new SimpleNetworkWindow("Capacities", workersPainter).setVisible(true);
		ActivityFacilityImpl fac = new ActivityFacilitiesImpl().createFacility(new IdImpl("dummy"), new CoordImpl(0,0));
		for(int c=0; c<matrixCapacities[0].length; c++) {
			double[] center = new double[]{0, 0};
			for(PointPerson pointPerson:clusters.get(c).getPoints())
				for(int i=0; i<2; i++)
					center[i] += pointPerson.getElement(i);
			for(int i=0; i<2; i++)
				center[i] /= clusters.get(c).getPoints().size();
			int minStart = ((int) Math.round(((center[0]%3600.0)/60)/15))*15;
			int minDuration = ((int) Math.round(((center[1]%3600.0)/60)/15))*15;
			NumberFormat numberFormat = new DecimalFormat("00");
			String optionText = "w_"+numberFormat.format(Math.floor(center[0]/3600))+numberFormat.format(minStart)+"_"+numberFormat.format(Math.floor(center[1]/3600))+numberFormat.format(minDuration);
			OpeningTime openingTime = new OpeningTimeImpl(DayType.wkday, Math.round(center[0]/900)*900, Math.round((center[0]+center[1])/900)*900);
			Iterator<MPAreaData> mPAreaI = dataMPAreas.values().iterator();
			for(int w=0; w<matrixCapacities[0].length; w++) {
				MPAreaData mPArea = mPAreaI.next();
				double pTCapacityFO = matrixCapacities[w][c];
				if(pTCapacityFO>0) {
					ActivityOption activityOption = new ActivityOptionImpl(optionText, fac);
					activityOption.setCapacity(pTCapacityFO+(pTCapacityFO/mPArea.getModeShare()));
					activityOption.addOpeningTime(openingTime);
					mPArea.putActivityOption(activityOption);
				}
			}
		}
	}
	private static ActivityFacilitiesImpl capacitiesToBuildings() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, NoConnectionException {
		ActivityFacilitiesImpl facilities;
		DataBaseAdmin dataBaseAux  = new DataBaseAdmin(new File("./data/facilities/DataBaseAuxiliar.properties"));
		ResultSet buildingsR = dataBaseAux.executeQuery("SELECT objectid, mpb.x as xcoord, mpb.y as ycoord, perc_surf as area_perc, fea_id AS id_building, postal_code as postal_code FROM work_facilities_aux.masterplan_areas mpa LEFT JOIN work_facilities_aux.masterplan_building_perc mpb ON mpa.objectid = mpb.object_id  WHERE use_for_generation = 1");
		facilities = new ActivityFacilitiesImpl();
		int b=0;
		while(buildingsR.next()) {
			Id areaId =  new IdImpl(buildingsR.getString(1));
			MPAreaData mPArea = dataMPAreas.get(areaId);
			Id id = new IdImpl((int)(buildingsR.getFloat(5)));
			if(facilities.getFacilities().get(id)!=null)
				continue;
			ActivityFacilityImpl building = facilities.createFacility(id, new CoordImpl(buildingsR.getDouble(2), buildingsR.getDouble(3)));
			building.setDesc(buildingsR.getString(6)+":"+mPArea.getType().replaceAll("&", "AND"));
			double proportion = buildingsR.getDouble(4);
			for(ActivityOption activityOptionArea:mPArea.getActivityOptions().values()) {
				double capacity = activityOptionArea.getCapacity()*proportion;
				if(capacity>0) {
					ActivityOption activityOption = new ActivityOptionImpl(activityOptionArea.getType(), building);
					activityOption.setCapacity(capacity);
					activityOption.addOpeningTime(activityOptionArea.getOpeningTimes(DayType.wkday).first());
					building.getActivityOptions().put(activityOption.getType(), activityOption);
				}
			}
			b++;
		}
		System.out.println(b + " buildings");
		capacitiesToIntegers(facilities);
		return facilities;
	}
	private static void capacitiesToIntegers(ActivityFacilities facilities) {
		for(ActivityFacility building:facilities.getFacilities().values()) {
			String minOption="", maxOption="";
			double minCapacity=Double.MAX_VALUE, maxCapacity=0;
			double rawRest = 0;
			Set<String> zeroOptions = new HashSet<String>();
			for(ActivityOption activityOption:building.getActivityOptions().values()) {
				double rawCapacity = activityOption.getCapacity();
				double capacity = Math.round(rawCapacity);
				if(capacity==0)
					zeroOptions.add(activityOption.getType());
				activityOption.setCapacity(capacity);
				if(rawCapacity<minCapacity) {
					minCapacity = rawCapacity;
					minOption = activityOption.getType();
				}
				if(rawCapacity>maxCapacity) {
					maxCapacity = rawCapacity;
					maxOption = activityOption.getType();
				}
				rawRest += rawCapacity-capacity;
			}
			double rest = Math.round(rawRest);
			if(rest>0)
				building.getActivityOptions().get(maxOption).setCapacity(Math.round(maxCapacity)+rest);
			else
				while(rest<0) {
					rest = Math.round(minCapacity)+rest;
					if(rest>0)
						building.getActivityOptions().get(minOption).setCapacity(rest);
					else {
						building.getActivityOptions().remove(minOption);
						if(rest<0) {
							minCapacity = Double.MAX_VALUE;
							for(ActivityOption activityOption:building.getActivityOptions().values())
								if(activityOption.getCapacity()<minCapacity) {
									minCapacity = activityOption.getCapacity();
									minOption = activityOption.getType();
								}
						}
					}
				}
			for(String zeroOption:zeroOptions)
				building.getActivityOptions().remove(zeroOption);
		}
	}

}
