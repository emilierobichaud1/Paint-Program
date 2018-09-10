package ca.utoronto.utm.paint;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
/**
 * Parse a file in Version 1.0 PaintSaveFile format. An instance of this class
 * understands the paint save file format, storing information about
 * its effort to parse a file. After a successful parse, an instance
 * will have an ArrayList of PaintCommand suitable for rendering.
 * If there is an error in the parse, the instance stores information
 * about the error. For more on the format of Version 1.0 of the paint 
 * save file format, see the associated documentation.
 * 
 * @author 
 *
 */
public class PaintSaveFileParser {
	private int lineNumber = 0; // the current line being parsed
	private String errorMessage =""; // error encountered during parse
	private ArrayList<PaintCommand> commands; // created as a result of the parse
	
	/**
	 * Below are Patterns used in parsing 
	 */
	private Pattern pFileStart=Pattern.compile("^PaintSaveFileVersion1.0$");
	private Pattern pFileEnd=Pattern.compile("^EndPaintSaveFile$");
	private Pattern pColor=Pattern.compile("^color:(-?[0-9]{1,3}),(-?[0-9]{1,3}),(-?[0-9]{1,3})$");
	private Pattern pFilled=Pattern.compile("^filled:(true|false)$");

	private Pattern pCircleStart=Pattern.compile("^Circle$");
	private Pattern pCenter=Pattern.compile("^center:\\((-?[0-9]{1,3}),(-?[0-9]{1,3})\\)$");
	private Pattern pRadius=Pattern.compile("^radius:(-?\\d*)$");
	private Pattern pCircleEnd=Pattern.compile("^EndCircle$");
	
	private Pattern pSquiggleStart=Pattern.compile("^Squiggle$");
	private Pattern pPoints=Pattern.compile("^points");
	private Pattern pPoint=Pattern.compile("^point:\\((-?[0-9]{1,3}),(-?[0-9]{1,3})\\)$");
	private Pattern pEndPoints=Pattern.compile("^endpoints$");
	private Pattern pSquiggleEnd=Pattern.compile("^EndSquiggle$");
	
	private Pattern pRectangleStart=Pattern.compile("^Rectangle$");
	private Pattern pP1=Pattern.compile("^p1:\\((-?[0-9]{1,3}),(-?[0-9]{1,3})\\)$");
	private Pattern pP2=Pattern.compile("^p2:\\((-?[0-9]{1,3}),(-?[0-9]{1,3})\\)$");
	private Pattern pRectangleEnd=Pattern.compile("^EndRectangle");
	private Pattern pWildMatcher=Pattern.compile("^\\S+$"); 
	
	/**
	 * Store an appropriate error message in this, including 
	 * lineNumber where the error occurred.
	 * @param mesg
	 */
	private void error(String mesg){
		this.errorMessage = "Error in line "+lineNumber+" "+mesg;
		JOptionPane.showMessageDialog(null, this.errorMessage, "Load Error ", JOptionPane.INFORMATION_MESSAGE);
	}
	/**
	 * 
	 * @return the PaintCommands resulting from the parse
	 */
	public ArrayList<PaintCommand> getCommands(){
		return this.commands;
	}
	/**
	 * 
	 * @return the error message resulting from an unsuccessful parse
	 */
	public String getErrorMessage(){
		return this.errorMessage;
	}
	public Color colorFinder(int r,int g, int b) {
		Color color = null;
		if(0<=r && r<=255 && 0<=g && g<=255 && 0<=b && b<=255) {
			color = new Color(r,g,b);
		}else {
			error("Color out of bound");
		}
		return color;
	}
	public boolean fillFinder(String str) {
		if(str == "true") {
			return true;
		}else {
			return false;
		}
	}
	public Point pointFinder(int x, int y) {
		return new Point(x,y);
	}
	
