package DISON.Partitioners
import DISON.Index.LocalIndex
import DISON.common.Trajectory

import scala.collection.mutable
/**
 * @author hanxi
 * @date 2022/3/13 15 42
 *       discription
 */
case class PackedPartition(partitionID:Int, data:mutable.HashMap[BigInt,Trajectory], localIndex: LocalIndex, LongestLength:Double){



}
