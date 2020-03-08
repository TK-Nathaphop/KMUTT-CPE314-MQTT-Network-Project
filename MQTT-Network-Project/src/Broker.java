import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * MQTT implementation, which implement in Broker role.
 * Get subscriber to wait for message
 * and let publisher to send message to all subscriber.
 * @author Group 4C
 */
public class Broker
{
	/** IP of server **/
	private static String ip = "localhost";
	
	/** Port of server **/
	private static int port = 50000;
	
	/** Server **/
	private static ServerSocket server;
	
	/** All of the connected BrokerThread **/
	private static ArrayList<BrokerThread> BrokerThreadList = new ArrayList<BrokerThread>();
	
	/**
	 * Main Function. Wait client to connect to the server and start thread.
	 * @param args
	 * @throws IOException
	 */
	static public void main(String[] args)
	{
		/* Ask IP to bind */
		while(true)
		{
			String ip = getIp();
			InetAddress address = null;
			int backlog = 30;

			try
			{
				address = InetAddress.getByName(ip);
				server = new ServerSocket(port, backlog, address);
				break;
			}
			catch(Exception e)
			{
				System.out.println("Cannot connect to the ip");
				System.out.println();
			}
		}

		/* Loop wait user to connect */
		while(true)
		{
			/* Wait for client connect to the server */
			System.out.println("Waiting for connection on port " + server.getLocalPort());			
			Socket socket = null;
			try
			{
				socket = server.accept();
			}
			catch(Exception e)
			{
				try
				{
					server.close();
				}
				catch (IOException e1)
				{
				}
				System.out.println("Good bye");
				System.exit(0);
			}
			System.out.println("Client is connected");
			
			/* Start thread server for multiple connection*/
			BrokerThread BrokerThread = new BrokerThread(socket);
			BrokerThreadList.add(BrokerThread);
			
			/* Start thread */
			BrokerThread.start();
			
			/* Waiting some log message before get next client */
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				System.out.println("Error: There are problem in thread");
			}
		}
	}
	
	/**
	 * Set port of the server
	 * @param port port value
	 */
	public void setPort(int port)
	{
		Broker.port = port;
	}
	
	/**
	 * Send all connected Client.
	 * @return Array list of BrokerThread
	 */
	public static ArrayList<BrokerThread> getAllBrokerThread()
	{
		return BrokerThreadList;
	}
	
	/**
	 * Remove disconnected Client.
	 * @param BrokerThread Client that want to delete.
	 */
	public static void removeBrokerThread(BrokerThread BrokerThread)
	{
		BrokerThreadList.remove(BrokerThread);
	}
	
	/**
	 * Validate IP function.
	 * References: https://stackoverflow.com/questions/4581877/validating-ipv4-string-in-java
	 * @param ip IP that want to validate
	 * @return Return true if correct, otherwise false.
	 */
	public static boolean checkIP(String ip)
	{
		try {
			if (ip == null || ip.isEmpty())
				return false;
			else if(ip.equals("localhost") || ip.equals("127.0.0.1"))
				return true;
			String[] parts = ip.split( "\\." );
			if (parts.length != 4)
				return false;
			for (String s : parts)
			{
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255))
					return false;
			}
			if (ip.endsWith("."))
				return false;
			return true;
			}
		 catch (NumberFormatException nfe)
		 {
			 return false;
		 }
	}
	
	/**
	 * Get IP from user and validate it.
	 * @return String of IP
	 */
	public static String getIp()
	{
		Scanner inputLine = new Scanner(System.in);
		String ip;
		/* Get command from user and validate the command */
	    do
	    {
	    	System.out.println("Input IP to bind: ");
	    	System.out.print("> ");
	    	ip = inputLine.nextLine();
	    	if(!checkIP(ip))
	    	{
	    		System.out.println("IP is not correct, please try again");
	    	}
	    } while(!checkIP(ip));
	    return ip;
	}
}


/**
 * Thread to run after client connect to the server.
 * Handle both subscriber and publisher that connect to the server.
 * @author Group 4C
 *
 */
class BrokerThread extends Thread
{
	/** Socket connection **/
	private Socket socket;
	
	/** Input stream for sending data **/
	private DataInputStream in;
	
	/** Output stream for receiving data **/
	private DataOutputStream out;
	
	/** Path connection **/
	private String topic;
	
	/** Identifier for subscriber or publisher */
	private boolean bSub;
	
	/** Id of client **/
	private int id;
	
	/** Counter for id **/
	private static int counter = 0;
	
	/** Used for prevent error. Identifier for disconnect **/
	private boolean bDis = false;
	

	/**
	 * Constructor of BrokerThread. Set socket and Id of instance.
	 * @param socket
	 */
	public BrokerThread(Socket socket)
	{
		id = counter;
		counter++;
		this.socket = socket;
	}

