

import java.io.IOException;

public class ChatDriver {
	
	private static final int SERVER_PORT = 4803;
	
	public static void main(String[] args) throws IOException {
		
		Server server = new Server (SERVER_PORT);
		server.start();
		
		}

}
