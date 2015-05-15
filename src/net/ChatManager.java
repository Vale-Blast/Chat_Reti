package net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import app.App;

import com.google.common.collect.HashBiMap;

public class ChatManager {

	private static ChatManager instance;
	private Server server;
	private Scan scan;
	private HashBiMap<String, String> nick_address;
	private List<Message> chat_now;
	private String my_nick;
	private App app;
//	private AudioInputStream audio;
//	private Clip clip;
	
	public void setNick(String nick) {
		my_nick = nick;
	}

	/********** getInstance() **********/
	/**
	   @return returns the instance of ChatManager, following the design pattern of Singleton we will have just one ChatManager object
	 */
	public static ChatManager getInstance() {
		if (instance == null)
			instance = new ChatManager();
		return instance;
	}

	/********** ChatManager() ***********/
	/**
	   @brief It's just the constructor of this class
	 */
	private ChatManager() {
		nick_address = HashBiMap.create();
//		try {
//			audio = AudioSystem.getAudioInputStream(new File("resources/Din Don.wav"));
//			clip = AudioSystem.getClip();
//			clip.open(audio);
//		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
//			System.err.println("Error while loading audio file");
//			e.printStackTrace();
//		}
	}

	/********** startServer() **********/
	/**
	   @brief instantiates the server and starts it as a new thread
	 */
	public void startServer() {
		app = App.getInstance();
		try {
			server = Server.getInstance();
		} catch (IOException e) {
			System.err.println("Error while starting the Server");
			e.printStackTrace();
		}
		server.start();
		System.out.println("startServer() done");
	}
	
	/********** startServer() **********/
	/**
	   @brief instantiates the net-scanner and starts it as a new thread
	 */
	public void startScan() {
		scan = Scan.getInstance();
		scan.start();
		System.out.println("startScan() done");
	}
	
	public String getMyNick() {
		return my_nick;
	}
	
	/********** addNickAddress() **********/
	/**
	   @brief Adds a couple nick-address to the map
	   @param nick is the nick I want to add
	   @param address is the address I want to add
	 */
	public void addNickAddress(String nick, String address) {
		nick_address.put(nick, address);
		app.ChatList();
		System.out.println("addNickAddress(" + nick + ", " + address + ") done");
	}

	/********** getAddress() **********/
	/**
	   @brief Searches in the map and gives back the address associated to a nickname
	   @param nick is the nick of which I want to find the address
	   @return is the address I found
	 */
	public String getAddress(String nick) {
		String addr = nick_address.get(nick);
		System.out.println("getAddress(" + nick + ") done");
		return addr;
	}
	
	/********** getNick() **********/
	/**
	   @brief Searches in the map and gives back the nickname associated to an address
	   @param address is the address of which I want to find the nickname
	   @return is the nickname I found
	 */
	public String getNick(String address) {
		if (!nick_address.containsValue(address))
			System.err.println("Address: " + address + " not found");
		System.out.println("getNick(" + address + ") done");
		return nick_address.inverse().get(address);
	}
	
	public List<String> getHosts() {
		List<String> list = new ArrayList<>();
		if (!nick_address.isEmpty())
			list.addAll(nick_address.keySet());
		return list;
	}
	
	/********** timestamp() **********/
	/**
	   @brief It just builds a string with current time in this format [dd/MM/yyyy hh:mm:ss]
	 * @return is the string mentioned above
	 */
	private String timestamp() {
		Calendar now = Calendar.getInstance();
		String day = "";
		if (now.get(Calendar.DAY_OF_MONTH) < 10)
			day = "0";
		day = day + now.get(Calendar.DAY_OF_MONTH);
		String month = "";
		if (now.get(Calendar.MONTH) < 10)
			month = "0";
		month = month + now.get(Calendar.MONTH);
		String year = "" + now.get(Calendar.YEAR);
		String hours = "";
		if (now.get(Calendar.HOUR_OF_DAY) < 10)
			hours = "0";
		hours = hours + now.get(Calendar.HOUR_OF_DAY); 
		String minutes = "";
		if (now.get(Calendar.MINUTE) < 10)
			minutes = "0";
		minutes = minutes + now.get(Calendar.MINUTE);
		String seconds = "";
		if (now.get(Calendar.SECOND) < 10)
			seconds = "0";
		seconds = seconds + now.get(Calendar.SECOND);
		return "[" + day + "/" + month + "/" + year + " " + hours + ":" + minutes + ":" + seconds + "]";
	}

