import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import java.awt.*;
import java.awt.event.*;


public class SimpleChatClient {
	
	JTextArea incoming, userList, textArea;
	JTextField outgoing;
	JTextPane chaosPane;
	JComboBox rankCombo;
	JRadioButton d100Btn, d10Btn, d6Btn, dEditBtn;
	ButtonGroup bG;
	BufferedReader reader;
	PrintWriter writer;
	Socket sock;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	private JTextField logField;
	private JTextField textField;
	private JTextField dEditField;
	String userName;
	ArrayList loggedUsers;
	int chaosNumber;

	
	public static void main(String[] args) {
		SimpleChatClient client = new SimpleChatClient();
		client.go();
	}
	
	public void go() {
		loggedUsers = new ArrayList();
		
		JFrame frame = new JFrame("Simple BETA v0.02 GinTonic");
		
		JPanel topPanel = new JPanel();
		topPanel.setSize(1024, 600);
		
		
		JPanel leftPanel = new JPanel();
		
		JPanel mainPanel = new JPanel();
		incoming = new JTextArea(15, 50);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		DefaultCaret incCaret = (DefaultCaret)incoming.getCaret();
		incCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setBounds(10, 58, 423, 276);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		outgoing = new JTextField(20);
		outgoing.setBounds(10, 345, 310, 46);
		outgoing.addActionListener(new SendButtonListener());
		JButton sendButton = new JButton("Send");
		sendButton.setBounds(330, 345, 103, 46);
		sendButton.addActionListener(new SendButtonListener());
		mainPanel.setLayout(null);
		leftPanel.setLayout(null);
		leftPanel.add(qScroller);
		leftPanel.add(outgoing);
		leftPanel.add(sendButton);
		setUpNetworking();
		
		
		
		//Dice Choices
		d100Btn = new JRadioButton("d100");
		d100Btn.setSelected(true);
		d100Btn.setBounds(255, 398, 65, 23);
		leftPanel.add(d100Btn);
		
		d10Btn = new JRadioButton("d10");
		d10Btn.setBounds(188, 398, 65, 23);
		leftPanel.add(d10Btn);
		
		d6Btn = new JRadioButton("d6");
		d6Btn.setBounds(121, 398, 65, 23);
		leftPanel.add(d6Btn);
		
		dEditBtn = new JRadioButton("d");
		dEditBtn.setBounds(20, 398, 43, 23);
		leftPanel.add(dEditBtn);
		
		dEditField = new JTextField();
		dEditField.setBounds(58, 398, 46, 23);
		leftPanel.add(dEditField);
		dEditField.setColumns(10);
		
		bG = new ButtonGroup();
		bG.add(d100Btn);
		bG.add(d10Btn);
		bG.add(d6Btn);
		bG.add(dEditBtn);
			
		
		JButton btnNewButton = new JButton("Roll");
		btnNewButton.addActionListener(new DiceButtonListener());
		btnNewButton.setBounds(330, 398, 103, 23);
		leftPanel.add(btnNewButton);
		
		// Rank Selection
		rankCombo = new JComboBox();
		rankCombo.setBounds(198, 428, 103, 23);
		leftPanel.add(rankCombo);
		
		rankCombo.addItem("Low");
		rankCombo.addItem("Below Average");
		rankCombo.addItem("Average");
		rankCombo.addItem("Above Average");
		rankCombo.addItem("High");
		rankCombo.setSelectedIndex(2);
		
		chaosPane = new JTextPane();
		chaosPane.setEditable(false);
		chaosPane.setBounds(330, 430, 103, 103);
		leftPanel.add(chaosPane);
		
		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		textPane.setBounds(10, 11, 423, 36);
		leftPanel.add(textPane);
		
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		

		
		JTabbedPane tabbedPane = new JTabbedPane();
		topPanel.setLayout(new GridLayout(0, 2, 0, 0));
		topPanel.add(leftPanel);
		
		JButton btnEventFocus = new JButton("Event Focus");
		btnEventFocus.addActionListener(new EventFocusListener());
		btnEventFocus.setBounds(20, 462, 103, 23);
		leftPanel.add(btnEventFocus);
		
		JButton btnEventMeaning = new JButton("Event Meaning");
		btnEventMeaning.addActionListener(new EventMeaningListener());
		btnEventMeaning.setBounds(133, 462, 103, 23);
		leftPanel.add(btnEventMeaning);
		

		
		
		
		
		topPanel.add(tabbedPane);
		frame.getContentPane().add(topPanel);		
		tabbedPane.addTab("Main", null, mainPanel, null);
		
		
		
		
		textArea = new JTextArea();
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(20, 33, 230, 297);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(scrollPane);
		
		userList = new JTextArea();
		userList.setLineWrap(true);
		userList.setWrapStyleWord(true);
		userList.setEditable(false);
		
		JScrollPane userListPane = new JScrollPane(userList);
		userListPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		userListPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		userListPane.setBounds(20, 341, 230, 46);
		mainPanel.add(userListPane);
		

		
		logField = new JTextField();
		logField.setBounds(20, 398, 130, 20);
		mainPanel.add(logField);
		logField.setColumns(10);
		
		JButton btnLogin = new JButton("UserID");
		btnLogin.setBounds(160, 397, 90, 23);
		mainPanel.add(btnLogin);
		btnLogin.addActionListener(new LoginButtonListener());
		

		
		
		
		JPanel charPanel = new JPanel();
		tabbedPane.addTab("Character Sheet", null, charPanel, null);
		charPanel.setLayout(null);
		
		JScrollPane charScroll = new JScrollPane();
		charScroll.setBounds(10, 11, 479, 500);
		charPanel.add(charScroll);
		
		JTextArea charTextArea = new JTextArea();
		charScroll.setViewportView(charTextArea);
		
		JPanel locPanel = new JPanel();
		tabbedPane.addTab("Location Info", null, locPanel, null);
		locPanel.setLayout(null);
		
		JScrollPane locScroll = new JScrollPane();
		locScroll.setBounds(10, 11, 479, 512);
		locPanel.add(locScroll);
		
		JTextArea locTextArea = new JTextArea();
		locScroll.setViewportView(locTextArea);
		
		//NPC Tab
		JPanel npcPanel = new JPanel();
		tabbedPane.addTab("NPCs", null, npcPanel, null);
		npcPanel.setLayout(null);
		
		JScrollPane npcScroll = new JScrollPane();
		npcScroll.setBounds(10, 11, 479, 512);
		npcPanel.add(npcScroll);
		
		JTextArea npcTextArea = new JTextArea();
		npcScroll.setViewportView(npcTextArea);
		
		
		//Notes Tab
		JPanel notesPanel = new JPanel();		
		notesPanel.setLayout(new BorderLayout(0, 0));		
		JScrollPane scrollPane_2 = new JScrollPane();
		notesPanel.add(scrollPane_2);
		
		JTextArea textArea_1 = new JTextArea();
		textArea_1.setColumns(50);
		textArea_1.setLineWrap(true);
		scrollPane_2.setViewportView(textArea_1);
		tabbedPane.addTab("Notepad", null, notesPanel, null);
		
		
		
		
		
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		frame.setSize(1024,600);
		frame.setVisible(true);
	}
	
