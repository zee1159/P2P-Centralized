/****************************************************************************************
 * Name			 : Zeeshan Aamir Khavas
 * Application	 : Centralized P2P Application
 * Program		 : Server.java
 * Description   : This is the main class for the Server application.
 * 				   It starts the Server by assigning user defined port to listen.
 * 				   Accepts connections from clients and creates thread for each client.
 * Date			 : 09/21/2015
  * @author Zee 
 ***************************************************************************************/

package com.zee.p2p;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

	/* main function of the server */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int id;
		ServerSocket myServer;
		Socket myClient;
		Manager myManager;

		Scanner scan = new Scanner(System.in);
		System.out.println("+---------------------------+");
		System.out.println("*** Welcome to P2P Server ***");
		System.out.println("+---------------------------+");
		System.out.println("Enter port for Server :");			//Takes port as user input
		id = Integer.parseInt(scan.nextLine());

		try {
			InetAddress serverAdd = InetAddress.getLocalHost();		//InetAddrress will return the IP of the server
			myServer = new ServerSocket(id);
			System.out.println("\n::Server is active on the following address & port::");
			System.out.println("Server Address : " + serverAdd.getHostAddress());
			System.out.println("Server Port    : " + myServer.getLocalPort());

			CentralIndex index = new CentralIndex();				//A central index will be created for all the upcoming client connections
			System.out.println("\n---@ Peer Activity @---");

			/* A loop will keep Server connection open.
			 * It accepts clients connections to the server
			 */
			while(true){
				myClient = myServer.accept();
				System.out.println("Peer [ " + (myClient.getInetAddress()).getHostAddress() + ":" + myClient.getPort() + " ] connected...");
				myManager = new Manager(myClient, index);			//A manager object will handle all the client requests
				Thread t = new Thread(myManager);					//A new thread is created for each client connection
				t.start();
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scan.close();
	}

}
