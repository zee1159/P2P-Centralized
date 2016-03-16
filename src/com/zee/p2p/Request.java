/****************************************************************************************
 * Name			 : Zeeshan Aamir Khavas
 * Application	 : Centralized P2P Application
 * Program		 : Request.java
 * Description   : This class processes all the client request.
 * Date			 : 09/21/2015 
 * @author Zee
 ***************************************************************************************/
package com.zee.p2p;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class Request {
	
	Socket myClient;
	ObjectOutputStream clientObj;
	CentralIndex index;
	int id;
	
	/* **********************************************************************
	 * Method Name 	:	Request
	 * Parameters	:	myClient, index, id
	 * Description	:	Parameterized constructor that will set the client values
	 * **********************************************************************/
	public Request(Socket myClient, CentralIndex index, int id){
		this.myClient = myClient;
		this.index = index;
		this.id = id;
	}
	
	/* **********************************************************************
	 * Method Name 	:	parser
	 * Parameters	:	serverOut, clientIn
	 * Returns		:	void
	 * Description	:	This method will take client requests and process it.
	 * **********************************************************************/
	public void parser(DataOutputStream serverOut, DataInputStream clientIn) {
		String fileName;
		int flag = 0;
		
		try {
			while(myClient.isBound()){		//till the client connection with server is bound, it will listen for requests from client 
				String code;
				code = clientIn.readUTF();
				
				/* "UPDATE" request will update files available at client to central index */
				if(code.equalsIgnoreCase("UPDATE")){
					int files = clientIn.readInt();
					if (files == -1){
						String add = clientIn.readUTF();
						fileName = clientIn.readUTF();
						String[] rep = add.split(" ");
						int pId = index.getPeer(Integer.parseInt(rep[1]), rep[0]);
						index.addFiles(myClient, fileName, pId);
					}
					else{
						for(int i = 0; i < files; i++){				//Receive file names from client sequentially
							fileName = clientIn.readUTF();
							index.addFiles(myClient, fileName, id);	//addFiles() will add file details to central index
						}
					}
					
				}
				
				/* "SEARCH" request will search for client requested file central index */
				else if(code.equalsIgnoreCase("SEARCH")){
					int count = 0, size;
					ArrayList<Integer> currentList = new ArrayList<Integer>();
					
					/* read file name to be searched from client */
					fileName = clientIn.readUTF();
					currentList = index.searchFiles(fileName);		//searchfile() will return list of peers containing requested file
					
					if(currentList != null){
						/* Check to remove the peer name from the list if it already has the file */
						if(currentList.size() != 0){
							Iterator<Integer> temp = currentList.iterator();
							while(temp.hasNext()){
								if(temp.next() != id){
									count++;
								}
							}
						}
					}
					size = count;
					
					serverOut.writeInt(size);		//send client the count of peers having file
					if(size != 0) {
						Iterator<Integer> iter = currentList.iterator();
						int temp;
						while(iter.hasNext()){
							temp = iter.next();
							if(temp != id){
								sendPeers(temp, serverOut);	//sendPeer() will send the peer details to client
							}
							count++;
						}
					}
					
				}
				
				/* "REPLICATE" request will send list of clients available for file replication */
				else if(code.equalsIgnoreCase("REPLICATE")){
					ArrayList<Integer> currentList = new ArrayList<Integer>();
					
					int num = clientIn.readInt();
					currentList = index.replicateFiles(num, id);
					
					if(currentList != null){
						serverOut.writeInt(currentList.size());
						Iterator<Integer> iter = currentList.iterator();
						
						while(iter.hasNext()){
							sendPeers(iter.next(), serverOut);
						}
					}
					else{
						serverOut.writeInt(0);
					}
					
				}
				
				/* "CLOSE" request will end the client connection with server */
				else if(code.equalsIgnoreCase("CLOSE")){
					flag = 1;
					index.removePeer(myClient, id);
					myClient.close();
					System.out.println("Peer [ " + (myClient.getInetAddress()).getHostAddress() + ":" + myClient.getPort() + " ] disconnected !");
				}
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			if(flag != 1){
				System.out.println("Peer [ " + (myClient.getInetAddress()).getHostAddress() + ":" + myClient.getPort() + " ] disconnected !");
			}
		}
	}

	/* **********************************************************************
	 * Method Name 	:	sendPeers
	 * Parameters	:	pid, serverOut
	 * Returns		:	void
	 * Description	:	This method will take id of the peer.
	 * 					Fetches address of peer from central index 
	 * 					and sends it to client
	 * **********************************************************************/
	private void sendPeers(Integer pid, DataOutputStream serverOut){
		// TODO Auto-generated method stub
		String host;
		String port;
		String peer;
		
		Peer resPeer = new Peer();
		resPeer = index.searchPeers(pid);
		host = resPeer.getPeerName();
		port = resPeer.getPeerPort().toString();
		peer = host + " " + port;
		
		try {
			serverOut.writeUTF(peer);
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Peer [ " + (myClient.getInetAddress()).getHostAddress() + ":" + myClient.getPort() + " ] disconnected !");
		}
	}

}