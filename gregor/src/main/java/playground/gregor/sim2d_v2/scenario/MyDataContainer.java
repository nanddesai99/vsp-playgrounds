package playground.gregor.sim2d_v2.scenario;


import org.matsim.core.utils.collections.QuadTree;

import playground.gregor.sim2d_v2.simulation.floor.forces.deliberative.velocityobstacle.CCWPolygon;

import com.vividsolutions.jts.geom.Coordinate;

public class MyDataContainer {

	private QuadTree<Coordinate> quad;
	private QuadTree<CCWPolygon> segQuad;

	public void setDenseCoordsQuadTree(QuadTree<Coordinate> quad) {
		this.quad = quad;
	}

	public QuadTree<Coordinate> getDenseCoordsQuadTree() {
		return this.quad;
	}

	public void setSegmentsQuadTree(QuadTree<CCWPolygon> q) {
		this.segQuad = q;
	}

	public QuadTree<CCWPolygon> getSegmentsQuadTree() {
		return this.segQuad;
	}
}
