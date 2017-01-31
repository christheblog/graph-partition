package org.cc.graph

import scala.collection.immutable.IndexedSeq

// Splitting a graph into buckets
object Splitter {

  import Graph._

  type Splitter2[A] = Graph[A] =>(Nodes[A], Nodes[A])
  type Buckets[A] = IndexedSeq[Nodes[A]]
  type Splitter[A] = (Graph[A],Int) => Buckets[A]

  // Splitting a graph in 2 buckets (left/right) of equal size +-1
  def split2[A](g: Graph[A]): (Nodes[A], Nodes[A]) =
    g.toList.map(_._2).splitAt(g.size / 2)

  // Split a graph in n buckets of nodes of equal size (+-1)
  def split[A](g: Graph[A], n: Int): Buckets[A] = {
    val buckets = Array.fill[Nodes[A]](n)(Nil)
    g.foldLeft((0,buckets)) { case ((curr,parts),(_,node)) =>
      buckets(curr) = node :: (buckets(curr))
      ((curr+1) % n, parts)
    }._2.toIndexedSeq
  }

}
