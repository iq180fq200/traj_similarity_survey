import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel
import TrajectoryPackage._
import AuxiFunPackage._
import RDD._

object Main {
  def main(args:Array[String])={
    val conf=new SparkConf().setAppName("TrajectorySearch").set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
//         val conf=new SparkConf().setAppName("TrajectorySearch").setMaster("local[4]")
//           .set("spark.driver.host", "localhost")
//          .set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
    val sc=new SparkContext(conf)
    sc. setLogLevel("ERROR")
    val timer=new MyTimer()
    val paramConfig=argsDeal(args)

    val rawTrajRDD=sc
      .textFile(paramConfig.rawDatasetFilePath)
      .zipWithIndex().map(x=>FileDeal.getDataFromTxt(x,args(0).toFloat))
      .filter(_.point.length >= 6)
      .filter(_.point.length <= 1000)
    val trajNum=rawTrajRDD.count()
    println(s"the number of rawTraj  is ${trajNum}")

    val queryTraj=TrajectoryMatric(sc
      .textFile(paramConfig.queryDatasetFilePath)
      .zipWithIndex.map(x=>FileDeal.getDataFromTxt(x,args(0).toFloat))
      .filter(_.point.length >= 6)
      .filter(_.point.length <= 1000)
      .collect())
    println(s"the number of queryTraj  is ${queryTraj.trajectory.size}")

    val distributeIndex=new TrieRDD(rawTrajRDD,paramConfig.partitionNum)
    val packPartitionRDD=distributeIndex.buildDistributeIndex(paramConfig)
    packPartitionRDD.persist(StorageLevel.MEMORY_AND_DISK_SER)
    packPartitionRDD.count()

    //output result
    val dataOutBuff=new collection.mutable.ArrayBuffer[(String,String)]()
    var allSearchTime=0.0
    timer.restart()
    var searchResult:Array[Array[MutableMyPair[Double,Int]]]=null
    for( i <- 0 until paramConfig.repeatNum) {
      searchResult=distributeIndex.batchDistributeSearchKnn(sc,queryTraj,packPartitionRDD,paramConfig)
    }
    allSearchTime+=timer.elapsed()
    println(s"search time is ${timer.elapsed()}ms")
    dataOutBuff+=("数据集表示"->paramConfig.dataset)
    dataOutBuff+=("网格宽度"->paramConfig.gama.toString)
    dataOutBuff+=("分区数目"->paramConfig.partitionNum.toString)
    dataOutBuff+=("度量方式"->paramConfig.measure.toString)
    dataOutBuff+=("重复次数"->paramConfig.repeatNum.toString)
    dataOutBuff+=("knn数目"->paramConfig.knn.toString)
    dataOutBuff+=("Pivot数目"->paramConfig.pivotNum.toString)
    dataOutBuff+=("数据集大小"->trajNum.toString)
    dataOutBuff+=("节点总数为"->paramConfig.NodeNum.toString)
    dataOutBuff+=("索引时间"->paramConfig.indexTime.toString)
    dataOutBuff+=("查询轨迹数目"->queryTraj.trajectory.size.toString)
    dataOutBuff+=("平均查询时间"->(allSearchTime/(paramConfig.repeatNum*queryTraj.trajectory.size)).toString)
    dataOutBuff_ToFile(dataOutBuff,paramConfig.queryResultFilePath)
  }

  def dataOutBuff_ToFile(dataOutBuff:collection.mutable.ArrayBuffer[(String,String)],filePath:String)={
    import java.io._
    val fileIter=new FileWriter(filePath,true)
    fileIter.write("本次查询结果为：")
    fileIter.write("\n")
    dataOutBuff.foreach(in=>{
      fileIter.write(s"${in._1}       ")
    })
    fileIter.write("\n")
    dataOutBuff.foreach(in=>{
      fileIter.write(s" ${in._2}       ")
    })
    fileIter.write("\n")
    fileIter.close()
  }
  def argsDeal(args:Array[String]):Config={
    println(s"输入参数个数为:${args.size}")
    //    val config=if(args.size!=15) {
    //      args.foreach(x=>println(x))
    //      println("请正确输入参数：数据集标识 数据集路径 查询集路径 结果路径 网格宽度gama 分区数目 knn数目 度量方式 重复查询次数 Pivot数目 最小经度 最大经度 最小纬度 最大纬度 是否优化Trie ")
    //      println("请强制结束程序")
    //      while(true){}
    //      Config(gama=0.1,knn=100,partitionNum = 4)
    //    }
    val config=if(args.size!=7) {
      args.foreach(x=>println(x))
      println("请正确输入参数：采样率 数据集路径 query数据集路径 最小经度 最大经度 最小纬度 最大纬度")
      //113.5,37.4,118.3,42
      println("请强制结束程序")
      while(true){}
      Config(gama=0.1,knn=100,partitionNum = 4)
    }
    else{
      val tmpConfig=Config(gama=0.1,knn=50,partitionNum = 64,
        repeatNum =1 ,pivotNum=5)
      tmpConfig.dataset="dataset"
      tmpConfig.rawDatasetFilePath=args(1)
      tmpConfig.queryDatasetFilePath=args(2)
      tmpConfig.queryResultFilePath="result"
      tmpConfig.measure="DTW"
      tmpConfig.globalTrajRange(0)=args(3).toDouble
      tmpConfig.globalTrajRange(1)=args(4).toDouble
      tmpConfig.globalTrajRange(2)=args(5).toDouble
      tmpConfig.globalTrajRange(3)=args(6).toDouble
      tmpConfig.optimalTree=1
      println("数据集标识为："+tmpConfig.dataset)
      println("数据集路径为："+tmpConfig.rawDatasetFilePath)
      println("查询集路径为："+tmpConfig.queryDatasetFilePath)
      println("结果路径："+tmpConfig.queryResultFilePath)
      println("网格宽度gama为："+tmpConfig.gama)
      println("分区数目为："+tmpConfig.partitionNum)
      println("knn数目为："+tmpConfig.knn)
      println("度量方式为："+tmpConfig.measure)
      println("重复查询次数为："+tmpConfig.repeatNum)
      println("Pivot数目："+tmpConfig.pivotNum)
      println("minLong："+tmpConfig.globalTrajRange(0))
      println("maxLong："+tmpConfig.globalTrajRange(1))
      println("minLati："+tmpConfig.globalTrajRange(2))
      println("maxLati："+tmpConfig.globalTrajRange(3))
      println("是否优化Trie："+tmpConfig.optimalTree)
      tmpConfig
    }
    config
  }

}


