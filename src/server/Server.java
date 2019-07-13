package server;
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

	private int[] bounds = new int[4];
	//min length, max length, min height, max height
	private boolean sbOnly; //whether square boards only
	private boolean noBlocked; //no blocked squares

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
			int[] oppobounds = null;
			while (!Thread.interrupted() && opponent == null) {
				ClientHandler ch = Server.clients.get(index);
				if (ch.isSearching && ch != self) {
					opponent = ch;
					oppobounds = opponent.getBounds();
					boolean[] oppoparams = opponent.getParams();
					if (bounds[1] < oppobounds[0] ||
							bounds[0] > oppobounds[1] ||
							bounds[3] < oppobounds[2] ||
							bounds[2] > oppobounds[3] ||
							sbOnly != oppoparams[0] ||
							noBlocked != oppoparams[1]) {
						opponent = null;
					}
					else 
						odos = opponent.getDOS();
				}
				index++;
				if (index >= Server.clients.size())
					index = 0;
			}
			try {
				if (opponent != null) {
					out.writeUTF("FOUNDOPPONENT#" + opponent.getName() + "#" + oppobounds[0] + "#" + oppobounds[1] + "#" + oppobounds[2] + "#" + oppobounds[3]);
				}
				Thread.sleep(10);
				isSearching = false;
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void run() {

		String origMessage;
		String[] received;
		String keyword;
		OpponentFinder finder = null;

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
					for (int i = 0; i < 4; i++)
						bounds[i] = Integer.parseInt(received[i+1]);
					if (received[5].equals("T"))
						sbOnly = true;
					else
						sbOnly = false;
					
					if (received[6].equals("T"))
						noBlocked = true;
					else
						noBlocked = false;
					
					finder = new OpponentFinder();
					finder.start();
				}
				else if (keyword.equals("CANCELFINDOPPONENT")) {
					isSearching = false;
					finder.interrupt();
				}
				else if (keyword.equals("CHALLENGERNUM")) {
					String num = received[1];
					odos.writeUTF(num);
				}
				else if (keyword.equals("OPPOEXIT") || keyword.equals("REMOVEOPPONENT")) { //opponent already exited
					opponent = null;
					odos = null;
				}
				else if (keyword.equals("MOVE") || 
						keyword.equals("PASS") || 
						keyword.equals("REMATCHREQUEST") ||
						keyword.equals("REMATCHACCEPT") ||
						keyword.equals("FINALDIMENSIONS"))
					odos.writeUTF(origMessage);
				else if (keyword.equals("REMATCHDECLINE")) {
					odos.writeUTF(origMessage);
					opponent = null;
					odos = null;
				}
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

	public int[] getBounds() {
		return bounds;
	}
	
	public boolean[] getParams() {
		boolean[] params = {sbOnly, noBlocked};
		return params;
	}
}