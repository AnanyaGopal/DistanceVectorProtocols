import java.io.*;
import java.net.*;
import java.util.*;

class ClientThread extends Thread {
	private final int time=15000;
	public final double INFINITE_VAL = 99999.0;
	private int BUFFER_SIZE = 32768;
	private String filename;
	private DatagramSocket sendSOCKET = null;	
	private int iteration = 0;
	Hashtable<String, Double> hostCostPair = null;
	ArrayList<String> list = null;
	ArrayList<String> ports = new ArrayList<String>();
	Hashtable<String, Vector<DV>> Host = null;

	public ClientThread(String filename, Hashtable<String, Double> hostCostPair, ArrayList<String> list, Hashtable<String, Vector<DV>> Host) throws IOException {
		super("Sender");
		this.filename = filename;	
		sendSOCKET = new DatagramSocket();		
		this.hostCostPair = hostCostPair;
		this.list = list;
		this.Host = Host;
	}

	public void run() { 
		
		while(true) {						
			iteration++;
			System.out.println("\n Iteration " + iteration + "\n");			

				
			try {							   
				DatagramPacket clientPacket = null;
				byte[] bufSent = new byte[BUFFER_SIZE];
				String sttring = "";
				
				if(iteration == 1) {
					Hostinfo(filename);
					sttring = routingTable(Host);					
					for(int n=0; n<list.size(); n++) {
						bufSent = sttring.getBytes();
						InetAddress address = InetAddress.getByName(list.get(n));
						String hostport = ports.get(n);
						int hport = Integer.parseInt(hostport);
				
						
						clientPacket = new DatagramPacket(bufSent, bufSent.length, address, hport);						
						sendSOCKET.send(clientPacket);
					}
					String simpleString = sysout(sttring);
					System.out.println(simpleString);
				}

				//For later intervals
				else if(iteration > 1) {
					boolean isChanged = false;
					LinkedList<String> newPair = filechange(filename);
					for(int i=0; i<newPair.size(); i++) {
						
						String[] pairCost = newPair.get(i).split(" ");
						if(hostCostPair.get(pairCost[0]) != Double.parseDouble(pairCost[1])) {
							isChanged = true;
							
							
							hostCostPair.remove(pairCost[0]);
							hostCostPair.put(pairCost[0], Double.parseDouble(pairCost[1]));							
						}
					}

					// If the data is send, then keep reading and send the data again
					if(isChanged) {
						list = new ArrayList<String>();
						Hostinfo(filename);
						sttring = routingTable(Host);						
						for(int n=0; n<list.size(); n++) {
							bufSent = sttring.getBytes();
							InetAddress address = InetAddress.getByName(list.get(n));
							String hostport = ports.get(n);
							int hport = Integer.parseInt(hostport);							
							clientPacket = new DatagramPacket(bufSent, bufSent.length, address, hport);
							sendSOCKET.send(clientPacket);
						}
						String simpleString = sysout(sttring);
						System.out.println(simpleString);
						//
						isChanged = false;
					}
					else {				
						sttring = routingTable(Host);						
						for(int n=0; n<list.size(); n++) {
							bufSent = sttring.getBytes();
							InetAddress address = InetAddress.getByName(list.get(n));
							String hostport = ports.get(n);
							int hport = Integer.parseInt(hostport);							
							clientPacket = new DatagramPacket(bufSent, bufSent.length, address, hport);
							sendSOCKET.send(clientPacket);
						}						
						String simpleString = sysout(sttring);
						System.out.println(simpleString);
					}
				}
			
				try {
					Thread.sleep(time);
					System.out.println("Refresh after 15 s..");
				} 
				catch(InterruptedException e) { 
					System.out.println(e);
				}
			}
			catch(IOException e) {
				e.printStackTrace();
				System.out.println(e);
			}			
		}		
	}

//Check if the file has changed
	private LinkedList<String> filechange(String filename) throws IOException {		
		FileInputStream FStream = null;
		InputStreamReader InpStream = null;
		BufferedReader BR = null;
		LinkedList<String> LVal = null;
		try {
			FStream = new FileInputStream(filename);
			InpStream = new InputStreamReader(FStream);
			BR = new BufferedReader(InpStream);
			LVal = new LinkedList<String>();
			String Line;
			while((Line = BR.readLine()) != null) {				
				String[] hostFile = filename.split("\\.dat");
				String[] part = Line.split(" ");
				if(part.length == 3) {					
					LVal.add(hostFile[0] + "-" + part[0] + " " + Double.parseDouble(part[1]));
				}
			}
		}
		catch(IOException e) {
			System.err.println(e);
		}		
		BR.close();
		InpStream.close();
		FStream.close();	
		return LVal;
	}	



