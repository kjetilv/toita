import sbt._

class LiftProject(info: ProjectInfo) extends DefaultWebProject(info) {

  override def repositories = super.repositories +
                              ("Scala-Tools Dependencies Repository for Releases" at "http://scala-tools.org/repo-releases") +
                              ("Scala-Tools Dependencies Repository for Snapshots" at "http://scala-tools.org/repo-snapshots") +
                              ("Akka Maven2 Repository" at "http://akka.io/repository/") +
                              ("Multiverse Maven2 Repository" at "http://multiverse.googlecode.com/svn/maven-repository/releases") +
                              ("GuiceyFruit Maven2 Repository" at "http://guiceyfruit.googlecode.com/svn/repo/releases/") +
                              ("JBoss Maven2 Repository" at "https://repository.jboss.org/nexus/content/groups/public/")

  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  type Mapper = String => String

  val defaultMapper: Mapper = string => string

  def sbtDepend(group: String, version: String, artifacts: String*) {
    sbtDepend(group, version, defaultMapper, artifacts: _*)
  }

  def sbtDepend(group: String, version: String, mapper: Mapper, artifacts: String*) {
    artifacts map (artifact => group %% mapper(artifact) % version)
  }

  def depend(group: String, version: String, artifacts: String*) {
    depend(group, version, defaultMapper, artifacts: _*)
  }

  def depend(group: String, version: String, mapper: Mapper, artifacts: String*) =
    artifacts map (artifact => group % mapper(artifact) % version)

  sbtDepend("net.liftweb", "2.2", "lift-" + _, "mapper", "actor", "json", "webkit")
  sbtDepend("com.googlecode.scalaz", "5.0", "scalaz-" + _, "core", "http")

  depend("se.scalablesolutions.akka", "1.0-RC3", "akka-" + _, "actor", "core")
//  depend("com.eaio", "3.2", "uuid")
  depend("com.h2database", "1.2.138", "h2")
  depend("junit", "4.8.2", "junit")
  depend("org.scribe", "1.0.9", "scribe")
  depend("org.apache.httpcomponents", "4.0.3", "httpclient")
  depend("joda-time", "1.6.2", "joda-time", "1.6.2")
  depend("org.mortbay.jetty", "6.1.25", "jetty")

  // uncomment the following if you want to use the snapshot repo
  // val scalatoolsSnapshot = ScalaToolsSnapshots

  //  val localMaven = ("local maven" at (System.getProperty("user.home") + "/.m2/repository"))

  // If you're using JRebel for Lift development, uncomment
  // this line
  // override def scanDirectories = Nil

}
