import java.io.*;
import java.util.*;

public class DVR {

	public static void main(String[] args) throws IOException {
		int port = 49000;
		String filename = "";
		Hashtable<String, Double> ncpair = new Hashtable<String, Double>();
		ArrayList<String> neighborslist = new ArrayList<String>();
		Hashtable<String, Vector<DV>> Host = new Hashtable<String, Vector<DV>>();		

		port = Integer.parseInt(args[0]);
		filename = args[1];	
		if(port <= 1024) {
			System.out.println("Port numbers above 1024 is preferred.");
			System.exit(-1);
		}			

		
		new ServerThread(port, filename, ncpair, neighborslist, Host).start(); 
		new ClientThread(filename, ncpair, neighborslist, Host).start();
	}
}
