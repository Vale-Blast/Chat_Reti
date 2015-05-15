package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;


public class Server extends Thread implements Runnable {
	
	private static int port = 25023;
	private int buff_size = 256;
	private static Server instance;
	private DatagramSocket socket;
	private ChatManager chat_manager;
	private String myIP;
	
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
		while (true) {
			buff = new byte[buff_size];
			packet = new DatagramPacket(buff, buff.length);
			try {
				socket.receive(packet); // receive
				// add decrypt
				String message = new String(packet.getData(), 0, packet.getLength());
				String ip = packet.getAddress().getHostAddress();
				System.out.println("Message \"" + message + "\" received from: " + ip);
				if (message.indexOf("##NICK:") != -1) {
					if (message.lastIndexOf("#$#") == -1) { // #$# compare solo nella risposta a un nick, non nell'invio di un nick
						chat_manager.sendIP(ip, "##NICK:" + chat_manager.getMyNick() + "#$#");
						String nick = message.substring(7, message.lastIndexOf("##"));
						chat_manager.addNickAddress(nick, ip);
					}
					else {
						String nick = message.substring(7, message.lastIndexOf("#$#"));
						chat_manager.addNickAddress(nick, ip);
					}
				}
				else {
					System.out.println("Messagio ricevuto: " + message);
					chat_manager.messageReceived(message, ip);
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
}
