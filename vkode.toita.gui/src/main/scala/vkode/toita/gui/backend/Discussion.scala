package vkode.toita.gui.backend

import vkode.toita.events.{StreamNode, Treeable}

case class Discussion[T <: Treeable](nodes: List[StreamNode[T]])
