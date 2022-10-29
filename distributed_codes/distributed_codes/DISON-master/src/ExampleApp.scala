import DISON.Algorithms.SearchAlgorithms
import DISON.Index.{GlobalIndex, LocalIndex}
import DISON.Partitioners.{GlobalPartitioner, PackedPartition}
import DISON.common.{DISONConfigConstants, DistanceTable, EdgeTable, NodeTable, Segment, Trajectory}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable

object ExampleApp {
  /*
   * @param line representing a trajectory from the file
   * @return trajectory
   * @Description: construct trajectory RDD from the trajectory data file
   */
  private def getTrajectory(line: (String, Long),ratio:Double): Trajectory = {
    val _segments = line._1.stripPrefix("[").stripSuffix("]").replace(" ","").split(",").map(x => Segment(x.toInt))
    val segments=_segments.take(math.ceil(_segments.length*ratio).toInt)
    val a = Trajectory(segments, line._2)
    a
  }


  def main(args: Array[String]): Unit = {
    //turn off irrelavent logs
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

    //set the threshold
    DISONConfigConstants.THRESHOLD_LIMIT=args(2).toDouble

    //get the clock
    var start = System.currentTimeMillis()
    var end = start

    //read all the data and construct the road network tables(node and edge tables, trajectory RDD)
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()
    start = System.currentTimeMillis()
    val _EDGETABLE=EdgeTable(spark.sqlContext,args(3)+"edge_weight.csv")//job0
    val _NODETABLE=NodeTable(spark.sqlContext,args(3)+"node.csv")//job1
//    val _EDGETABLE = EdgeTable(spark.sqlContext, "src/resources/edge_weight.csv")
//    val _NODETABLE=NodeTable(spark.sqlContext, "src/resources/node.csv")
    end = System.currentTimeMillis()
    Logger.getLogger("program").warn(s"Road Map Reading Time: ${end - start} ms")



    //read the candidate and query trajectories
    start = System.currentTimeMillis()
    val trajs = spark.sparkContext
      .textFile(args(3) + "matched_path.txt")
//      .textFile("src/resources/matched_path.txt")
      .zipWithIndex().filter(_._1 != '\n').filter(_._1 != "[]").sample(false,args(0).toDouble).map (line => getTrajectory(line,args(1).toDouble));
    val queryTrajs = spark.sparkContext
      .textFile(args(3) + "query_path.txt")
//      .textFile("src/resources/query_path.txt")
      .zipWithIndex().map(line => getTrajectory(line,args(1).toDouble));




    //create key-value RDD of trajectory, where key is (sNode,eNode) of the trajetory
    val pairTrajs = trajs.map(traj => ((traj.sNode, traj.eNode), traj))
    pairTrajs.persist(StorageLevel.MEMORY_AND_DISK_SER)
    val keys = pairTrajs.keys

    val pairQueryTrajs = queryTrajs.map(traj => ((traj.sNode, traj.eNode), traj))
    pairQueryTrajs.persist(StorageLevel.MEMORY_AND_DISK_SER)
    println(s"Query Trajectory count: ${pairQueryTrajs.count()}") //job2
    println(s"Candidate Trajectory count: ${pairTrajs.count()}") //job3
    end = System.currentTimeMillis()
    Logger.getLogger("program").warn(s"trajectory Reading Time: ${end - start} ms")

    //create the global index according to the keys
    val globalIndex = GlobalIndex(keys, spark.sqlContext)


    //divide the trajectories according to the globalIndex
    val partRDD = pairTrajs.partitionBy(new GlobalPartitioner(globalIndex))

    //build local inverted index, and then every packed partition is an RDD with inverted index table, trajectory, longest
    //length of all the trajectories and partition ID
    //packedPatitions is an RDD with PackedPartition as its element. Actually, there is only one PackedPartition in each partition
    start = System.currentTimeMillis()
    val packedPartitions = partRDD.mapPartitionsWithIndex { case (partID, iter) =>
      val iterArrays = iter.toArray
      val _trajectorys = iterArrays.map(x => x._2)
      //from array to hash
      var trajectorys = mutable.HashMap[BigInt, Trajectory]()
      _trajectorys foreach {
        trajectory => trajectorys.put(trajectory.Tid, trajectory)
      }
      val localInvertedIndex = LocalIndex(_trajectorys, spark.sqlContext)
      val longestLength: Double = if (_trajectorys.isEmpty) 0.0 else _trajectorys.map(t => t.length).max
      Array(PackedPartition(partID, trajectorys, localInvertedIndex, longestLength)).iterator
    }
    packedPartitions.persist(StorageLevel.MEMORY_AND_DISK_SER)
    val a = packedPartitions.count()//to trigger the spark action so the time can be counted
    end = System.currentTimeMillis()
    Logger.getLogger("program").warn(s"Local Index Building Time: ${end - start} ms")
    Logger.getLogger("program").warn(s"total number of partitions is $a")


    //trajectory search for several arbitrarily choosed trajectories
    start = System.currentTimeMillis()
    val query=pairQueryTrajs.map(x=>x._2).first()
    val answer = SearchAlgorithms.ThresholdSearch(spark, query, packedPartitions, globalIndex)
    println(s"${answer.length} trajectories are similar to query");
    end = System.currentTimeMillis()
    Logger.getLogger("program").warn(s"Total Searching Time for 1 trajectory: ${end - start} ms")


  }


}
