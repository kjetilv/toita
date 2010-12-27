package vkode.toita.backend

import java.util.Date
import org.junit.{Assert, Test}

class RenderStuff {

  @Test def renderMentionHashes {
    val text = "hey @zip #zot foobar #zip fussball"
    val atIdx = text indexOf "@"
    val hIdx1 = text indexOf "#"
    val hIdx2 = text lastIndexOf "#"
    val tsu = Rendrer renderStatuses  (List(TwitterStatusUpdate(TOStatus(10,
                                                                         text,
                                                                         false,
                                                                         Some("foo"),
                                                                         false,
                                                                         new Date()),
                                                                None,
                                                                Some(TOUser(100,
                                                                            "me",
                                                                            "Me",
                                                                            Some("en"),
                                                                            10,
                                                                            "http://www.vg.no")),
                                                                TOEntities(List(TOHashtag(List(hIdx1, hIdx1 + 4), "#zot"),
                                                                                TOHashtag(List(hIdx2, hIdx2 + 4), "#zip")),
                                                                           List(TOMention(10, "zip", "Zip", List(atIdx + 1, atIdx + 4))),
                                                                           Nil),
                                                                None)))
    println(tsu)
  }

  @Test def renderPlain {
    val tsu = Rendrer renderStatuses  (List(TwitterStatusUpdate(TOStatus(10,
                                                                         "foobar",
                                                                         false,
                                                                         Some("foo"),
                                                                         false,
                                                                         new Date()),
                                                                None,
                                                                Some(TOUser(100,
                                                                            "me",
                                                                            "Me",
                                                                            Some("en"),
                                                                            10,
                                                                            "http://www.vg.no")),
                                                                TOEntities(Nil,
                                                                           Nil,
                                                                           Nil),
                                                                None)))
  }

  @Test def renderReply {
    val tsu = Rendrer renderStatuses  (List(TwitterStatusUpdate(TOStatus(10,
                                                                         "@zip foobar",
                                                                         false,
                                                                         Some("foo"),
                                                                         false,
                                                                         new Date()),
                                                                None,
                                                                Some(TOUser(100,
                                                                            "me",
                                                                            "Me",
                                                                            Some("en"),
                                                                            10,
                                                                            "http://www.vg.no")),
                                                                TOEntities(Nil,
                                                                           Nil,
                                                                           Nil),
                                                                None)))
    println(tsu)
  }

  @Test def renderCivix {
    val tsu = Rendrer renderStatuses  (List(TwitterStatusUpdate(TOStatus(BigInt("16483093779062784", 10),
                                                                         "@CiViX Lovteksten er vel forandret minst fem ganger siden 1992. Hysteri og rastloshet. Jeg skal kladde en ny lovtekst. @Olvew",
                                                                         false,
                                                                         Some("""<a href="http://www.tweetdeck.com" rel="nofollow">TweetDeck</a>"""),
                                                                         false,
                                                                         new Date()),
                                                                None,
                                                                Some(TOUser(49590734,
                                                                            "Madsws",
                                                                            "Mads Wam Schneider",
                                                                            Some("en"),
                                                                            13623,
                                                                            "http://a2.twimg.com/profile_images/1097295142/Madsws_normal.jpeg")),
                                                                TOEntities(List(),
                                                                           List(TOMention(9550672,
                                                                                          "Gunnar R Tjomlid",
                                                                                          "CiViX",List(0, 6)),
                                                                                TOMention(67617143,
                                                                                          "Olve Wold",
                                                                                          "Olvew",
                                                                                          List(119, 125))),
                                                                           List()),
                                                                Some(TOReply(BigInt("16479574028197888", 10),9550672, "CiViX")))))
    println(tsu)
  }

  @Test def renderUrl {
    val tsu = Rendrer renderStatuses  (List(TwitterStatusUpdate(TOStatus(BigInt("16469778663211008", 10),
                                                                         "Pa bloggen na: Strom - http://bit.ly/giVQdm",
                                                                         false,
                                                                         Some("&lt;a href=&quot;http://twitoaster.com&quot; rel=&quot;nofollow&quot;&gt;Twitoaster&lt;/a&gt;"),
                                                                         false,
                                                                         new Date()),
                                                                None,
                                                                Some(TOUser(14417779,
                                                                            "sigvei",
                                                                            "Sigve Indregard",
                                                                            Some("en"),
                                                                            2257,
                                                                            "http://a3.twimg.com/profile_images/1128768899/41411_861520108_1603_n_normal.jpg")),
                                                                TOEntities(List(),
                                                                           List(),
                                                                           List(TOURL(List(23, 43),"http://bit.ly/giVQdm"))),
                                                                Some(TOReply(null,null,null)))))
    println (tsu)
  }

  @Test def renderMention {
    val text = "foobar @zip"
    val atIdx = text indexOf "@"
    val tsu = Rendrer renderStatuses  (List(TwitterStatusUpdate(TOStatus(10,
                                                                         text,
                                                                         false,
                                                                         Some("foo"),
                                                                         false,
                                                                         new Date()),
                                                                None,
                                                                Some(TOUser(100,
                                                                            "me",
                                                                            "Me",
                                                                            Some("en"),
                                                                            10,
                                                                            "http://www.vg.no")),
                                                                TOEntities(Nil,
                                                                           List(TOMention(10, "zip", "Zip", List(atIdx + 1, atIdx + 4))),
                                                                           Nil),
                                                                None)))
    println(tsu)
  }

  @Test def renderSjetilv {
    val tsu = Rendrer renderStatuses List(TwitterStatusUpdate(TOStatus(BigInt("16453482592600065", 10),
                                                                       "@hogrim par for the course",
                                                                       false,
                                                                       Some("""<a href="http://www.echofon.com/" rel="nofollow">Echofon</a>"""),
                                                                       false,
                                                                       new Date()),
                                                              None,
                                                              Some(TOUser(55487753,
                                                                          "sjetilv",
                                                                          "Sjetil Wahlstavehd",
                                                                          Some("en"),
                                                                          9646,
                                                                          """http://a3.twimg.com/profile_images/610112767/Screen_shot_2009-10-29_at_15.25.06_normal.png""")),
                                                              TOEntities(List(),
                                                                         List(),
                                                                         List()),
                                                              Some(TOReply(BigInt("16450228211154944", 10),
                                                                           BigInt("14276602", 10),
                                                                           "hogrim"))))
    println (tsu)
  }

  private def parseAndRender(json: String): Unit = {
    import ParseStuff._
    val event = parse(json).get.asInstanceOf[TwitterStatusUpdate]
    val nodes = Rendrer render RenderableStatus(event, 0)
    Assert.assertEquals("Bad render: " + nodes,
                        2, nodes.size)
  }

  @Test def parseAndRenderMentionURL = parseAndRender(ParseStuff.mention_url)

  @Test def parseAndRenderRTTag = parseAndRender(ParseStuff.rt_tag)

  @Test def parseAndRenderLongRTTag = parseAndRender(ParseStuff.longRt)
}
