package app;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import net.ChatManager;
import net.Message;
import net.Scan;

public class App implements Runnable {
	
	private static App instance;

	private Size size;
	private Scan scan;
	private ChatManager chat_manager;
	private HashSet<String> hosts;
	public HashSet<String> senders; // a Set of nicks who sent me an unread message
	private JPanel messages_panel;
	private JFrame frame;
	private JPanel chat_panel = new JPanel();
	private JScrollPane chat_list;
	private JButton send;
	private JButton attach;
	private String chat_now = ""; // The nick I'm chatting now with
	
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
		senders = new HashSet<>();
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
				final JFrame settings = new JFrame("Settings");
				settings.setLayout(null);
				int hOffset = size.getSettings().width/10;
				int padd = size.getSettings().width/15;
				JLabel tts_label = new JLabel("Time to Sleep between net scans:");
				int vpadd = size.getSettings().height/20;
				tts_label.setBounds(hOffset, vpadd, size.getSettings().width*4/5, size.getSettings().height/10);
				final JTextField tts_field = new JTextField("" + scan.getSleep());
				tts_field.setBounds(hOffset, vpadd*3/2 + size.getSettings().height/10, size.getSettings().width*4/5, size.getSettings().height/10);
				JLabel empty_label = new JLabel("Number of iterations to empty hosts list, insert 0 to disable:");
				empty_label.setBounds(hOffset, vpadd*5/2 + size.getSettings().height/5, size.getSettings().width*4/5, size.getSettings().height/10);
				final JTextField empty_field = new JTextField("" + scan.getEmpty());
				empty_field.setBounds(hOffset, vpadd*3 + size.getSettings().height*3/10, size.getSettings().width*4/5, size.getSettings().height/10);
				JLabel music_label = new JLabel("Notification sound:");
				music_label.setBounds(hOffset, vpadd*4 + size.getSettings().height*2/5, size.getSettings().width*4/5, size.getSettings().height/10);
				final JLabel music_file = new JLabel(chat_manager.getSound());
				music_file.setBounds(hOffset, vpadd*9/2 + size.getSettings().height/2, size.getSettings().width*4/5, size.getSettings().height/10);
				JButton music = new JButton(new ImageIcon(new ImageIcon("resources/folder.png")
								.getImage().getScaledInstance(size.getToolbar().height/2, size.getToolbar().height/2, Image.SCALE_FAST)));
				music.setBounds(size.getSettings().width - size.getToolbar().height/2 - hOffset, vpadd*9/2 + size.getSettings().height/2, size.getToolbar().height/2, size.getToolbar().height/2);
				final JButton play = new JButton(new ImageIcon(new ImageIcon("resources/music.png")
								.getImage().getScaledInstance(size.getToolbar().height/2, size.getToolbar().height/2, Image.SCALE_FAST)));
				music.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						JFileChooser jfc = new JFileChooser();
						int res = jfc.showOpenDialog(settings);
						if (res == 0) {
							//System.out.println(jfc.getSelectedFile().getAbsolutePath());
							music_file.setText(jfc.getSelectedFile().getAbsolutePath());
							play.setEnabled(true);
						}
					}
				});
				play.setBounds(size.getSettings().width - hOffset + padd/2, vpadd*9/2 + size.getSettings().height/2, size.getToolbar().height/2, size.getToolbar().height/2);
				play.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						chat_manager.play();						
					}
				});
				JButton ok = new JButton("Ok");
				JButton cancel = new JButton("Cancel");
				ok.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (tts_field.getText().length() > 0 && empty_field.getText().length() > 0) {
							if (!chat_manager.setSound(music_file.getText())) {
								JOptionPane.showMessageDialog(settings, "Wrong Sound file, please check it and try again, maybe you should use .wav" ,"Bad Sound File", JOptionPane.ERROR_MESSAGE);
								play.setEnabled(false);
							} else {
								scan.setSleep(Integer.parseInt(tts_field.getText()));
								scan.setEmpty(Integer.parseInt(empty_field.getText()));
								BufferedWriter sett_writer;
								try {
									sett_writer = new BufferedWriter(new FileWriter(".chat", false));
									sett_writer.write("NICK: " + chat_manager.getMyNick() + "\n");
									sett_writer.write("TToS: " + scan.getSleep() + "\n");
									sett_writer.write("EMPT: " + scan.getEmpty() + "\n");
									sett_writer.write("DING: " + chat_manager.getSound() + "\n");
									sett_writer.close();
								} catch (IOException e1) {
									System.err.println("Error while saving settings to file");
									e1.printStackTrace();
								}
								settings.dispose();
							}
						}
					}
				});
				cancel.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						settings.dispose();						
					}
				});
				settings.add(tts_label);
				settings.add(tts_field);
				settings.add(empty_label);
				settings.add(empty_field);
				settings.add(music_label);
				settings.add(music_file);
				settings.add(music);
				settings.add(play);
				settings.add(ok);
				settings.add(cancel);
				settings.setResizable(false);
				settings.setBounds(size.getScreen_offset().width, size.getScreen_offset().height, size.getSettings().width, (int) (size.getSettings().height*1.3));
				ok.setBounds(size.getSettings().width/2 - size.getButton_inside().width - padd, settings.getBounds().height - hOffset - size.getButton_inside().height, size.getButton_inside().width, size.getButton_inside().height);
				cancel.setBounds(size.getSettings().width/2 + padd, settings.getBounds().height - hOffset - size.getButton_inside().height, size.getButton_inside().width, size.getButton_inside().height);
				settings.setVisible(true);
			}
		});
		JButton exit = new JButton(new ImageIcon(new ImageIcon("resources/exit.png")
								.getImage().getScaledInstance(size.getToolbar().height - 8, size.getToolbar().height - 8, Image.SCALE_FAST)));
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		toolbar.add(refresh);
		toolbar.add(settings);
		toolbar.add(exit); // leave last one in the toolbar
		chat_list = new JScrollPane(chat_panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
				JFileChooser jfc = new JFileChooser();
				int res = jfc.showOpenDialog(frame);
				if (res == 0) {
					if (jfc.getSelectedFile().length() > 10 * 1024 * 1024)
						JOptionPane.showMessageDialog(frame, "File too big, max 10 MB", "File too big", JOptionPane.ERROR_MESSAGE);
					else {
						System.out.println(jfc.getSelectedFile().getAbsolutePath());
						try {
							chat_manager.attach(jfc.getSelectedFile(), chat_now);
							openChat(chat_now);
							ChatList();
						} catch (FileNotFoundException e1) {
							JOptionPane.showMessageDialog(frame, "File not Found, please check it and try again" ,"File not found", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
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
		senders.remove(nick);
		ChatList();
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
	public void ChatList() {
		hosts = chat_manager.getHosts();
		frame.remove(chat_list);
		chat_panel.removeAll();
		chat_panel = new JPanel();
		chat_list = new JScrollPane(chat_panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		chat_panel.setLayout(new GridLayout(hosts.size(), 1));
		JButton chat[] = new JButton[hosts.size()];
		Iterator<String> iter = hosts.iterator();
		int i = 0;
		while (iter.hasNext()) {
			final String nick = iter.next();
			System.out.println("Host: " + nick);
			chat[i] = new JButton(nick);
			if (senders.contains(nick)) {
				if (nick == chat_now)
					openChat(nick);
				else
					chat[i].setBackground(Color.red); // Red color if this nick sent me unread messages but I'm not talking with him
			}
			System.out.println("Clicked");
			chat[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					System.out.println("Clicked");
					openChat(nick);
				}
			});
			chat_panel.add(chat[i]);
			chat[i].validate();
			++i;
		}
		chat_panel.validate();
		frame.add(chat_list);
		chat_list.setBounds(0, size.getToolbar().height, size.getChats().width, size.getChats().height);
		frame.validate();
		System.out.println("ChatList() done");
	}

	/********** exit() **********/
	/**
	   @brief This method asks confirm yes/no, if you want to exit he broadcasts all hosts he's going to
	          shut down
	 */
	private void exit() {
		int reply = JOptionPane.showConfirmDialog(frame, "Do you really want to quit?",
				"Confirm quit", JOptionPane.YES_NO_OPTION);
		if (reply == JOptionPane.YES_OPTION) {
			Iterator<String> iter = hosts.iterator();
			List<String> everyone = new ArrayList<String>();
			while (iter.hasNext()) {
				everyone.add(chat_manager.getAddress(iter.next()));				
			}
			scan.broadcast(everyone, "##DOWN##");
			System.exit(0);
		}
	}
}