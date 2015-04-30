package net;

import java.util.Date;

public class Message {
	private Date date;
	private String message;
	private boolean type; //0 means received, 1 means sent
	
	public Date getDate() {
		return date;
	}
	
	public String getMessage() {
		return message;
	}
	
	public boolean isType() {
		return type;
	}

	public Message(Date date, String message, boolean type) {
		this.date = date;
		this.message = message;
		this.type = type;
	}
}