	private void setUpNetworking() {
		
		try {
			sock = new Socket("ec2-52-88-215-46.us-west-2.compute.amazonaws.com", 5050);
			//sock = new Socket("127.0.0.1", 5050);
			
			OutputStream os = sock.getOutputStream();
			oos = new ObjectOutputStream(os);
			InputStream is = sock.getInputStream();
			ois = new ObjectInputStream(new BufferedInputStream(is));
			
			
			/*InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(sock.getOutputStream());*/
			
			System.out.println("networking established");
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			
			try {
				
				if (!(outgoing.getText().trim().equals(""))) {
					oos.writeObject(    new MsgObj(0, new String(outgoing.getText()), userName)    );
					outgoing.setText("");					
			 	} else {
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			/*try {
				writer.println(outgoing.getText());
				writer.flush();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			outgoing.setText("");
			outgoing.requestFocus();*/
		}
	}
	
	public class IncomingReader implements Runnable {
		public void run() {
			Object obj;
			
			try {
			
				while((obj = ois.readObject()) != null ) {
					
					
					if (obj instanceof MsgObj)  {			// Messages
						incoming.append( ((MsgObj) obj).user + " : " + ((MsgObj) obj).id + "\n"  );
					} else if (obj instanceof DiceRoll) {	// Dice Rolls
						if ("100".equals(((DiceRoll) obj).id)){
							d100Roll((DiceRoll)obj);
						}
						
					} else if (obj instanceof LogName) {	// Log Name
						userList.append( ((LogName) obj).id  );
						userName = ((LogName) obj).id.trim();
						loggedUsers.add(((LogName) obj).id.trim());						
					} else if (obj instanceof NameChange) {	// Name Change
						incoming.append( "\n Server :: " + ((NameChange) obj).id + " has changed their name to " + ((NameChange) obj).user + "\n");
						String oldName = ((NameChange) obj).id;
						
						Iterator it = loggedUsers.iterator();
						userList.setText("");
						while(it.hasNext()) {
							String logName = (String) it.next();
							if ( logName.equals(oldName) ) {
								logName = ((NameChange) obj).user;
							}
							userList.append(logName + "\n");
						}
						
					} else if (obj instanceof ChaosNumObj) {// Chaos Number
						chaosNumber = ((ChaosNumObj) obj).a;
						incoming.append("\n Server :: Chaos Number is now " + chaosNumber + "\n");
						chaosPane.setText(Integer.toString(chaosNumber));
					}
					
					
					else if (obj instanceof testObject) {
						System.out.println(((testObject) obj).id);
						incoming.append(  ((testObject) obj).user + " : " + ((testObject) obj).id + "\n"   );
					}
					
					else {
						System.out.println(obj);
						incoming.append((String)obj + "\n");
					}
				}
			
			} catch (Exception e ) {
				e.printStackTrace();
			}
			
			/*
			String message;
			try {
				
				while ((message = reader.readLine()) != null ) {
					System.out.println("read " + message);
					incoming.append(message + "\n");
				}
			} catch (Exception ex) {ex.printStackTrace();} */
		}
	}
	
	public class DiceButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
		
			if (d100Btn.isSelected()) {
				int roll = (int) (Math.random() * 100) + 1;		
					try {				
						oos.writeObject(new DiceRoll(roll, "100", userName, rankCombo.getSelectedIndex() ));
					} catch (Exception e) {
					}
			} else if (d6Btn.isSelected()) {
				try {
					int roll = (int) (Math.random() * 6) + 1;
					oos.writeObject(userName + ": " + roll + " (d6)");
				} catch (Exception e) {
					
				}
			} else if (d10Btn.isSelected()) {
				try {
					int roll = (int) (Math.random() * 10) +1;
					oos.writeObject(userName + ": " + roll + " (d10)");
				} catch (Exception e) {
				}
			} else if (dEditBtn.isSelected()) {
				try {
					if ( !dEditField.getText().equals("")) {
						int roll = (int) (Math.random() * (Integer.parseInt(dEditField.getText())) + 1);
						oos.writeObject(userName + ": " + roll + " (d" + dEditField.getText() + ")");
					}
				} catch (Exception e) {}
			}
		}
	}
	
