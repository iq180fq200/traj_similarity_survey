The project is run on Spark2.2.0, scala2.10 and hadoop2.6.

**Build** 

In main directory, you can use maven to build project with command "mvn package" and dependencies will be automatically downloaded.



**Run**

Run it by feeding the package to spark-submit with parameters <sample rate> <candidate trajectory file path> <query trajectory file path> <min_long> <max_long> <min_lat> <max_lat>

NOTE: the trajectory length is decided by different  ${candidate trajectory file path}

eg. spark-submit --class Main --master spark://node188:7077 TrajecorySearch-1.0-SNAPSHOT-jar-with-dependencies.jar 1.0 hdfs://node188:4399/hanxi/data/dita&repose/geolife/freespace_data50000 hdfs://node188:4399/hanxi/data/dita&repose/geolife/queryData.txt 113.5 118.3 37.4 42
