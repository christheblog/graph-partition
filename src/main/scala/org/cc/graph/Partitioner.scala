package org.cc.graph

object Partitioner {

  import collection.immutable.Seq

  import Graph._
  import Splitter._


  // Generates a stream graph nodes splitted in left/right partitions
  // Each new stream element attempt to minimize the node connections between partitions
  //
  // Algorithm :
  // - Split nodes into 2 equal size buckets, left and right
  // - Compute for each node the "gain" of a swap, ie nb of left/right connections removed if we swap the node
  // - Swap a batch of nodes between left and right, keeping the balance of the partitions
  def partition2[A](g: Graph[A])(split : Splitter2[A]): Stream[(Nodes[A], Nodes[A])] = {
    val (left,right) = split(g)
    lazy val iter: Stream[(Nodes[A], Nodes[A])] =
      (left, right) #:: iter.map { case (l,r) => partition2(g,l,r) }
    iter
  }

  // Generates a stream graph nodes splitted in left/right partitions
  // Each new stream element attempt to minimize the node connections between partitions
  private[graph] def partition2[A](g: Graph[A], left: Nodes[A],right: Nodes[A]): (Nodes[A], Nodes[A]) = {
    val leftSet: Set[A] = left.map(_._2).toSet
    val rightSet: Set[A] = right.map(_._2).toSet

    // Number of connected nodes in the (left,right) parts
    def count(node: Node[A]): (Int,Int) = {
      val (in,_,out) = node
      val (inLeft,inRight) = in.partition(leftSet)
      val (outLeft,outRight) = out.partition(leftSet)
      (inLeft.size + outLeft.size, inRight.count(rightSet) + outRight.count(rightSet))
    }

    // TODO : make gain() a parameter ?
    // How many cross-connections would we save if the node was on the other side ?
    def gain(node: Node[A], count: (Int,Int)): Int = {
      val (l,r) = count
      if(leftSet(node._2)) r-l
      else l-r
    }

    val cgLeft = left.map { n => (n, gain(n,count(n))) }.sortBy(-_._2)
    val cgRight = right.map { n => (n, gain(n,count(n))) }.sortBy(-_._2)

    // Note :
    // we would have liked to use something more elegant like :
    //    val (newLeft, newRight) = (cgLeft zip cgRight).map(swapIfNecessary).unzip
    // but the length of the right and left partitions may not be equal - so zip could miss the last node
    // This lead to the following inelegant and much less efficient solution below :

    // Computing the number of node to swap
    // Condition is that : gain(left node) + gain(right node) > 0
    val swapCount = (cgLeft zip cgRight)
      .foldLeft(0) { case (acc, ((_,l),(_,r))) => if(l+r > 0) acc + 1 else acc }
    val (rl,rr) = cgRight.splitAt(swapCount)
    val (ll,lr) = cgLeft.splitAt(swapCount)
    val newLeft = (rl ++ lr).map(_._1)
    val newRight = (ll ++ rr).map(_._1)
    (newLeft, newRight)
  }

  // Count the number of crossing connections (ie connections between left and right part nodes)
  def crossCount2[A](g: Graph[A])(left: Nodes[A], right: Nodes[A]): Int = {
    val leftSet: Set[A] = left.map(_._2).toSet
    val rightSet: Set[A] = right.map(_._2).toSet

    // Number of connected nodes in the (left,right) parts
    def count(node: Node[A]): (Int,Int) = {
      val (in,_,out) = node
      val (inLeft,inRight) = in.partition(leftSet)
      val (outLeft,outRight) = out.partition(leftSet)
      (inLeft.size + outLeft.size, inRight.count(rightSet) + outRight.count(rightSet))
    }
    val res = left.map(count)
    left.map(count).foldLeft(0) { case (acc,(_,r)) => acc + r }
  }



  // Partitioning of a graph into n buckets reusing partition2
  // Partition a graph in n buckets of nodes of equal size, trying to minimize connections between buckets
  def partition[A](g: Graph[A])(split : Splitter[A])(n: Int): Stream[Buckets[A]] = {
    val buckets = split(g,n)
    lazy val iter: Stream[Buckets[A]] =
      buckets #:: iter.map { buckets => partition(g,buckets) }
    iter
  }

  // Naive implementation optimizing buckets pairwise
  // Partitioning of a graph into n buckets reusing partition2
  // Partition of a graph in n buckets of nodes of equal size, trying to minimize connections between buckets
  private[graph] def partition[A](g: Graph[A], buckets: Buckets[A]): Buckets[A] = {
    pairs(buckets).foldLeft(buckets) { case (bcks,(l,r)) =>
      val (nl,nr) = partition2(g,bcks(l),bcks(r))
      bcks.updated(l,nl).updated(r,nr)
    }
  }

  // Count the number of crossing connections (ie connections between a bucket and another one)
  def crossCount[A](g: Graph[A])(buckets: Buckets[A]): Int =
    pairs(buckets).foldLeft(0) { case (count,(l,r)) =>
      count + crossCount2(g)(buckets(l),buckets(r))
    }

  // Returns all the unordered pair of indices to get possible pair of buckets
  private[graph] def pairs[A](buckets: Seq[A]): Seq[(Int,Int)] =
    for(i <- 0 until buckets.size; j <- 0 until buckets.size if i < j) yield (i,j)

}
