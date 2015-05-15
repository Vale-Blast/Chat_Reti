package app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.ChatManager;
import net.Scan;
import net.Server;

public class Login {

	private static Login instance;
	private ChatManager chat_manager;
	private Size size;

	public static Login getInstance() {
		if (instance == null)
			instance = new Login();
		return instance;
	}

	private Login() {
		chat_manager = ChatManager.getInstance();
		size = Size.getInstance();
	}

	public boolean getNick() {
		File nick = new File(".chat");
		boolean ret = false;
		if (!nick.exists())
			return false;
		BufferedReader read_nick;
		try {
			String line;
			read_nick = new BufferedReader(new FileReader(nick));
			int index;
			while((line = read_nick.readLine()) != null) {
				index = line.indexOf("NICK: ");
				if (index != -1) {
					chat_manager.setNick(line.substring(6));
					ret = true;
				}
				index = line.indexOf("TTS: ");
				if (index != -1) 
					Scan.getInstance().setSleep(Integer.parseInt(line.substring(5)));
				index = line.indexOf("BUFF: ");
				if (index != -1)
					Server.getInstance().setBuff_size(Integer.parseInt(line.substring(6)));
			}
			read_nick.close();
		} catch(IOException e) {
			return false;
		}
		return ret;
	}

	boolean cont = true;
	
	public void createNick() {
		JFrame frame = new JFrame("Insert nickname");
		frame.setLayout(null);
		int vpadd = size.getScreen_offset().height/20;
		frame.setBounds(size.getScreen_offset().width, size.getScreen_offset().height, size.getNew_nick().width, size.getNew_nick().height);
		JLabel nick_label = new JLabel("Insert your nickname");
		nick_label.setBounds(size.getNew_nick().width/4, 5*vpadd, size.getNew_nick().width*4/5, size.getNew_nick().height*1/6);
		JTextField nick_field = new JTextField();
		nick_field.setBounds(size.getNew_nick().width/10, size.getNew_nick().height*1/8 + 10*vpadd, size.getNew_nick().width*4/5, size.getNew_nick().height*1/8);
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				cont = false;
			}
		});
		ok.setBounds(size.getNew_nick().width/2 - size.getButton_inside().width/2, size.getNew_nick().height/4 + 15*vpadd, size.getButton_inside().width, size.getButton_inside().height/2);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.add(nick_label);
		frame.add(nick_field);
		frame.add(ok);
		while(cont) {
			System.out.println(nick_field.getText());
			if(nick_field.getText().length() > 0)
				ok.setEnabled(true);
			else
				ok.setEnabled(false);
		}
		frame.dispose();
		File nick = new File(".chat");
		BufferedWriter nick_writer;
		try {
			nick.createNewFile();
			nick_writer = new BufferedWriter(new FileWriter(nick, false));
			nick_writer.write("NICK: " + nick_field.getText() + "\n");
			nick_writer.close();
		} catch (IOException e) {
			System.err.println("Error while saving Nickname");
			e.printStackTrace();
		}
		chat_manager.setNick(nick_field.getText());
		System.out.println("nick set: " + nick_field.getText());
	}
}
