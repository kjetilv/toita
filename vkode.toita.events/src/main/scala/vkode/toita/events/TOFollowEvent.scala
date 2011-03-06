package vkode.toita.events

import java.util.Date

case class TOFollowEvent(target: TOUser,
                         source: TOUser,
                         event: String,
                         created_at: Date)
