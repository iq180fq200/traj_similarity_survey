package DISON.Algorithms

import DISON.Index.{GlobalIndex, LocalIndex}
import DISON.Partitioners.{GlobalPartitioner, PackedPartition}
import DISON.common.{DISONConfigConstants, Segment, Trajectory}
import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.rdd.{PartitionPruningRDD, RDD}
import org.apache.spark.sql.SparkSession

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
/**
 * @author hanxi
 * @date 2022/3/14 13 01
 *       discription
 */

object SearchAlgorithms {
  case class PackedIndexPrunnedFlag(prunned:Boolean,index:Int){}
  case class AnsTraWithDistance(tra:Trajectory,dis:Double){}
  private def GetIntersectionByOrder(segArray:Array[Segment],segSet:Set[Segment]):ArrayBuffer[Segment]={
    var interSegArray=new ArrayBuffer[Segment]()
    segArray foreach {seg=>
      if(segSet.contains(seg))
        interSegArray.append(seg)
    }
    interSegArray
  }

  private def my_contains(ele:Array[Long],ref_eles:Array[Array[Long]]):Boolean={
    for(ele1<-ref_eles){
      if(ele1(0)==ele(0) && ele1(1)==ele(1))
        return true
    }
    return false
  }

  /*
   * @param null
   * @return null
   * @Description:Get the partitions that may have similar trajectories
   */
  private def GlobalPrunning(sparkSession: SparkSession, query: Trajectory, packedPartitions:RDD[PackedPartition],globalIndex: GlobalIndex):ArrayBuffer[Int]={
    //1. get sf, all the start nodes and distance within the range
    val headID=query.sNode
    var start=System.currentTimeMillis()
    val sf=MinDistanceSearch.DjistraSearch(headID,(1-DISONConfigConstants.THRESHOLD_LIMIT)/DISONConfigConstants.THRESHOLD_LIMIT*query.length)

    //2. get sl, all the end nodes and distance within the range
    val lastID=query.eNode
    val sl=MinDistanceSearch.DjistraSearch(lastID,(1-DISONConfigConstants.THRESHOLD_LIMIT)/DISONConfigConstants.THRESHOLD_LIMIT*query.length)

    var time=System.currentTimeMillis()-start
    Logger.getLogger("program").warn(s"sf and sl searching Time: ${time} ms")

    //3. get the first level group who has common element with sf, that is has a trajectory-first-node common with a node in sf
    val sf_node=sf.keySet
    val _groupL1ID=sf_node.map(node=>globalIndex.indexL1.get(node))//which L1 groups are get
    val groupL1ID=_groupL1ID.map(x=>x.getOrElse(-1)).filter(x=>x!= -1)
    val sl_node=sl.keySet
    //4.get candidate partition
    var candidatePartition=new ArrayBuffer[Int]()
    for(ele<-groupL1ID){
      //compute the min start distance of sNodes in ele(one of the selected L1 group), it must have something common with sf
      val snodesInGroup:Array[Int]=globalIndex.GetsNodesInL1Group(ele)
      val df=sf.filterKeys(snodesInGroup.contains).values.min
      //get the L2 group ID
      val groupL2ID=sl_node.map(node=>globalIndex.indexL2(ele).get(node)).map(x=>x.getOrElse(-1)).filter(x=>x != -1)
      for(ele1<-groupL2ID){
        // it must contains the intended node
        val enodesInGroup:Array[Int]=globalIndex.GeteNodesInL2Group(ele,ele1)
        val dl=sl.filterKeys(enodesInGroup.contains).values.min
        val partitionID=GlobalPartitioner.GetPartitionIDWithGroupID(ele,ele1)
        val boundary1=(1-DISONConfigConstants.THRESHOLD_LIMIT)/(DISONConfigConstants.THRESHOLD_LIMIT)*query.length
        val boundary2=(1-DISONConfigConstants.THRESHOLD_LIMIT)/(1+DISONConfigConstants.THRESHOLD_LIMIT)*(query.length+packedPartitions.filter(x=>x.partitionID==partitionID).map(x=>x.LongestLength).collect()(0))
        if(df+dl<=boundary1 && (df+dl)<=boundary2)
          candidatePartition.append(partitionID)
      }
    }
   var end=System.currentTimeMillis()
//    LOG.warn(s"global pruning Time: ${end - start} ms")
    candidatePartition
  }
//  private def LocalKNNCandidate(packedPartitionIter: Iterator[PackedPartition],query:Trajectory,sparkSession: SparkSession):ArrayBuffer[AnsTraWithDistance]={
//    var CandidateTrajectories=ArrayBuffer[BigInt]()//candidate set getting from inverted index
//    var KNNCandidateAnswers=ArrayBuffer[AnsTraWithDistance]()//the answer
//    val _packedPartition=packedPartitionIter.toArray
//    val packedPartition=_packedPartition(0)//the data
//    var D=0// the max D to compare is (1-thereshold)*|Q|
//    import scala.util.control._
//    val loop = new Breaks;
//    val loop1=new Breaks;
//    loop.breakable{
//      query.segments foreach { seg =>
//        if(D>(1-DISONConfigConstants.THRESHOLD_LIMIT)*query.length)
//          loop.break()//the trajectories must already be returned
//        else{
//          loop1.breakable{
//            //get inverted list I(q_i) for signature q_i
//            val invIndexOfSeg=packedPartition.localIndex.localInvertedIndex.get(seg.sid) match {
//              case Some(index) => index
//              case None => loop1.break() //equal to continue
//            }
//            invIndexOfSeg foreach {
//              index =>
//                if (!CandidateTrajectories.contains(index._1)) { // if the trajectory is not in candidate set
//                  val preCandidateTrajectory=packedPartition.data.get(index._1) match{
//                    case None=>println("error! find inverted index but not find the corresponding trajectory!"); throw new Error("unconsistency error")
//                    case Some(trajectory)=>trajectory
//                  } //get the trajectory corresponding to the segment
//                  val boundary = (1 - DISONConfigConstants.THRESHOLD_LIMIT) / (1 + DISONConfigConstants.THRESHOLD_LIMIT) * (preCandidateTrajectory.length + query.length)
//                  if (D + index._2 <= boundary) {
//                    CandidateTrajectories.append(index._1)
//                    val R_1=preCandidateTrajectory.segments
//                    val R_2=query.segments
//                    val Ts=R_1.filter(seg=>R_2.contains(seg))
//                    val Qs=R_2.filter(seg=>R_1.contains(seg))
//                    val v=DPSimilarityAlgorithms.DPSimilarityValue(Ts,Qs,sparkSession.sqlContext)
//                    if (v>=DISONConfigConstants.THRESHOLD_LIMIT) {
//                      KNNCandidateAnswers.append(new AnsTraWithDistance(preCandidateTrajectory,v))
//                    }
//                  }
//                }
//            }
//          }
//
//        }
//
//
//      }
//    }
//   KNNCandidateAnswers
//
//  }

