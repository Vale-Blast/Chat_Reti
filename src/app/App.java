package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import net.ChatManager;
import net.Message;

public class App implements Runnable {
	
	private static App instance;

	private Size size;
	private ChatManager chat_manager;
	private String nicks[];
	private String chats[];
	private JPanel messages_panel;
	private JFrame frame;
	private JPanel chat_panel = new JPanel();
	private JButton send;
	private JButton attach;
	private String chat_now; // The nick I'm chatting now with
	
	public static App getInstance() {
		if (instance == null)
			instance = new App();
		return instance;
	}

	private App() {
		size = Size.getInstance();
		chat_manager = ChatManager.getInstance();
		chats = chat_manager.getChats();
		messages_panel = new JPanel(); //TODO Per ora è vuoto ma in futuro potremmo scrivere qualcosa tipo "apri la chat"
		messages_panel.setBounds(size.getChats().width, size.getToolbar().height, size.getMessages().width, size.getMessages().height);
		send = new JButton(new ImageIcon(new ImageIcon("resources/send.png")
						.getImage().getScaledInstance(size.getToolbar().height, size.getToolbar().height, Image.SCALE_FAST)));
		send.setEnabled(false); // finché non scelgo una chat lo lascio disabilitato
		attach = new JButton(new ImageIcon(new ImageIcon("resources/attach.png")
						.getImage().getScaledInstance(size.getToolbar().height, size.getToolbar().height, Image.SCALE_FAST)));
		attach.setEnabled(false); // finché non scelgo una chat lo lascio disabilitato
	}

	@Override
	public void run() {
		try {
			chat_manager.startServer();
		} catch (IOException e) {
				// TODO SEGNALA CHE SERVER NON PUÒ ESSERE APERTO E MAGARI APRI UN PANNELLO CON LE IMPOSTAZIONI
				e.printStackTrace();
		}
		create_gui();		
	}

	private void create_gui() {
		frame = new JFrame("Chat with your friends");
		frame.setLayout(null); // absolute values in pixels
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JToolBar toolbar = new JToolBar("Manage you chats", JToolBar.HORIZONTAL);
		toolbar.setFloatable(false); // Can't be detached 
		JButton new_chat = new JButton(new ImageIcon(new ImageIcon("resources/new_chat.png")
								.getImage().getScaledInstance(size.getToolbar().height, size.getToolbar().height, Image.SCALE_FAST)));
		new_chat.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFrame new_chat = new JFrame("New Chat");
				new_chat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				new_chat.setLayout(null);
				final JComboBox<String> nick_list = new JComboBox<String>(nicks);
				nick_list.setFont(nick_list.getFont().deriveFont((float) size.getNew_chat().height/7));
				JButton ok = new JButton("Ok");
				JButton cancel = new JButton("Cancel");
				ok.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						openChat((String) nick_list.getSelectedItem());
						ChatList();
						new_chat.dispose();
					}
				});
				cancel.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						new_chat.dispose();						
					}
				});
				new_chat.add(nick_list);
				new_chat.add(ok);
				ok.setFont(ok.getFont().deriveFont((float) size.getNew_chat().height/12));
				new_chat.add(cancel);
				cancel.setFont(cancel.getFont().deriveFont((float) size.getNew_chat().height/12));
				new_chat.setResizable(false);
				int padd = size.getNew_chat().width/15;
				new_chat.setBounds(size.getScreen_offset().width, size.getScreen_offset().height, size.getNew_chat().width, size.getNew_chat().height);
				nick_list.setBounds(size.getNew_chat().width/10, size.getNew_chat().height/10, size.getNew_chat().width*4/5, size.getNew_chat().height/4);
				ok.setBounds(size.getNew_chat().width/2 - size.getButton_inside().width - padd, size.getNew_chat().height*45/100, size.getButton_inside().width, size.getButton_inside().height);
				cancel.setBounds(size.getNew_chat().width/2 + padd, size.getNew_chat().height*45/100, size.getButton_inside().width, size.getButton_inside().height);
				new_chat.setVisible(true);

			}
		});
		JButton exit = new JButton(new ImageIcon(new ImageIcon("resources/exit.png")
								.getImage().getScaledInstance(size.getToolbar().height, size.getToolbar().height, Image.SCALE_FAST)));
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int reply = JOptionPane.showConfirmDialog(frame, "Do you really want to quit?",
						"Confirm quit", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION)
					System.exit(0);
			}
		});
		toolbar.add(new_chat);
		toolbar.add(exit); // leave last one in the toolbar
		chat_panel = new JPanel(new GridLayout(chats.length, 1));
		JScrollPane chat_list = new JScrollPane(chat_panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		ChatList();
		JPanel write_here_panel = new JPanel(null);
		final JTextArea text = new JTextArea();
		text.setLineWrap(true); // at the end of the line goes to newline
		text.setWrapStyleWord(true); // when going to the next line doesn't split word
		JScrollPane text_scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		text_scroll.setBounds(0, 0, size.getWrite().width - size.getToolbar().height, size.getWrite().height);
		text_scroll.setViewportView(text);
		send.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (text.getText().length() > 0) {//TODO volendo si potranno aggiungere dei controlli per i messaggi con gli spazi all'inizio o alla fine ecc
					try {
						chat_manager.send(chat_now, text.getText());
						openChat(chat_now);
						text.setText("");
						ChatList();
					} catch (Throwable e) {
						// TODO 
						e.printStackTrace();
					}
				}
				
			}
		});
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
	
	public void openChat(String nick) {
		chat_now = nick;
		List<Message> messages = chat_manager.openChat(nick);
		Iterator<Message> iterator = messages.iterator();
		JTextArea text_chat = new JTextArea();
		text_chat.setEditable(false);
		text_chat.setLineWrap(true);
		text_chat.setWrapStyleWord(true);
		String all_messages = "";
		SimpleDateFormat date_format = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]");
		while(iterator.hasNext()) {
			Message message = iterator.next();
			String sender;
			if (message.isType()) {
				sender = " You";
			} else {
				sender = " " + chat_now;
			}
			if (all_messages == "")
				all_messages = date_format.format(message.getDate()) + sender + " sent: " + message.getMessage();
			else
				all_messages = all_messages + "\n\n" + date_format.format(message.getDate()) + sender + " sent: " + message.getMessage();
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
	
	private void ChatList() {
		chats = chat_manager.getChats();
		chat_panel.removeAll();
		chat_panel.setLayout(new GridLayout(chats.length, 1));
		JButton chat[] = new JButton[chats.length];
		for (int i = 0; i < chats.length; ++i) {
			chat[i] = new JButton(chats[i]);
			final int j = i; // local copy because "final" doesn't allow me to increment i
			chat[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {	
					openChat(chats[j]);
				}
			});
			chat_panel.add(chat[i]);
		}
		chat_panel.validate();
		frame.validate();
		System.out.println("ChatList() done");
	}
}