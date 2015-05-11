package app;

import java.awt.Dimension;
import java.awt.Toolkit;

public class Size {
	private static Size instance;
	
	private Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	private Dimension screen_offset = new Dimension(screen.width/10, screen.height/10);
	private Dimension jframe = new Dimension(screen.width*7/10, screen.height*7/10);
	private Dimension toolbar = new Dimension(jframe.width, jframe.height/10);
	private Dimension chats = new Dimension(jframe.width/6, jframe.height - toolbar.height);
	private Dimension messages = new Dimension(jframe.width - chats.width, jframe.height - 3 * toolbar.height);
	private Dimension write = new Dimension(messages.width, jframe.height - toolbar.height - messages.height);
	private Dimension new_chat = new Dimension(jframe.width/4, jframe.height/3);
	private Dimension button_inside = new Dimension(new_chat.width/3, new_chat.height/3);
	private Dimension add_nick = new Dimension(jframe.width/2, jframe.height/2);

	/********** getInstance() **********/
	/**
	   @return returns the instance of Size, following the design pattern of Singleton we will have just one Size object
	 */
	public static Size getInstance() {
		if (instance == null)
			instance = new Size();
		return instance;
	}

	private Size() {
		super();
	}
	
	public Dimension getScreen() {
		return screen;
	}

	public Dimension getScreen_offset() {
		return screen_offset;
	}
	
	public Dimension getJframe() {
		return jframe;
	}

	public Dimension getToolbar() {
		return toolbar;
	}
	
	public Dimension getChats() {
		return chats;
	}

	public Dimension getMessages() {
		return messages;
	}

	public Dimension getWrite() {
		return write;
	}

	public Dimension getNew_chat() {
		return new_chat;
	}
	
	public Dimension getButton_inside() {
		return button_inside;
	}

	public Dimension getAdd_nick() {
		return add_nick;
	}
}
