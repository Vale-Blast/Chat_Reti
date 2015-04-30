package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Server extends Thread implements Runnable {
	
	private static int port = 25023;
	private int buff_size = 256;
	private static Server instance;
	private DatagramSocket socket;
	private ChatManager chat_manager;
	
	public static Server getInstance() throws UnknownHostException, IOException {
		if (instance == null)
			instance = new Server();
		return instance;
	}
	
	private Server() throws SocketException {
		socket = new DatagramSocket(port);
		chat_manager = ChatManager.getInstance();
	}
	
	@Override
	public void run() {
		byte[] buff;
		DatagramPacket packet;
		while (true) {
			 buff = new byte[buff_size];
			 packet = new DatagramPacket(buff, buff.length);
			try {
				socket.receive(packet); // receive
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Messagio ricevuto: " + packet.getData());
			int port = 0; //TODO controlla dove prendere la porta
			try {
				chat_manager.messageReceived(new String(packet.getData(), 0, packet.getLength()), packet.getSocketAddress().toString(), port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
//packet = new DatagramPacket(buf, buf.length, socketaddress);
//socket.send(packet);
//socket.close();
	
	public DatagramSocket getSocket() {
		return socket;
	}
}
