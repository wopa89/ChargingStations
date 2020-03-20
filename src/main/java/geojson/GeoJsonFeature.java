package geojson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.jts2geojson.GeoJSONWriter;

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
		// close file
		file.close();

		return file;
	}
}
