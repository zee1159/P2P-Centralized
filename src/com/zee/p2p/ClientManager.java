/****************************************************************************************
 * Name			 : Zeeshan Aamir Khavas
 * Application	 : Centralized P2P Application
 * Program		 : ClientManager.java
 * Description   : This class is a runnable thread. It will accept connections form other
 * 				   peers, creates thread for each peer.
 * Date			 : 09/21/2015
  * @author Zee 
 ***************************************************************************************/

package com.zee.p2p;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientManager implements Runnable {
	ServerSocket myServer;
	Socket myClient;
	DataInputStream clientIn;
	DataOutputStream clientOut;

	/* **********************************************************************
	 * Method Name 	:	ClientManager
	 * Parameters	:	locServer
	 * Description	:	Parameterized constructor that will set the peer values
	 * **********************************************************************/
	public ClientManager(ServerSocket locServer) throws IOException {
		// TODO Auto-generated constructor stub
		this.myServer = locServer;
	}

	/*default constructor*/
	public ClientManager() {
		// TODO Auto-generated constructor stub
	}

	/*This method will be executed when thread is started*/
	@Override
	public void run() {
		// TODO Auto-generated method stub
		ClientParser locCLient;

		/* A loop will keep local Server connection open.
		 * It accepts other peer connections.
		 */
		while(true){
			try {
				myClient = myServer.accept();
				locCLient = new ClientParser(myClient);
				Thread t = new Thread(locCLient);		//create a thread for each peer connection
				t.start();		//start the thread
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


}
