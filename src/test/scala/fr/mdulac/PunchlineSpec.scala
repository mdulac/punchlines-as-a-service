package fr.mdulac

import fr.mdulac.model.Punchline
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json.parse

import scala.io.Source.fromResource

class PunchlineSpec extends FlatSpec with Matchers {

  "A Punchlines file" should "be a correct json value" in {
    parse(fromResource("punchlines.json").mkString).as[List[Punchline]]
  }

}
