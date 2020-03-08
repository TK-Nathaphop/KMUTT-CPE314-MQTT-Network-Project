import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

/**
 * MQTT implementation, which implement in Publisher role.
 * Get command and try to connect.
 * If can connect, publish message and then close socket.
 * Else, Wait for command again.
 * @author Group 4C
 */
public class Publisher
{
	/** Port of the socket to connect **/
	private static int port = 50000;
	
	/** IP that connect to the socket **/
	private static String ip;
	
	/** Socket that connect **/
	private static Socket socket;
	
	/** Input stream for sending data to socket **/
	private static DataInputStream in;
	
	/** Output stream for receiving data from socket **/
	private static DataOutputStream out;
	
	/** Used for prevent error. Identifier for disconnect **/
	private static boolean bDis = false;
	
	/**
	 * Main Function. Loop ask for message and send message it to server.
	 */
	public static void main(String[] args)
	{
		String[] fields;
		while(true)
		{
			/** Loop until connect to the socket **/
			do
			{
				socket = null;
				
				/* Get Command */
				fields = getCommand();
				if(fields[0].equals("exit"))
				{
					System.out.println("Good bye");
					System.exit(0);
				}
				else
					ip = fields[1];
	
				/* Try connect to the server */
				SocketAddress socketAddr = new InetSocketAddress(ip, port);
				try
				{
					socket = new Socket();
					socket.connect(socketAddr,30*1000);
					socket.setSoTimeout(30000);
				}
				catch (Exception e)
				{
					System.out.println("\nCannot connect to the server");
				}
			} while(socket == null || !socket.isConnected());

			/** Set buffer stream and send connected message to server **/
			System.out.println("Just connected to " + socket.getRemoteSocketAddress());
			initialSocket();
			writeMessage("publish " + fields[2] + " " + fields[3]);

			/* Send exit message to server and disconnected */
			writeMessage("exit");
			if(bDis == false) // Do not have any error occurs before
			{
				System.out.println("The message has sent already\n");
				closeSocket();
			}
		}
	}
	
	/**
	 * Read message from input stream.
	 * @return Message that read from input stream.
	 */
	public static String readMessage()
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
				System.out.println("Error: Cannot read from server");
				closeSocket();
			}
		}
		return message;
	}
	
	/**
	 * Write message to output stream
	 * @param message Message that want to write into output stream
	 */
	public static void writeMessage(String message)
	{
		if(out != null)
		{
			try
			{
				out.writeUTF(message);
			}
			catch (IOException e)
			{
				System.out.println("Error: Cannot write '" + message + "' to server");
				closeSocket();
			}
		}
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
		else if(split.length < 3) //Check syntax is in format or not
			return null;
		else if(split[0] == null || !split[0].equals("publish")) //Check first syntax
			return null;
		else if(split[1] == null || !checkIP(split[1])) //Check IP
			return null;
		else if(split[2] == null || split[2].charAt(0) != '/') //Check topic
			return null;

		int init = split[0].length() + 1 + split[1].length() + 1 + split[2].length() + 1;
		String message = command.substring(init, command.length());
		if(message == null)
			return null;
		/** Set return value **/
		String ret[] = new String[4];
		ret[0] = split[0]; //Publish
		ret[1] = split[1]; //IP
		ret[2] = split[2]; //Topic
		ret[3] = message; //Message
		return ret;
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
			for ( String s : parts )
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
	    	System.out.println("Please use command 'publish [ip] [topic] [data]' or 'exit'");
	    	System.out.println("Ex: publish 127.0.0.1 / hello");
	    	System.out.print("> ");
	    	command = inputLine.nextLine();
			fields = checkCommand(command);
			if (fields == null)
				System.out.println("\nCommand not correct. Please try again");
	    } while(fields == null);
	    return fields;
	}
	
	/**
	 * Set the stream buffer and some initial value
	 */
	private static void initialSocket()
	{
		try
		{
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			
			/* Set to false to know that we have connect, and have not disconnected */
			bDis = false;
		}
		catch (IOException e)
		{
			System.out.println("Error: Buffer stream error occured");
			closeSocket();
		}
		
	}
	
	/**
	 * Close input and output stream and set some value
	 */
	private static void closeSocket()
	{
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
			catch(Exception e)
			{
			}
		}

		/* Set to true to know that have already close socket */
		bDis = true;
		
		/* Set to null for next socket connection */
		socket = null;
	}
}
