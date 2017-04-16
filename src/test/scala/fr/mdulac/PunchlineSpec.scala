package fr.mdulac

import fr.mdulac.model.Punchline
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json.parse

import scala.io.Source.fromResource

class PunchlineSpec extends FlatSpec with Matchers {

  "A Punchlines file" should "be a correct json value" in {
    parse(fromResource("punchlines.json").mkString).as[List[Punchline]]
  }

  "A Punchlines html author" should "be with underlying title and artist name" in {
    // Given
    val p = Punchline("punchline", "artist", None, Some("title"))

    // When
    val author = p.htmlAuthor

    // Then
    author shouldBe "<u>title</u>, artist"
  }

  "A Punchlines html author" should "be with only artist name" in {
    // Given
    val p = Punchline("punchline", "artist", None, None)

    // When
    val author = p.htmlAuthor

    // Then
    author shouldBe "artist"
  }

}
