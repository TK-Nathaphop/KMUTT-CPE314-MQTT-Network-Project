import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Broker implementation. Can send all data from publisher to subscriber.
 * @author Group No.4
 *
 */
public class Broker
{
	/** Ip of server **/
	private static String ip = "localhost";
	
	/** Port of server **/
	private static int port = 50000;
	
	/** Server **/
	private static ServerSocket server;
	
	/** All of the connected BrokerThread **/
	private static ArrayList<BrokerThread> BrokerThreadList = new ArrayList<BrokerThread>();
	
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
		/* Ask IP to bind */
		while(true)
		{
			String ip = getIp();
			InetAddress address=InetAddress.getByName(ip);  
			int backlog = 30;
			try
			{
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
			try
			{ 
				System.out.println("Waiting for connection on port " + server.getLocalPort());
				
				/* The BrokerThread is connect to the server */
				Socket socket = server.accept();
				System.out.println("Have BrokerThread connected");
				
				BrokerThread BrokerThread = new BrokerThread(socket);
				BrokerThreadList.add(BrokerThread);
				
				/* Start thread */
				BrokerThread.start();
				Thread.sleep(500);
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
		Broker.port = port;
	}
	
	/**
	 * Send all BrokerThread out.
	 * @return Array list of BrokerThread
	 */
	public static ArrayList<BrokerThread> getAllBrokerThread()
	{
		return BrokerThreadList;
	}
	
	public static void removeBrokerThread(BrokerThread BrokerThread)
	{
		BrokerThreadList.remove(BrokerThread);
	}
	
	public static boolean checkIP(String ip)
	{
		try {
			if ( ip == null || ip.isEmpty() )
				return false;
			String[] parts = ip.split( "\\." );
			if ( parts.length != 4 )
				return false;
			for ( String s : parts )
			{
				int i = Integer.parseInt( s );
				if ( (i < 0) || (i > 255) )
					return false;
			}
			if ( ip.endsWith(".") )
				return false;
			return true;
			}
		 catch (NumberFormatException nfe)
		 {
			 return false;
		 }
	}
	
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
	    	if(!(ip.equals("localhost") || checkIP(ip)))
	    	{
	    		System.out.println("IP is not correct, please try again");
	    	}
	    } while(!(ip.equals("localhost") || checkIP(ip)));
	    return ip;
	}
}


/**
 * BrokerThread thread after connect to the server
 * @author Group No.4
 *
 */
class BrokerThread extends Thread
{
	/** Socket connection from BrokerThread **/
	private Socket socket;
	
	/** Input stream for sending data to BrokerThread **/
	private DataInputStream in;
	
	/** Output stream for receiving data from BrokerThread **/
	private DataOutputStream out;
	
	/** Path connection **/
	private String topic;
	
	/** Identifier it's subscriber or publisher */
	private boolean bSub;
	
	private int id;
	
	private static int counter = 0;
	

	/** Constructor for BrokerThread 
	 * @throws IOException **/
	public BrokerThread(Socket socket)
	{
		id = counter;
		counter++;
		this.socket = socket;
		try
		{
			initialMessage();
		}
		catch (IOException e)
		{
		}
	}

	/** Running thread. Get command and data from BrokerThread, then check that
	 * either subscriber or publisher.
	 * If subscriber, loop receive get input.
	 * If publisher, just send data to all related subscriber.
	 */
	public void run()
	{
		/* Get the data command from BrokerThread */
		String message = null;
		
		try
		{
			message = readMessage();
		}
		catch (IOException e1)
		{
		}

		String fields[] = checkCommand(message);
		
		/* If get null from check command, means the command is wrong */
		if(fields == null)
		{
			System.out.println("Error from data sending");
			try
			{
				endMessage();
				socket.close();
			}
			catch (IOException e)
			{
			}
			
		}
		
		/* Keep the topic */
		this.topic = fields[1];
		
		/** If it is subscriber, loop and wait until exit */
		if(fields[0].equals("subscribe"))
		{
			bSub = true;
			subscriber();
		}

		/** If it is publisher, send the data to all subscriber in topic */
		else if(fields[0].equals("publish"))
		{
			bSub = false;
			try
			{
				publisher(fields[2]);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		Broker.removeBrokerThread(this);
		System.out.println("BrokerThread is disconnected\n");
		try
		{
			endMessage();
			socket.close();
		}
		catch (IOException e)
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
	
	/**
	 * Continue loop and waiting publisher to publish the data
	 * until use "exit" command.
	 * @param fields
	 */
	public void subscriber()
	{
		String message = "";
		System.out.println("BrokerThread is subscriber\n");
		while(!message.equals("exit"))
		{
			try
			{
				message = readMessage();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Get input and send to all subscriber
	 * @param fields
	 * @throws IOException 
	 */
	public void publisher(String message) throws IOException
	{
		System.out.println("BrokerThread is publisher\n");
		ArrayList<BrokerThread> BrokerThreadList = Broker.getAllBrokerThread();
		
		/** Loop send message to all subscriber in topic **/
		for (int i = 0; i < BrokerThreadList.size(); i++)
		{
			BrokerThread BrokerThread = BrokerThreadList.get(i);
			if(BrokerThread.isSub() && BrokerThread.checkTopic(this.topic))
			{
				System.out.println("Write to subscriber id " + id +": " + message);
				BrokerThread.writeMessage(message);
			}
		}
		
		/** Wait for exit command from publisher **/
		String exit = readMessage();
		if(!exit.equals("exit"))
		{
			System.out.println("Error: Cannot get exit message from publisher");
			System.out.println("Error occur exit program");
			System.exit(0);
		}
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
	
	public boolean isSub()
	{
		return bSub;
	}
	
	/**
	 * Read message from server and send the acknowledge message back 
	 * @return Message that reading from server
	 * @throws IOException
	 */
	public String readMessage() throws IOException
	{
		String message = null;
		if(in != null)
			message = in.readUTF();
//		if(out != null)
//			out.writeUTF("[ACK] " + message);
		return message;
	}
	
	/**
	 * Write message to server and wait for acknowledge from server
	 * @param message
	 * @return Return true if can write successfully. Otherwise, false.
	 * @throws IOException
	 */
	public boolean writeMessage(String message) throws IOException
	{
		boolean ret = true;
		int count = 0;
		int limit = 5;
		if(out != null)
			out.writeUTF(message);
//		String ack;
//		if(in != null)
//		{
//			do
//			{
//				ack = in.readUTF();
//				if(!ack.equals("[ACK] " + message) && count < limit)
//				{
//					System.out.println("Cannot get [ACK] message from socket");
//					System.out.println("["+count +"]" + "Try sending again");
//					count++;
//				}
//				else if(count == limit)
//				{
//					System.out.println("Cannot get [ACK] message from socket");
//					System.out.println("Exceeding limit [" + limit + "]. Stop sending ");
//					ret = false;
//				}
//			}while (!ack.equals("[ACK] " + message));
//		}
		return ret;
	}
	
	/**
	 * Set the message stream buffer
	 */
	private void initialMessage() throws IOException
	{
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());	
	}
	
	/**
	 * Close the message stream buffer
	 */
	private void endMessage() throws IOException
	{
		if(in != null)
			in.close();
		if(out != null)
			out.close();
	}
}