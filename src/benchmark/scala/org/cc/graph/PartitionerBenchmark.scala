package org.cc.graph


import org.scalatest.FunSuite

// Benchmarking implementation
// Note : each test is running a fixed count of iteration of the algorithm (10) when just a few ones may be enough in practice.
class PartitionerBenchmark extends FunSuite {

  import Splitter._
  import Partitioner._

  test("Partitioning a bigger random graph in 2") {
    // Generating 4 independent graphs of equal size : 10^5 nodes, 2.5*10^6 connections each
    val N = 100000
    val C = 25 * N
    val graph =
      GraphGenerator.generate(1,N,C) ++ GraphGenerator.generate(N+1,N,C) ++
      GraphGenerator.generate(2*N+1,N,C) ++ GraphGenerator.generate(3*N+1,N,C)

    timing("Partitioning a bigger random graph in 2") {
      val parts = partition2(graph)(split2)
      val (l1, r1) = parts.head
      assert(crossCount2(graph)(l1, r1) > 1000000) // more than 10^6 cross connection at the beginning
      val (l2, r2) = parts.drop(10).head
      assert(crossCount2(graph)(l2, r2) < 100) // reduced to less than 100 connections
    }
  }

  test("Partitioning a big random graph in 3") {
    // Generating 3 independent graphs of equal size : 10^5 nodes, 2.5*10^6 connections each
    val N = 100000
    val C = 25 * N
    val graph =
      GraphGenerator.generate(1,N,C) ++ GraphGenerator.generate(N+1,N,C) ++
      GraphGenerator.generate(2*N+1,N,C)

    timing("Partitioning a big random graph in 3") {
      val parts = partition(graph)(split)(3)
      val start = parts.head
      println("Initial cross-connection = " + crossCount(graph)(start))
      assert(crossCount(graph)(start) > 1000000) // more than 5 * 10^6 cross connection at the beginning
      val end = parts.drop(10).head
      println("Final cross-connection = " + crossCount(graph)(end))
      assert(crossCount(graph)(end) < 100) // reduced to less than 100
    }
  }

  ignore("Partitioning a big random graph in 3 (2)") {
    // Generating 4 independent graphs of equal size : 10^5 nodes, 2.5*10^6 connections each
    val N = 100000
    val C = 25 * N
    val graph =
      GraphGenerator.generate(1,N,C) ++ GraphGenerator.generate(N+1,N,C) ++
      GraphGenerator.generate(2*N+1,N,C) ++ GraphGenerator.generate(3*N+1,N,C)

    timing("Partitioning a big random graph in 3 (2)") {
      val parts = partition(graph)(split)(3)
      val start = parts.head
      println("Initial cross-connection = " + crossCount(graph)(start))
      assert(crossCount(graph)(start) > 5000000) // more than 5 * 10^6 cross-connection at the beginning
      val end = parts.drop(10).head
      println("Final cross-connection = " + crossCount(graph)(end))
      assert(crossCount(graph)(end) < 2500000) // reduced to less than 2.5 * 10^6
    }
  }

  ignore("Partitioning a big random graph in 10 (2)") {
    // Generating 4 independent graphs of equal size : 10^5 nodes, 2.5*10^6 connections each
    val N = 100000
    val C = 25 * N
    val graph =
      GraphGenerator.generate(1,N,C) ++ GraphGenerator.generate(N+1,N,C) ++
      GraphGenerator.generate(2*N+1,N,C) ++ GraphGenerator.generate(3*N+1,N,C)

    timing("Partitioning a big random graph in 10") {
      val parts = partition(graph)(split)(10)
      val start = parts.head
      println("Initial cross-connection = " + crossCount(graph)(start))
      assert(crossCount(graph)(start) > 5000000) // more than 5 * 10^6 cross-connection at the beginning
      val end = parts.drop(10).head
      println("Final cross-connection = " + crossCount(graph)(end))
      assert(crossCount(graph)(end) < 2500000) // reduced to less than 2.5 * 10^6
    }
  }

  private def timing(desc: String)(block: => Unit): Unit = {
    val start = System.currentTimeMillis
    block
    val end = System.currentTimeMillis
    println(s"${desc} executed in ${(end-start) / 1000} sec(s)")
  }
}

