package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import app.Encryption;


public class Server extends Thread implements Runnable {
	
	private static int port = 25023;
	private int buff_size = 256;
	private static Server instance;
	private DatagramSocket socket;
	private ChatManager chat_manager;
	private String myIP;
	private Encryption encryption;
	
	/********** getInstance() **********/
	/**
	   @return returns the instance of Server, following the design pattern of Singleton we will have just one Server object
	 */
	public static Server getInstance() throws UnknownHostException, IOException {
		if (instance == null)
			instance = new Server();
		return instance;
	}
	
	/********** Server() **********/
	/**
	   @brief It's just the constructor
	   @throws SocketException
	 */
	private Server() throws SocketException {
		socket = new DatagramSocket(port);
		chat_manager = ChatManager.getInstance();
		try {
			Enumeration<NetworkInterface> inters = NetworkInterface.getNetworkInterfaces();
			while (inters.hasMoreElements()) {
				Enumeration<InetAddress> adds = inters.nextElement().getInetAddresses();
				while(adds.hasMoreElements()) {
					String add = adds.nextElement().getHostAddress();
					if (add.indexOf(":") == -1 && add.indexOf("127.0.0") == -1) {
						myIP = add;
						System.out.println("Your IP: " + myIP);
					}
				}
			}
		} catch (SocketException e1) {
			System.err.println("Error while scanning interfaces, cannot find my IP");
			e1.printStackTrace();
		}
	}
	
	/********** run() **********/
	/**
	   @brief Starts the thread of the server, just initialises myIP and waits for messages
	 */
	@Override
	public void run() {
		byte[] buff;
		DatagramPacket packet;
		encryption = Encryption.getInstance();
		while (true) {
			buff = new byte[buff_size]; //TODO vedi note.txt 14)
			packet = new DatagramPacket(buff, buff.length);
			try {
				socket.receive(packet); // receive
				System.out.println(buff.length);
				String ip = packet.getAddress().getHostAddress();
				String message = new String(buff, 0, buff.length);
				if (message.startsWith("##")) { // It's a special message, they arent' encrypted
					System.out.println("Message \"" + message + "\" received from: " + ip);
					switch(message.substring(2, 6)) {
					case "NICK" : {// Nick messages
						String nick;
						if (message.lastIndexOf("#$#") == -1) {// #$# is present only when he answers to another nick message, this means he already knows my nick
							chat_manager.sendIP(ip, "##NICK:" + chat_manager.getMyNick() + "#$#");
							nick = message.substring(7, message.lastIndexOf("##"));
						} else
							nick = message.substring(7, message.lastIndexOf("#$#"));
						chat_manager.addNickAddress(nick, ip);
					} break;
					case "RCVD" : {// Ack messages
						String m = message.substring(11, message.lastIndexOf("##"));
						chat_manager.ack(m, ip);
					} break;
					case "DOWN" : {// Sender is shutting down
						chat_manager.remove(ip);
					} break;
					case "RESZ" : {// Sender is going to send me an attachment
						buff_size = Integer.parseInt(message.substring(7, message.lastIndexOf("##")));
					}
					}
				} else {
					try {
						message = encryption.decrypt(buff);
					} catch (Throwable e) {
						message = new String(buff, 0, buff.length); // attachments aren't encrypted
						System.out.println("Message was uncrypted");
					}
					if (!message.startsWith("#")) { // It means I recived an attachment
						chat_manager.attachReceived(buff, ip);
						buff_size = 256;
					}
					else { // Not an attachment 
						message = message.substring(1, message.lastIndexOf("#"));
						System.out.println("Messagio ricevuto: " + message);
						chat_manager.sendIP(ip, "##RCVD:" + message + "##"); // Sending ack to sender
						chat_manager.messageReceived(message, ip);
					}
				}
			} catch (IOException e) {
				System.err.println("Error while receiving");
				e.printStackTrace();
			}
		}
	}	
				
	public int getPort() {
		return port;
	}
	
	public String getMyIP() {
		return myIP;
	}

	public int getBuff_size() {
		return buff_size;
	}

	public void setBuff_size(int buff_size) {
		this.buff_size = buff_size;
	}
}
