import monix.execution.Scheduler.Implicits.global
import rushb.crawler.{HltvHtmlCrawler, QuerySettings}
import rushb.downloader.DemoDownloader
import rushb.model.DemoLink
import rushb.parser.{ContentType, DemoObject, Parser, Prepare}

import java.util.{Calendar, Date, UUID}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt


object Main extends App {
  private val calendar = Calendar.getInstance()

  private val initialDate = calendar.getTime.getTime

  // Delay is set to 30 days which is 30 days * 24 hours * 3600 seconds * 1000 millis
  private val delayDays: Long = 30
  private val delay: Long = delayDays * 24 * 3600 * 1000

  def calculateQuerySettings: QuerySettings = QuerySettings(initialDate - delay, initialDate - delay)

  val start = System.currentTimeMillis() / 1000
  println(s"START: $start")
  val crawler = new HltvHtmlCrawler(calculateQuerySettings)
  val x = crawler.getLinks.collect {
    case Right(value) => value
  }
    .map(e => new DemoDownloader(e).download())
    .collect {
      case Right(value) => value
    }
    .map(response => DemoObject(response.is, ContentType.parse(response.contentType), DemoLink(UUID.randomUUID().toString, UUID.randomUUID().toString, Math.random().toLong, UUID.randomUUID().toString)))
    .map(Parser.parseDemo)
    .toListL.runToFuture

  val links = Await.result(x, 9999.minutes)

  val end = System.currentTimeMillis() / 1000
  println(s"END: $end")
}