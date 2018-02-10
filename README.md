# DistanceVectorProtocols
To run this project:
After making changes to the Hosts file in Windows/Linux system, 

Go to src folder and compile
$ javac ClientThread.java DV.java DVR.java RTable.java ServerThread.java


Open 6 different windows for the 6 different nodes (A,B,C,D,E,F) given in the Project graph.
and run the following in each terminals:

$ java DVR a.dat 6001
$ java DVR b.dat 6002
$ java DVR c.dat 6003
$ java DVR d.dat 6004
$ java DVR e.dat 6005
$ java DVR f.dat 6006

After the network is initialized, refresh takes place after every 15 seconds, and the costs are updated.
