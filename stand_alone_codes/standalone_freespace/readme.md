to run the code, first install the maven, then do the following steps in the same directory as this readme file:
1. mvn compile
2. mvn exec:java -Dexec.mainClass=main -Dexec.args=\<data directory>\<threshold for EDR and LCSS>
   
    eg1. mvn exec:java -Dexec.mainClass=main -Dexec.args="D:\trajectory_similarity\stand_alone_data\GeolifeData 100"
   
    eg2. mvn exec:java -Dexec.mainClass=main -Dexec.args="D:\trajectory_similarity\stand_alone_data\AISData 30000"
   
    eg3. mvn exec:java -Dexec.mainClass=main -Dexec.args="AISData 30000"


   
