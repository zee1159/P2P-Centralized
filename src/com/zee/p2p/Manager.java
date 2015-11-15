/****************************************************************************************
 * Name			 : Zeeshan Aamir Khavas
 * Application	 : Centralized P2P Application
 * Program		 : Manager.java
 * Description   : This class is a runnable thread. I is instantiated for each client.
 * 				   It handles all the client by calling the Request class
 * Date			 : 09/21/2015
  * @author Zee 
 ***************************************************************************************/

package com.zee.p2p;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Manager implements Runnable{

	DataOutputStream serverOut;
	DataInputStream clientIn;
	private Socket myClient;
	CentralIndex index;
	Request req;

	/* **********************************************************************
	 * Method Name 	:	Manager
	 * Parameters	:	myClient, index
	 * Returns		:	void
	 * Description	:	Parameterized constructor that will set the client values
	 * **********************************************************************/
	public Manager(Socket myClient, CentralIndex index) {
		// TODO Auto-generated constructor stub
		this.myClient = myClient;
		this.index = index;
	}

	/*Default constructor*/
	public Manager() {
		// TODO Auto-generated constructor stub
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		processClient();
	}


	/* **********************************************************************
	 * Method Name 	:	processCLient
	 * Parameters	:	No parameters
	 * Returns		:	void
	 * Description	:	Method to process client info by adding it to central index
	 * **********************************************************************/
	private void processClient() {
		// TODO Auto-generated method stub

		try {
			clientIn = new DataInputStream(myClient.getInputStream());
			int id = index.addPeer(myClient);			//addPeer() will add an entry of peer in Central Index

			if(id > 0){
				serverOut = new DataOutputStream(myClient.getOutputStream());
				serverOut.writeByte(1);				   //Send a byte to client indicating connection  and central update was successful
			}
			else {
				serverOut = new DataOutputStream(myClient.getOutputStream());
				serverOut.writeByte(0);				   //Send a byte to client indicating client was already registered with server
			}

			req = new Request(myClient, index, id);
			req.parser(serverOut, clientIn);			//parser() will process all incoming client requests
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Peer [ " + (myClient.getInetAddress()).getHostAddress() + ":" + myClient.getPort() + " ] disconnected !");
			//e.printStackTrace();
		}
	}

}
