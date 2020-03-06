import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Publisher
{
	/** Port of the socket to connect **/
	private static int port = 50000;
	
	/** IP that connect to the socket **/
	private static String ip;
	
	/** socket that connect **/
	private static Socket socket;
	
	/** Input stream for sending data to socket **/
	private static DataInputStream in;
	
	/** Output stream for receiving data from socket **/
	private static DataOutputStream out;
	
	public Publisher(String ip,int port)
	{
		Publisher.port = port;
	}
	
	/**
	 * Main Function
	 * @param args input argument
	 * @throws IOException Exception from socket
	 */
	public static void main(String[] args) throws IOException
	{
		String[] fields;
		while(true)
		{
			/* Loop until connect to the socket */
			do
			{
				/* Get command from user */
				fields = getCommand();
				if(fields[0].equals("exit"))
				{
					System.out.println("Good bye");
					System.exit(0);
				}
				else
					ip = fields[1];
	
				/* Try connect to socket */
				try
				{
					socket = new Socket(ip, port);
					socket.setSoTimeout(3000);
				}
				catch (IOException e)
				{
					System.out.println("Cannot connect the socket");
					System.out.println("Please try again");
				}
				
				/* If connect to socket, display result and initial the message buffer stream */
				if(socket != null)
				{
					System.out.println("Just connected to " + socket.getRemoteSocketAddress());
					initialMessage();
				}
			} while(socket == null);
			
			/* Send the message to server */
			writeMessage("publish " + fields[2] + " " + fields[3]);

			/* Send exit message to server and disconnected */
			writeMessage("exit");
			endMessage();
			socket.close();
			socket = null;
			
			System.out.println("The message has sent already\n");
		}
	}
	
	/**
	 * Read message from server and send the acknowledge message back 
	 * @return Message that reading from server
	 * @throws IOException
	 */
	public static String readMessage() throws IOException
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
	public static boolean writeMessage(String message) throws IOException
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
	 * Check the command is correct or not.
	 * @param command String that want to validate
	 * @return The array of string that contains first array for
	 */
	private static String[] checkCommand(String command)
	{
		String split[] = command.split(" ");
		if(command.equals("exit"))
			return split;
		if(split.length != 4)
			return null;
		/* Check syntax */
		else if(!split[0].equals("publish"))
			return null;
		/* Check that have ip or not */
		else if(split[1].length() == 0)
			return null;
		/* Check the path is correct or not */
		else if(split[2].charAt(0) != '/')
			return null;
		return split;
	}
	
	/**
	 * Get command line from user and send to validate
	 * @return return the array of command split in array
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
	    	command = inputLine.nextLine();
			fields = checkCommand(command);
			if (fields == null)
				System.out.println("\nCommand not correct. Please try again");
	    } while(fields == null);
	    return fields;
	}
	
	/**
	 * Set the message stream buffer
	 */
	private static void initialMessage() throws IOException
	{
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());	
	}
	
	/**
	 * Close the message stream buffer
	 */
	private static void endMessage() throws IOException
	{
		if(in != null)
			in.close();
		if(out != null)
			out.close();
	}
}
