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
		nicks = chat_manager.getNicks();
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
		JButton new_nick = new JButton(new ImageIcon(new ImageIcon("resources/new_nick.png")
								.getImage().getScaledInstance(size.getToolbar().height, size.getToolbar().height, Image.SCALE_FAST)));
		new_nick.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final JFrame add_nick = new JFrame("Add a new Nickname");
				add_nick.setLayout(null);
				add_nick.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				JLabel name_label = new JLabel("Name: ");
				final JTextField name_field = new JTextField();
				JLabel address_label = new JLabel("IP Address: ");
				final JTextField address_field = new JTextField();
				JLabel port_label = new JLabel("Port: ");
				final JTextField port_field = new JTextField();
				JButton ok = new JButton("Ok");
				JButton cancel = new JButton("Cancel");
				ok.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (name_field.getText().length() == 0 || address_field.getText().length() == 0 || port_field.getText().length() == 0)
							JOptionPane.showMessageDialog(add_nick, "Name field, Address field or Port field empty",
									"EMPTY_FIELDS", JOptionPane.WARNING_MESSAGE); // error popup
						else {
							try {
								chat_manager.addNick(name_field.getText(), address_field.getText(), Integer.parseInt(port_field.getText()));
								nicks = chat_manager.getNicks();
								ChatList();
								add_nick.dispose();
							} catch (IOException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(add_nick, "IO Error while trying to add nickname",
										"IO_ERROR", JOptionPane.WARNING_MESSAGE);
							} catch (NumberFormatException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(add_nick, "Error on port number, please check it and try again",
										"NUMBER_ERROR", JOptionPane.WARNING_MESSAGE);
							} catch (Throwable e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(add_nick, e1.getMessage(),
										"NICK_ERROR", JOptionPane.WARNING_MESSAGE);
							}
						}
					}
				});
				cancel.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						add_nick.dispose();						
					}
				});
				add_nick.add(name_label);
				add_nick.add(name_field);
				add_nick.add(address_label);
				add_nick.add(address_field);
				add_nick.add(port_label);
				add_nick.add(port_field);
				add_nick.add(ok);
				add_nick.add(cancel);
				add_nick.setResizable(false);
				int vpadd = size.getAdd_nick().height/10;
				int hpadd = size.getNew_chat().width/10;
				add_nick.setBounds(size.getScreen_offset().width, size.getScreen_offset().height, size.getAdd_nick().width, size.getAdd_nick().height);
				name_label.setBounds(size.getAdd_nick().width/15, size.getAdd_nick().height/10, size.getAdd_nick().width/2, size.getAdd_nick().height/7);
				name_field.setBounds(size.getAdd_nick().width/2, size.getAdd_nick().height/10, size.getAdd_nick().width/2, size.getAdd_nick().height/7);
				add_nick.setVisible(true);
			}
		});
		JButton edit_nick = new JButton(new ImageIcon(new ImageIcon("resources/edit_nick.png")
								.getImage().getScaledInstance(size.getToolbar().height, size.getToolbar().height, Image.SCALE_FAST)));
		edit_nick.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFrame edit_nick = new JFrame("Edit Nick");
				edit_nick.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				edit_nick.setSize(400, 360);
				JPanel main_panel = new JPanel(new BorderLayout(10, 10));
				JPanel panel = new JPanel(new FlowLayout(50, 10, 10));
				JPanel ok_cancel = new JPanel(new FlowLayout(50, 10, 10));
				main_panel.setBorder(new EmptyBorder(10, 10, 10, 10));
				panel.setBorder(new EmptyBorder(10, 10, 10, 10));
				ok_cancel.setBorder(new EmptyBorder(10, 10, 10, 10));
				edit_nick.add(main_panel);
				final JComboBox<String> nick_list = new JComboBox<String>(nicks);
				JLabel name_label = new JLabel("Name: ");
				final JTextField name_field = new JTextField(23);
				JLabel address_label = new JLabel("IP Address: ");
				final JTextField address_field = new JTextField(23);
				JLabel port_label = new JLabel("Port: ");
				final JTextField port_field = new JTextField(23);
				JButton ok = new JButton("Ok");
				JButton cancel = new JButton("Cancel");
				JButton delete = new JButton("Delete");
				ok.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (name_field.getText().length() == 0 || address_field.getText().length() == 0 || port_field.getText().length() == 0)
							JOptionPane.showMessageDialog(edit_nick, "Name field, Address field or Port field empty",
									"EMPTY_FIELDS", JOptionPane.WARNING_MESSAGE); // error popup
						else {
							try {
								chat_manager.editNick((String) nick_list.getSelectedItem(), name_field.getText(),
										address_field.getText(), Integer.parseInt(port_field.getText()));
								nicks = chat_manager.getNicks();
								ChatList();
								edit_nick.dispose();
							} catch (NumberFormatException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(edit_nick, "Error on port number, please check it and try again",
										"NUMBER_ERROR", JOptionPane.WARNING_MESSAGE);
							} catch (Throwable e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(edit_nick, e1.getMessage(),
										"NICK_ERROR", JOptionPane.WARNING_MESSAGE);
							}
						}
					}
				});
				cancel.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						edit_nick.dispose();						
					}
				});
				nick_list.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						@SuppressWarnings("unchecked")
						JComboBox<String> cb = (JComboBox<String>) e.getSource();
						name_field.setText((String) cb.getSelectedItem());
						String addr_port = chat_manager.getAddress((String) cb.getSelectedItem());
						address_field.setText(addr_port.substring(0, addr_port.lastIndexOf(":")));
						port_field.setText(addr_port.substring(addr_port.lastIndexOf(":")+1));
					}
				});
				delete.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						int reply = JOptionPane.showConfirmDialog(edit_nick, "Do you really want to delete " +
								nick_list.getSelectedItem() + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
						if (reply == JOptionPane.YES_OPTION) {
							try {
								chat_manager.deleteNick((String) nick_list.getSelectedItem());
								ChatList();
							} catch (Throwable e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(edit_nick, e.getMessage(),
										"NICK_ERROR", JOptionPane.WARNING_MESSAGE);
							}
							nicks = chat_manager.getNicks();
							edit_nick.dispose();
						}	
					}
				});
				main_panel.add(nick_list, BorderLayout.NORTH);
				panel.add(name_label);
				panel.add(name_field);
				panel.add(address_label);
				panel.add(address_field);
				panel.add(port_label);
				panel.add(port_field);
				main_panel.add(panel, BorderLayout.CENTER);
				ok_cancel.add(ok);
				ok_cancel.add(cancel);
				ok_cancel.add(delete);
				main_panel.add(ok_cancel, BorderLayout.SOUTH);
				edit_nick.setResizable(false);
				edit_nick.setVisible(true);
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
		toolbar.add(new_nick);
		toolbar.add(edit_nick);
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