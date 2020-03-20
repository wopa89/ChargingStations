package geojson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

public class ChargingStations extends GeoJsonFeature {

	public ChargingStations(Map<String, Object> props, Geometry geom) {
		super(props, geom);
	}

	public static List<ChargingStations> loadGeoJson(Path path) throws IOException {
		String geoJSON = new String(Files.readAllBytes(path));
		GeoJSONReader reader = new GeoJSONReader();
		GeoJSON gs = GeoJSONFactory.create(geoJSON);
		List<ChargingStations> list = new ArrayList<>();
		STRtree tree = new STRtree();

		// check if geojson has one or more features
		if (gs instanceof Feature) {
			// add geometry and properties to list
			Feature feature = (Feature) gs;
			Map<String, Object> props = feature.getProperties();
			Geometry geom = reader.read(feature.getGeometry());
			list.add(new ChargingStations(props, geom));
		} else if (gs instanceof FeatureCollection) {
			// loop over feature collection and add geometry and properties to list
			// use spatial index for faster iteration
			FeatureCollection features = (FeatureCollection) GeoJSONFactory.create(geoJSON);
			for (Feature feature : features.getFeatures()) {
				Integer featIdx = Integer.valueOf(list.size());
				Map<String, Object> props = feature.getProperties();
				Geometry geom = reader.read(feature.getGeometry());
				list.add(new ChargingStations(props, geom));
				tree.insert(geom.getEnvelopeInternal(), featIdx);
			}
			tree.build();
			// in case of features have no propteries
		} else if (gs instanceof org.wololo.geojson.Geometry) {
			Map<String, Object> props = Collections.emptyMap();
			Geometry geom = reader.read(gs);
			list.add(new ChargingStations(props, geom));
		}
		return list;
	}

}