	/********** openChat() **********/
	/**
	   @brief Loads all messages from the chat file associated to a nickname and gives back a List of these Messages
	   @param nick is the nick mentioned above
	   @return is the List of Messages loaded from the chat file or and empty ArrayList  
	 */
    @SuppressWarnings("null")
	public List<Message> openChat(String nick) {
		File chat = new File("chats/" + nick + ".txt");
		chat_now = new ArrayList<Message>();
		String line;
		Boolean type = null; //0 means received, 1 means sent
		String date = null;
		String message = null;
		if (chat.exists()) {
			try {
				BufferedReader read_chat = new BufferedReader(new FileReader(chat));
				while((line = read_chat.readLine()) != null) {
					if (line.charAt(0) == 'S') {
						type = true;
					}
					if (line.charAt(0) == 'R') {
						type = false;
					}
					date = line.substring(1, 21);
					message = line.substring(22);
//					System.out.println("" + date);
//					System.out.println("" + message);
//					System.out.println("" + type);
					chat_now.add(new Message(date, message, type));
					message = null;
					date = null;
					type = null;
				}
				read_chat.close();
			} catch (IOException e1) {
				System.out.println("Error while loading the chat");
				e1.printStackTrace();
			}
		}
		else {
			try {
				chat.createNewFile();
			} catch (IOException e) {
				System.err.println("Error while creating a new chat file");
				e.printStackTrace();
			}
		}
		System.out.println("openChat(" + nick + ") done");
		return chat_now;
	}

    /********* send() **********/
    /**
       @brief This method sends a message and saves it into the chat file
       @param nick is the recipient
       @param message is the message to be sent
     */
	public void send(String nick, String message) {
		byte[] buff = message.getBytes(); // TODO add encrypt
		String address = nick_address.get(nick);
		//System.out.println("buff = " + buff + ", address = " + address);
		DatagramPacket packet = new DatagramPacket(buff, buff.length, new InetSocketAddress(address, server.getPort()));
		DatagramSocket sock = null;
		try {
			sock = new DatagramSocket(); //TODO COSA FARE SE INVIO NON RIESCE? E NEL CASO STIA CERCANDO DI INVIARE IL MIO NICK (QUINDI UTENTE NON LO SA)?
			sock.send(packet);		
			BufferedWriter chat_writer;
			String timestamp = timestamp();
			try {
				chat_writer = new BufferedWriter(new FileWriter("chats/" + nick + ".txt", true));
				chat_writer.write("S" + timestamp + message + "\n");
				chat_writer.close();
			} catch (IOException e) {
				System.err.println("Error while saving message sent in chat file");
				e.printStackTrace();
			}    
			chat_now.add(new Message(timestamp, message, true));
		} catch (IOException e) {
			System.err.println("Error while sending a message");
			e.printStackTrace();
			sock.close();
		}
		sock.close();
		System.out.println("send(" + nick + ", " + message + ") done");
	}
	
	public void sendIP(String ip, String message) {
		byte[] buff = message.getBytes();
		//System.out.println("buff = " + buff + ", ip = " + ip);
		DatagramPacket packet = new DatagramPacket(buff, buff.length, new InetSocketAddress(ip, server.getPort()));
		DatagramSocket sock = null;
		try {
			sock = new DatagramSocket();
			sock.send(packet);
		} catch (IOException e) {
			System.err.println("Error while sendingIP a message");
			e.printStackTrace();
			sock.close();
		}
		sock.close();
		System.out.println("sendIP(" + ip + ", " + message + ") done");
	}
	
	/********** messageReceived() **********/
	/**
	   @brief This method saves a message received into the chat file and warns the user he received a new message 
	   @param message is the message received
	   @param address is the sender
	 */
	public void messageReceived(String message, String address) {
		BufferedWriter chat_writer;
		String timestamp = timestamp();
		String nick = nick_address.inverse().get(address); //TODO COSA FARE SE NON CONOSCO QUEL NICK? È POSSIBILE CHE QUALCUNO MI PARLI E IO NON CONOSCA IL SUO NICK?
		try {
			chat_writer = new BufferedWriter(new FileWriter("chats/" + nick + ".txt", true));
			chat_writer.write("R" + timestamp + message + "\n");
			chat_writer.close();
		} catch (IOException e) {
			System.err.println("Error while saving message received in chat file");
			e.printStackTrace();
		}
		app.senders.add(nick);
		app.ChatList();
		//clip.loop(1);
		System.err.println("messageReceived(" + message + ", " + address + ")");
	}
}