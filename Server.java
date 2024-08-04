import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
*  Java Car Project Chat app - Chat App
*  @author Domagoj Kurf�rst
*/

public class Server implements Runnable 
{
	private int portNumber;
	private ServerSocket socket;
	private ArrayList<Socket> clients;
	private ArrayList<ClientThread> clientThreads;
	public ObservableList<String> serverLog;
	public ObservableList<String> clientNames;
   
	public Server(int portNumber) throws IOException 
   {
		this.portNumber = portNumber;
		serverLog = FXCollections.observableArrayList();
		clientNames = FXCollections.observableArrayList();
		clients = new ArrayList<Socket>();
		clientThreads = new ArrayList<ClientThread>();
		socket = new ServerSocket(portNumber);
	}

	public void startServer() 
   {
		try 
      {
			socket = new ServerSocket(CarProject.SERVER_PORT);
			serverLog = FXCollections.observableArrayList();
		} 
      catch (IOException e) 
      {
			e.printStackTrace();
		}
	}

	public void run() 
   {

		try 
      {
			while (true) 
         {
				Platform.runLater(new Runnable() 
            {
					@Override
					public void run() 
               {
						serverLog.add("Waiting for client(s) to join...");
					}
				});

				final Socket clientSocket = socket.accept();
				clients.add(clientSocket);
				Platform.runLater(new Runnable() 
            {
					@Override
					public void run() 
               {
						serverLog.add("Client " + clientSocket.getRemoteSocketAddress() + " connected");
					}
				});
				ClientThread clientThreadHolderClass = new ClientThread(clientSocket, this);
				Thread clientThread = new Thread(clientThreadHolderClass);
				clientThreads.add(clientThreadHolderClass);
				clientThread.setDaemon(true);
				clientThread.start();
				RunServer.threads.add(clientThread);
			}
		} 
      catch (IOException e) 
      {
			e.printStackTrace();
		}
	}
   
	public void clientDisconnected(ClientThread client) 
   {
		Platform.runLater(new Runnable() 
      {
			@Override
			public void run() 
         {
				serverLog.add("Client " + client.getClientSocket().getRemoteSocketAddress() + " disconnected");
				clients.remove(clientThreads.indexOf(client));
				clientNames.remove(clientThreads.indexOf(client));
				clientThreads.remove(clientThreads.indexOf(client));
			}
		});
	}

	public void writeToAllSockets(String input) 
   {
		for(ClientThread clientThread : clientThreads) 
      {
			clientThread.writeToServer(input);
		}
	}
}
