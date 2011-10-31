package playground.sergioo.NetworksMatcher.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.utils.collections.Tuple;

import playground.sergioo.Visualizer2D.LayersPanel;
import playground.sergioo.Visualizer2D.NetworkVisualizer.NetworkPainters.NetworkPainter;

public class NetworkCapacitiesPainter extends NetworkPainter {
	
	//Attributes
	private Color selectedLinkColor = Color.MAGENTA;
	private Color selectedNodeColor = Color.CYAN;
	private Stroke selectedStroke = new BasicStroke(2);
	private boolean withSelected = true;
	private double maxCapacity;
	private double minCapacity;
	private Map<Link, Tuple<Link,Double>> linksChanged;
	private boolean isA;
	private byte mode = 0;
	private byte numModes = 2;
	
	//Methods
	public NetworkCapacitiesPainter(Network network, boolean isA, Map<Link, Tuple<Link,Double>> linksChanged) {
		super(network, new NetworkNodesPainterManager(network));
		this.isA = isA;
		this.linksChanged = linksChanged;
		maxCapacity = 0;
		minCapacity = Double.MAX_VALUE;
		for(Tuple<Link, Double> capacitiesL:linksChanged.values()) {
			if(capacitiesL.getSecond()>maxCapacity)
				maxCapacity = capacitiesL.getSecond();
			if(capacitiesL.getSecond()<minCapacity)
				minCapacity = capacitiesL.getSecond();
		}
		if(isA)
			mode = 1;
	}
	public void paint(Graphics2D g2, LayersPanel layersPanel) {
		try {
			switch(mode) {
			case 0:
				for(Entry<Link, Tuple<Link,Double>> linksE:linksChanged.entrySet()) {
					Link link = isA?linksE.getValue().getFirst():linksE.getKey();
					double fraction = 0.07+((linksE.getValue().getSecond()-minCapacity)/(maxCapacity-minCapacity))*0.93;
					paintLink(g2, layersPanel, link, new BasicStroke(2f), 1.5, new Color(255-(int)(fraction*255),(int)(fraction*255),0));
				}
				break;
			case 1:
				for(Link link:networkPainterManager.getNetwork().getLinks().values()) {
					if(link.getCapacity()!=0) {
						double fraction = 0.07+((link.getCapacity()-minCapacity)/(maxCapacity-minCapacity))*0.93;
						paintLink(g2, layersPanel, link, new BasicStroke(2f), 1.5, new Color(255-(int)(fraction*255),(int)(fraction*255),0));
					}
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(withSelected)
			paintSelected(g2, layersPanel);
	}
	public void changeMode() {
		mode++;
		if(mode==numModes)
			mode=0;
	}
	private void paintSelected(Graphics2D g2, LayersPanel layersPanel) {
		Link link=networkPainterManager.getSelectedLink();
		if(link!=null)
			paintLink(g2, layersPanel, link, selectedStroke, 2, selectedLinkColor);
		Node node=networkPainterManager.getSelectedNode();
		if(node!=null)
			paintCross(g2, layersPanel, node.getCoord(), 4, selectedNodeColor);
	}
	public void changeVisibleSelectedElements() {
		withSelected = !withSelected;
	}

}
