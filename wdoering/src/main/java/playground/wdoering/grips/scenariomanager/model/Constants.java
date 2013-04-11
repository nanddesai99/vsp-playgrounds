package playground.wdoering.grips.scenariomanager.model;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;

import org.matsim.core.gbl.MatsimResource;

import playground.wdoering.grips.scenariomanager.model.shape.Shape.DrawMode;
import playground.wdoering.grips.scenariomanager.model.shape.ShapeStyle;

public class Constants
{
	
	public static enum ModuleType { EVACUATION, POPULATION, BUSSTOPS, ROADCLOSURE, ANALYSIS, MATSIMSCENARIO, GRIPSSCENARIO };
	public static enum LayerType { SLIPPYMAP, SHAPE, LINES, INFO, MISC };
	public static enum Mode { EVACUATION, UTILIZATION, CLEARING };
	public enum Unit { TIME, PEOPLE }


	public static String DEFAULT_MATSIM_CONFIG_DESTINATION = "/output/config.xml";

	public static final String COLOR_PREFIX = "color_";
	
	public static String DESC_OSM_BOUNDINGBOX = "osm bounding box"; 
	
	//ID's
	public static String ID_NETWORK_BOUNDINGBOX = "networkboundingbox";
	public static String ID_EVACAREACIRCLE = "EvacuationAreaCircle";
	public static String ID_EVACAREAPOLY = "EvacuationAreaPolygon";
	
	public static String ID_HOVERELEMENT = "HoverElement";
	public static String ID_LINK_PRIMARY = "primarySelectedLink";
	public static String ID_LINK_SECONDARY = "secondarySelectedLink";
	
	public static int ID_GRIDRENDERER = 768;
	
	//COLORS
	public static Color COLOR_NET_BOUNDINGBOX_FILL = new Color(0, 120, 255, 70);
	public static Color COLOR_NET_BOUNDINGBOX_CONTOUR = new Color(0, 60,120,130);
	public static Color COLOR_NET_LIGHT_BOUNDINGBOX_FILL = new Color(0, 120, 255, 30);
	
	public static Color COLOR_EVACAREA_FILL = new Color(255, 120, 0, 100);
	public static Color COLOR_EVACAREA_CONTOUR = new Color(255, 120, 0, 150);
	
	public static Color COLOR_POPAREA_FILL = new Color(0, 150, 200, 100);
	public static Color COLOR_POPAREA_CONTOUR = new Color(0, 150, 200, 100);
	public static Color COLOR_POPAREA_HOVER = new Color(0, 180, 255, 150);
	public static Color COLOR_POPAREA_SELECTED = new Color(0, 80, 255, 170);
	
	public static Color COLOR_ROADCLOSURE_FILL = new Color(150, 0, 0, 170);
	public static Color COLOR_ROADCLOSURE_HOVER = new Color(255, 255, 0, 210);
	public static Color COLOR_ROADCLOSURE_SELECTED = new Color(255, 50, 0, 200);
	
	public static Color MENU_COLOR_EVACUATION = new Color (200,55,0);
	public static Color MENU_COLOR_POPULATION = new Color (0,55,200);
	public static Color MENU_COLOR_GRIPSSCENARIO = new Color (100,100,55);
	public static Color MENU_COLOR_ROADCLOSURE = new Color (50,150,55);
	public static Color MENU_COLOR_PTLINES = new Color (0,200,0);
	public static Color MENU_COLOR_ANALYSIS= new Color (0,120,120);
	public static Color MENU_COLOR_MATSIMSCENARIO = new Color (55,100,100);
	
	public static ShapeStyle SHAPESTYLE_EVACAREA = new ShapeStyle(COLOR_EVACAREA_FILL, COLOR_EVACAREA_CONTOUR, 4, DrawMode.FILL_WITH_CONTOUR);
	public static ShapeStyle SHAPESTYLE_POPAREA = new ShapeStyle(COLOR_POPAREA_FILL, COLOR_POPAREA_CONTOUR, 4, DrawMode.FILL_WITH_CONTOUR);
	public static ShapeStyle SHAPESTYLE_ROADCLOSURE = new ShapeStyle(COLOR_ROADCLOSURE_FILL, COLOR_ROADCLOSURE_FILL, 6, DrawMode.FILL);
	
	public static ShapeStyle SHAPESTYLE_HOVER_LINE = new ShapeStyle(COLOR_ROADCLOSURE_HOVER, COLOR_ROADCLOSURE_HOVER, 5, DrawMode.FILL);
	
	public static ShapeStyle SHAPESTYLE_SELECTED_LINE = new ShapeStyle(COLOR_ROADCLOSURE_SELECTED, COLOR_ROADCLOSURE_SELECTED, 4, DrawMode.FILL);
	
	public static String POPULATION = "population";

