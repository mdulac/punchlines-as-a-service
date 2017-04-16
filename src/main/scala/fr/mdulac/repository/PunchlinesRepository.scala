package fr.mdulac.repository

import fr.mdulac.model.Punchline
import play.api.libs.json.Json.parse

import scala.io.Source.fromResource
import scala.util.Random

object PunchlinesRepository {

  lazy val punchlines = parse(fromResource("punchlines.json").mkString).as[List[Punchline]]

  def findAll = punchlines

  def findByArtist(artist: String) = punchlines.filter(_.artist.toLowerCase == artist.toLowerCase)

  def random = punchlines(Random.nextInt(punchlines.length))

}
