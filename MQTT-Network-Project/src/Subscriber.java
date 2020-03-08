import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * MQTT implementation, which implement in Subscriber role.
 * Get connection command from user then try to connect to the server
 * If can connect, create a thread and start thread to read message.
 * Otherwise, wait for command again.
 * @author Group No.4
 */
public class Subscriber
{
	/** Port of the server to connect **/
	private static int port = 50000;
	
	/** IP of the server to connect **/
	private static String ip;
	
	/** socket that connect **/
	private static Socket socket;
		
	/** Subscriber Thread for loop read message from server. **/
	private static SubscriberThread subscriberT;
	
	/**
	 * Main Function. Ask for command and try to connected.
	 * If be able to connect, start thread to read message.
	 * Then wait for exit command.
	 */
	public static void main(String[] args)
	{
		String[] fields;
		/* Loop until able to connect to the server */
		do
		{
			socket = null;
			
			/* Get Command */
			fields = getCommand();
			if(fields[0].equals("exit")) //Command is exit
			{
				System.out.println("Good bye");
				System.exit(0);
			}
			else
				ip = fields[1];
			
			/* Try connect to the server */
			try
			{
				socket = new Socket(ip, port);
				socket.setSoTimeout(3000);
			}
			catch (IOException e)
			{
				System.out.println("\nCannot connect the socket");
				System.out.println("Please try again");
			}
		} while(socket == null);		

		
		/** If connect to the server, start thread for looping get message from server **/
		System.out.println("\nJust connected to " + socket.getRemoteSocketAddress());
		subscriberT = new SubscriberThread(socket, fields[2]);
		subscriberT.start();
		
		/** Wait for exit command from user **/
		Scanner inputLine = new Scanner(System.in);
		String command;
		do
		{
			try
			{
				/* Waiting some log message */
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{}
			
			System.out.println("Type 'exit' to disconnected from server and exit");
			command = inputLine.nextLine();
		}while(!command.equals("exit"));
	
		/** Close connection **/
		inputLine.close();
		subscriberT.closeSocket();
		System.out.println("Exit program");
		System.out.println("Good bye");
	}

	/**
	 * Check the command is correct or not.
	 * @param command String that want to validate
	 * @return The array of string that contains first array for
	 */
	private static String[] checkCommand(String command)
	{
		if (command == null || command.isEmpty()) // Check null
			return null;

		String split[] = command.split(" ");
		if(command.equals("exit"))
			return split;
		else if(split.length != 3) //Check syntax is in format or not
			return null;
		else if(!split[0].equals("subscribe")) //Check first syntax
			return null;
		else if(!checkIP(split[1])) //Check IP
			return null;
		else if(split[2].charAt(0) != '/') //Check topic
			return null;
		return split;
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
			if ( parts.length != 4 )
				return false;
			for (String s : parts )
			{
				int i = Integer.parseInt( s );
				if ((i < 0) || (i > 255))
					return false;
			}
			if (ip.endsWith(".") )
				return false;
			return true;
			}
		 catch (NumberFormatException nfe)
		 {
			 return false;
		 }
	}
	
	/**
	 * Get command line from user and send to validate
	 * @return return the array of command in segments
	 */
	private static String[] getCommand()
	{
		Scanner inputLine = new Scanner(System.in);
		String command;
		String[] fields;
		/* Get command from user and validate the command */
	    do
	    {
	    	System.out.println("Please use command 'subscribe [ip] [topic]'");
	    	System.out.println("Ex: subscribe 127.0.0.1 /");
	    	command = inputLine.nextLine();
			fields = checkCommand(command);
			if (fields == null)
				System.out.println("\nCommand not correct. Please try again");
	    } while(fields == null);
	    return fields;
	}
}

/**
 * Subscriber Thread. Loop get message from server until error occurs.
 * @author Group No.4
 */
class SubscriberThread extends Thread
{
	/** socket that connect **/
	private static Socket socket;
	
	/** Input stream for sending data to server **/
	private static DataInputStream in;
	
	/** Output stream for receiving data from server **/
	private static DataOutputStream out;
	
	/** Topic to subscribe**/
	private static String topic;
	
	/**
	 * Set socket connection and topic
	 * @param socket Socket that connected to the server
	 * @param topic Topic that want to subscribe
	 */
	public SubscriberThread(Socket socket, String topic)
	{
		SubscriberThread.socket = socket;
		SubscriberThread.topic = topic;
	}
	
	/**
	 * Thread run function, loop get message from server and print that message.
	 */
	public void run()
	{
		/** Set buffer stream and send connected message to server **/
		initialSocket();
		writeMessage("subscribe " + topic);
		
		/** Set time out to infinity for continue waiting input **/
		try
		{
			socket.setSoTimeout(0);
		}
		catch (SocketException e1)
		{
			System.out.println("Error: Cannot set time out");
		}
		
		/** Loop get input from server and print **/
		while(true)
		{
			String message = null;
			try
			{
				message = readMessage();
			}
			catch (IOException e)
			{
				break;
			}
			if(message != null)
				System.out.println(message);
		}
	}
	
	/**
	 * Read message from input stream.
	 * @return Message that read from input stream.
	 * @throws IOException 
	 */
	public String readMessage() throws IOException
	{
		String message = null;
		if(in != null)
			message = in.readUTF();
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
				closeSocket();
			}
		}
	}

	/**
	 * Set the message stream buffer
	 */
	private void initialSocket()
	{
		try
		{
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		}
		catch (IOException e)
		{
			System.out.println("Error: Buffer stream error occured");
			closeSocket();
		}
		
	}

	/**
	 * Close buffer stream, and server.
	 */
	public void closeSocket()
	{
		try
		{
			writeMessage("exit");
			if(in != null)
				in.close();
			if(out != null)
				out.close();
			socket.close();
		}
		catch(Exception e)
		{}
	}
}