package DISON.Algorithms

import DISON.common.{EdgeTable, NodeTable}

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable
import scala.util.control.Breaks

/**
 * @author hanxi
 * @date 2022/3/15 15 52
 *       discription
 */
object MinDistanceSearch {
  def GetRoadLiknedList():mutable.HashMap[Int,ArrayBuffer[(Int,Double)]]={
    var start=System.currentTimeMillis()
    val result=new mutable.HashMap[Int,ArrayBuffer[(Int,Double)]]
    EdgeTable.hashEdgeTable.value.map({item=>
      val s1=item._2._1
      val s2=item._2._2
      val s3=item._2._3
      if(result.contains(s1)){
        var currentArray=result.get(s1).getOrElse(ArrayBuffer[(Int,Double)]())
        currentArray.append((s2,s3))
      }
      else{
        result.put(s1,ArrayBuffer[(Int,Double)]((s2,s3)))
      }
    })
    var end=System.currentTimeMillis()
//    LOG.warn(s"road map linkedlist building Time: ${end - start} ms")
    result
  }
  def renewCurrentGraph(nodeAndDis: (Int, Double), currentGraph: mutable.HashMap[Int, (Double,Boolean)], currentDis: Double)={
    currentGraph.get(nodeAndDis._1) match {
      case Some(ele) => if(ele._1>currentDis+nodeAndDis._2) currentGraph.put(nodeAndDis._1,(currentDis+nodeAndDis._2,false))
      case None=> currentGraph.put(nodeAndDis._1,(currentDis+nodeAndDis._2,false))
    }
  }
  def DjistraSearch(originID:Int,upperBound:Double):mutable.HashMap[Int,Double]={
    val roadLinkedList:mutable.HashMap[Int,ArrayBuffer[(Int,Double)]]=GetRoadLiknedList()
    var currentGraph:mutable.HashMap[Int,(Double,Boolean)]=new mutable.HashMap[Int,(Double,Boolean)]()
    currentGraph.put(originID,(0.0,true))
    var currentNode=originID
    var currentLength=0.0
    var round=0
    val roundMax=roadLinkedList.size
    val loop=new Breaks
    loop.breakable{
      while(currentLength<=upperBound && round<roundMax){
        roadLinkedList.get(currentNode) match {
          case Some(ele) =>ele.map(el=>renewCurrentGraph(el,currentGraph,currentLength))
          case None=>
        }
        round+=1
        var _t=currentGraph.filter(_._2._2==false)
        var t:(Int,(Double,Boolean))=null
        if(_t.size==1)
          t=_t.head
        else if(_t.size==0)
          loop.break()
        else{
          t=_t.reduce((a,b)=>if(a._2._1<b._2._1) a else b)
        }
        currentNode=t._1
        currentLength=t._2._1
        currentGraph.put(currentNode,(currentLength,true))
      }
    }

    currentGraph.filter(_._2._1<=upperBound).map(x=>(x._1,x._2._1))
  }
}
