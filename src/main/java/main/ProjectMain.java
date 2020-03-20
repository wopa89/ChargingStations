package main;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;

import csv.CSVFile;
import geojson.Boundary;
import geojson.ChargingStations;
import geojson.GeoJsonFeature;

public class ProjectMain {
	public static void main(String[] args) throws IOException, NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException {
		Path pathToBoundary = Paths.get("germany_boundaries.geojson");
		Path pathToChargingStations = Paths.get("charging_stations.geojson");
		String csvGDP = "Bruttoinlandsprodukt_je_Einwohner.csv";
		String csvPopulation = "population_bundeslaender.csv";
		String outputfile = "output.geojson";

		// create list with features
		List<Feature> features = new ArrayList<Feature>();

		// printing messages
		System.out.println("Begin to compute charging stations for every Bundesland...");
		System.out.println();

		try {
			int counter = 0;
//			loop over boundary geojson
			for (Boundary bounds : Boundary.loadGeoJson(pathToBoundary)) {
				counter += 1;

				// create map to store the properties of every feature
				Map<String, Object> properties = new HashMap<String, Object>();
				properties.put("id", counter);

				// initiate re-projection from WGS84 to UTM32N
				CoordinateReferenceSystem sourceCRS = DefaultGeographicCRS.WGS84;
				CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:32632");
				MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

				// re-project boundaries geometry
				Geometry reprojGeometryBounds = JTS.transform(bounds.geom, transform);
				// get name of the Bundesland
				String localname = (String) bounds.props.get("GEN");
				// add the value to the map
				properties.put("name", localname);

//				calculate charging station density
				// counter for number of charging stations
				int chargingStations = 0;
				// get area in square kilometer
				double value = reprojGeometryBounds.getArea() / 1000000;

				for (ChargingStations cs : ChargingStations.loadGeoJson(pathToChargingStations)) {
					// re-project charging stations geometry
					Geometry reprojGeometryCS = JTS.transform(cs.geom, transform);
					// check which charging stations lie within boundary of Bundesländer
					// and add the values to the counter variable
					if (reprojGeometryCS.within(reprojGeometryBounds)) {
						chargingStations += reprojGeometryCS.getNumPoints();
					}
				}
				// calculate the density of charging stations
				double csDensity = chargingStations / value;

				// add density value to map
				properties.put("cs_density", csDensity);

//				read csv
				// name of column that stores the population value
				String columnNamePopulation = "insgesamt";
				int resultPopulation = CSVFile.readCSV(csvPopulation, columnNamePopulation, localname);
				properties.put("population", resultPopulation);
				// calculate population density
				double popDensity = resultPopulation / value;
				properties.put("pop_density", popDensity);

				// name of column that stores the recent gdp values
				String columnNameGDP = "2018";
				int resultGDP = CSVFile.readCSV(csvGDP, columnNameGDP, localname);
				properties.put("gdp_percapita", resultGDP);

//				add features to list
				// convert from JTS to wololo geometry
				GeoJSONWriter writer = new GeoJSONWriter();
				org.wololo.geojson.Geometry geometry = writer.write(bounds.geom);
				// add features
				features.add(new Feature(geometry, properties));

				// printing messages
				System.out.printf("%d. feature created%n", counter);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

//		write GeoJSON
		GeoJsonFeature.writeGeoJSON(outputfile, features);

		System.out.println("End of process");
	}
}
