
public class Output {

	private double v;
	private double delta;
	private double bias;
	private double y;
	
	public Output(double bias){
		this.bias = bias;
	}
	
	public void setbias(double bias){
		this.bias = bias;
	}
	
	public double bias(){
		return bias;
	}
	
	public void setdelta(double delta){
		this.delta = delta;
	}
	
	public double delta(){
		return delta;
	}
	
	public void setv(double v){
		this.v = v;
	}
	
	public double v(){
		return v;
	}
	
	public void sety(double y){
		this.y = y;
	}
	
	public double y(){
		return y;
	}
	
}
