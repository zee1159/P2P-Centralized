/****************************************************************************************
 * Name			 : Zeeshan Aamir Khavas
 * Application	 : Centralized P2P Application
 * Program		 : Peer.java
 * Description   : This class will create a Peer object for each client.
 * 				   Peer object will contain the host name and port details of the client.
 * Date			 : 09/21/2015
  * @author Zee 
 ***************************************************************************************/

package com.zee.p2p;

public class Peer{
	/*Variables to store peer address & port*/
	private String peerName;
	private Integer peerPort;

	/*Accessors and modifiers for peer*/
	String getPeerName() {
		return peerName;
	}
	void setHostname(String peerName) {
		this.peerName = peerName;
	}
	Integer getPeerPort() {
		return peerPort;
	}
	void setPeerPort(Integer peerPort) {
		this.peerPort = peerPort;
	}

}
