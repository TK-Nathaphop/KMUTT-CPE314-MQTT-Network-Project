import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Broker implementation. Can send all data from publisher to subscriber.
 * @author Group No.4
 *
 */
public class Broker extends Thread
{
	/** Port of server **/
	private static int port = 9999;
	
	/** Server **/
	private static ServerSocket server;
	
	/** All of the connected client **/
	private static ArrayList<Client> clientList = new ArrayList<Client>();
	
	/** Constructor for set the port **/
	public Broker(int port)
	{
		setPort(port);
	}
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	static public void main(String[] args) throws IOException
	{
		server = new ServerSocket(port);
//		server.setSoTimeout(30000);
		while(true)
		{
			try
			{ 
				System.out.println("Waiting for connection on port " + server.getLocalPort());
				
				/* The Client is connect to the server */
				Socket socket = server.accept();
				System.out.println("Have client connected");
				Client client = new Client(socket);
				clientList.add(client);
				
				/* Start thread */
				client.start();
			}
			catch (Exception e)
			{
				System.out.println(e.toString());
				try
				{
					server.close();
					System.out.println("Bye bye");
					break;
				}
				catch (IOException el)
				{
					el.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Set port of the server
	 * @param port port value
	 */
	public void setPort(int port)
	{
		this.port = port;
	}
	
	/**
	 * Send all client out.
	 * @return Array list of client
	 */
	public static ArrayList<Client> getAllClient()
	{
		return clientList;
	}
}


/**
 * Client thread after connect to the server
 * @author Group No.4
 *
 */
class Client extends Thread
{
	/** Socket connection from client **/
	private Socket socket;
	
	/** Input stream for sending data to client **/
	private DataInputStream input;
	
	/** Output stream for receiving data from client **/
	private DataOutputStream output;
	
	/** Path connection **/
	private String topic;
	
	private int id;
	

	/** Constructor for client 
	 * @throws IOException **/
	public Client(Socket socket) throws IOException
	{
		id = Broker.getAllClient().size();
		this.setSocket(socket);
	}

	/**
	 * Set the socket and get input stream and output stream from socket
	 * @param socket The socket connection from client
	 * @throws IOException
	 */
	private void setSocket(Socket socket) throws IOException
	{
		this.socket = socket;
		try
		{
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Check the command is correct or not.
	 * @param command String that want to validate
	 * @return The array of string that contains first array for
	 */
	private String[] checkCommand(String command)
	{
		String split[] = command.split(" ");
		if(split.length < 2)
			return null;
		if(split[0].equals("subscribe"))
			if(split.length != 2)
				split = null;
			
		else if(split[0].equals("publish"))
			if(split.length != 3)
				split = null;
		else
			split = null;
		
		/* Check the path is correct or not */
		if(split[1].charAt(0) != '/')
			split = null;
		return split;
	}
	
	/** Running thread. Get command and data from client, then check that
	 * either subscriber or publisher.
	 * If subscriber, loop receive get input.
	 * If publisher, just send data to all related subscriber.
	 */
	public void run()
	{
		/* Get the data command from client */
		String message = null;
		
		try
		{
			message = input.readUTF();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

		String fields[] = checkCommand(message);
		
		/* If get null from check command, means the command is wrong */
		if(fields == null)
		{
			System.out.println("Error from data sending");
			try
			{
				input.close();
				output.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		/* Keep the topic */
		topic = fields[1];
		
		/** If it is subscriber, loop and wait until exit */
		if(fields[0].equals("subscribe"))
		{
			subscriber();
		}
		
		/** If it is publisher, send the data to all subscriber in topic */
		else if(fields[0].equals("publish"))
		{
			publisher(fields);
		}
		System.out.println("Client is disconnected\n");
		try
		{
			input.close();
			output.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Continue loop and waiting publisher to publish the data
	 * until use "exit" command.
	 * @param fields
	 */
	public void subscriber()
	{
		System.out.println("Client is subscriber\n");
		while(socket.isConnected());
	}
	
	/**
	 * Check that topic is the same with current topic or not
	 * @param topic Topic that want to check
	 * @return True for the same topic and false for not the same
	 */
	public boolean checkTopic(String topic)
	{
		if(topic.equals(this.topic))
			return true;
		else
			return false;
	}
	
	/**
	 * Send the message to current subscriber
	 * @param message
	 */
	public void writeOutput(String message)
	{
		try
		{
			output.writeUTF(message);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Get input and send to all subscriber
	 * @param fields
	 */
	public void publisher(String fields[])
	{
		System.out.println("Client is publisher\n");
		ArrayList<Client> clientList = Broker.getAllClient();
		System.out.println("Message: "+ fields[2]);
		for (int i = 0; i < clientList.size(); i++)
			if(clientList.get(i).checkTopic(this.topic))
			{
				System.out.println("Write to subscriber id " + id +": " + fields[2]);
				clientList.get(i).writeOutput(fields[2]);
			}
	}
}
