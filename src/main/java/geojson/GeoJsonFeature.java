package geojson;

import java.util.Map;

import org.locationtech.jts.geom.Geometry;

public class GeoJsonFeature {
	public final Map<String, Object> props;
	public final Geometry geom;

	public GeoJsonFeature(Map<String, Object> props, Geometry geom) {
		this.props = props;
		this.geom = geom;
	}
}
