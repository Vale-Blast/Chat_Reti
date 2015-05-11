package net;

public class Message {
	private String date;
	private String message;
	private boolean type; //0 means received, 1 means sent
	
	public String getDate() {
		return date;
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean isType() {
		return type;
	}

	public Message(String date, String message, boolean type) {
		this.date = date;
		this.message = message;
		this.type = type;
	}
}
