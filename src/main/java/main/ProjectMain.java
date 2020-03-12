package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.index.strtree.STRtree;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.opencsv.CSVReader;

import geojson.Boundary;
import geojson.ChargingStations;
import geojson.GeoJsonFeature;

public class ProjectMain {
	public static void main(String[] args) throws IOException, NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException {
		Path pathToBoundary = Paths.get("D:\\Pascal\\Java\\Data\\Germany.geojson");
		Path pathToChargingStations = Paths.get("D:\\Pascal\\Java\\Data\\charging_stations\\germany.geojson");
		String csvFile = "D:\\Pascal\\Java\\Data\\population.csv";
		String outputfile = "D:\\Pascal\\Java\\Data\\Output\\test.geojson";

		// create list with features
		List<Feature> features = new ArrayList<Feature>();

		// printing messages
		System.out.println("Begin to compute charging stations for every Bundesland...");
		System.out.println();
		
		try {
			int counter = 0;
			//loop over boundary geojson
			for (Boundary bounds : Boundary.loadGeoJson(pathToBoundary)) {
				counter +=1;
				// create map to store the properties of every feature
				Map<String, Object> properties = new HashMap<String, Object>();
				
				// get geometry of boundaries
				Geometry geometry = bounds.geom;
				// get name of the Bundesland
				String localname = (String) bounds.props.get("localname");

				// re-projection does not work yet but needed to compute the charging stations
//				CoordinateReferenceSystem sourceCRS = DefaultGeographicCRS.WGS84;
//				CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:32632");
//
//				MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
//				Geometry targetGeometry = JTS.transform(geometry, transform);
//				System.out.println(targetGeometry.getSRID());
				
				// convert from JTS to wololo geometry
				GeoJSONWriter writer = new GeoJSONWriter();
				org.wololo.geojson.Geometry geom = writer.write(geometry);

//				read csv file
				try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
					String[] nextLine;
					String csvSplitBy = ";";

					// read csv line by line
					while ((nextLine = csvReader.readNext()) != null) {
						// nextLine[] is an array of values from the line
						String line = nextLine[0];
						// split the line by csv delimiter
						String[] country = line.split(csvSplitBy);
						String s = country[0];

						// joining the csv file with matching value of boundary
						if (s.equals(localname)) {
							properties.put("name", country[0]);
							properties.put("population", country[1]);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
//				calculate charging station density
				// counter for number of charging stations
				int chargingStations = 0;
				// get names of Bundesländer
				String string = (String) bounds.props.get("name");
				// get area in square kilometer
				double value = geometry.getArea() / 1000000;

				for (ChargingStations cs : ChargingStations.loadGeoJson(pathToChargingStations)) {
					// check which charging stations lie within boundary of Bundesländer
					// and add the values to the counter variable
					if (cs.geom.within(geometry)) {
						chargingStations += cs.geom.getNumPoints();
					}
				}
				// calculate the density of charging stations
				double density = chargingStations / value;
				// add the value to the map
				properties.put("cs_density", density);
				
				// add feature to list
				features.add(new Feature(geom, properties));

				// printing messages
				System.out.printf("%s: %f%n", string, density);
				System.out.printf("%d. feature created%n", counter);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		write GeoJSON
		GeoJsonFeature.writeGeoJSON(outputfile, features);
	}
}
