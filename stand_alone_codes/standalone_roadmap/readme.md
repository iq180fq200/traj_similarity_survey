to run the code, first install the maven, then do the following steps in the same directory as this readme file:
1. mvn complie

2. mvn exec:java -Dexec.mainClass=main -Dexec.args=<data directory><Lamda for TP><reference point ID for ERP><threshold for netEDR and netLCSS><number of candidate trajectories involved for the shape query experiment>
   eg1. mvn exec:java -Dexec.mainClass=main -Dexec.args="PortoData 0.2 1219 1000 10000"
   eg2.
   
   export MAVEN_OPTS="-Xmx100g -XX:MaxPermSize=100g"
   
    mvn exec:java -Dexec.mainClass=main -Dexec.args="TDrive_data 0.2 49568 1000 128935"





NOTE: the road distance matrix(roadDistance.txt) **is not** in the "road" sub-directory for the portoData, but you will have to use this file to run the project. The file is as large as 67G, you can generate it yourself before running the code.