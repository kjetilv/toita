package bootstrap.liftweb

import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.provider._
import net.liftweb.sitemap._
import vkode.toita.model._
import akka.actor.Actor
import Actor._
import vkode.toita.backend.ToitaCentral

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  val central = actorOf[ToitaCentral].start

  def boot {
    import LiftRules._

    addToPackages("vkode.toita")

    /*
     * Build SiteMap
     */
    setSiteMapFunc(() => SiteMap(Menu("Login") / "login",
                                 Menu("Home") / "index",
                                 Menu("User") / "user"))

    /*
     * Show the spinny image when an Ajax call starts
     */
    ajaxStart = Full(() => jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    ajaxEnd = Full(() => jsArtifacts.hide("ajax-loader").cmd)

    early.append(makeUtf8)

    loggedInTest = Full(() => User.loggedIn_?)

    central.start

    unloadHooks.append(() => central.stop)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req setCharacterEncoding "UTF-8"
  }
}
