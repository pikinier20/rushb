package rushb.crawler.lambda

import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.amazonaws.services.sqs.model.SendMessageRequest
import monix.execution.Scheduler
import org.apache.logging.log4j.LogManager
import rushb.crawler.{HltvHtmlCrawler, QuerySettings}
import rushb.model.DemoLink

import java.io.{InputStream, OutputStream}
import java.util.concurrent.ForkJoinPool
import java.util.{Calendar, Date}
import scala.concurrent.Await
import scala.concurrent.duration._
import upickle.default._

class LambdaHandler extends RequestStreamHandler {
  private implicit val scheduler: Scheduler = {
    val pool = new ForkJoinPool(4)
    Scheduler(pool)
  }
  private val log = LogManager.getLogger(getClass)
  private val calendar = Calendar.getInstance()

  private val initialDate = calendar.getTime.getTime

  // Delay is set to 30 days which is 30 days * 24 hours * 3600 seconds * 1000 millis
  private val delayDays: Long = 30
  private val delay: Long = delayDays * 24 * 3600 * 1000

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val querySettings = calculateQuerySettings
    val crawler = new HltvHtmlCrawler(querySettings)
    val future = crawler.getLinks.foreach {
      case Left(value) => log.error(value)
      case Right(value) => putLinkToSqs(value)
    }
    Await.result(future.map(_ => log.info("Crawling finished")), 5.minutes)
  }

  def calculateQuerySettings: QuerySettings = {
    log.info(s"Current date: ${new Date(initialDate).toString}")
    log.info(s"Querying demos with delay: $delayDays days.")
    log.info(s"Demos date: ${new Date(initialDate - delay).toString}")
    import QuerySettings.formatDate
    QuerySettings(from = Some(initialDate - delay), to = Some(initialDate - delay))
  }

  def putLinkToSqs(link: DemoLink): Unit = {
    import rushb.utils.AWSUtils._
    val json = write(link)
    val request = new SendMessageRequest()
    request.setMessageGroupId("1")
    request.setMessageBody(json)
    request.setQueueUrl(crawlerQueueName)

    sqs.sendMessage(request)
  }
}
