package app;

public class Main {

	public static void main(String[] args) {
		Login login = Login.getInstance();
		if(!login.getNick())
			login.createNick();
		login.checkFolders();
		App app = App.getInstance();
		app.run();
	}

}
