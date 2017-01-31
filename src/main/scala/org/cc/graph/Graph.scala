package org.cc.graph

object Graph {

  type In[A] = List[A]
  type Out[A] = List[A]
  type Node[A] = (In[A],A,Out[A])
  type Nodes[A] = List[Node[A]]
  type Graph[A] = Map[A,Node[A]]

  // Graph operations
  def in[A](node: Node[A]): In[A] = node._1
  def label[A](node: Node[A]): A = node._2
  def out[A](node: Node[A]): Out[A] = node._3

  def in[A](g: Graph[A])(node: A): In[A] = in(g(node))
  def label[A](g: Graph[A])(node: A): A = label(g(node))
  def out[A](g: Graph[A])(node: A): Out[A] = out(g(node))

  def inDegree[A](g: Graph[A])(node: A): Int =
    in(g)(node).size
  def outDegree[A](g: Graph[A])(node: A): Int =
    out(g)(node).size


  // Graph manipulation

  def empty[A] = Map[A,Node[A]]()

  // Directed connection
  def isConnected[A](g: Graph[A])(a: A)(b: A): Boolean =
    if(g.contains(a) && g.contains(b)) out(g(a)).contains(b) else false

  // Adds a node to a graph if it doesn't exist
  def add[A](g: Graph[A])(a: A): Graph[A] =
    if(g.contains(a)) g else g + (a -> (Nil,a,Nil))

  // Connect 2 nodes together within a graph, if they are not already connected
  // Directed connection : a -> b
  def connect[A](g: Graph[A])(a: A)(b: A): Graph[A] =
    if (isConnected(g)(a)(b)) g
    else {
      val safe = add(add(g)(a))(b)
      val (ina,_,outa) = safe(a)
      val (inb,_,outb) = safe(b)
      safe ++ Map(a -> (ina, a, b :: outa), b -> (a :: inb, b, outb))
    }
}
