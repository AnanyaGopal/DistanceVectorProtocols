import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

class ServerThread extends Thread {
	private int port;
	Hashtable<String, Vector<DV>> Host = null;
	public final double INFINTE = 99999.0;
	private int BUFFER_SIZE = 32768;
	protected DatagramSocket rcvSocket = null; 
	
	ArrayList<String> list = null;
	
	Hashtable<String, Double> hostCostPair = null;	
	
	private boolean condtn = true;
	
	private String file;
	
	public ServerThread(int port, String file, Hashtable<String, Double> ncpair, ArrayList<String> adjNodes, Hashtable<String, Vector<DV>> Host) throws IOException {
	
	
		this.port = port;
		
		this.file = file;        
		
		rcvSocket = new DatagramSocket(port);		
		
		this.hostCostPair = ncpair;
		
		this.list = adjNodes;
		
		this.Host = Host;		
	}

	public void run() { 
		while(condtn) {
			try {
				byte[] bufReceived = new byte[BUFFER_SIZE];      
				
				DatagramPacket receiverPacket = new DatagramPacket(bufReceived, bufReceived.length);
				// Receive the datagram packet
				
				rcvSocket.receive(receiverPacket); 
				
				Hashtable<String, Vector<DV>> latest = Host;
				
				String received = new String(receiverPacket.getData(), 0, receiverPacket.getLength());
				
				String[] hostFile = file.split("\\.dat");
				
				LinkedList<RTable> receivedList = getResults(received);				
				
				String start = hostFile[0];
				
				for(int i=0; i<receivedList.size(); i++) {
					RTable table = receivedList.get(i);
					if(latest.containsKey(start + "-" + table.getDest())) {
						Vector<DV> dVector = latest.get(start + "-" + table.getDest());
						int index = list.indexOf(table.getSource());
						double newCost = hostCostPair.get(start + "-" + table.getSource()) + table.getCost();
						if(dVector.get(index).getCost() > newCost) {
							dVector.get(index).setCost(newCost);
						}
					}
					else {
						if(start.equals(table.getDest())) {
							continue;
						}
						// new destination
						else {
							Vector<DV> newVector = new Vector<DV>();							
							for(int k=0; k<list.size(); k++) {
								newVector.add(new DV(start, list.get(k), table.getDest(), INFINTE));
							}
							int index = list.indexOf(table.getSource());
							newVector.set(index, new DV(start, table.getSource(), table.getDest(), hostCostPair.get(start + "-" + table.getSource()) + table.getCost()));
							Host.put(start + "-" + table.getDest(), newVector);
						}						
					}
				}			

			} 
			catch(IOException e) {
				e.printStackTrace();
			}			
		}
		rcvSocket.close();
	}

	private LinkedList<RTable> getResults(String received) {
		String receivedReg = "(?:([A-Za-z\\s]+path ))([A-Za-z\\d\\.]+) - ([A-Za-z\\d\\.]+):(?:([A-Za-z\\s]+is ))([A-Za-z\\d\\.]+)(?:([A-Za-z\\s]+)+)([\\d.]+)";		
		LinkedList<RTable> routeList = new LinkedList<RTable>();
		String[] lines = received.split("\\n");			
		Pattern pat = Pattern.compile(receivedReg, Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
		for(int i=0; i<lines.length; i++) {			
			String Nodestart = "";
			String Nodeend = "";
			String Nodevia = "";
			String Nodecost = "";
			Matcher pattern = pat.matcher(lines[i]);
			if(pattern.find()) {			
				
				Nodestart = pattern.group(2);
				Nodeend = pattern.group(3);
				Nodevia = pattern.group(5);
				Nodecost = pattern.group(7);
			}
			routeList.add(new RTable(Nodestart, Nodevia, Nodeend, Double.parseDouble(Nodecost)));			
		}
		return routeList;
	}
}