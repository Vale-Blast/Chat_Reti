package net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.HashBiMap;

public class ChatManager {
	
	private static ChatManager instance;
	private Server server;
	private BufferedReader nick_file_read;
	private BufferedWriter nick_file_write;
	private HashBiMap<String, InetSocketAddress> nick_address;
	private String separator = " $$$ ";
	private int nick_number = 0;
	private List<Message> chat_now;
	
	public static ChatManager getInstance() {
		if (instance == null)
			instance = new ChatManager();
		return instance;
	}
	
	private ChatManager() {
		nick_address = HashBiMap.create();
		File f = new File("files/nick.txt");
		try {
			if (!f.exists() || f.isDirectory()) // If it doesn't exist I create it to avoid FileNotFoundException
				f.createNewFile();
			nick_file_read = new BufferedReader(new FileReader("files/nick.txt"));
			String line, nick, address;
			while ((line = nick_file_read.readLine()) != null) {
				++nick_number;
				nick = line.substring(0, line.lastIndexOf(separator));
				address = line.substring(line.lastIndexOf(separator) + separator.length());
				nick_address.put(nick, new InetSocketAddress(address.substring(0, address.lastIndexOf(":")),
						Integer.parseInt(address.substring(address.lastIndexOf(":")+ 1))));
			}
			nick_file_read.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startServer() throws UnknownHostException, IOException {
		server = Server.getInstance();
		server.start();		
		System.out.println("startServer() done");
	}
	
	public void addNick(String nick, String address, int port) throws Throwable {
		if (nick_address.containsValue(new InetSocketAddress(address, port)))
			throw new Throwable("Address:port = " + address + ":" + port + ", already in use");
		if (nick_address.containsKey(nick))
			throw new Throwable("Nick = " + nick + ", already in use");
		nick_address.put(nick, new InetSocketAddress(address, port));		
		nick_file_write = new BufferedWriter(new FileWriter("files/nick.txt", true));
		nick_file_write.write(nick + separator + address + ":" + port + "\n");
		nick_file_write.close();
		++nick_number;
		System.out.println("addNick(" + nick + ", " + address + ", " + port + ") done");
	}
	
	public void deleteNick(String nick) throws Throwable {
		if (!nick_address.containsKey(nick))
			throw new Throwable("Nick = " + nick + ", not found");
		String address = nick_address.get(nick).toString();
		address = address.substring(1);
		nick_address.remove(nick);
		nick_file_write = new BufferedWriter(new FileWriter("files/nick.txt", false));
		if (!nick_address.isEmpty()) {
			Iterator<String> keyIter = nick_address.keySet().iterator();
			while (keyIter.hasNext()) {
				String key = keyIter.next();
				InetSocketAddress value = nick_address.get(key);
				nick_file_write.write(key + separator + value.getHostString() + ":" + value.getPort() + "\n"); // change getHostString() with getHostName() for a different visualization
			}
		}
		nick_file_write.close();
		--nick_number;
		System.out.println("deleteNick(" + nick + ") done");
	}
	
	public void editNick(String old_nick, String new_nick, String new_address, int new_port) throws Throwable {
		if (!nick_address.containsKey(old_nick))
			throw new Throwable("Nick = " + old_nick + ", not found");
		String old_address = nick_address.get(old_nick).getHostString() + ":" + nick_address.get(old_nick).getPort();
		nick_address.remove(old_nick);
		nick_address.put(new_nick, new InetSocketAddress(new_address, new_port));
		nick_file_write = new BufferedWriter(new FileWriter("files/nick.txt", false));
		if (!nick_address.isEmpty()) {
			Iterator<String> keyIter = nick_address.keySet().iterator();
			while (keyIter.hasNext()) {
				String key = keyIter.next();
				InetSocketAddress value = nick_address.get(key);
				nick_file_write.write(key + separator + value.getHostString() + ":" + value.getPort()); // change getHostString() with getHostName() for a different visualization
			}
		}
		nick_file_write.close();
		File chat = new File("chats/" + old_address + ".txt");
		if (chat.exists())
			chat.renameTo(new File("chats/" + new_address + ":" + new_port + ".txt"));
		System.out.println("editNick(" + old_nick + ", " + new_nick + ", " + new_address + ", " + new_port + ") done");
	}
	
	public String[] getNicks() {
		String[] result = new String[nick_number];
		int index = 0;
		if (!nick_address.isEmpty()) {
			Iterator<String> keyIter = nick_address.keySet().iterator();
			while (keyIter.hasNext()) {
				result[index] = keyIter.next();
				++index;
			}
		}
		System.out.println("getNicks() done");
		return result;
	}
	
	public String getAddress(String nick) {
		String addr = nick_address.get(nick).toString();
		System.out.println("getAddress(" + nick + ") done");
		return addr.substring(addr.lastIndexOf("/")+1);
		 // InetSocketAddress are represented with a string like Blast/127.0.1.1:25022
	}
	
	public String getNick(String address, int port) throws Throwable {
		if (!nick_address.containsValue(new InetSocketAddress(address, port)))
			throw new Throwable("Address = " + address + ":" + port + ", not found");
		System.out.println("getNick(" + address + ") done");
		return nick_address.inverse().get(new InetSocketAddress(address, port));
	}
	
	@SuppressWarnings("null")
	public List<Message> openChat(String nick) {
		File chat;
		try {
			chat = new File("chats/" + nick_address.get(nick).getHostString() + ":" + nick_address.get(nick).getPort() + ".txt");
		} catch (NullPointerException e) { // non si conosce un nick per quell'indirizzo
			chat = new File("chats/" + nick + ".txt");
		}
		chat_now = new ArrayList<Message>();
		String line;
		Boolean type = null; //0 means received, 1 means sent
		Date date = null;
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
					date = new Date(new GregorianCalendar(Integer.parseInt(line.substring(8, 12)),
							Integer.parseInt(line.substring(5, 7)), Integer.parseInt(line.substring(2, 4)),
							Integer.parseInt(line.substring(13, 15)), Integer.parseInt(line.substring(16, 18)),
							Integer.parseInt(line.substring(19, 21))).getTimeInMillis()); // parses date from S[gg/mm/aaaa hh:mm:ss]
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
				e1.printStackTrace();
				//TODO				
			}
		}
		else {
			try {
				chat.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				//TODO
			}
		}
		System.out.println("openChat(" + nick + ") done"); 
		return chat_now;
	}
	