	private void Hostinfo(String filename) throws IOException {		
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader brrr = null;
		try {
			fis = new FileInputStream(filename);
			isr = new InputStreamReader(fis);
			brrr = new BufferedReader(isr);
			String Line;
			while((Line = brrr.readLine()) != null) {				
				String[] hostFile = filename.split("\\.dat");
				String[] part = Line.split(" ");
				if(part.length == 3) {					
					list.add(part[0]);
					ports.add(part[2]);
					hostCostPair.put(hostFile[0] + "-" + part[0], Double.parseDouble(part[1]));					
				}
			}
			fis = new FileInputStream(filename);
			isr = new InputStreamReader(fis);
			brrr = new BufferedReader(isr);
			while((Line = brrr.readLine()) != null) {
				String[] hostFile = filename.split("\\.dat");
				String[] part = Line.split(" ");
				Vector<DV> myDV = new Vector<DV>();
				if(part.length == 3) {				
					Iterator<String> itr = list.iterator();
					while (itr.hasNext()) {
						String element = itr.next();
						if(element.equals(part[0])) {
							myDV.add(new DV(hostFile[0], element, part[0], Double.parseDouble(part[1])));
						}
						else {							
							myDV.add(new DV(hostFile[0], element, part[0], INFINITE_VAL));
						}											
					}

					Host.put(hostFile[0] + "-" + part[0], myDV);
				}				
			}
		}		
		catch(IOException e) {
			System.err.println(e);
		}            
		brrr.close();
		isr.close();
		fis.close();			        
	}	

	private String routingTable(Hashtable<String, Vector<DV>> dvList) {
		Double min = Double.MAX_VALUE;
		String LVal = "";		
		for(Vector<DV> v : dvList.values()) {
			String source = "";
			String via = "";
			String dest = "";
			double[] costs = new double[v.size()];			
			for(int j=0; j<v.size(); j++) {
				costs[j] = v.get(j).getCost();				
			}
			source = v.get(calculateMin(costs)).getSource();
			dest = v.get(calculateMin(costs)).getDest();
			via = v.get(calculateMin(costs)).getVia();
			min = v.get(calculateMin(costs)).getCost();

			LVal += "Shortest Path " + source + " - " + dest + ": the next hop is " + via + " and the cost is " + min + "\n";
		}

		return LVal;			
	}

	private int calculateMin(double[] d) {
		double min = Double.MAX_VALUE;
		for(int i=0; i<d.length; i++) {
			if(min > d[i]) {
				min = d[i];
			}
		}
		for(int i=0; i<d.length; i++) {
			if(min == d[i]) {
				return i;
			}
		}
		return -1;
	}


	private String sysout(String input) {
		String stringg = "";		
		String[] lines = input.split("\\n");
		for(int i=0; i<lines.length; i++) {
			String[] array = lines[i].split(" ");
			String[] start = array[2].split("\\.");
			String[] dest = array[4].split("\\.");
			String[] via = array[9].split("\\.");
			stringg += "Shortest Path " + start[0] + " - " + dest[0] + ": the next hop is " + via[0] + " and the cost is " + array[14] + "\n";			

		}

		return stringg;			
	}


}