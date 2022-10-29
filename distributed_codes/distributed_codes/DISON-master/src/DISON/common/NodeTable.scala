package DISON.common
import DISON.common.NodeTable.{NODETABLE, hashNodeTable, hashNodeTableOnMaster}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SparkSession}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable
case class NodeTable(sparksc:SQLContext,mapFilePath:String){
  val df = sparksc.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true") .option("delimiter",",").load(mapFilePath)
  df.createOrReplaceTempView("dff")
  val df1=sparksc.sql("SELECT node as Node,lng as lon,lat from dff")
  NODETABLE=df1
  NODETABLE.rdd.collect().map({
    item=>
      val s1=item.getAs[Int]("Node")
      val s2=item.getAs[Double]("lon")
      val s3=item.getAs[Double]("lat")
      hashNodeTableOnMaster.put(s1,(s2,s3))
  })
  hashNodeTable=sparksc.sparkContext.broadcast(hashNodeTableOnMaster)
}
object NodeTable{
  var NODETABLE:DataFrame=null
  var hashNodeTableOnMaster=mutable.HashMap[Int,(Double,Double)]()
  var hashNodeTable:Broadcast[mutable.HashMap[Int,(Double,Double)]]=null
}