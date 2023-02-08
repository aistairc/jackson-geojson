package com.bedatadriven;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Before;
import org.junit.Test;

import com.bedatadriven.geojson.GeoJsonModule;
import org.locationtech.jts.geom.*;

public class GeoJsonTest {
	private GeometryFactory gf = new GeometryFactory();
	private ObjectWriter writer;
	private ObjectMapper mapper;

	@Before
	public void setupMapper() {
		
		mapper = new ObjectMapper();
		mapper.registerModule(new GeoJsonModule());

		writer = mapper.writer();
	}

	@Test
	public void point() throws Exception {
		Point point = gf.createPoint(new Coordinate(1.2345678, 2.3456789));
		assertRoundTrip(point);
		assertThat(
				toJson(point),
				is("{\"type\":\"Point\",\"coordinates\":[1.2345678,2.3456789]}"));
	}

	private String toJson(Object value) throws JsonGenerationException,
            JsonMappingException, IOException {
		return writer.writeValueAsString(value);
	}

	@Test
	public void multiPoint() throws Exception {
		MultiPoint multiPoint = gf.createMultiPoint(new Point[] { gf
				.createPoint(new Coordinate(1.2345678, 2.3456789)) });
		assertRoundTrip(multiPoint);
		assertThat(
				toJson(multiPoint),
				is("{\"type\":\"MultiPoint\",\"coordinates\":[[1.2345678,2.3456789]]}"));
	}

	@Test
	public void lineString() throws Exception {
		LineString lineString = gf.createLineString(new Coordinate[] {
				new Coordinate(100.0, 0.0), new Coordinate(101.0, 1.0) });
		assertRoundTrip(lineString);
		assertThat(
				toJson(lineString),
				is("{\"type\":\"LineString\",\"coordinates\":[[100.0,0.0],[101.0,1.0]]}"));
	}

	@Test
	public void multiLineString() throws Exception {

		MultiLineString multiLineString = gf
				.createMultiLineString(new LineString[] {
						gf.createLineString(new Coordinate[] {
								new Coordinate(100.0, 0.0),
								new Coordinate(101.0, 1.0) }),
						gf.createLineString(new Coordinate[] {
								new Coordinate(102.0, 2.0),
								new Coordinate(103.0, 3.0) }) });

		assertRoundTrip(multiLineString);
		assertThat(
				toJson(multiLineString),
				is("{\"type\":\"MultiLineString\",\"coordinates\":[[[100.0,0.0],[101.0,1.0]],[[102.0,2.0],[103.0,3.0]]]}"));
	}

	@Test
	public void polygon() throws Exception {
		LinearRing shell = gf.createLinearRing(new Coordinate[] {
				new Coordinate(102.0, 2.0), new Coordinate(103.0, 2.0),
				new Coordinate(103.0, 3.0), new Coordinate(102.0, 3.0),
				new Coordinate(102.0, 2.0) });
		LinearRing[] holes = new LinearRing[0];
		Polygon polygon = gf.createPolygon(shell, holes);

		assertRoundTrip(polygon);
		assertThat(
				toJson(polygon),
				is("{\"type\":\"Polygon\",\"coordinates\":[[[102.0,2.0],[103.0,2.0],[103.0,3.0],[102.0,3.0],[102.0,2.0]]]}"));
	}

	@Test
	public void polygonWithNoHoles() throws Exception {
		LinearRing shell = gf.createLinearRing(new Coordinate[] {
				new Coordinate(102.0, 2.0), new Coordinate(103.0, 2.0),
				new Coordinate(103.0, 3.0), new Coordinate(102.0, 3.0),
				new Coordinate(102.0, 2.0) });
		LinearRing[] holes = new LinearRing[] { gf
				.createLinearRing(new Coordinate[] {
						new Coordinate(100.2, 0.2), new Coordinate(100.8, 0.2),
						new Coordinate(100.8, 0.8), new Coordinate(100.2, 0.8),
						new Coordinate(100.2, 0.2) }) };
		assertThat(
				toJson(gf.createPolygon(shell, holes)),
				is("{\"type\":\"Polygon\",\"coordinates\":[[[102.0,2.0],[103.0,2.0],[103.0,3.0],[102.0,3.0],[102.0,2.0]],[[100.2,0.2],[100.8,0.2],[100.8,0.8],[100.2,0.8],[100.2,0.2]]]}"));
	}

	@Test
	public void multiPolygon() throws Exception {
		LinearRing shell = gf.createLinearRing(new Coordinate[] {
				new Coordinate(102.0, 2.0), new Coordinate(103.0, 2.0),
				new Coordinate(103.0, 3.0), new Coordinate(102.0, 3.0),
				new Coordinate(102.0, 2.0) });
		MultiPolygon multiPolygon = gf.createMultiPolygon(new Polygon[] { gf
				.createPolygon(shell, null) });

		assertRoundTrip(multiPolygon);
		assertThat(
				toJson(multiPolygon),
				is("{\"type\":\"MultiPolygon\",\"coordinates\":[[[[102.0,2.0],[103.0,2.0],[103.0,3.0],[102.0,3.0],[102.0,2.0]]]]}"));
	}

	@Test
	public void geometryCollection() throws Exception {
		GeometryCollection collection = gf
				.createGeometryCollection(new Geometry[] { gf
						.createPoint(new Coordinate(1.2345678, 2.3456789)) });
		assertRoundTrip(collection);
		assertThat(
				toJson(collection),
				is("{\"type\":\"GeometryCollection\",\"geometries\":[{\"type\":\"Point\",\"coordinates\":[1.2345678,2.3456789]}]}"));
	}

	private void assertRoundTrip(Geometry geom) throws JsonGenerationException,
			JsonMappingException, IOException {
		String json = writer.writeValueAsString(geom);
		Geometry regeom = mapper.reader(Geometry.class).readValue(json);
		assertThat(regeom, equalTo(geom));
	}
	//
	// @Test
	// public void feature() {
	// SimpleFeature feature = buildFeature();
	// assertThat(
	// toJson(feature),
	// is("{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[102.0,2.0]},\"properties\":{\"name\":\"Hello, World\"},\"id\":\"fid-1\"}"));
	// }
	//
	// @Test
	// public void featureCollection() {
	// SimpleFeatureCollection collection = FeatureCollections.newCollection();
	// assertThat(toJson(collection),
	// is("{\"type\":\"FeatureCollection\",\"features\":[]}"));
	// }
	//
	// SimpleFeature buildFeature() {
	// SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
	// typeBuilder.setName("poi");
	// typeBuilder.setDefaultGeometry("location");
	// typeBuilder.add("location", Point.class);
	// typeBuilder.add("name", String.class);
	// typeBuilder.nillable(true).add("etc", String.class);
	// SimpleFeatureType featureType = typeBuilder.buildFeatureType();
	// SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
	// featureType);
	// featureBuilder.add(gf.createPoint(new Coordinate(102.0, 2.0)));
	// featureBuilder.add("Hello, World");
	// featureBuilder.add(null);
	// SimpleFeature feature = featureBuilder.buildFeature("fid-1");
	// return feature;
	// }
}
