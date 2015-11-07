import java.io.*;
import java.net.*;
import java.util.*;

public class SimpleChatServer implements Serializable{

	ArrayList clientOutputStreams;
	ArrayList clientObjOutStreams;
	ArrayList loggedUsers;
	int chaosNumber;
	

	
	public class ClientHandler implements Runnable {
		BufferedReader reader;
		Socket sock;
		ObjectInputStream ois;
		
				
		
		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
				
				InputStream is = sock.getInputStream();
				ois = new ObjectInputStream(new BufferedInputStream(is));
				/*
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);*/
				
			} catch(Exception ex) {ex.printStackTrace();}
		}
		
		public void run() {
			
			Object obj;
			
			try {
				while((obj = ois.readObject()) != null) {
					
					if (obj instanceof NameChange) {
						String oldName = ((NameChange) obj).id;
						Iterator logIt = loggedUsers.iterator();
						
						while (logIt.hasNext()) {
							LogName logObj = (LogName) logIt.next();
							
							if (logObj.id.equals(oldName)) {
								logObj.id = ((NameChange) obj).user;
								break;
							}
						}
					}
					tellEveryone(obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			/*String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("read " + message);
					tellEveryone(message);
				}
			} catch (Exception ex) {ex.printStackTrace();}*/
		}
	}
	
	public static void main (String[] args) {
		new SimpleChatServer().go();
	}
	
	public void go() {
		clientOutputStreams = new ArrayList();
		clientObjOutStreams = new ArrayList();
		loggedUsers = new ArrayList();
		chaosNumber = 5;
		
		try {
			ServerSocket serverSock = new ServerSocket(5050);
			
			while (true) {
				Socket clientSocket = serverSock.accept();
				
				OutputStream os = clientSocket.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);
				clientObjOutStreams.add(oos);
				
				/*
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
				clientOutputStreams.add(writer);*/
				
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
				
				Iterator it = loggedUsers.iterator();
				while(it.hasNext()) {
					LogName lg = (LogName) it.next();
					tellCurrent(oos, lg);
				}
				LogName tlg = new LogName(0, "player" + (int)(Math.random()*10000) +"\n", "Server");
				loggedUsers.add(tlg);
				tellEveryone(tlg);
				tellCurrent(oos, new ChaosNumObj(chaosNumber, Integer.toString(chaosNumber), "Server"));
				System.out.println("got a connection");
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void tellEveryone(Object obj) {
		
		//Iterator it = clientOutputStreams.iterator();
		
		Iterator it = clientObjOutStreams.iterator();
		while(it.hasNext()) {
			try {
				
				ObjectOutputStream outStre = (ObjectOutputStream) it.next();
				
				if (obj instanceof MsgObj) {
					outStre.writeObject(obj);
				} else if (obj instanceof DiceRoll) {
					outStre.writeObject(obj);
				} else if (obj instanceof LogName) {
					outStre.writeObject(obj);
					outStre.writeObject(new String (  ((LogName) obj).id.trim() + " has logged in. \n"  ));
				}
				
				else {
					outStre.writeObject(obj);
				}
					
					
				//outStre.writeObject(obj);
				
					//outStre.flush();
				
				/*PrintWriter writer = (PrintWriter) it.next();
				writer.println(message);
				writer.flush();*/
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void tellCurrent(ObjectOutputStream out, Object obj) {
		try {
			out.writeObject(obj);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}

/*class testObject implements Serializable {
	int a;
	String id;
	
	public testObject(int a, String str) {
		this.a = a;
		id = str;
	}
}*/
