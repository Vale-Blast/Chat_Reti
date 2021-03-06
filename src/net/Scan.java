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
		private int sleep = 15;
		private int empty = 4;
		
		public int getSleep() {
			return sleep;
		}

		public void setSleep(int sleep) {
			this.sleep = sleep;
		}
		
		public int getEmpty() {
			return empty;
		}
		
		public void setEmpty(int empty) {
			this.empty = empty;
		}

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
			long c = 1;
			while (true) {
				try {
					scan();
					System.out.println("Scan going to sleep: " + sleep + " seconds");
					sleep(sleep * 1000);
					System.out.println("Scan woke up, scanning the net");
					if (c % empty == 0 && empty != 0)
						chat_manger.remove("ALL");
					c++;
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
			System.out.println("Scan() started");
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
						if (ip.indexOf("(") != -1) // Sometimes you find some addresses like "hotspot.internavigare.com (172.16.12.1)"
							ip = ip.substring(ip.indexOf("(") + 1, ip.lastIndexOf(")"));
						//System.out.println("IP: " + ip);
						hosts.add(ip);
					}
				}
				broadcast(hosts, "##NICK:" + chat_manger.getMyNick() + "##");	
				in.close();
				s.close();
			} catch (IOException e) {
				System.err.println("Error while scanning the net");
				e.printStackTrace();
			}
		System.out.println("scan() done");
		}
		
		/********** broadcast() **********/
		/**
		   @brief sends a message to all hosts on LAN
		   @param hosts is a list of hosts on the LAN
		   @param message is the message I want to send
		 */
		public void broadcast(List<String> hosts, String message) {
			Iterator<String> iter = hosts.iterator();
			while (iter.hasNext()) {
				String ip = iter.next();
				if (ip.indexOf(server.getMyIP()) == -1) {
					//System.out.println(ip);
					chat_manger.sendIP(ip, message);
				}
			}
		}
}