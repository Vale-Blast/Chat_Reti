package app;

import java.io.IOException;

import javax.swing.JOptionPane;

import net.Server;

public class Main {

	public static void main(String[] args) {
		try {
			Server.getInstance();
			Login login = Login.getInstance();
			if(!login.getNick())
				login.createNick();
			login.checkFolders();
			App app = App.getInstance();
			app.run();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error while starting the server, please check if another process has already bound the same port and try again", "Server Error", JOptionPane.ERROR_MESSAGE);
			System.err.println("Error while starting the server");
			e.printStackTrace();
		}
	}

}
