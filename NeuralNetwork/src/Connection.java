
public class Connection {
	
	private double weight;
	private double dWeight;
	
	public Connection(double w){
		this.weight = w;
	}
	
	public void setDWeight(double dw){
		this.dWeight = dw;
	}
	
	public void setWeight(double w){
		this.weight = w;
	}
	
	public double getWeight(){
		return this.weight;
	}
	
	public double getDWeight(){
		return this .dWeight;
	}

}