  private def LocalFilterAndRefine(packedPartitionIter: Iterator[PackedPartition],query:Trajectory,sparkSession: SparkSession): ArrayBuffer[Trajectory] ={
    var CandidateTrajectories=ArrayBuffer[BigInt]()//candidate set getting from inverted index
    var FinalAnswerTrajectories=ArrayBuffer[Trajectory]()//the answer
    val _packedPartition=packedPartitionIter.toArray
    val packedPartition=_packedPartition(0)//the data
    var D=0// the max D to compare is (1-thereshold)*|Q|
    import scala.util.control._
    val loop = new Breaks;
    val loop1=new Breaks;
    loop.breakable{
      query.segments foreach { seg =>
        if(D>(1-DISONConfigConstants.THRESHOLD_LIMIT)*query.length)
          loop.break()//the trajectories must already be returned
        else{
          loop1.breakable{
            //get inverted list I(q_i) for signature q_i
            val invIndexOfSeg=packedPartition.localIndex.localInvertedIndex.get(seg.sid) match {
              case Some(index) => index
              case None => loop1.break() //equal to continue
            }
              invIndexOfSeg foreach {
                index =>
                  if (!CandidateTrajectories.contains(index._1)) { // if the trajectory is not in candidate set
                    val preCandidateTrajectory=packedPartition.data.get(index._1) match{
                      case None=>println("error! find inverted index but not find the corresponding trajectory!"); throw new Error("unconsistency error")
                      case Some(trajectory)=>trajectory
                    } //get the trajectory corresponding to the segment
                    val boundary = (1 - DISONConfigConstants.THRESHOLD_LIMIT) / (1 + DISONConfigConstants.THRESHOLD_LIMIT) * (preCandidateTrajectory.length + query.length)
                    if (D + index._2 <= boundary) {
                      CandidateTrajectories.append(index._1)
                      val R_1=preCandidateTrajectory.segments
                      val R_2=query.segments
                      val Ts=R_1.filter(seg=>R_2.contains(seg))
                      val Qs=R_2.filter(seg=>R_1.contains(seg))
                      if (DPSimilarityAlgorithms.DPJudgeSimilar(Ts,Qs,sparkSession.sqlContext)) {
                        FinalAnswerTrajectories.append(preCandidateTrajectory)
                      }
                    }
                  }
              }
          }

        }


      }
    }
    FinalAnswerTrajectories
  }
  def ThresholdSearch(sparkSession: SparkSession, query: Trajectory, packedPartitions:RDD[PackedPartition],globalIndex: GlobalIndex): Array[BigInt] ={
    val bQuery=sparkSession.sparkContext.broadcast(query)
    //global prunning
    val candidatePartitions=GlobalPrunning(sparkSession,query, packedPartitions, globalIndex)
    //local prunning
    var start=System.currentTimeMillis()
    val answer=PartitionPruningRDD.create(packedPartitions,candidatePartitions.contains).mapPartitions(iter=>{val Finalanswers=LocalFilterAndRefine(iter,query,sparkSession)
    Finalanswers.iterator}).map(tra=>tra.Tid).collect()
    var end=System.currentTimeMillis()
//    LOG.warn(s"local prunning Time: ${end - start} ms")//job3
    answer
  }
//  def KNNSearch(sparkSession: SparkSession, query: Trajectory, packedPartitions:RDD[PackedPartition],globalIndex: GlobalIndex,k:Int): Array[BigInt] ={
//    val bQuery=sparkSession.sparkContext.broadcast(query)
//    //global prunning
//    val candidatePartitions=GlobalPrunning(sparkSession,query, packedPartitions, globalIndex)
//    //local prunning
//    var start=System.currentTimeMillis()
//    val _CandidateAnswers=PartitionPruningRDD.create(packedPartitions,candidatePartitions.contains).mapPartitions(iter=>{val Finalanswers=LocalKNNCandidate(iter,query,sparkSession)
//      Finalanswers.iterator}).sortBy(-_.dis);
//    var CandidateAnswers:Array[AnsTraWithDistance]=Array[AnsTraWithDistance]();
//    if (_CandidateAnswers.count()>=k){
//     CandidateAnswers=_CandidateAnswers.take(k);
//    }
//      else{
//      CandidateAnswers=_CandidateAnswers.collect();
//    }
//    CandidateAnswers foreach(println)
//    var end=System.currentTimeMillis()
//    //    LOG.warn(s"local prunning Time: ${end - start} ms")//job3
//   CandidateAnswers.map(_.tra.Tid)
//  }
}
