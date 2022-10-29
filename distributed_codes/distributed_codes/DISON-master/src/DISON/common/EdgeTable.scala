package DISON.common

import DISON.common.EdgeTable.{EDGETABLE, hashEdgeTable, hashEdgeTableOnMaster}
import DISON.common.Segment.getSegLength
import org.apache.log4j.{Level, Logger}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SQLContext, SparkSession}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class EdgeTable(sparksc:SQLContext,mapFilePath:String){
  import sparksc.implicits._
  val df = sparksc.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true") .option("delimiter",",").load(mapFilePath)
  df.createOrReplaceTempView("dff")
  val df1=sparksc.sql("SELECT section_id,length,s_node as startNode,e_node as endNode from dff")
  EDGETABLE=df1
  EDGETABLE.rdd.collect().map({item=>
    val s0=item.getAs[Int]("section_id")
    val s1= item.getAs[Int]("startNode")
    val s2=item.getAs[Int]("endNode")
    val s3=item.getAs[Double]("length")
    hashEdgeTableOnMaster.put(s0,(s1,s2,s3))
  }
  )
  hashEdgeTable=sparksc.sparkContext.broadcast(hashEdgeTableOnMaster)
}

object EdgeTable{
  var EDGETABLE:DataFrame=null
  var hashEdgeTableOnMaster=mutable.HashMap[Int,(Int,Int,Double)]()
  var hashEdgeTable:Broadcast[mutable.HashMap[Int,(Int,Int,Double)]]=null
}