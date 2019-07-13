package server;

import java.io.IOException;

public class Server_Launcher {

	public static void main(String[] args) throws IOException {
		
		Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"java -jar Server.jar\"");
		
	}
	
}
