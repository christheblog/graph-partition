# Graph Partition


## An algorithm to partition a graph's set of nodes into equal sized buckets

This algorithm is an attempt to find (probably rediscover !) an algorithm to split the nodes of a given graph into `n` partitions (also call buckets) of equal size, 
while minimizing the number of cross-buckets connections.

An application of this could be answering questions like :
- how do I distribute a system of `x` location-transparent actors on `n` servers, 
in order to minimize network traffic (that is, maximize intra-JVM messages).


The implementation (and thinking !) of this algorithm is still at a very early stage, 
but first test results on some randomly generated graph seem encouraging.<br>
Performances (especially the `n buckets` case are not good - but several code optimisation are still possible, including paralelisation)



## The algorithm : `partition2`

The base implementation of the algorithm is partitioning nodes of a in 2 equal parts (called left and right).
```scala
def partition2[A](g: Graph[A])(split : Splitter2[A]): Stream[(Nodes[A], Nodes[A])]
```
Algorithm works as follow :
<ol>
<li>Randomly and evenly distribute nodes between a left and a right bucket
<li>For each node in each bucket compute its gain - which can be interpreted as follow :<br>
How many cross-connections (connection between the left and right buckets) would we save if the node was on the opposite bucket.<br>
_Note : the gain measure can be negative_
<li>Sort the left and right buckets by gain in descending order
<li>Exchange nodes from left and right buckets, as long as the global gain is positive.<br>
The highest gain node from left is exchanged with the highest gain one from right.
<li>This is one iteration done. Repeat from 2.
</ol>

In practice just a few iterations seems to be enough to reach a significant reduction of the cross bucket connections.
The algorithm will reach a 'relative' convergence (Number of cross-bucket connections can oscillate - this is due to the 'batch' nature of the gain calculation)



## The algorithm : extending `partition2` to `n` buckets

The algorithm can naively be extended to `n` buckets, reusing the base implementation for 2 buckets.<br>
```scala
def partition[A](g: Graph[A])(split : Splitter[A])(n: Int): Stream[Buckets[A]]
```
`partition` for `n` buckets:
<ol>
<li>Randomly and evenly distribute nodes `n` buckets
<li>For each pair of buckets :<br>
apply one (or more) iteration of `partition2` to minimize the number of connection between the pair.
<li>This is one iteration done. Repeat from 2.
</ol>

The complexity of this version is exponential, due to the pair-wise bucket processing.<br><br>
_Note : It should be possible to implement a more generic version of `partition2` where the gain measure will help deciding what is the best possible redistribution of `n` nodes in the n current buckets._



## Possible improvements and straight-forward modifications

<ol>
<li>Gain measure could easily be adapted to consider weighted node connections.<br>
The algorithm will then attempt to minimize the global weight between the buckets, and not the number of connection.
<li>Allowing non-even partitions (to further reduce cross-bucket connections), ie having for instance the left bucket allowed to have up to 10% more nodes than the right one.<br>
This could be done by simply running a plain `partition2` algorithm until convergence, and then transfering 10% of the highest (and positive) gain node from one bucket to the other one.
<li>Parallelization of gain computation and possibly node swapping.
<li>Having a specialized version of `partition` for n buckets dealing only with powers of 2.<br>
This would allow to recursively split and optimize buckets with partition2.
</ol>