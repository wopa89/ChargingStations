package main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.index.strtree.STRtree;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import geojson.Boundary;
import geojson.ChargingStations;

/**
 * Hello world!
 *
 */
public class ProjectMain {
	public static void main(String[] args) throws IOException, NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException {
		Path pathToBoundary = Paths.get("pathToBoundary");
		Path pathToChargingStations = Paths.get("pathToChargingStations");

		Map<String, Double> area = new HashMap<>();
		STRtree tree = new STRtree();
		int land = 1;
		
		System.out.println("Begin to compute charging stations for every Bundesland...");
		System.out.println();
		
		try {
			for (Boundary bounds : Boundary.loadGeoJson(pathToBoundary)) {

				// counter for number of charging stations
				int chargingStations = 0;
				// get names of Bundesländer
				String string = (String) bounds.props.get("name");
				// get area in square kilometer
				double value = bounds.geom.getArea() / 1000000;

				for (ChargingStations cs : ChargingStations.loadGeoJson(pathToChargingStations)) {
					// check which charging stations lie within boundary of Bundesländer
					// and add the values to the counter variable
					if (cs.geom.within(bounds.geom)) {
						chargingStations += cs.geom.getNumPoints();
					}
				}
				// calculate the density of charging stations
				double density = chargingStations / value;

				// fill the hashmap
				area.put(string, density);
				
//				System.out.printf("Land %d: %d%n", land, chargingStations);
				land +=1;

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Charging station density for every Bundesland: ");
		
		// loop over map and print entries
		for (Map.Entry mapElement : area.entrySet()) {
			String key = (String) mapElement.getKey();
			double value = (double) mapElement.getValue();
			System.out.println(key + " : " + value);
		}
	}
}
