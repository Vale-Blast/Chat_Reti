package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.ChatManager;

public class Login {

	private static Login instance;
	private ChatManager chat_manager;
	
	public static Login getInstance() {
		if (instance == null)
			instance = new Login();
		return instance;
	}
	
	private Login() {
	}

	public boolean getNick() {
		chat_manager = ChatManager.getInstance();
		File nick = new File("priv/.hidden.txt");
		if (!nick.exists())
			return false;
		BufferedReader read_nick;
		try {
			String line;
			read_nick = new BufferedReader(new FileReader(nick));
			while((line = read_nick.readLine()) != null) {
				if (line.length() > 0) {
					chat_manager.setNick(line);
					read_nick.close();
					return true;
				}
			}
			read_nick.close();
		} catch(IOException e) {	
			return false;
		}
		return false;
	}

	public void createNick() {
		//ASK NICKNAME
		File nick = new File("priv/.hidden.txt");
		try {
			nick.createNewFile();
		} catch (IOException e) {
			System.err.println("Error while saving Nickname");
			e.printStackTrace();
		}
		chat_manager.setNick("VALE");
	}
	
	

}