	public class LoginButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			String oldName = userName;
			userName = logField.getText();
			try {
				oos.writeObject( new NameChange(0, oldName, userName) );
			} catch (Exception e) {
				
			}
			logField.setText("");
		}
	}
	
	public class EventFocusListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			int dieRoll = (int) (Math.random() * 100);
			System.out.println(dieRoll);
			if (dieRoll < 8) {
				try {
					oos.writeObject(new String("Event Focus :: Remote Event \n"));
				} catch (Exception e) {}
			} else if (dieRoll < 23) {
				try {
					oos.writeObject(new String("Event Focus :: Character Action \n"));
				} catch (Exception e) {}				
			} else if (dieRoll < 31) {
				try {
					oos.writeObject(new String("Event Focus :: Introduce a new character \n"));
				} catch (Exception e) {}				
			} else if (dieRoll < 43) {
				try {
					oos.writeObject(new String("Event Focus :: Thread related event \n"));
				} catch (Exception e) {}				
			} else if (dieRoll < 48) {
				try {
					oos.writeObject(new String("Event Focus :: Close or open a thread \n"));
				} catch (Exception e) {}				
			} else if (dieRoll < 56) {
				try {
					oos.writeObject(new String("Event Focus :: Player character negative \n"));
				} catch (Exception e) {}				
			} else if (dieRoll < 64) {
				try {
					oos.writeObject(new String("Event Focus :: Player character positive \n"));
				} catch (Exception e) {}				
			} else if (dieRoll < 89) {
				try {
					oos.writeObject(new String("Event Focus :: Neutral Event \n"));
				} catch (Exception e) {}				
			} else if (dieRoll < 95) {
				try {
					oos.writeObject(new String("Event Focus :: NPC negative \n"));
				} catch (Exception e) {}				
			} else {
				try {
					oos.writeObject(new String("Event Focus :: NPC positive \n"));
				} catch (Exception e) {}				
			}   
		}
	}
	
	public class EventMeaningListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			int roll = (int) ((Math.random() * 200) + 1);
			try {
				oos.writeObject("Event Meaning ::  " + EventMeaning.eventMean[roll] + "\n");
			} catch (Exception e) { e.printStackTrace();}
		}
		
	}
	
	public void d100Roll(DiceRoll obj) {
		
		if (obj.a % 11 == 0 && obj.a < ((chaosNumber * 10) + 10)) {
			incoming.append("\n **Roll Random Event!** \n");
		}
		
		if (obj.rank == 2) {
			if (chaosNumber > 3 && chaosNumber < 7) {			// Average
				if (obj.a > 90) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 50) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 10) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} else if (chaosNumber > 6 && chaosNumber < 9) {	// Below Average
				if (obj.a > 94) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 65) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 13) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} else if (chaosNumber > 1 && chaosNumber < 4) {	// Above Average
				if (obj.a > 88) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 35) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 7) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} else if (chaosNumber < 8) {						// Low
				if (obj.a > 96) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 75) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 15) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} 
		} else if (obj.rank == 1) {  // Rank Below Average
			if (chaosNumber > 3 && chaosNumber < 7) {			// Average
				if (obj.a > 88) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 35) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 7) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} else if (chaosNumber > 6 && chaosNumber < 9) {	// Below Average
				if (obj.a > 91) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 50) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 10) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} else if (chaosNumber > 1 && chaosNumber < 4) {	// Above Average
				if (obj.a > 85) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 20) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 4) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} else if (chaosNumber < 8) {						// Low
				if (obj.a > 92) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 55) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 11) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} 
		} else if (obj.rank == 3) {  // Rank Above Average
			if (chaosNumber > 3 && chaosNumber < 7) {			// Average
				if (obj.a > 94) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 65) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 13) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} else if (chaosNumber > 6 && chaosNumber < 9) {	// Below Average
				if (obj.a > 97) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 80) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 16) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} else if (chaosNumber > 1 && chaosNumber < 4) {	// Above Average
				if (obj.a > 91) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 50) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 10) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} else if (chaosNumber < 8) {						// Low
				if (obj.a > 97) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional NO --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else if (obj.a > 85) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "No --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");				
				} else if (obj.a > 16) {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Yes --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				} else {
					incoming.append( ((DiceRoll) obj).user + " rolls d100 ---  " + "Exceptional YES --- (" + ((DiceRoll) obj).a + ", C: " +chaosNumber + ")\n");
				}
			} 
		}
		
		
	}
}