	public String[] getChats() {
		File path = new File("chats/");
		File[] chats = path.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) { // Criteri per non includere tra le chat i temporanei di linux che terminano con ~ o iniziano con #
				if (arg1.charAt(arg1.length() - 1) == '~' || arg1.charAt(0) == '#')
					return false;
				return true;
			}
		});
		String[] result = new String[chats.length];
		for(int i = 0; i < chats.length; ++i) {
			if (nick_address.containsValue(new InetSocketAddress(chats[i].getName(). // Se per quell'indirizzo c'è un nick mostro il nick
					substring(0, chats[i].getName().lastIndexOf(":")),               // all'utente
					Integer.parseInt(chats[i].getName().substring(chats[i].getName().lastIndexOf(":") + 1,
					chats[i].getName().lastIndexOf(".")))))) {
				result[i] = nick_address.inverse().get(new InetSocketAddress(chats[i].getName().
					substring(0, chats[i].getName().lastIndexOf(":")),   
					Integer.parseInt(chats[i].getName().substring(chats[i].getName().lastIndexOf(":") + 1, 
					chats[i].getName().lastIndexOf("."))))); // il "." è per togliere il ".txt"
			}
			else // il pulsante della chat conterrà indirizzo:porta visto che non c'è un nick associato
				result[i] = chats[i].getName().substring(0, chats[i].getName().lastIndexOf(".")); // il "." è per togliere il ".txt"
			System.out.println(result[i]);
		}
		System.out.println("getChats() done");
		return result;
	}
	
	public void send(String nick, String message) throws Throwable {
		if (!nick_address.containsKey(nick))
			throw new Throwable("Nickname \"" + nick + "\" not found");
		send(nick_address.get(nick).getHostString(), nick_address.get(nick).getPort(), message);
	}
	
	public void send(String address, int port, String message) throws IOException {//TODO add encrypt
		byte[] buff = message.getBytes(); // TODO PUOI AGGIUNGERE CODIFICA
		DatagramPacket packet = new DatagramPacket(buff, buff.length, new InetSocketAddress(address, port));
		server.getSocket().send(packet);		
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
		BufferedWriter chat_writer = new BufferedWriter(new FileWriter("chats/" + address + ":" + port + ".txt", true));          
		chat_writer.write("S[" + day + "/" + month + "/" + year + " " + hours + ":" + minutes + ":" + seconds + "]" + message + "\n");
		chat_writer.close();
		chat_now.add(new Message(new Date(now.getTimeInMillis()), message, true));
		System.out.println("send(" + address + ", " + port + ", " + message + ") done");
	}
	
	public void messageReceived(String message, String address, int port) throws IOException {//TODO add decrypt
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
		BufferedWriter chat_writer;
		chat_writer = new BufferedWriter(new FileWriter("chats/" + address + ":" + port + ".txt", true));
		chat_writer.write("R[" + day + "/" + month + "/" + year + " " + hours + ":" + minutes + ":" + seconds + "]" + message);
		chat_writer.close();
		chat_now.add(new Message(new Date(now.getTimeInMillis()), message, false));
		Notify();
		repaintChats();
		System.err.println("messageReceived(" + message + ", " + address + ", " + port + ")");
		//TODO
	}
	
	private void repaintChats() {
		//TODO repaint chat list adding highlighting the message received right now
		System.err.println("repaintChats()");
	}
	
	private void Notify() {
		//TODO
		System.err.println("Notify()");
	}
}
