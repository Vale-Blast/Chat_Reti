package app;

public class Main {

	public static void main(String[] args) {
		Login login = Login.getInstance(); //INSERISCI TUO NICK SE È PRIMO ACCESSO
		if(!login.getNick())
			login.createNick();
		App app = App.getInstance();
		app.run();
	}

}