	/**
	 * Parse the inputStream as a Paint Save File Format file.
	 * The result of the parse is stored as an ArrayList of Paint command.
	 * If the parse was not successful, this.errorMessage is appropriately
	 * set, with a useful error message.
	 * 
	 * @param inputStream the open file to parse
	 * @return whether the complete file was successfully parsed
	 */
	public boolean parse(BufferedReader inputStream) {
		commands = new ArrayList<PaintCommand>();
		this.errorMessage="";
		
		// During the parse, we will be building one of the 
		// following shapes. As we parse the file, we modify 
		// the appropriate shape.
		
		Circle circle = null; 
		Rectangle rectangle = null;
		Squiggle squiggle = null;
	
		try {
			int state=0; Matcher m; String l;
			
			this.lineNumber=0;
			while ((l = inputStream.readLine()) != null) {
				this.lineNumber++;
				l = l.replaceAll("\\s", "");
				if (l.length()==0) {
					continue;
					}
				System.out.println(lineNumber+" "+l+" "+state);
				switch(state){
					case 0:
						m=pFileStart.matcher(l);
						if(m.matches()){
							state=1;
							break;
							
						}else {
							error("Expected the begining of the file");
							return false;
						}
					case 1: // Looking for the start of a new object or end of the save file
						m=pCircleStart.matcher(l);
						if(m.matches()){
							circle = new Circle();
							state = 2;
							break;
						}
						m=pRectangleStart.matcher(l);
						if(m.matches()) {
							rectangle = new Rectangle();
							state = 7;
							break;
							}
						m=pSquiggleStart.matcher(l);
						if(m.matches()) {
							squiggle = new Squiggle();
							state = 12;
							break;
						}
						m=pFileEnd.matcher(l);
						if(m.matches()) {
							state = 17;
							inputStream.close();
							break;
						}else {
							error("Expected the beginning of a shape or the end of the file");
							return false;
						}
					case 2:
						m=pColor.matcher(l);
						if(m.matches()) {
							circle.setColor(colorFinder(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(1)),Integer.parseInt(m.group(1))));
							state = 3;
							break;
						}else {
							error("Expected Color");
							return false;
						}
						
					case 3:
						m=pFilled.matcher(l);
						if(m.matches()) {
							circle.setFill(fillFinder(m.group(1)));
							state = 4;
							break;
						}else {
							error("Expected fill option");
							return false;
						}
					case 4:
						m = pCenter.matcher(l);
						if(m.matches()) {
							circle.setCentre(pointFinder(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2))));
							state = 5;
							break;
						}else {
							error("Expected center");
							return false;
						}
					case 5:
						m=pRadius.matcher(l);
						if(m.matches()) {
							int r = Integer.parseInt(m.group(1));
							if(0<=r) {
								circle.setRadius(Integer.parseInt(m.group(1)));
								state = 6;
								break;
							}else {
								error("incorrect radius");
								state = 6;
								break;
							}
							
						}else{
							error("Expected radius");
							return false;
						}
					case 6:
						m=pCircleEnd.matcher(l);
						if(m.matches()) {
							CircleCommand circleCommand = new CircleCommand(circle);
							commands.add(circleCommand);
							
							state = 1;
							break;
						}else {
							error("Expected the end of the circle");
							return false;
						}
					case 7:
						m=pColor.matcher(l);
						if(m.matches()) {
							rectangle.setColor(colorFinder(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(1)),Integer.parseInt(m.group(1))));
							state = 8;
							break;
						}else {
							error("Expected Color");
							return false;
						}
					case 8:
						m=pFilled.matcher(l);
						if(m.matches()) {
							rectangle.setFill(fillFinder(m.group(1)));
							state = 9;
							break;
						}else {
							error("Expected fill option");
							return false;
						}
					case 9:
						m = pP1.matcher(l);
						if(m.matches()) {
							rectangle.setP1(pointFinder(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2))));
							state = 10;
							break;
						}else {
							error("Expected p1");
							return false;
						}
					case 10:
						m = pP2.matcher(l);
						if(m.matches()) {
							rectangle.setP2(pointFinder(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2))));
							state = 11;
							break;
						}else {
							error("Expected p2");
							return false;
						}
					case 11:
						m=pRectangleEnd.matcher(l);
						if(m.matches()) {
							RectangleCommand rectangleCommand = new RectangleCommand(rectangle);
							commands.add(rectangleCommand);
							state = 1;
							break;
						}else {
							error("Expected the end of the rectangle");
							return false;
						}
					case 12:
						m=pColor.matcher(l);
						if(m.matches()) {
							squiggle.setColor(colorFinder(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(1)),Integer.parseInt(m.group(1))));
							state = 13;
							break;
						}else {
							error("Expected Color");
							return false;
						}
					case 13:
						m=pFilled.matcher(l);
						if(m.matches()) {
							squiggle.setFill(fillFinder(m.group(1)));
							state = 14;
							break;
						}else {
							error("Expectedfill");
							return false;
						}
					case 14:
						m=pPoints.matcher(l);
						if(m.matches()) {
							state = 15;
							break;
						}else {
							error("Expected start points");
							return false;
						}
					case 15:
						m=pPoint.matcher(l);
						if(m.matches()) {
							squiggle.getPoints().add(pointFinder(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2))));
							break;
						}
						m=pEndPoints.matcher(l);
						if(m.matches()) {
							state = 16;
							break;
						}else {
							error("Expected points");
							return false;
						}
					case 16:	
						m=pSquiggleEnd.matcher(l);
						if(m.matches()) {
							SquiggleCommand squiggleCommand = new SquiggleCommand(squiggle);
							commands.add(squiggleCommand);
							state = 1;
							break;
						}else { 
							error("Expected the end of the squiggle");
							return false;
						}
					case 17:
						m=pWildMatcher.matcher(l); 
						if(m.matches()){
							error("Expected file to be over");
							return false;
						}
				}
			}
		}catch (Exception e){
		}
		return true;
	}
}
