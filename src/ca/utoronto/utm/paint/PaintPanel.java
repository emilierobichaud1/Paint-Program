package ca.utoronto.utm.paint;

import javax.swing.*;  
import java.awt.*;
import java.io.PrintWriter;
import java.util.ArrayList;

class PaintPanel extends JPanel {
	private static final long serialVersionUID = 3277442988868869424L;
	private ArrayList<PaintCommand> commands = new ArrayList<PaintCommand>();
	
	public PaintPanel(){
		this.setBackground(Color.white);
		this.setPreferredSize(new Dimension(300,300));
	}
	
	public void setCommands(ArrayList<PaintCommand> commands){
		this.commands=commands;
	}
	public void reset(){
		this.commands.clear();
		this.repaint();
	}
	
	public void addCommand(PaintCommand command){
		this.commands.add(command);
	}
	public void save(PrintWriter writer){
		writer.print("Paint Save File Version 1.0");
		writer.print("\n");
		for(int i=0; i<commands.size(); i++) {
			String shape = commands.get(i).getShape();
			Shape type = commands.get(i).getObject();
			switch (shape) {
			case "circle": 
				writer.print("Circle");
				writer.print("\n");
				writer.print(type.toString());
				Circle circleShape = (Circle) type;
				writer.print("\tcenter:" + (circleShape.getCentre().toString()));
				writer.print("\n");
				writer.print("\tradius:" + Integer.toString(circleShape.getRadius()));
				writer.print("\n");
				writer.print("End Circle");
				writer.print("\n");
				break;
			case "rectangle":
				writer.print("Rectangle");
				writer.print("\n");
				writer.print(type.toString());
				Rectangle rectangleShape = (Rectangle) type;
				writer.print("\tp1:" + rectangleShape.getP1().toString());
				writer.print("\n");
				writer.print("\tp2:" + rectangleShape.getP2().toString());
				writer.print("\n");
				writer.print("End Rectangle");
				writer.print("\n");
				break;
			case "squiggle":
				writer.print("Squiggle");
				writer.print("\n");
				writer.print(type.toString());
				writer.print("\tpoints");
				writer.print("\n");
				Squiggle squiggleShape = (Squiggle) type;
				for(int j=0; j<squiggleShape.getPoints().size(); j++){
					writer.print("\t\tpoint:" + squiggleShape.getPoints().get(j).toString() + "\n");}
				writer.print("\tend points");
				writer.print("\n");
				writer.print("End Squiggle");
				writer.print("\n");
				break;
				}
			}
		writer.print("End Paint Save File");
		writer.close();
	}
	public void paintComponent(Graphics g) {
        super.paintComponent(g); //paint background
        Graphics2D g2d = (Graphics2D) g;		
		for(PaintCommand c: this.commands){
			c.execute(g2d);
		}
		g2d.dispose();
	}
}
