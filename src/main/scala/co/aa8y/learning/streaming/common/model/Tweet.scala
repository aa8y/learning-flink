package co.aa8y.learning.streaming.common.model

import scala.util.Try

import org.json4s.{DefaultFormats, FieldSerializer}
import org.json4s.FieldSerializer.renameFrom
import org.json4s.jackson.JsonMethods.{ parse => parseJson }

case class Tweet(
  id: Long,
  text: String,
  truncated: Boolean,
  retweetCount: Int,
  createdAt: String,
  timestamp: String,
  entities: Entities,
  lang: Option[String],
  favoriteCount: Option[Int],
  favorited: Option[Boolean]
) {
}

object Tweet {
	lazy val formats = DefaultFormats +
    FieldSerializer[Tweet](deserializer =
      renameFrom("created_at", "createdAt") orElse
      renameFrom("favorite_count", "favoriteCount") orElse
      renameFrom("retweet_count", "retweetCount") orElse
      renameFrom("timestamp_ms", "timestamp")) +
    FieldSerializer[Entities](deserializer = renameFrom("user_mentions", "userMentions")) +
    FieldSerializer[UserMention](deserializer = renameFrom("screen_name", "screenName"))

  def parse(json: String): Option[Tweet] = {
    implicit val formats = Tweet.formats
    Try(parseJson(json).extract[Tweet]).toOption
  }
}

case class Entities(hashtags: Seq[Hashtag], userMentions: Seq[UserMention])
case class Hashtag(text: String, indices: Seq[Int])
case class UserMention(id: Long, screenName: String, name: String, indices: Seq[Int])
