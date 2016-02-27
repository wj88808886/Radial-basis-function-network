import java.util.ArrayList;


public class Cluster {

	private ArrayList<Double> points = new ArrayList<Double>();
	private double mean;
	private double var;
	
	public Cluster(double mean){
		this.mean = mean;
	}
	
	public void append(double d){
		points.add(d);
	}
	
	public void removeAll(){
		points.clear();
	}
	
	public ArrayList<Double> points(){
		return points;
	}
	
	public void setmean(double mean){
		this.mean = mean;
	}
	
	public double mean(){
		return mean;
	}

	public void setvar(double var){
		this.var = var;
	}
	
	public double var(){
		return var;
	}
	
	public double std(){
		return Math.sqrt(var);
	}
}
