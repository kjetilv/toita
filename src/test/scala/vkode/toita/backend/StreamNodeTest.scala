package vkode.toita.backend

import org.junit.{Assert, Test}
import Assert._

class StreamNodeTest {

  implicit def toBig (i: Int) = BigInt(i)

  case class Item(id: BigInt, name: String, timestamp: Long) extends Treeable

  @Test def buildSingle {
    val tree = TStream[Item](List(BigInt(1)),
                                  Map(BigInt(1) -> Item(1, "one", 1000)),
                                  Map[BigInt, List[BigInt]]())
    assertEquals(1, tree.items.size)
    assertEquals(1, tree.items(0).nodeCount)
  }

  @Test def buildSimple {
    val conversation = TStream[Item](List(BigInt(1), BigInt(2)),
                                          Map(BigInt(1) -> Item(1, "one", 1000),
                                              BigInt(2) -> Item(2, "two", 2000),
                                              BigInt(3) -> Item(3, "three", 3000),
                                              BigInt(4) -> Item(4, "four", 4000),
                                              BigInt(5) -> Item(5, "five", 5000)),
                                          Map(BigInt(2) -> List(BigInt(3), BigInt(4)),
                                              BigInt(4) -> List(BigInt(5))))
    val tree = conversation.tree
    val items = conversation.tree.nodes

    assertEquals(2, items.size)
    assertTrue(items(0).subnodes.isEmpty)
    assertEquals(2, items(1).subnodes.size)

    assertEquals(1, items(0).nodeCount)
    assertEquals(4, items(1).nodeCount)

    assertEquals(1000, items(0).latest)
    assertEquals(5000, items(1).latest)

    assertEquals(Set("one"), items(0).names)
    assertEquals(Set("two", "three", "four", "five"), items(1).names)

    assertEquals(5, tree.nodeCount)
    assertEquals(5000, tree.latest)
    assertEquals(Set("one", "two", "three", "four", "five"), tree.names)
  }
}
