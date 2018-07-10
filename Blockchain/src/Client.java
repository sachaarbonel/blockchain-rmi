import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Client {
	
    public static void main(String[] args) {

        String reg_host = "localhost";
        int reg_port = 1099;

        if (args.length == 1) {
            reg_port = Integer.parseInt(args[0]);
        } else if (args.length == 2) {
            reg_host = args[0];
            reg_port = Integer.parseInt(args[1]);
        }


        NodeInterface node;
		try {
			node = (NodeInterface) Naming.lookup("rmi://" + reg_host + ":" + reg_port + "/Node");
			cli(node);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
public static void cli(NodeInterface n) {
		
		while (true) {
            int choice = -1;
            System.out.println("//-------------------------//");
            System.out.println("[1] Get First Block");
            System.out.println("[2] Add data to blockchain");
            System.out.println("[3] Get Block id");
            System.out.println("[4] Is blockchain valid ?");
            System.out.println("[5] Give me the Blockchain");
            System.out.println("//-------------------------//");
            System.out.println("Choose a number: ");

            Scanner scn = new Scanner(System.in);
            choice = scn.nextInt();
            switch (choice) {
                case 1:
				String firstBlockData;
				try {
					firstBlockData = n.getBlock(0);
					System.out.println("The first block data : ");
                	System.out.println(firstBlockData);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
                    break;
                case 2:
                	Scanner scn2 = new Scanner(System.in);
                    String data = scn2.next();
				try {
					n.addTransactionToPool(data);
					System.out.println("Trying to send transaction processing pool " + data);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					break;
                case 3:
                	Scanner scn3 = new Scanner(System.in);
                    int blockId = scn3.nextInt();
                    String blockData;
    				try {
    					blockData = n.getBlock(blockId);
    					System.out.println("The block "+blockId+" data: ");
                    	System.out.println(blockData);
    				} catch (RemoteException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				} 
    					break;
                case 4:
                    boolean valid;
    				try {
    					valid = n.isChainValid();
                    	if(valid) System.out.println("Yes");
    				} catch (RemoteException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}  	
                        break;
                case 5:
                	String blockchainJson;
    				try {
    					blockchainJson = n.getBlockchainJson();
    					System.out.println("The Blockchain");
    					System.out.println(blockchainJson);
    				} catch (RemoteException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}  	
                        break;
				
            }
        }

}
}
