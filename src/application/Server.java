package application;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

	public static Vector<ClientHandler> clients = new Vector<>();
	public static int numClients = 0;

	public static void main(String[] args) throws IOException {

		ServerSocket ss = new ServerSocket(5331);
		Socket socket;

		System.out.println("[INFO] Server has started.");

		while (true) {

			socket = ss.accept();

			System.out.println("[INFO] New user has joined.");

			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());

			ClientHandler client = new ClientHandler(socket, "client" + numClients, in, out);
			Thread thread = new Thread(client);
			System.out.println("[INFO] Adding user to clients list");

			synchronized(clients) {
				clients.add(client);
			}

			thread.start();

			numClients++;

		}

	}

}

class ClientHandler implements Runnable {

	private String name; 
	private final DataInputStream in; 
	private final DataOutputStream out; 
	private Socket socket; 
	private ClientHandler opponent;
	private DataOutputStream odos;
	private boolean isSearching;

	// constructor 
	public ClientHandler(Socket s, String name, 
			DataInputStream dis, DataOutputStream dos) { 
		out = dos; 
		in = dis;
		this.name = name; 
		socket = s;
		opponent = null;
		odos = null;
	}

	@Override
	public void run() {

		String origMessage;
		String[] received;

		try {
			
			//correspond with client to make sure name isn't a duplicate
			boolean duplicate;
			String trialname = "";
			do {
				received = in.readUTF().split("#");
				trialname = received[0];
				if (trialname.equals("EXIT")) {
					exit();
					return;
				}
				duplicate = false;
				synchronized(Server.clients) {
					for (ClientHandler client : Server.clients) {
						if (client.getName().equalsIgnoreCase(trialname)) {
							duplicate = true;
							break;
						}
					}
				}
				if (duplicate) {
					out.writeUTF("taken");
				}
				else {
					out.writeUTF("nottaken");
				}
			} while(duplicate);

			name = trialname;

			//expecting client to click find opponent or back button
			received = in.readUTF().split("#");
			if (!received[0].equals("FINDOPPONENT")) {
				exit();
				return;
			}
			isSearching = true;
			int index = 0;
			while (opponent == null) {
				ClientHandler ch = Server.clients.get(index);
				if (ch.isSearching && !ch.getName().equals(name)) {
					opponent = ch;
					odos = opponent.getDOS();
				}
				index++;
				if (index >= Server.clients.size())
					index = 0;
			}
			isSearching = false;
			out.writeUTF(opponent.getName());

			String num = in.readUTF();
			odos.writeUTF(num);
			

			//now begin to send moves
			//Message format: msg#xcor#ycor
			//If a move, msg =  MOVE
			while(true) {

				out.flush();

				origMessage = in.readUTF();
				received = origMessage.split("#");
				if (received[0].equals("EXIT")) {
					exit();
					return;
				}

				String recipientname = received[1];
				synchronized(Server.clients) {
					for (ClientHandler client : Server.clients) {
						if (client.getName().equals(recipientname)) {
							client.getDOS().writeUTF(origMessage);
							break;
						}
					}
				}

			}

		} catch (IOException e) {
			exit();
			return;
		}

	}

	public void exit() {
		try {
			System.out.println("[INFO] Player " + name + " has exited the game.");
			in.close();
			out.close();
			socket.close();
			Server.numClients--;
			synchronized(Server.clients) {
				Server.clients.remove(this);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public DataOutputStream getDOS() {
		return out;
	}

	public String getName() {
		return name;
	}

	public boolean isSearching() {

		return isSearching;

	}
}