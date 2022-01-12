package rushb.downloader

import org.apache.commons.io.FileUtils
import rushb.downloader.lambda.{ErrorHandler, SuccessHandler}
import rushb.model.DemoLink
import upickle.default._
import rushb.utils.AWSUtils._

import java.nio.file.Paths

object Main extends App {
  def handleMessage(m: String): Unit = {
    val link = read[DemoLink](m)
    println(s"Downloading $link")
    new DemoDownloader(link).download() match {
      case Right(value) => SuccessHandler.handle(link, value)
      case Left(value) => ErrorHandler.handle(link, value)
    }
  }

  longPollMessages(crawlerQueueName)(handleMessage)
}
