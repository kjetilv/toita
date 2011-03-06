package vkode.toita.events

import vkode.toita.events.{StreamNode, TreeStat, StreamItem, Treeable}

case class Tree[T <: Treeable](nodes: List[StreamNode[T]]) extends TreeStat[T] {

  lazy val nodeCount = nodes match {
    case Nil => 0
    case nodes => nodes map (_.nodeCount) reduceLeft (_ + _)
  }

  lazy val latest = nodes match {
    case Nil => 0
    case nodes => (nodes map (_.latest) sortWith (_ > _)) (0)
  }

  lazy val names = (nodes flatten (_.names)).toSet

  lazy val namesOther = namesBelow

  lazy val namesBelow = names

  lazy val items = nodes flatten (_.items)
}















