package vkode.toita.events

case class TOEntities(hashtags: List[TOHashtag],
                      mentions: List[TOMention],
                      urls: List[TOURL])
