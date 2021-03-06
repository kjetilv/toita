package vkode.toita.gui.backend

import java.util.Date
import org.junit.{Assert, Test}
import net.liftweb.json.JsonAST.JNothing
import vkode.toita.events._

class RenderStuff {

  private val deco = UserDeco(None, false, None, Some("1C351D"), Some("005500"), Some("A0C5C7"), Some("000000"))

  private def render (tsu: TwitterStatusUpdate) =
    Rendrer renderStatusStream (List(StreamItem(tsu, 0, 0, 0, Set[String](), Set[String](), Set[String]())),
          <span/>)

  @Test def renderMentionHashes {
    val text = "hey @zip #zot foobar #zip fussball"
    val atIdx = text indexOf "@"
    val hIdx1 = text indexOf "#"
    val hIdx2 = text lastIndexOf "#"
    val tsu = render(TwitterStatusUpdate(TOStatus(10, text),
                                         TOMeta(false,
                                                None,
                                                false,
                                                "source",
                                                false,
                                                new Date(),
                                                ""),
                                         TOUser(UserData(100,
                                                         "me",
                                                         "Me",
                                                         Some("'scription")),
                                                deco),
                                         None,
                                         TOEntities(List(TOHashtag(List(hIdx1, hIdx1 + 4), "#zot"),
                                                         TOHashtag(List(hIdx2, hIdx2 + 4), "#zip")),
                                                    List(TOMention(10, "zip", "Zip", List(atIdx + 1, atIdx + 4))),
                                                    Nil),
                                         None,
                                         false,
                                         "sjetilv",
                                         JNothing))
    println(tsu)
  }

  @Test def renderPlain {
    val tsu = render(TwitterStatusUpdate(TOStatus(10, "foobar"),
                                         TOMeta(false,
                                                None,
                                                false,
                                                "foo",
                                                false,
                                                new Date,
                                                ""),
                                         TOUser(UserData(100,
                                                         "me",
                                                         "Me",
                                                         None),
                                                deco),
                                         None,
                                         TOEntities(Nil,
                                                    Nil,
                                                    Nil),
                                         None,
                                         false,
                                         "sjetilv",
                                         JNothing))
  }

  @Test def renderReply {
    val tsu = render(TwitterStatusUpdate(TOStatus(10, "@zip foobar"),
                                         TOMeta(false,
                                                None,
                                                false,
                                                "foo",
                                                false,
                                                new Date,
                                                ""),
                                         TOUser(UserData(100,
                                                         "me",
                                                         "Me",
                                                         None),
                                                deco),
                                         None,
                                         TOEntities(Nil,
                                                    Nil,
                                                    Nil),
                                         None,
                                         false,
                                         "sjetilv",
                                         JNothing))
    println(tsu)
  }

  @Test def renderCivix {
    val tsu = render(TwitterStatusUpdate(TOStatus(BigInt("16483093779062784", 10),
                                                  "@CiViX Lovteksten er vel forandret minst fem ganger siden 1992. Hysteri og rastloshet. Jeg skal kladde en ny lovtekst. @Olvew"),
                                         TOMeta(false,
                                                None,
                                                false,
                                                """<a href="http://www.tweetdeck.com" rel="nofollow">TweetDeck</a>""",
                                                false,
                                                new Date,
                                                ""),
                                         TOUser(UserData(49590734,
                                                         "Madsws",
                                                         "Mads Wam Schneider",
                                                         None),
                                                deco copy (profile_image_url = Some("http://a2.twimg.com/profile_images/1097295142/Madsws_normal.jpeg"))),
                                         None,
                                         TOEntities(List(),
                                                    List(TOMention(9550672,
                                                                   "Gunnar R Tjomlid",
                                                                   "CiViX",List(0, 6)),
                                                         TOMention(67617143,
                                                                   "Olve Wold",
                                                                   "Olvew",
                                                                   List(119, 125))),
                                                    List()),
                                         Some(TOReply(BigInt("16479574028197888", 10),9550672, "CiViX")),
                                         false,
                                         "sjetilv",
                                         JNothing))
    println(tsu)
  }

  @Test def renderUrl {
    val tsu = render(TwitterStatusUpdate(TOStatus(BigInt("16469778663211008", 10),
                                                  "Pa bloggen na: Strom - http://bit.ly/giVQdm"),
                                         TOMeta(false,
                                                None,
                                                false,
                                                "&lt;a href=&quot;http://twitoaster.com&quot; rel=&quot;nofollow&quot;&gt;Twitoaster&lt;/a&gt;",
                                                false,
                                                new Date,
                                                ""),
                                         TOUser(UserData(14417779,
                                                         "sigvei",
                                                         "Sigve Indregard",
                                                         None),
                                                deco copy (profile_image_url = Some("http://a3.twimg.com/profile_images/1128768899/41411_861520108_1603_n_normal.jpg"))),
                                         None,
                                         TOEntities(List(),
                                                    List(),
                                                    List(TOURL(List(23, 43),"http://bit.ly/giVQdm"))),
                                         Some(TOReply(null,null,null)),
                                         false,
                                         "sjetilv",
                                         JNothing))
    println (tsu)
  }

  @Test def renderMention {
    val text = "foobar @zip"
    val atIdx = text indexOf "@"
    val tsu = render(TwitterStatusUpdate(TOStatus(10, text),
                                         TOMeta(false,
                                                None,
                                                false,
                                                "foo",
                                                false,
                                                new Date,
                                                ""),
                                         TOUser(UserData(100,
                                                         "me",
                                                         "Me",
                                                         None),
                                                deco copy (profile_image_url = Some("http://www.vg.no"))),
                                         None,
                                         TOEntities(Nil,
                                                    List(TOMention(10, "zip", "Zip", List(atIdx + 1, atIdx + 4))),
                                                    Nil),
                                         None,
                                         false,
                                         "sjetilv",
                                         JNothing))
    println(tsu)
  }

  @Test def renderSjetilv {
    val tsu =
      render(TwitterStatusUpdate
                 (TOStatus(BigInt("16453482592600065", 10),
                           "@hogrim par for the course"),
                  TOMeta(false,
                         None,
                         false,
                         """<a href="http://www.echofon.com/" rel="nofollow">Echofon</a>""",
                         false,
                         new Date,
                         ""),
                  TOUser(UserData(55487753,
                                  "sjetilv",
                                  "Sjetil Wahlstavehd",
                                  None),
                         deco copy (profile_image_url = Some("""http://a3.twimg.com/profile_images/610112767/Screen_shot_2009-10-29_at_15.25.06_normal.png"""))),
                  None,
                  TOEntities(List(),
                             List(),
                             List()),
                  Some(TOReply(BigInt("16450228211154944", 10),
                               BigInt("14276602", 10),
                               "hogrim")),
                  false,
                  "sjetilv",
                  JNothing))
    println (tsu)
  }

  private def parseAndRender(json: String): Unit = {
    import ParseStuffData._
    val event = parse(json).get.asInstanceOf[TwitterStatusUpdate]
    val nodes = Rendrer renderStatusStream (List(StreamItem(event, 0, 0, 0, Set[String](), Set[String](), Set[String]())), <span/>)
    Assert.assertEquals("Bad render: " + nodes,
                        2, nodes.size)
  }

  @Test def parseAndRenderMentionURL = parseAndRender(ParseStuffData.mention_url)

  @Test def parseAndRenderRTTag = parseAndRender(ParseStuffData.rt_tag)

  @Test def parseAndRenderLongRTTag = parseAndRender(ParseStuffData.longRt)
}