class testObject implements Serializable {
	/**
	 * 
	 */
	int a;
	String id, user;
	
	public testObject(int a, String str, String user) {
		this.a = a;
		id = str;
		this.user = user;
	}
}

class MsgObj extends testObject {
	public MsgObj(int a, String str, String user) {
		super(a, str, user);
	}
}

class DiceRoll extends testObject {
	int rank;
	public DiceRoll(int a, String str, String user, int rank) {
		super(a, str, user);
		this.rank = rank;
		
	}
}

class LogName extends testObject {
	public LogName(int a, String str, String user) {
		super(a, str, user);
	}
}

class NameChange extends testObject {
	public NameChange(int a, String str, String user) {
		super(a, str, user);
	}
}

class ChaosNumObj extends testObject {
	public ChaosNumObj(int a, String str, String user) {
		super(a, str, user);
	}
}

class EventMeaning {
	
static String[] eventMean = {"",
"1. Attainment of goals.",
"2. The founding of a fellowship.",
"3. Neglect of the environment.",
"4. Blight.",
"5. The beginning of an enterprise which may harm others.",
"6. Ecstasy to the point of divorce from reality.",
"7. Conquest by force.",
"8. Macho excess.",
"9. Willpower.",
"10. The recruitment of allies.",
"11. The triumph of an evil cause.",
"12. Physical and emotional violation.",
"13. Weakness in the face of opposition.",
"14. Force applied with deliberate malice.",
"15. A declaration of war.",
"16. Persecution of the innocent.",
"17. Love.",
"18. Abandonment of the spiritual.",
"19. Instant gratification.",
"20. Intellectual inquiry.",
"21. Antagonism towards new ideas.",
"22. Joy and laughter.",
"23. Written messages.",
"24. Movement.",
"25. Wasteful dispersal of energies.",
"26. Truce.",
"27. Balance disturbed.",
"28. Tension released.",
"29. Disloyalty.",
"30. Friendship.",
"31. Physical attraction.",
"32. Love for the wrong reasons.",
"33. Passion which interferes with judgment.",
"34. A physical challenge.",
"35. Desertion of a project.",
"36. Domination.",
"37. Procrastination.",
"38. Acclaim.",
"39. A journey which causes temporary separation.",
"40. Loss.",
"41. A matter concluded in plenty.",
"42. Healing.",
"43. Excessive devotion to the pleasures of the senses.",
"44. Swiftness in bringing a matter to its conclusion.",
"45. Delay in obtaining material possessions.",
"46. Delay.",
"47. Prosperity.",
"48. Material difficulties.",
"49. Cessation of benefits.",
"50. Temporary companionship.",
"51. Loss due to the machinations of another.",
"52. Lies made public.",
"53. Spite.",
"54. A situation does not live up to expectations.",
"55. Defeat.",
"56. Return of an old friend.",
"57. New alliances.",
"58. Imitation of reality.",
"59. Confusion in legal matters.",
"60. Bureaucracy.",
"61. Unfairness in a business matter.",
"62. Journey by water.",
"63. A path away from difficulties.",
"64. A temporary respite in struggle.",
"65. Stalemate.",
"66. Publicity.",
"67. Public recognition for one's efforts.",
"68. Good news.",
"69. Bad news.",
"70. Indefinite postponement by another of a project.",
"71. Cause for anxiety due to exterior factors.",
"72. Delay in achieving one's goal.",
"73. Theft.",
"74. A journey by land.",
"75. Good advice from an expert.",
"76. The exposure and consequent failure of a plot.",
"77. A project about to reach completion.",
"78. Intellectual competition.",
"79. Haggling.",
"80. Imprisonment.",
"81. Illness.",
"82. Release.",
"83. Opposition collapses.",
"84. A matter believed to be of great importance is actually of small consequence.",
"85. Loss of interest.",
"86. Celebration of a success.",
"87. Rapid development of an undertaking.",
"88. Travel by air.",
"89. Non-arrival of an expected communication.",
"90. Jealousy.",
"91. Dispute among par tners.",
"92. A project does not work out.",
"93. The possible loss of home.",
"94. An investment proves worthless.",
"95. Suffering.",
"96. Mental imprisonment.",
"97. Debasement.",
"98. Material desires are wholly fulfilled.",
"99. Overindulgence.",
"100. Wishes fall shor t.",
"101. Delaying tactics.",
"102. Stalemate leading to adjournment.",
"103. Adversity, but not insurmountable.",
"104. Gambling.",
"105. Lack of solidity.",
"106. Misfortune.",
"107. The death of a dream.",
"108. Disruption.",
"109. Temporary success.",
"110. Usurped power.",
"111. A balance is made, but it is temporary.",
"112. Failure of a partnership.",
"113. Possible loss of friendship.",
"114. Betrayal.",
"115. Abuse of power.",
"116. Becoming a burden to another.",
"117. Oppression of the few by the many.",
"118. Intrigues.",
"119. Resentment.",
"120. Fears realized.",
"121. A student.",
"122. Messages.",
"123. The bearer of bad news.",
"124. Fears proven unfounded.",
"125. A sentinel.",
"126. Inspection or scrutiny.",
"127. Ambush.",
"128. Spying.",
"129. Mutiny.",
"130. News.",
"131. Attachment to the point of obsession.",
"132. The affairs of the world.",
"133. Unexpected aid.",
"134. A bearer of intelligence.",
"135. Rumor.",
"136. Old wounds reopened.",
"137. Carelessness.",
"138. Friendship strained.",
"139. Guerrilla warfare.",
"140. Ruin.",
"141. Unwise extravagance.",
"142. Dirty tricks.",
"143. Arrival of a friend.",
"144. Propositions.",
"145. Fraud.",
"146. Rivalry.",
"147. A spiritual representative.",
"148. Triumph over adversities.",
"149. Travel by air.",
"150. Frustration.",
"151. Division.",
"152. The refusal to listen to views at variance to one's own.",
"153. Motherly figure.",
"154. Opulence.",
"155. Ill-natured gossip.",
"156. Mistrust of those near.",
"157. Liberty.",
"158. Deceit.",
"159. Cruelty from intolerance.",
"160. A person not to be trusted.",
"161. Excitement from activity.",
"162. Someone of assistance.",
"163. Father figure.",
"164. A dull individual.",
"165. Military.",
"166. A judge.",
"167. A wise counselor.",
"168. The mundane.",
"169. A teacher.",
"170. Trials overcome.",
"171. Frenzy.",
"172. Negligence.",
"173. Duality.",
"174. Passion.",
"175. Hard work.",
"176. The control of masses.",
"177. Alliance as a formality, not sincere.",
"178. Attraction to an object or person.",
"179. Travel by vehicle.",
"180. Success in an artistic or spiritual pursuit.",
"181. Vengeance.",
"182. An unethical victory.",
"183. Judicial proceedings.",
"184. Dispute.",
"185. Legal punishment.",
"186. Guidance from an elder.",
"187. A journey.",
"188. Good fortune.",
"189. Too much of a good thing.",
"190. The spiritual over the material.",
"191. The material over the spiritual.",
"192. Transformation and change.",
"193. Disunion.",
"194. Amassment of riches.",
"195. Overthrow of the existing order.",
"196. Communication by technological means.",
"197. Oppression.",
"198. Hope.",
"199. Hope deceived, daydreams fail.",
"200. Change of place." };
}