	/** Running thread. Get connected message from client,
	 * then validate message and check that whether subscriber or publisher.
	 * If subscriber, loop wait for input, until exit command exist.
	 * If publisher, send data to subscriber on topic.
	 */
	public void run()
	{
		initialSocket();

		/** Get connected message **/
		String message = null;
		message = readMessage();
		
		/** Validate the connected message **/
		String fields[] = checkCommand(message);
		if(fields == null) // Error occur
		{
			System.out.println("Error: Wrong connected message");
			endSocket();
		}
		else //Continue
		{
			this.topic = fields[1];
			
			/** If it is subscriber, loop and wait for 'exit' */
			if(fields[0].equals("subscribe"))
			{
				bSub = true;
				subscriber();
			}
	
			/** If it is publisher, send the data to all subscriber in topic */
			else if(fields[0].equals("publish"))
			{
				bSub = false;
				publisher(fields[2]);
			}
			/** Otherwise, wrong connection **/
			else
			{
				System.out.println("Error: Client id:" + id + " is neither subscriber or publisher");
			}

			System.out.println("Client id:"+ id + " is disconnected");
			endSocket();
		}
	}
	
	/**
	 * Check the connected message is correct or not.
	 * @param Connected message that want to validate
	 * @return Separated message in each type.
	 */
	private String[] checkCommand(String command)
	{
		if (command == null || command.isEmpty()) // Check null
			return null;
		
		String split[] = command.split(" ");
		String ret[] = null;
		if(split[1].charAt(0) != '/') // Check topic
			return null;

		if(split[0].equals("subscribe")) //Check subscriber
		{
			if(split.length != 2) //Don't have only 2 segments
				return null;
			else
				ret = split;
		}
		
		else if(split[0].equals("publish")) //Check publisher
		{
			if(split.length < 3) //Have less than 3 segments
				return null;
			else
			{
				/** Set return value for publish **/
				ret = new String[3];
				ret[0] = split[0]; //Publish
				ret[1] = split[1]; //Topic
				ret[2] = split[2]; //Message
				for(int i = 3; i < split.length; i++)
					ret[2] = ret[3] + " " + split[i]; //Concat the rest message
			}
		}
		return ret;
	}
	
	/**
	 * Continue loop and waiting publisher to publish the data,
	 * until client send the "exit" command.
	 * @param fields
	 */
	public void subscriber()
	{
		String message = "";
		System.out.println("Client id:" + id + " is subscriber\n");
		try
		{
			while(!message.equals("exit"))
			{
				message = readMessage();
			}	
		}
		catch(Exception e)
		{
			endSocket();
		}
	}
	
	
	/**
	 * Get input and send message to all subscriber in topic
	 * @param fields
	 * @throws IOException 
	 */
	public void publisher(String message)
	{
		System.out.println("Client id:" + id + " is publisher\n");
		System.out.println("Message: '"+ message +"'\n");
		
		/** Loop send message to all subscriber in topic **/
		ArrayList<BrokerThread> BrokerThreadList = Broker.getAllBrokerThread();
		for (int i = 0; i < BrokerThreadList.size(); i++)
		{
			BrokerThread BrokerThread = BrokerThreadList.get(i);
			
			if(BrokerThread.isSub() && BrokerThread.checkTopic(this.topic))
			{
				System.out.println("Write message to subscriber id:" + id);
				BrokerThread.writeMessage(message);
			}
		}
		System.out.println("");

		/** Wait for exit command from publisher **/
		String exit = readMessage();
		if(!exit.equals("exit"))
		{
			System.out.println("Error: Cannot get exit message from publisher");
			endSocket();
		}
	}
	
	/**
	 * Check that topic is same with current topic or not
	 * @param topic Topic that want to check
	 * @return If same topic, true. Otherwise, false.
	 */
	public boolean checkTopic(String topic)
	{
		if(topic.equals(this.topic))
			return true;
		else
			return false;
	}
	
	/**
	 * Check that current connection is subscriber or publisher.
	 * @return True for subscriber. Otherwise, False.
	 */
	public boolean isSub()
	{
		return bSub;
	}
	
	/**
	 * Read message from input stream.
	 * @return Message that read from input stream.
	 */
	public String readMessage()
	{
		String message = null;
		if(in != null)
		{
			try
			{
				message = in.readUTF();
			}
			catch (IOException e)
			{
				endSocket();
			}
		}
		return message;
	}
	
	/**
	 * Write message to output stream
	 * @param message Message that want to write into output stream
	 */
	public void writeMessage(String message)
	{
		if(out != null)
		{
			try
			{
				out.writeUTF(message);
			}
			catch (IOException e)
			{
				endSocket();
			}
		}
	}
	
	/**
	 * Initial socket and buffer stream.
	 * @param socket Connected socket that want to initial.
	 */
	private void initialSocket()
	{
		try
		{
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			bDis = false;
		}
		catch (IOException e)
		{
			endSocket();
		}
			
	}
	
	/**
	 * Close the buffer stream and socket. Then remove client from list.
	 */
	private void endSocket()
	{
		/* If haven't close socket/buffer stream, close it */
		if(!bDis)
		{
			try
			{
				if(in != null)
					in.close();
				if(out != null)
					out.close();
				socket.close();
			}
			catch(IOException e)
			{
			}
			
			/* After close socket, remove client from list */
			Broker.removeBrokerThread(this);
		}
		
		/* Set to true to know that have already close socket */
		bDis = true;
	}
}