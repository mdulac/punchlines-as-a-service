package fr.mdulac.model

import play.api.libs.json.Json

case class Punchline(punchline: String, artist: String, album: Option[String], title: Option[String]) {

  def toJson = Json.toJson(this)

  def toJsonString = toJson.toString()

  def htmlAuthor = title
    .filterNot(_.isEmpty)
    .map(t => s"<u>$t</u>, $artist")
    .getOrElse(s"$artist")

}

object Punchline {

  implicit val reads = Json.reads[Punchline]
  implicit val writes = Json.writes[Punchline]

}