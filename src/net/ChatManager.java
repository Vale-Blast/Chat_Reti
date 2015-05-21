package net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import app.App;
import app.Encryption;

import com.google.common.collect.HashBiMap;

public class ChatManager {

	private Encryption encryption;
	private static ChatManager instance;
	private Server server;
	private Scan scan;
	private HashMap<String, Integer> sending; // Map to keep track of message sent for which I haven't received an ACK yet
	private HashBiMap<String, String> nick_address;
	private List<Message> chat_now;
	private String my_nick;
	private App app;
	private AudioInputStream audio;
	private Clip clip;
	private String sound = "resources/Din Don.wav";
	
	public void play() {
		clip.loop(1);
	}
	
	public void setNick(String nick) {
		my_nick = nick;
	}
	
	public boolean setSound(String sound) {
		String old_sound = sound;
		this.sound = sound;
		try {
			audio = AudioSystem.getAudioInputStream(new File(sound));
			clip = AudioSystem.getClip();
			clip.open(audio);
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			System.err.println("Error while loading audio file");
			e.printStackTrace();
			this.sound = old_sound;
			return false;
		}
		return true;
	}
	
	public String getSound() {
		return sound;
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
		sending = new HashMap<>();
		try {
			audio = AudioSystem.getAudioInputStream(new File(sound));
			clip = AudioSystem.getClip();
			clip.open(audio);
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			System.err.println("Error while loading audio file");
			e.printStackTrace();
		}
		encryption = Encryption.getInstance();
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
	
	/********** getHosts() **********/
	/**
	   @brief Gets the list of reachable hosts
	   @return list is the list of reachable hosts
	 */
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
		byte[] buff = encryption.encrypt("#" + message, ".keys/." + nick + ".txt");
		String address = nick_address.get(nick);
		//System.out.println("buff = " + buff + ", address = " + address);
		DatagramPacket packet = new DatagramPacket(buff, buff.length, new InetSocketAddress(address, server.getPort()));
		DatagramSocket sock = null;
		try {
			sock = new DatagramSocket();
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
			JOptionPane.showMessageDialog(null, "Error while sending the message\nPlease try again" ,"Message not sent", JOptionPane.ERROR_MESSAGE);
			sock.close();
		}
		sock.close();
		if (sending.containsKey(nick + "-" + message)) {
			Integer count = sending.remove(nick + "-" + message);
			sending.put(nick + "-" + message, ++count);
		}
		else
			sending.put(nick + "-" + message, 1);
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
		String nick = nick_address.inverse().get(address);
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
		play();
		System.out.println("messageReceived(" + message + ", " + address + ")");
	}
	
	/********** ack() **********/
	/**
	   @brief Modifies sending HashMap when Server thread receives an ack
	   @param address is the address from which I received an ack
	   @param message is the message for which I received an ack
	 */
	public void ack(String message, String address) {
		String nick = nick_address.inverse().get(address);
		if(!sending.containsKey(nick + "-" + message))
			return;
		Integer count = sending.remove(nick + "-" + message);
		if (count > 1)
			sending.put(nick + "-" + message, --count);
		System.out.println("ACK received for message: \"" + message + "\"");
	}
	
	/********** attach() **********/
	/**
	   @brief Sends a file
	   @param file is the name of the file I want to send
	   @param nick is the person I want to sent the file to
	 * @throws FileNotFoundException 
	 */
	public void attach(File file, String nick) throws FileNotFoundException {
		InputStream to_send = new FileInputStream(file);
		byte buf[] = new byte[(int) file.length()]; //TODO VEDI note.txt 14) conversion it's okay because max_int in java is 2^31 and 2^31 B ==> 2 GB
		try {
			to_send.read(buf);
			attachReceived(buf, "Fake");
			byte[] buff_enc = encryption.encrypt(buf, ".keys/." + nick + ".txt");
			String address = nick_address.get(nick);
			//System.out.println("buff = " + buff + ", address = " + address);
			DatagramPacket packet = new DatagramPacket(buff_enc, buff_enc.length, new InetSocketAddress(address, server.getPort()));
			DatagramSocket sock = null;
			String sent = file.getAbsolutePath() + "/" + file.getName();
			try {
				sock = new DatagramSocket();
				sock.send(packet);		
				BufferedWriter chat_writer;
				String timestamp = timestamp();
				try {
					chat_writer = new BufferedWriter(new FileWriter("chats/" + nick + ".txt", true));
					chat_writer.write("S" + timestamp + "File: " + sent + "\n");
					chat_writer.close();
				} catch (IOException e) {
					System.err.println("Error while saving message sent in chat file");
					e.printStackTrace();
				}    
				chat_now.add(new Message(timestamp, sent, true));
			} catch (IOException e) {
				System.err.println("Error while sending a message");
				JOptionPane.showMessageDialog(null, "Error while sending the attachment\nPlease try again" ,"Attachment not sent", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				sock.close();
			}
			sock.close();
			if (sending.containsKey(nick + "-" + sent)) {
				Integer count = sending.remove(nick + "-" + sent);
				sending.put(nick + "-" + sent, ++count);
			}
			else
				sending.put(nick + "-" + sent, 1);
			to_send.close();
			System.out.println("send(" + nick + ", " + sent + ") done");
		} catch (IOException e) {
			System.err.println("Error while sending attachment");
			e.printStackTrace();
		}
	}

	/********** attachReceived() **********/
	/**
	   @brief Saves the attachment received 
	   @param buff
	   @param ip
	 */
	public void attachReceived(byte[] buff, String address) {
		clip.loop(1);
		JFileChooser jfc = new JFileChooser("attachments/");
		int res = jfc.showSaveDialog(null);
		if (res == 0) {
			String file_name = jfc.getSelectedFile().getAbsolutePath();
			System.out.println(file_name);
			try {
				OutputStream wri = new FileOutputStream(file_name, true); //TODO vedi note.txt 14)
				wri.write(buff, 0, buff.length);
				wri.close();
			} catch (IOException e) {
				System.err.println("Error while saving attachment received");
				e.printStackTrace();
			}
			System.out.println("saved");
			BufferedWriter chat_writer;
			String timestamp = timestamp();
			String nick = nick_address.inverse().get(address); 
			try {
				chat_writer = new BufferedWriter(new FileWriter("chats/" + nick + ".txt", true));
				chat_writer.write("R" + timestamp + "File: " + file_name + "\n");
				chat_writer.close();
			} catch (IOException e) {
				System.err.println("Error while saving message received in chat file");
				e.printStackTrace();
			}
		}
		app.ChatList();
		System.out.println("attachReceived() done");		
	}	
}