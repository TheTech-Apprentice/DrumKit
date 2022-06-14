

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread{

	private final  int serverPort;

	private ArrayList<ServerHandler> List = new ArrayList<>();
	//private ExecutorService pool = Executors.newFixedThreadPool(5);


	public Server( int serverPort) {

		this.serverPort = serverPort;

	}
	// allows server worker to access server handler methods
	public ArrayList<ServerHandler> getWokerList(){
		return List;
	}


	public void run()  {

		try {
			ServerSocket serverSocket = new ServerSocket( serverPort);
			while(true) {


				System.out.println("[SERVER] Waiting for client connection... ");

				Socket clientSocket = serverSocket.accept();

				System.out.println("[SERVER] Connected to client");

				ServerHandler handler =new ServerHandler(this, clientSocket);

				List.add(handler);
				//pool.execute(handler);
				handler.start();


			}


		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void removeWorker(ServerHandler serverHandler) {

		List.remove(serverHandler);
		// TODO Auto-generated method stub

	}
	
}
