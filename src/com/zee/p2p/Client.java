/****************************************************************************************
 * Name			 : Zeeshan Aamir Khavas
 * Application	 : Centralized P2P Application
 * Program		 : Client.java
 * Description   : This is the main class for the Client application.
 * 				   It connects to user requested server and also starts local port to
 * 				   listen other peer requests. Accepts connections from other peers and
 * 				   creates thread for each peer.
 * Date			 : 09/21/2015
  * @author Zee 
 ***************************************************************************************/

package com.zee.p2p;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class Client {
	/*Global variables*/
	static int key = 1;				
	static ClientManager locManager;

	public static void main(String[] args) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		
		int id, myId;
		String serverName;
		ServerSocket locServer;
		Socket myClient;
		InputStream serverIn;
		DataInputStream clientIn;
		DataOutputStream clientOut;
		
		Scanner scan = new Scanner(System.in);
		
		/*User is prompted for server address and details*/
		System.out.println("+---------------------------+");
		System.out.println("|** Welcome to P2P Client **|");
		System.out.println("+---------------------------+");
		System.out.println("Enter Server address:");
		serverName = scan.nextLine();
		System.out.println("Enter Server port:");
		id = Integer.parseInt(scan.nextLine());
		
		try {
			
			myClient = new Socket(serverName, id);		//connection is made to the requested server
			myId = myClient.getLocalPort();				//local connected port is retrieved
			myId++;										//new port is created for other peer connections
			locServer = new ServerSocket(myId);			//local server is started to listen other peer requests
			locManager = new ClientManager(locServer);	
			Thread t = new Thread(locManager);			//a thread is created for the local server
			t.start();
			
			/*Server IO streams are instantiated*/
			serverIn = myClient.getInputStream();
			clientIn = new DataInputStream(serverIn);
			clientOut = new DataOutputStream(myClient.getOutputStream());

			if(clientIn.readByte() == 1){				//if server connection is successful display message
				System.out.println("\n---@ Connection to Server successful ! @---");
				long lStartTime = System.currentTimeMillis();
				serverUpdate(myClient, clientOut);		//serverUpdate() will send file names to the server
				long lEndTime = System.currentTimeMillis();
				long difference = lEndTime - lStartTime;
				System.out.println("Elapsed time for file upload: " + difference + " ms");
				while(key != 3){
					displayMenu(myClient, clientOut, clientIn);	//display() will show user a application menu to user
				}
				
			}
			clientOut.writeUTF("CLOSE");		//if client wishes to close connection send server close request
			System.out.println("\n*   *   *   *   *   *   *   *   *   *   *   *   *   *   *");
			System.out.println("Thank you for using P2P client. Your connection is closed !");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Connection to Server failed ! ");
			e.printStackTrace();
		}
		scan.close();
		
	}
	
	/* **********************************************************************
	 * Method Name 	:	displayMenu
	 * Parameters	:	myClient, clientOut, clientIn
	 * Returns		:	void
	 * Description	:	This method will display a application menu to the user.
	 * 					It input from user and calls respective method to handle
	 * 					requests.
	 * **********************************************************************/
	public static void displayMenu(Socket myClient, DataOutputStream clientOut, DataInputStream clientIn) throws ClassNotFoundException, IOException {
		// TODO Auto-generated method stub
		
		Scanner scan = new Scanner(System.in);
		
		/*Display menu*/
		System.out.println("\n+-------------------------+");
		System.out.println("|        P2P Menu         |");
		System.out.println("+-------------------------+");
		System.out.println("|    1. Search file       |");
		System.out.println("|    2. Replicate file    |");
		System.out.println("|    3. Exit network      |");
		System.out.println("+-------------------------+");
		System.out.println("Enter the selection number:");
		key = scan.nextInt();
		
		/*based on user selection call respective methods*/
		if(key == 1){
			searchFile(myClient, clientOut, clientIn);		//method to search file
		}
		else if(key == 2){
			replicateFile(myClient, clientOut, clientIn);	//method to replicate files
		}
		//scan.close();
	}
	
	/* **********************************************************************
	 * Method Name 	:	replicateFile
	 * Parameters	:	myClient, clientOut, clientIn
	 * Returns		:	void
	 * Description	:	This method will send replicate request to server and
	 * 					retrieves peer list. Then it will send requested file
	 * 					to peer list.
	 * **********************************************************************/
	private static void replicateFile(Socket myClient, DataOutputStream clientOut, DataInputStream clientIn) throws IOException {
		// TODO Auto-generated method stub
		String fileName;
		int num;
		String host;
		int port;
		
		Scanner scan = new Scanner(System.in);
		
		/*Take file name and number of copies to be replicated from user*/
		System.out.println("Enter the file name with extension to replicate: ");
		fileName = scan.nextLine();
		System.out.println("Enter number of copies to be replicated: ");
		num = scan.nextInt();
		
		/*Check for empty file names*/
		while(fileName.isEmpty()){
			System.out.println("Enter a valid file name !");
			fileName = scan.nextLine();
		}
		
			clientOut.writeUTF("REPLICATE");			//send server replicate request
			clientOut.writeInt(num);					//send replication number
			int size = clientIn.readInt();
			String[] currentPeers = new String[2];
			String tempObj;
			
			/*If peers are available for replication, send files to peers*/
			if(size != 0){
				String[] cList = new String[size];
				for(int i = 0; i < size; i++){
					tempObj = clientIn.readUTF();
					cList[i] = tempObj;
					currentPeers = tempObj.split(" ");	//get peer address and port
					Socket repClient;
					DataOutputStream repOut;
					DataInputStream repIn;
					FileInputStream fis;
					BufferedInputStream bis;
					OutputStream ois;
					
					host = currentPeers[0];
					port = Integer.parseInt(currentPeers[1]);
					port++;							//get port number of the local peer server
					//System.out.println(host + " " + port);
					
					repClient = new Socket(host, port);		//connect to local peer server
					repOut = new DataOutputStream(repClient.getOutputStream());
					repIn = new DataInputStream(repClient.getInputStream());
					repOut.writeUTF("REPLICATE");			//send replicate request to the peer
					repOut.writeUTF(fileName);				//send name of file to be replicated
					ois = repClient.getOutputStream();
					String folder = "up", file;
					file = folder +"/" + fileName;
					
					/*Search for file directory if doesn't exist create*/
					File directory = new File("up");
					if(!directory.exists()){
						directory.mkdir();
					}
					
					/*Send the file to peer using buffer stream*/
					File myFile = new File(file);
					
			        byte [] mybytearray  = new byte [(int)myFile.length()];
			        repOut.writeInt((int)myFile.length());;
			        
			        fis = new FileInputStream(myFile);
			        bis = new BufferedInputStream(fis);
			        bis.read(mybytearray,0,mybytearray.length);
			        ois.write(mybytearray,0,mybytearray.length);
			        ois.flush();
					
			        bis.close();
			        fis.close();
			        ois.close();
			        repClient.close();
				}
				for (int j = 0; j < cList.length; j++) {
					clientOut.writeUTF("UPDATE");		//send file update request to server
					clientOut.writeInt(-1);
					clientOut.writeUTF(cList[j]);
					clientOut.writeUTF(fileName);
				}
				System.out.println("File replicated successfully !");
				
			}
			else{
				System.out.println("File cannot be replicated !");
				System.out.println("Peers are not available or less than the replicates requested !");
			}
		//scan.close();
	}
	
	/* **********************************************************************
	 * Method Name 	:	searchFile
	 * Parameters	:	myClient, clientOut, clientIn
	 * Returns		:	void
	 * Description	:	This method will search the requested file in the 
	 * 					server central index by sending request to server.
	 * **********************************************************************/
	private static void searchFile(Socket myClient, DataOutputStream clientOut, DataInputStream clientIn) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		String fileName;
		
		Scanner scan = new Scanner(System.in);
		
		/*Take name of file to be searched from the user*/
		System.out.println("Enter the file name with extension to search: ");
		fileName = scan.nextLine();
		
		if(fileName.isEmpty()){
			System.out.println("Enter a valid file name !");
		}
		else{
			long lStartTime = System.currentTimeMillis();
			clientOut.writeUTF("SEARCH");		//send search request to server
			clientOut.writeUTF(fileName);		//send name of file to be searched
			searchResults(myClient, fileName, clientOut, clientIn, lStartTime);		//searchResults() will display search results
		}
		//scan.close();
	}
	
	/* **********************************************************************
	 * Method Name 	:	searchResults
	 * Parameters	:	myClient, fileName, clientOut, clientIn
	 * Returns		:	void
	 * Description	:	This method will retrieve the search results from 
	 * 					server and displays it to user for further processing.
	 * **********************************************************************/
	private static void searchResults(Socket myClient, String fileName, DataOutputStream clientOut, DataInputStream clientIn, long lStartTime) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
		int counter = 0, sel;
		int pPort;
		String pHost;
		
		String[] currentPeers = new String[2];			//variable to save peer details
		String tempObj;
		Hashtable<Integer, String[]> peerList = new Hashtable<Integer, String[]>();	//a local hash table to save the retrieved peer details
		Scanner scan = new Scanner(System.in);
		
		int size = clientIn.readInt();		//get size of peer list
		
		/*Display peer details*/
		if(size != 0){
			System.out.println("\nFile - '" + fileName + "' is available at the following peers: ");
			System.out.println("+------------------------------------+");
			System.out.println("| ID       Host Name         Port    |");
			System.out.println("+------------------------------------+");
			
			for(int i = 0; i < size; i++){
				counter++;
				tempObj = clientIn.readUTF();			//get peer details
				currentPeers = tempObj.split(" ");
				peerList.put(counter, currentPeers);	//add peer to the local hash table
				System.out.println("|  " + counter + "       " + currentPeers[0] + "          " + currentPeers[1] + "  |");
			}
			System.out.println("+------------------------------------+");
			long lEndTime = System.currentTimeMillis();
			long difference = lEndTime - lStartTime;
			System.out.println("Elapsed time for file search: " + difference + " ms");
			System.out.println("Enter the ID of the peer to download file from it: ");	//prompt user for peer from where file have to be downloaded
			sel = scan.nextInt();
			
			currentPeers = peerList.get(sel);	//get the peer details from corresponding selection
			pHost = currentPeers[0];
			pPort = Integer.parseInt(currentPeers[1]);
			pPort++;
			download(pHost, pPort, fileName);	//download() will download file form the selected peer
		}
		/*If file not available at any peer display message*/
		else{
			System.out.println("+----------------------------------+");
			System.out.println("# File not avilable at any peers ! #");
			System.out.println("+----------------------------------+");
			displayMenu(myClient, clientOut, clientIn);	
		}
	}

	/* **********************************************************************
	 * Method Name 	:	download
	 * Parameters	:	pHost, pPort, fileName
	 * Returns		:	void
	 * Description	:	This method will download file from the requested 
	 * 					peer address and port.
	 * **********************************************************************/
	private static void download(String pHost, int pPort, String fileName) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		Socket dnClient;
		DataOutputStream dnOut;
		DataInputStream dnIn;
		InputStream dnInput;
		BufferedOutputStream buffOut = null;
		FileOutputStream fileOut = null;
		
		File directory = new File("down");
		String down = "down/" + fileName;
		
		/*Search for download directory if doesn't exist create*/
		if(!directory.exists()){
			directory.mkdir();
		}
		long lStartTime = System.currentTimeMillis();
		dnClient = new Socket(pHost, pPort);		//connect to the peer
		
		/*Server IO streams are instantiated*/
		dnOut = new DataOutputStream(dnClient.getOutputStream());
		dnIn = new DataInputStream(dnClient.getInputStream());
		
		dnOut.writeUTF("GET");			//send download request
		dnOut.writeUTF(fileName);		//send name of file to be downloaded
		int fileSize = dnIn.readInt();	//read size of file
		
		int bytesRead;
	    int current = 0;
	    
	    /*Initiate file receive using buffered stream*/
	    try {
	      System.out.println("\nRecieving file " + fileName + "...!");
	      byte [] mybytearray  = new byte [fileSize];
	      dnInput = dnClient.getInputStream();
	      fileOut = new FileOutputStream(down);
	      buffOut = new BufferedOutputStream(fileOut);
	      bytesRead = dnInput.read(mybytearray,0,mybytearray.length);
	      current = bytesRead;
	      
	      do {
	         bytesRead = dnInput.read(mybytearray, current, (mybytearray.length-current));
	         if(bytesRead >= 0){
	        	 current += bytesRead;
	         }
	      } while(bytesRead > 0);
		  
	      buffOut.write(mybytearray, 0 , current);		//write the downloaded file 
	      buffOut.flush();
	      System.out.println("File - " + fileName + " downloaded successfully !");
	      long lEndTime = System.currentTimeMillis();
	      long difference = lEndTime - lStartTime;
	      System.out.println("Elapsed time for file download: " + difference + " ms");
	    }
	    finally {
	    	dnClient.close();
	        if (buffOut != null) buffOut.close();
	      }
	}
	
	/* **********************************************************************
	 * Method Name 	:	serverUpdate
	 * Parameters	:	myClient, clientOut
	 * Returns		:	void
	 * Description	:	This method will update the central index at server
	 * 					with files available the client for sharing
	 * **********************************************************************/
	private static void serverUpdate(Socket myClient, DataOutputStream clientOut) throws IOException {
		// TODO Auto-generated method stub
		
		clientOut.writeUTF("UPDATE");		//send file update request to server
		
		File directory = new File("up");
		
		/*Search for upload directory if doesn't exist create*/
		if(!directory.exists()){
			directory.mkdir();
		}

		File[] files = directory.listFiles();		//List all the files in upload directory
		
		clientOut.writeInt(files.length);

		for(File file : files)
		{
		    String name = file.getName();
		    clientOut.writeUTF(name);				//send file names to the server
		}
	}

}