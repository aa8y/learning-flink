package co.aa8y.learning.streaming.flink

import java.util.Properties

import scala.collection.JavaConverters._

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{createTypeInformation, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.twitter.TwitterSource
import org.apache.flink.util.Collector

import co.aa8y.learning.streaming.common.model.Tweet

case class HashtagCount(hashtag: String, count: Long)

object HashtagCountJob {
  def main(args: Array[String]) {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val consumerKey = sys.env("TWITTER_API_KEY")
    val consumerSecret = sys.env("TWITTER_API_SECRET")
    val token = sys.env("TWITTER_ACCESS_TOKEN")
    val tokenSecret = sys.env("TWITTER_ACCESS_TOKEN_SECRET")

    val props = new Properties()
    props.setProperty(TwitterSource.CONSUMER_KEY, consumerKey)
    props.setProperty(TwitterSource.CONSUMER_SECRET, consumerSecret)
    props.setProperty(TwitterSource.TOKEN, token)
    props.setProperty(TwitterSource.TOKEN_SECRET, tokenSecret)

    val streamSource = env.addSource(new TwitterSource(props))
    streamSource
      .flatMap { msg => Tweet.parse(msg) }
      .keyBy(_.lang)
      .flatMap(new HashtagCountProcessor())
  }
}

class HashtagCountProcessor extends RichFlatMapFunction[Tweet, String] {
  var hashtagCount: MapState[String, Long] = _

  override def flatMap(tweet: Tweet, hastagCollector: Collector[String]) {
    val hashtags = tweet.entities.hashtags.map(_.text)
    hashtags.foreach { hashtag =>
      val count = if (hashtagCount.contains(hashtag)) hashtagCount.get(hashtag) + 1 else 1
      hashtagCount.put(hashtag, count)
      hastagCollector.collect(hashtag)
    }
  }

  override def open(params: Configuration) {
    hashtagCount = getRuntimeContext.getMapState(
      new MapStateDescriptor[String, Long](
        "state.hashtagCount",
        createTypeInformation[String],
        createTypeInformation[Long]
      )
    )
  }
}
