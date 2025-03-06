
public class Data {
	
	private String configuration;
	private int s;
	
	
	public Data(String config, int score) {
		 configuration = config;
		 s = score;
	}
	
	public String getConfiguration() {
		return this.configuration;
	}
	
	public int getScore() {
		return this.s;
	}
}
