import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Subscriber
{
	/** Port of the server to connect **/
	private static int port = 50000;
	
	/** IP of the server to connect **/
	private static String ip;
	
	/** socket that connect **/
	private static Socket socket;
		
	private static SubscriberThread subscriberT;
	public Subscriber(String ip, int port)
	{
		Subscriber.port = port;
	}
	
	/**
	 * Main Function
	 * @param args input argument
	 */
	public static void main(String[] args) throws IOException
	{
		String[] fields;
		/* Loop until can connect to the server */
		do
		{
			/* Get command from user */
			fields = getCommand();
			/* If command is exit, exit program */
			if(fields[0].equals("exit"))
			{
				System.out.println("Good bye");
				System.exit(0);
			}
			/* Set the IP of server */
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

			/* If connect to server, display result and initial the message buffer stream */
			if(socket != null)
			{
				System.out.println("\nJust connected to " + socket.getRemoteSocketAddress());
				subscriberT = new SubscriberThread(socket, fields);
			}
		} while(socket == null);		

		/* Start thread */
		subscriberT.start();
		
		/* Wait command exit from user */
		Scanner inputLine = new Scanner(System.in);
		String command;
		do
		{
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			System.out.println("Input 'exit' to disconnected from server and exit");
			command = inputLine.nextLine();
		}while(!command.equals("exit"));
		
		System.out.println("Exit program");
		inputLine.close();
		subscriberT.closeSocket();
		
		System.out.println("Bye bye");
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
		if(split.length != 3)
			return null;
		/* Check syntax */
		else if(!split[0].equals("subscribe"))
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

class SubscriberThread extends Thread
{
	/** socket that connect **/
	private static Socket socket;
	
	/** Input stream for sending data to server **/
	private static DataInputStream in;
	
	/** Output stream for receiving data from server **/
	private static DataOutputStream out;
	
	private static String topic;
	
	public SubscriberThread(Socket socket, String[] fields) throws IOException
	{
		SubscriberThread.socket = socket;
		SubscriberThread.topic = fields[2];
		initialMessage();
	}
	
	public void run()
	{
		/* Set time out to infinity */
		try
		{
			socket.setSoTimeout(0);
		}
		catch (SocketException e1)
		{
			e1.printStackTrace();
		}
		
		/* Send connect message to server */
		try
		{
			if(!writeMessage("subscribe " + topic))
			{
				System.out.println("\nExit program");
				endMessage();
				socket.close();
				System.exit(0);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		/* Loop get input from server and print */
		while(true)
		{
			String message = null;
			try
			{
				message = readMessage();
			} catch (IOException e)
			{
				break;
			}

			if(message != null)
				System.out.println(message);
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
	 * Set the message stream buffer
	 */
	public void initialMessage() throws IOException
	{
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());	
	}
	
	/**
	 * Close the message stream buffer
	 */
	public void endMessage() throws IOException
	{
		if(in != null)
			in.close();
		if(out != null)
			out.close();
	}
	
	public void closeSocket()
	{
		try
		{
			/* Write exit message to server and exit */
			writeMessage("exit");
			endMessage();
			System.out.println("Close socket");
			socket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}