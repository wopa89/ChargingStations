package geojson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.index.strtree.STRtree;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.opencsv.CSVReader;

public class GeoJsonFeature {
	public final Map<String, Object> props;
	public final Geometry geom;

	public GeoJsonFeature(Map<String, Object> props, Geometry geom) {
		this.props = props;
		this.geom = geom;
	}

	public static FileWriter writeGeoJSON(String output, List<Feature> features) throws IOException {
		// initialize file
		FileWriter file = new FileWriter(output);
		
		// write features to feature collection
		GeoJSONWriter writer = new GeoJSONWriter();
		FeatureCollection json = writer.write(features);
		// write file
		file.write(json.toString());
		file.flush();
		//close file
		file.close();

		return file;
	}
}
