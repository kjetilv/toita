package vkode.toita.backend

import org.specs.Specification
import org.specs.runner.{Runner, JUnit}

class ExtractJSONTest extends Runner(ExtractJSON) with JUnit

object ExtractJSON extends Specification {

  import net.liftweb.json._
  import JsonParser._

  implicit val formats = DefaultFormats

  val joe = """
    { "name" : "joe"
      "street" : "nonestreet"
      "city" : "oslo"
      "phone" : "99393939"
      "email" : "joe@nonestreet.oslo"
    }"""

  val titledJoe = """
    { "name" : "joe"
      "title" : "herr"
      "street" : "nonestreet"
      "city" : "oslo"
      "phone" : "99393939"
      "email" : "joe@nonestreet.oslo"
    }"""

  val structuredJoe = """
    { "name" : "joe"
      "title" : "herr"
      "address" : {
        "street" : "nonestreet"
        "city" : "oslo"
      },
      "contact" : {
        "phone" : "99393939"
        "email" : "joe@nonestreet.oslo"
      }
    }"""

  val awkwardJoe = """
    { "full_name" : "joe"
      "street_address" : "nonestreet"
    }"""

  case class BetterJoe(fullName: String, streetAddress: String)

  case class Joe(name: String, title: Option[String])

  case class ResidentJoe(name: String, title: Option[String], address: Address)

  case class OptionalResidentJoe(name: String, title: Option[String], address: Option[Address])

  case class StructuredJoe(name: String, title: Option[String], address: Address, contact: Contact)

  case class Address(street: String, city: String)

  case class Contact(phone: String, email: String)

  "Simple Joe extraction, with no title field" in {
    parse(joe).extract[Joe] mustEqual Joe("joe", None)
  }

  "Titled Joe extraction - Herr Joe" in {
    parse (titledJoe).extract[Joe] mustEqual Joe("joe", Some("herr"))
  }

  "Address extraction using address-related fields" in {
    parse(joe).extract[Address] mustEqual Address("nonestreet", "oslo")
  }

  "Contact extraction using contact-related fields" in {
    parse(joe).extract[Contact] mustEqual Contact("99393939", "joe@nonestreet.oslo")
  }

  "Optionally resident Joe extraction, with optional Address" in {
    parse (joe).extract[OptionalResidentJoe] mustEqual OptionalResidentJoe("joe", None, None)
  }

  "Extract Structured Joe" in {
    parse (structuredJoe).extract[StructuredJoe] mustEqual StructuredJoe("joe",
                                                                         Option("herr"),
                                                                         Address("nonestreet", "oslo"),
                                                                         Contact("99393939", "joe@nonestreet.oslo"))
  }

  "Extract better Joe" in {
    parse (awkwardJoe).extract[BetterJoe] mustEqual BetterJoe("joe", "nonestreet")
  }

  // Fails: (But would be very nifty if it worked)
  "Resident Joe extraction, with a real Address, fails" in {
    parse (joe).extract[ResidentJoe] mustEqual ResidentJoe("joe", None, Address("nonestreet", "oslo"))
  }

  // Fails: (But would be very nifty if it worked)
  "Resident, titled Joe extraction, with real Address, same as above" in {
    parse (titledJoe).extract[ResidentJoe] mustEqual ResidentJoe("joe", Some("herr"), Address("nonestreet", "oslo"))
  }
}
