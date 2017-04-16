package fr.mdulac.repository

import java.time.ZonedDateTime

import fr.mdulac.model.Punchline
import org.scalacheck.Gen.oneOf
import play.api.libs.json.Json.parse

import scala.io.Source.fromResource

object PunchlinesRepository {

  lazy val punchlines = parse(fromResource("punchlines.json").mkString).as[List[Punchline]]

  def findAll = punchlines

  def findByArtist(artist: String) = punchlines.filter(_.artist.toLowerCase == artist.toLowerCase)

  def random = oneOf(punchlines).sample.get

  def forDate(date: ZonedDateTime) = punchlines((date.toEpochSecond % punchlines.length).toInt)

}
