package app;


public class TripleDES {
	private static TripleDES instance;
	
	public static TripleDES getInstance() {
		if (instance == null)
			instance = new TripleDES();
		return instance;
	}
	
	private TripleDES() {
		
	}
	
	public byte[] encrypt(String message) {
		
	}
	
	public String decrypt(byte[] message) {
		
	}
	
	public void importKey(String keyfile) {
		
	}

}
