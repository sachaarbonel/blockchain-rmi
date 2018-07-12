import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.google.gson.GsonBuilder;


public class Node implements NodeInterface {
	private ArrayList<Block> blockchain = new ArrayList<Block>(); 
	private int difficulty = 5;
	private PriorityQueue<Block> memPool;
	private int blockCount=0;
	private int txCount=0;
	//private boolean ready = false;
	private PriorityQueue<String> transactionPool;
	private String status = "alive";
	private NodeInterface masterNode;
	
	//protected Node() {}
	
	
	protected Node(String type, int port) throws MalformedURLException, RemoteException, NotBoundException {
		//this.status = type.concat(" is ").concat(status);
		if(type == "master") {
			memPool = new PriorityQueue<Block>();
			this.status = type.concat(" is ").concat(status);
		}else {
			this.status = type.concat(" is ").concat(status);
			transactionPool = new PriorityQueue<String>();
//			NodeInterface n = (NodeInterface) Naming.lookup("rmi://localhost:" + port + "/Node");
//			this.masterNode = (NodeInterface) UnicastRemoteObject.exportObject(n, 0);
		}
		
	}
	
	
//	protected Node(NodeInterface masterNode) throws RemoteException {
//		transactionPool = new PriorityQueue<String>();
//		this.masterNode = (NodeInterface) UnicastRemoteObject.exportObject(masterNode, 0);
//	}



	//Create blockchain based on arriving blocks from slave and broadcasting blockchain to slave  
	public void processBlocks() throws RemoteException {
	//while(!memPool.isEmpty() && isChainValid()) {
	while(!memPool.isEmpty()) { //We simplified the case with one transaction per Block but otherwise we can check if the chain of blocks is Valid see above
		Block arrivingBlock = getBlockFromPool(); // Get block from slaveNode
		Block newBlock = new Block(arrivingBlock.getData(),arrivingBlock.getHash(),blockCount);  
		if(isBlockValid(newBlock)) blockchain.add(newBlock);
		System.out.println("Trying to mine Block " +blockCount);
		blockchain.get(blockCount).mineBlock(difficulty); //Only masterNode mine blocks, slaveNode validate transactions
		//if(memPool.isEmpty()) setReady();
		blockCount+=1;
	}
	}
	
	
	//Take the longest chain
	public void replaceChain(ArrayList<Block> newBlocks) throws RemoteException {
		if(newBlocks.size() > blockchain.size()) blockchain = newBlocks;
	}

	//Get the hash of the latest block
	public String getHash() throws RemoteException  {
	String hash = "0"; // prevent from Non null pointer exception
	if((blockchain.size()== 0)) {
		blockchain.add(new Block("genesis block",hash,blockCount)); // create genesis block if doesnt exist
	} else {
		hash = blockchain.get(blockchain.size()-1).getHash();
	}
	return hash;
	}
	
	
	
	public String getBlockHashById(int blockId) throws RemoteException  {
		return blockchain.get(blockId).getData();
	}
	
	public String getBlockDataById(int blockId) throws RemoteException  {
		return blockchain.get(blockId).getData();
	}
	

	//getters
	
	public String getStatus() {
		return status;
	}
	
	public ArrayList<Block> getBlockchain() throws RemoteException {
		return this.masterNode.getBlockchain();
	}
	
	public String getBlockchainJson() throws RemoteException {
		String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(this.masterNode.getBlockchain());
		return blockchainJson;
	}
	
	public int getDifficulty() throws RemoteException  {
		return difficulty;
	}
	
	public Block getBlockFromPool() throws RemoteException {
		Block block = memPool.remove();
		return block;
		
	}
	
//	public boolean isReady() throws RemoteException {
//		return this.ready;
//	}
	
	public int getBlockCount() throws RemoteException  {
		return blockCount;
	}
	
	public int getTxCount() throws RemoteException  {
		return txCount;
	}
	
	
	//setters
	public void addBlockToPool(Block block) throws RemoteException {
		memPool.add(block);
	}
	
//	public void setReady() throws RemoteException {
//		this.ready = true;
//	}
		
	
	
	
	/**
	Check the integrity of our blockchain. Loop through all blocks in the chain and compare the hashes. 
	This method will need to check the hash variable is actually equal to the calculated hash, 
	and the previous block�s hash is equal to the previousHash variable.
	@param the parameters used by the method
	@return the value returned by the method
	@throws what kind of exception does this method throw
	*/
	public Boolean isChainValid() throws RemoteException {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		
		for(int i=1;i<blockchain.size();i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			
			//compare registered hash and calculated hash:
			if(!currentBlock.getHash().equals(currentBlock.calculateHash())) {
				System.out.println("Current Hash not equal");
				return false;
			}
			
			//compare previous hash and registered previous hash
			if(!previousBlock.getHash().equals(previousBlock.calculateHash())) {
				System.out.println("Previous hash not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.getHash().substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("This block hasn't been mined");
				return false;
			}
		}
		return true;
	}
	
	public Boolean isBlockValid(Block newBlock) throws RemoteException {
		if((newBlock.calculateHash() != newBlock.getHash())) {
			return false; 
		}
		if((getHash() != newBlock.getHash())) {
			return false;
		}
		if((newBlock.calculateHash() != newBlock.getHash())) {
			return false; 
		}
		return true;
		
		
	}


	//SlaveNode methods
	
	public void processTransactions() throws RemoteException {
		while(!transactionPool.isEmpty() && !getTransactionFromPool().isEmpty()) {
			String t = getTransactionFromPool();
			//sendTransaction(t,this.masterNode);
			sendTransaction(t);
			txCount++;
			
		}
	}

	private String getTransactionFromPool() {
		String transaction = transactionPool.remove();
		return transaction;
	}
	
	public void broadcastBlock(String newData) throws RemoteException {
		Block newBlock = new Block(newData,this.masterNode.getHash()); //Calculate block hash with previous block hash aka from MasterNode latest block hash
		masterNode.addBlockToPool(newBlock);
		
	}
	
	
	//public void sendTransaction(String data, NodeInterface masterNode) throws RemoteException {
	public void sendTransaction(String data) throws RemoteException {
		
			broadcastBlock(data);
			//broadcastBlock(data,masterNode);
//			try {
//				TimeUnit.SECONDS.sleep(10); // broadcasting blocks every 10 seconds
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	}


	public String getMasterStatus() throws RemoteException {
		this.masterNode.getStatus(); 
		return null;
	}


	public String getStatus(NodeInterface masterNode) throws RemoteException {
		String status = masterNode.getStatus();
		return status;
	}


}
