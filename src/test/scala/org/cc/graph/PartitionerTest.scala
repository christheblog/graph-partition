package org.cc.graph

import org.scalatest.FunSuite


class PartitionerTest extends FunSuite {

  import scala.collection.immutable.IndexedSeq

  import Graph._
  import Splitter._
  import Partitioner._

  //
  //    8 -----> 6
  //    |       / \
  //    +> 1 <-+   +-> 2 <-+
  //     /  \              |
  //    |    |             |
  // 5 <+    +-> 3         |
  //             /  \      |
  //            |    |    /
  //        4 <-+    +-> 7
  //
  val SimpleGraph: Graph[Int] = Map(
    1 -> (List(8,6),1,List(5,3)),
    2 -> (List(6,7),2,List()),
    3 -> (List(1),3,List(4,7)),
    4 -> (List(3),4,List()),
    5 -> (List(1),5,List()),
    6 -> (List(8),6,List(1,2)),
    7 -> (List(3),7,List(2)),
    8 -> (List(),8,List(1,6))
  )

  test("Cross-connection count for 2 partitions - 1") {
    def n(label: Int) = SimpleGraph(label)
    val left = List(n(1),n(3),n(4),n(5),n(6),n(8))
    val right = List(n(2),n(7))
    assert(2 === crossCount2(SimpleGraph)(left,right))
  }

  test("Cross-connection count for 2 partitions - 2") {
    def n(label: Int) = SimpleGraph(label)
    val left = List(n(1),n(3),n(4))
    val right = List(n(2),n(5),n(6),n(7),n(8))
    assert(4 === crossCount2(SimpleGraph)(left,right))
  }

  test("Cross-connection count for 2 partitions when right is empty") {
    def n(label: Int) = SimpleGraph(label)
    val left = List(n(1),n(3),n(4),n(5),n(6),n(2),n(7),n(8))
    val right = List()
    assert(0 === crossCount2(SimpleGraph)(left,right))
  }

  test("Cross-connection count for 2 partitions when left is empty") {
    def n(label: Int) = SimpleGraph(label)
    val left = List()
    val right = List(n(1),n(3),n(4),n(5),n(6),n(2),n(7))
    assert(0 === crossCount2(SimpleGraph)(left,right))
  }

  test("Cross-connection count for 3 partitions - 1") {
    def n(label: Int) = SimpleGraph(label)
    val buckets = IndexedSeq(
      List(n(1),n(3),n(4)),
      List(n(5)),
      List(n(6),n(2),n(7),n(8))
    )
    assert(4 === crossCount(SimpleGraph)(buckets))
  }

  test("Partitioning a simple graph in 2") {
    val parts = partition2(SimpleGraph)(split2)
    assert(parts.take(5).exists { case (l, r) => crossCount2(SimpleGraph)(l, r) == 3 })
  }

}
