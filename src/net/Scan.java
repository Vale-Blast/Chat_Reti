package net;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Scan extends Thread implements Runnable {
	
		private static Scan instance;
		private Server server;
		private ChatManager chat_manger;
		
		/********** getInstance() **********/
		/**
		   @return returns the instance of Scan, following the design pattern of Singleton we will have just one Scan object
		 */
		public static Scan getInstance() {
			if (instance == null)
				instance = new Scan();
			return instance;
		}
		
		/********** Scan() **********/
		/**
		   @brief It's just the constructor of the class
		 */
		private Scan() {
			try {
				server = Server.getInstance();
			} catch (IOException e) {
				System.err.println("Error while starting the Server");
				e.printStackTrace();
			}
			chat_manger = ChatManager.getInstance();
		}
		
		/********** run() **********/
		/**
		   @brief Starts the net-scanner as a new thread, it scans the net periodically
		 */
		@Override
		public void run() {
			while (true) {
				try {
					scan(); //TODO OGNI TANTO DOBBIAMO SVUOTARE LA LISTA DI HOST PER EVITARE DI CONSERVARNE ALCUNI HOST NON RAGGIUNGIBILI O CHE SI SONO DISCONNESSI
					sleep(15 * 1000); 
				} catch (InterruptedException e) {
					System.out.println("Interrup network scanning");
				}
			}
		}
	
		/********** scan() **********/
		/**
		   @brief This method scans the LAN and sends my nickname to any host up
		 */
		public void scan() {
			String myIP = server.getMyIP();
			String net = myIP.substring(0, myIP.lastIndexOf(".")) + ".0/24";
			//System.out.println(net);
			try {
				Process process = Runtime.getRuntime().exec("nmap " + net);
				InputStream in=process.getInputStream();
				String line;
				List<String> hosts = new ArrayList<>();
				Scanner s = new Scanner(in);
				s.useDelimiter("\\n");
				while (s.hasNext()) {
					line = s.next();
					int index = line.indexOf("Nmap scan report for ");
					if (index != -1) {
						String ip = line.substring(index + 21);
						//System.out.println("IP: " + ip);
						hosts.add(ip);
					}
				}
				Iterator<String> iter = hosts.iterator();
				while (iter.hasNext()) {
					String ip = iter.next();
					if (ip.indexOf("(") != -1) // Sometimes you find some addresses (usually public hotspot) like "hotspot.internavigare.com (172.16.12.1)"
						ip = ip.substring(ip.indexOf("(") + 1, ip.indexOf(")"));
					//System.out.println("MyIP = " + server.getMyIP() + ", IP = " + ip);
					if (ip.indexOf(server.getMyIP()) == -1) 
						chat_manger.sendIP(ip, "##NICK:" + chat_manger.getMyNick() + "##");
				}	
				in.close();
				s.close();
			} catch (IOException e) {
				System.err.println("Error while scanning the net");
				e.printStackTrace();
			}
		System.out.println("scan() done");
		}
		
}
