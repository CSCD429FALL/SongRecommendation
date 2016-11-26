package spandora;


public class MultiValuedAttribute{

	private boolean[] values;
	private int index;
	private String str;

	public MultiValuedAttribute(int numberOfValues, int index){
		values = new boolean[numberOfValues];
		this.index = index;
	}

	public int getIndex(){
		return index;
	}
	public boolean contains(int index){
		return values[index];
	}

	public int getLength(){
		return values.length;
	}

	public void setValue(boolean contains, int index){
		values[index] = contains;
	}

	public boolean equals(MultiValuedAttribute other) {

		if(other.getLength() != values.length){
			return false;
		}
		else{
			for(int i = 0; i < values.length; i++){
				if(!this.contains(i) && other.contains(i) || this.contains(i) && !other.contains(i)){
					return false;
				}
			}
		}


		return true;

	}

	public String toString(){


		str = "";
		int counter = 0;
		
		for(int i = 0; i < values.length; ++i){
			if(i < values.length - 1){
				if(values[i]){
					str += counter + " ";
				}
				
			}
			else{
				str += counter;
			}
			++counter;
		}
		



		return str;

	}





}
