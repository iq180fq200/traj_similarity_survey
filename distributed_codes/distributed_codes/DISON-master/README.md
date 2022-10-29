The project is run on Spark2.2.0, scala2.10 and hadoop2.6.

**Build** 

In main directory, you can use maven to build project with command "mvn package" and dependencies will be automatically downloaded.



**Run**

Run it by feeding the package to spark-submit with parameters <trajectory number rate>  <sample rate><threshold limit> <data_directory>
eg:spark-submit --conf spark.driver.memory=80g --class ExampleApp --master spark://node188:7077 DISON-1.0-SNAPSHOT-jar-with-dependencies.jar 1.0 1.0 0.8 /hanxi/data/dison/porto
