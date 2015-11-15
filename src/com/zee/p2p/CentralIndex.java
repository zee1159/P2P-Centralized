/****************************************************************************************
 * Name			 : Zeeshan Aamir Khavas
 * Application	 : Centralized P2P Application
 * Program		 : CentralIndex.java
 * Description   : This class will create a central index for all clients and
 * 				   performs operations on central index.
 * Date			 : 09/21/2015
 * @author Zee 
 ***************************************************************************************/

package com.zee.p2p;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;


public class CentralIndex {
	String ip, port;
	static int counter;

	private Hashtable<Integer, Peer> peerList;					//Hashtable to store the peer details
	private Hashtable<String, ArrayList<Integer>> fileList;		//Hashtable to store the file details

	/*Default constructor that will instantiate Hashtable*/
	public CentralIndex() {
		// TODO Auto-generated constructor stub
		setPeerList(new Hashtable<Integer, Peer>());
		setFileList(new Hashtable<String, ArrayList<Integer>>());
	}

	/*Accessors and modifiers for the Hashtables*/
	public Hashtable<Integer, Peer> getPeerList() {
		return peerList;
	}

	public void setPeerList(Hashtable<Integer, Peer> peerList) {
		this.peerList = peerList;
	}

	public Hashtable<String, ArrayList<Integer>> getFileList() {
		return fileList;
	}

	public void setFileList(Hashtable<String, ArrayList<Integer>> fileList) {
		this.fileList = fileList;
	}

	/* **********************************************************************
	 * Method Name 	:	addPeers
	 * Parameters	:	myClient
	 * Returns		:	pid
	 * Description	:	This method will add the peers to the central index.
	 * 					If registration was successful, it will return a unique
	 * 					id assigned to the peer.
	 * **********************************************************************/
	public int addPeer(Socket myClient) {
		Peer checkPeer = new Peer();
		String host;
		int id = 0, port;

		/*Iterate through the peerList table to check if the peer already exists*/
		for(Entry<Integer, Peer> entry: peerList.entrySet()){
			checkPeer = entry.getValue();
			host = checkPeer.getPeerName();
			port = checkPeer.getPeerPort();
			if(myClient.getRemoteSocketAddress().equals(host) && (myClient.getPort() == (port))){
				id = entry.getKey();
				break;
			}
		}

		if(id > 0)
			return id;		//If peer found, return its ID
		else{
			counter++;
			int pid = counter;		//If its a new peer assign a new ID

			Peer newPeer = new Peer();		//create a peer object that will contain peer details
			newPeer.setHostname((myClient.getInetAddress()).getHostAddress());		//add peer info to the peer object
			newPeer.setPeerPort(myClient.getPort());
			getPeerList().put(pid, newPeer);	//add the peer to central index

			return pid;
		}


	}

	/* **********************************************************************
	 * Method Name 	:	addFiles
	 * Parameters	:	myClient, fileName, id
	 * Returns		:	void
	 * Description	:	This method will add the files details received from
	 * 					the client to the central index.
	 * **********************************************************************/
	public void addFiles(Socket myClient, String fileName, int id){
		String file;
		int chk = 0;

		ArrayList<Integer> currentList = new ArrayList<Integer>();

		/*Iterate through the fileList table to check if the file already exists*/
		for(Entry<String, ArrayList<Integer>> entry: fileList.entrySet()){
			file = entry.getKey();
			if(file.equalsIgnoreCase(fileName)){
				chk = 1;
				break;
			}
		}

		if(chk == 1){		//if file exists in table add the peer to the existing entry.
			currentList = fileList.get(fileName);
			currentList.add(id);
			fileList.put(fileName, currentList);
		}
		else{				//if file doesn't exists in table, create new entry in the table.
			currentList.add(id);
			fileList.put(fileName, currentList);
		}
	}

	/* **********************************************************************
	 * Method Name 	:	searchFiles
	 * Parameters	:	fileName
	 * Returns		:	ArrayList of peers
	 * Description	:	This method will search central index for the files
	 * 					requested file and returns list of peers having it.
	 * **********************************************************************/
	public ArrayList<Integer> searchFiles(String fileName) {
		// TODO Auto-generated method stub
		return getFileList().get(fileName);
	}

	/* **********************************************************************
	 * Method Name 	:	searchPeers
	 * Parameters	:	pid
	 * Returns		:	Peer
	 * Description	:	This method will search central index for the peer
	 * 					id and returns peer details of corresponding peer id.
	 * **********************************************************************/
	public Peer searchPeers(int pid){
		return peerList.get(pid);
	}

	/* **********************************************************************
	 * Method Name 	:	removePeer
	 * Parameters	:	myClient, id
	 * Returns		:	void
	 * Description	:	This method will remove peer details of
	 * 					corresponding id from the central index.
	 * **********************************************************************/
	public void removePeer(Socket myClient, int id) {
		// TODO Auto-generated method stub
		String file;
		ArrayList<Integer> currentList = new ArrayList<Integer>();

		peerList.remove(id);		//Removes peer from central peer table

		Set<String> keys = fileList.keySet();
		Iterator<String> itr = keys.iterator();

		/*Iterate through fileList table and removes the peer id*/
		while (itr.hasNext()) {
			file = itr.next();
			currentList = fileList.get(file);
			for(int i = 0; i < currentList.size(); i++){
				Iterator<Integer> it = currentList.iterator();
	            while (it.hasNext()) {
	            	int val = it.next();
	                if (val ==id) {
	                    it.remove();
	                }
	            }
			}
		}
	}

	/* **********************************************************************
	 * Method Name 	:	replicateFiles
	 * Parameters	:	num, id
	 * Returns		:	ArrayList of peers
	 * Description	:	This method will check if requested number of peers
	 * 					are available for replication and returns list of
	 * 					available peers for replication.
	 * **********************************************************************/
	public ArrayList<Integer> replicateFiles(int num, int id) {
		// TODO Auto-generated method stub
		int peerId;
		ArrayList<Integer> currentList = new ArrayList<Integer>();

		/*Iterate through peerList for available peers*/
		if((getPeerList().size() - 1) >= num ){
			for(Entry<Integer, Peer> entry: peerList.entrySet()){
				peerId = entry.getKey();
				if(peerId != id){
					currentList.add(peerId);	//add available peers to a list
				}
			}
		}
		else{
			currentList = null;
		}
		return currentList;
	}

}
