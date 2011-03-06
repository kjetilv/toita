package vkode.toita.events

case class TOReply(in_reply_to_status_id: BigInt,
                   in_reply_to_user_id: BigInt,
                   in_reply_to_screen_name: String)
