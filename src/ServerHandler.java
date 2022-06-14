

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerHandler extends Thread {
	
	private Lock lock = new ReentrantLock();
	private static List<String> names = Collections.synchronizedList(new ArrayList<String>());
	private final Socket clientSocket;
	private final Server server;
	private String name;
	private Scanner in;
	private PrintWriter out;
	private OutputStream output;

	public ServerHandler(Server server, Socket clientSocket) {
		this.server =server;
		this.clientSocket= clientSocket;
	}

	public void run() {

		try {

			handleClientSocket();
			handleMessage();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private  void handleClientSocket() throws IOException {
		InputStream inputStream = clientSocket.getInputStream(); // multiplexing io, read data from the client 
		this.output = clientSocket.getOutputStream(); // direct communication with the client connect
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		in = new Scanner(clientSocket.getInputStream());
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		
		// this lock forces the thread to complete task before moving on
		try {
			lock.lock();
			out.println("SUBMIT ID NAME");

			String line;
			while((line = reader.readLine()) != null) {	
	
				// Keeps requesting a name until you get a unique one. it also sends an invalid message to tell the client fix username

				name = line;
				if (name == null) {

					return;
				}

				synchronized (names) {
					if (!name.isBlank() && !names.contains(name)) {
						names.add(name);
						break;
					}
					out.println(" INVALID ID. TRY AGAIN");
				}


			}
		}finally {
			lock.unlock();
		}

		out.println("NAMEACCEPTED " + name);

	}

	// This controls the message that is being broadcasted to all users
	private  void handleMessage() throws IOException {	
		try {
			

			List<ServerHandler> workerList = server.getWokerList();

			for(String name : names ) {

				String message2= "[ONLINE]" + name + "\n";
				send(message2);
			}
			// sends other online users current users status
			String message = "[ONLINE]" + name + "\n";
			for(ServerHandler worker: workerList) {
				if(!name.equals (worker.name)) {
					worker.send(message);
					//handleLogoff();

				}

			}

			while(true){
				String msg2 = in.nextLine();
				for(ServerHandler worker: workerList) { 

					String msg = "[MESSAGE] " + name + ": " + msg2 + "\n";
					if("logoff".equalsIgnoreCase(msg2)||"quit".equalsIgnoreCase(msg2)) {
						handleLogoff()	; 
					}
					worker.send(msg);}

				}
		
			} catch (Exception e) {
			System.out.println(e);
		
		}
	};


	// controls the message all user receive when one user logs out. It then removes the useer from the chat.
	private void handleLogoff() throws IOException {

		server.removeWorker(this);
		List<ServerHandler> workerList = server.getWokerList();
		//String msg2 = in.nextLine();
		for(ServerHandler worker: workerList) { 
			if(!name.equals (worker.name)) {
				String msg = "[USER] " + name + " LOGGED OFF " + "\n";

				worker.send(msg);
			}
		}

		clientSocket.close();
	}

	//sends message
	private void send(String message) throws IOException {

		if(name != null) {
			output.write(message.getBytes());
		}

	}
	

}
