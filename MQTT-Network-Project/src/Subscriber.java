import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Subscriber extends Thread
{
	/** Port of the server to connect **/
	private static int port = 9999;
	
	/** IP that connect to the server **/
	private static String ip;
	
	/** Server that connect **/
	private static Socket server;
	
	public Subscriber(String ip,int port)
	{
		this.port = port;
	}
	
	/**
	 * Check the command is correct or not.
	 * @param command String that want to validate
	 * @return The array of string that contains first array for
	 */
	private static String[] checkCommand(String command)
	{
		String split[] = command.split(" ");
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
	
	private static String[] getCommand()
	{
		Scanner myObj = new Scanner(System.in);;
		String command;
		String[] fields;
		/* Get command from user and validate the command */
	    do
	    {
	    	System.out.println("Please use command 'subscribe [ip] [topic]'");
	    	System.out.println("Ex: subscribe 127.0.0.1 /");
	    	command = myObj.nextLine();
			fields = checkCommand(command);
			if (fields == null)
				System.out.println("\nCommand not correct. Please try again");
	    } while(fields == null);
	    return fields;
	}
	
	public static void main(String[] args) throws IOException
	{
		String[] fields;
		do
		{
			/* Get command from user */
			fields = getCommand();
			ip = fields[1];

			try
			{
				server = new Socket(ip, port);
//				server.setSoTimeout(30000);
			}
			catch (IOException e)
			{
				System.out.println("Cannot connect the server");
				System.out.println("Please try again");
			}

			if(server != null)
			{
				System.out.println("Just connected to " + server.getRemoteSocketAddress());
			}
		} while(server == null);
		
		
		DataOutputStream out = new DataOutputStream(server.getOutputStream());
		 out.writeUTF(fields[0] + " " + fields[2]);
		while(server.isConnected())
		{
			DataInputStream in = new DataInputStream(server.getInputStream());
			String message = null;
			try
			{
				message = in.readUTF();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			System.out.println(message);
		}
		server.close();
		System.out.println("Bye bye");
	}
}
