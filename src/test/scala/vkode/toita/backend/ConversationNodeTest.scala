package vkode.toita.backend

import org.junit.{Assert, Test}
import Assert._

class ConversationNodeTest {

  implicit def toBig (i: Int) = BigInt(i)

  case class Item(id: BigInt, name: String, timestamp: Long) extends Treeable

  @Test def buildSimple {
    val tree = TreeBuilder[Item](List(BigInt(1), BigInt(2)),
                                 Map(BigInt(1) -> Item(1, "one", 1000),
                                     BigInt(2) -> Item(2, "two", 2000),
                                     BigInt(3) -> Item(3, "three", 3000),
                                     BigInt(4) -> Item(4, "four", 4000),
                                     BigInt(5) -> Item(5, "five", 5000)),
                                 Map(BigInt(2) -> List(BigInt(3), BigInt(4)),
                                     BigInt(4) -> List(BigInt(5)))).build
    val nodes = tree.nodes

    assertEquals(2, nodes.size)
    assertTrue(nodes(0).subnodes.isEmpty)
    assertEquals(2, nodes(1).subnodes.size)

    assertEquals(1, nodes(0).nodeCount)
    assertEquals(4, nodes(1).nodeCount)

    assertEquals(1000, nodes(0).latest)
    assertEquals(5000, nodes(1).latest)

    assertEquals(List("one"), nodes(0).names)
    assertEquals(List("two", "three", "four", "five"), nodes(1).names)

    assertEquals(5, tree.nodeCount)
    assertEquals(5000, tree.latest)
    assertEquals(List("one", "two", "three", "four", "five"), tree.names)
  }
}