	public static String ID_ROADCLOSURE_PREFIX = "roadclosurelink_";
	public static String ID_BUSSTOP_PREFIX = "busstop_";

	public static String META_LINKID = "linkid";

	private static ArrayList<ModuleType> NEXT_MODULES_FOR_COLOR_EVACUATION = new ArrayList<ModuleType>(){{ add(ModuleType.POPULATION); }};
	private static ArrayList<ModuleType> NEXT_MODULES_FOR_COLOR_POPULATION = new ArrayList<ModuleType>(){{ add(ModuleType.GRIPSSCENARIO); }};
	private static ArrayList<ModuleType> NEXT_MODULES_FOR_COLOR_GRIPSSCENARIO = new ArrayList<ModuleType>(){{ add(ModuleType.ROADCLOSURE); add(ModuleType.BUSSTOPS); add(ModuleType.MATSIMSCENARIO);}};
	private static ArrayList<ModuleType> NEXT_MODULES_FOR_COLOR_ROADCLOSURE = new ArrayList<Constants.ModuleType>();
	private static ArrayList<ModuleType> NEXT_MODULES_FOR_COLOR_PTLINES = new ArrayList<Constants.ModuleType>();
	private static ArrayList<ModuleType> NEXT_MODULES_FOR_COLOR_MATSIMSCENARIO = new ArrayList<Constants.ModuleType>(){{ add(ModuleType.ANALYSIS); }};
	private static ArrayList<ModuleType> NEXT_MODULES_FOR_COLOR_ANALYSIS = new ArrayList<Constants.ModuleType>();

	public static Color getModuleColor(ModuleType type)
	{
		switch (type)
		{
			case EVACUATION : return MENU_COLOR_EVACUATION; 
			case POPULATION : return MENU_COLOR_POPULATION; 
			case GRIPSSCENARIO : return MENU_COLOR_GRIPSSCENARIO; 
			case ROADCLOSURE : return MENU_COLOR_ROADCLOSURE; 
			case BUSSTOPS : return MENU_COLOR_PTLINES; 
			case ANALYSIS : return MENU_COLOR_ANALYSIS; 
			case MATSIMSCENARIO : return MENU_COLOR_MATSIMSCENARIO; 
		}
		
		return null;
	}

	public static Image getModuleImage(ModuleType moduleType)
	{
		String img = "";
		
		switch (moduleType)
		{
			case EVACUATION :     img = IMG_ICON_EVACUATION; break;
			case POPULATION :     img = IMG_ICON_POPULATION; break;
			case GRIPSSCENARIO :  img = IMG_ICON_GRIPSSCENARIO; break;
			case ROADCLOSURE :    img = IMG_ICON_ROADCLOSURE; break;
			case BUSSTOPS :       img = IMG_ICON_BUSSTOPS; break;
			case ANALYSIS :       img = IMG_ICON_ANALYSIS; break;
			case MATSIMSCENARIO : img = IMG_ICON_MATSIMSCENARIO; break;
		}
		
		return MatsimResource.getAsImage("grips/" + img);
	}
	
	public static ArrayList<ModuleType> getNextModules(ModuleType moduleType)
	{
		switch (moduleType)
		{
			case EVACUATION : return NEXT_MODULES_FOR_COLOR_EVACUATION;  
			case POPULATION : return NEXT_MODULES_FOR_COLOR_POPULATION; 
			case GRIPSSCENARIO : return NEXT_MODULES_FOR_COLOR_GRIPSSCENARIO; 
			case ROADCLOSURE : return NEXT_MODULES_FOR_COLOR_ROADCLOSURE; 
			case BUSSTOPS : return NEXT_MODULES_FOR_COLOR_PTLINES; 
			case ANALYSIS : return NEXT_MODULES_FOR_COLOR_ANALYSIS; 
			case MATSIMSCENARIO : return NEXT_MODULES_FOR_COLOR_MATSIMSCENARIO; 
		}
		
		return null;
	}	
	
	public static Image getImageFromMatsimResouce(String imageName)
	{
		return MatsimResource.getAsImage(imageName);
	}
	
	public static String IMG_BUSSTOP = MatsimResource.getAsURL("grips/busstop.png").getFile();
	public static String IMG_ICON_EVACUATION = "EVACUATION.png";
	public static String IMG_ICON_POPULATION = "POPULATION.png";
	public static String IMG_ICON_GRIPSSCENARIO = "GRIPSSCENARIO.png";
	public static String IMG_ICON_ROADCLOSURE = "ROADCLOSURE.png";
	public static String IMG_ICON_BUSSTOPS = "BUSSTOPS.png";
	public static String IMG_ICON_ANALYSIS = "ANALYSIS.png";
	public static String IMG_ICON_MATSIMSCENARIO = "MATSIMSCENARIO.png";
		
	
	

	

	


}
