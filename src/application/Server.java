package application;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

	public static Vector<ClientHandler> clients = new Vector<>();
	public static int numClients = 0;

	public static void main(String[] args) throws IOException {

		@SuppressWarnings("resource")
		ServerSocket ss = new ServerSocket(5337);
		Socket socket;

		System.out.println("[INFO] Server has started.");
		InetAddress ip = InetAddress.getLocalHost();
		System.out.println("[INFO] The IP Address is " + ip.getHostAddress());
		System.out.println("[INFO] Use the above IP Address for clients to connect.");

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
	private ClientHandler self = this;

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

	class OpponentFinder extends Thread {

		public void run() {
			isSearching = true;
			int index = 0;
			while (opponent == null) {
				ClientHandler ch = Server.clients.get(index);
				if (ch.isSearching && ch != self) {
					opponent = ch;
					odos = opponent.getDOS();
				}
				index++;
				if (index >= Server.clients.size())
					index = 0;
			}
			isSearching = false;
			try {
				out.writeUTF("FOUNDOPPONENT#" + opponent.getName());
				String num = in.readUTF();
				odos.writeUTF(num);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void run() {

		String origMessage;
		String[] received;
		String keyword;

		try {
			while(true) {
				out.flush();

				origMessage = in.readUTF();
				received = origMessage.split("#");
				keyword = received[0];

				if (keyword.equals("USERNAME")) {
					name = received[1];
				}
				else if (keyword.equals("FINDOPPONENT")) {
					OpponentFinder finder = new OpponentFinder();
					finder.start();
				}
				else if (keyword.equals("OPPOEXIT")) { //opponent already exited
					exit();
					return;
				}
				else if (keyword.equals("MOVE") || keyword.equals("PASS"))
					odos.writeUTF(origMessage);
				else if (keyword.equals("GAMEEND")) {
					odos.writeUTF(origMessage);
				}
				else if (keyword.equals("EXIT")) {
					if (odos != null)
						odos.writeUTF("OPPOEXIT");
					exit();
					return;
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