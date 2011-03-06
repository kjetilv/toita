package vkode.toita.backend

import vkode.toita.events.{StreamNode, Treeable}

case class Discussion[T <: Treeable](nodes: List[StreamNode[T]])
