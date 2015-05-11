package app;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import net.ChatManager;
import net.Message;
import net.Scan;

public class App implements Runnable {
	
	private static App instance;

	private Size size;
	private Scan scan;
	private ChatManager chat_manager;
	private List<String> hosts;
	private JPanel messages_panel;
	private JFrame frame;
	private JPanel chat_panel = new JPanel();
	private JButton send;
	private JButton attach;
	private String chat_now; // The nick I'm chatting now with
	
	/********** getInstance() **********/
	/**
	   @return returns the instance of App, following the design pattern of Singleton we will have just one App object
	 */
	public static App getInstance() {
		if (instance == null)
			instance = new App();
		return instance;
	}

	/********** App() ***********/
	/**
	   @brief It's just the constructor of this class, it instantiates objects size and chat_manager 
	 */
	private App() {
		size = Size.getInstance();
		chat_manager = ChatManager.getInstance();
		messages_panel = new JPanel(); //TODO Per ora è vuoto ma in futuro potremmo scrivere qualcosa tipo "apri la chat"
		messages_panel.setBounds(size.getChats().width, size.getToolbar().height, size.getMessages().width, size.getMessages().height);
	}

	/********** run() ***********/
	/**
	   @brief Runs the application
	 */
	@Override
	public void run() {
		scan = Scan.getInstance();
		chat_manager.startServer();
		chat_manager.startScan();
		createGUI();		
	}
	
	public void setHosts(List<String> hs) {
		hosts = hs;
	}

	/********** createGUI() ***********/
	/**
	   @brief It just creates the GUI for the application
	 */
	private void createGUI() {
		frame = new JFrame("Chat with your friends");
		frame.setLayout(null); // absolute values in pixels
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JToolBar toolbar = new JToolBar("Manage you chats", JToolBar.HORIZONTAL);
		toolbar.setFloatable(false); // Can't be detached
		JButton refresh = new JButton(new ImageIcon(new ImageIcon("resources/refresh.png")
								.getImage().getScaledInstance(size.getToolbar().height - 12, size.getToolbar().height - 12, Image.SCALE_FAST)));
		refresh.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				scan.scan();
			}
		});
		JButton settings = new JButton(new ImageIcon(new ImageIcon("resources/settings.png")
								.getImage().getScaledInstance(size.getToolbar().height, size.getToolbar().height, Image.SCALE_FAST)));
		settings.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.err.println("Settings button clicked, NOT YET IMPLEMENTED");
			}
		});
		JButton exit = new JButton(new ImageIcon(new ImageIcon("resources/exit.png")
								.getImage().getScaledInstance(size.getToolbar().height - 8, size.getToolbar().height - 8, Image.SCALE_FAST)));
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int reply = JOptionPane.showConfirmDialog(frame, "Do you really want to quit?",
						"Confirm quit", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION)
					System.exit(0);
			}
		});
		toolbar.add(refresh);
		toolbar.add(settings);
		toolbar.add(exit); // leave last one in the toolbar
		JScrollPane chat_list = new JScrollPane(chat_panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		ChatList();
		JPanel write_here_panel = new JPanel(null);
		final JTextArea text = new JTextArea();
		text.setLineWrap(true); // at the end of the line goes to newline
		text.setWrapStyleWord(true); // when going to the next line doesn't split word
		JScrollPane text_scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		text_scroll.setBounds(0, 0, size.getWrite().width - size.getToolbar().height, size.getWrite().height);
		text_scroll.setViewportView(text);
		send = new JButton(new ImageIcon(new ImageIcon("resources/send.png")
		.getImage().getScaledInstance(size.getToolbar().height, size.getToolbar().height, Image.SCALE_FAST)));
		send.setEnabled(false); // disabled until user choses a chat
		send.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (text.getText().length() > 0) { // can't send empty messages
						chat_manager.send(chat_now, text.getText());
						openChat(chat_now);
						text.setText("");
						ChatList();
				}
				
			}
		});
		attach = new JButton(new ImageIcon(new ImageIcon("resources/attach.png")
		.getImage().getScaledInstance(size.getToolbar().height, size.getToolbar().height, Image.SCALE_FAST)));
		attach.setEnabled(false); // disabled until user choses a chat
		attach.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.err.println("attach called");
			}
		});
		JScrollPane chat_messages = new JScrollPane(messages_panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		frame.add(chat_messages);
		write_here_panel.add(send);
		write_here_panel.add(attach);
		write_here_panel.add(text_scroll);
		frame.add(toolbar);
		frame.add(write_here_panel);
		frame.add(chat_list);
		frame.setResizable(false);
		chat_messages.setBounds(size.getChats().width, size.getToolbar().height, size.getMessages().width, size.getMessages().height);
		attach.setBounds(size.getWrite().width - size.getToolbar().height, size.getToolbar().height, size.getToolbar().height, size.getToolbar().height);
		send.setBounds(size.getWrite().width - size.getToolbar().height, 0, size.getToolbar().height, size.getToolbar().height);
		write_here_panel.setBounds(size.getChats().width, size.getToolbar().height + size.getMessages().height, size.getWrite().width, size.getWrite().height);
		chat_list.setBounds(0, size.getToolbar().height, size.getChats().width, size.getChats().height);
		toolbar.setBounds(0, 0, size.getToolbar().width, size.getToolbar().height);
		frame.setBounds(size.getScreen_offset().width, size.getScreen_offset().height, size.getJframe().width, size.getJframe().height);
		frame.setVisible(true);	
	}
	
	/*********** openChat() **********/
	/**
	   @brief Opens a chat, called every time user clicks on a chat on the left, loads the chat from the file and shows previous messages on the right
	   @param nick is the person you want to chat with
	 */
	public void openChat(String nick) {
		chat_now = nick;
		List<Message> messages = chat_manager.openChat(nick);
		Iterator<Message> iterator = messages.iterator();
		JTextArea text_chat = new JTextArea();
		text_chat.setEditable(false);
		text_chat.setLineWrap(true);
		text_chat.setWrapStyleWord(true);
		String all_messages = "";
		while(iterator.hasNext()) {
			Message message = iterator.next();
			String sender;
			if (message.isType()) {
				sender = " You";
			} else {
				sender = " " + chat_now;
			}
			if (all_messages == "")
				all_messages = message.getDate() + sender + " sent: " + message.getMessage();
			else
				all_messages = all_messages + "\n\n" + message.getDate() + sender + " sent: " + message.getMessage();
		}
		text_chat.setText(all_messages);
		messages_panel.removeAll();
		messages_panel.add(text_chat);
		messages_panel.setBackground(Color.white);
		text_chat.setBounds(0, 0, size.getMessages().width*978/1000, size.getMessages().height);
		send.setEnabled(true);
		attach.setEnabled(true);
		chat_now = nick;
		frame.validate();
		System.out.println("App.openChat(" + nick + ") done");
	}
	
	/********** ChatList() **********/
	/**
	   @brief Organizes the list of chats on the left, uses all hosts in my LAN
	 */
	private void ChatList() {
		hosts = chat_manager.getHosts();
		chat_panel.removeAll();
		chat_panel.setLayout(new GridLayout(hosts.size(), 1));
		JButton chat[] = new JButton[hosts.size()];
		Iterator<String> iter = hosts.iterator();
		int i = 0;
		while (iter.hasNext()) {
			final String nick = iter.next();
			chat[i] = new JButton(nick);
			chat[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {	
					openChat(nick);
				}
			});
			chat_panel.add(chat[i]);
			++i;
		}
		chat_panel.validate();
		frame.validate();
		System.out.println("ChatList() done");
	}
}