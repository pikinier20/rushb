package rushb.downloader

import org.apache.commons.io.FileUtils
import rushb.downloader.lambda.{ErrorHandler, SuccessHandler}
import rushb.model.DemoLink
import upickle.default._
import rushb.utils.AWSUtils._
import scala.jdk.CollectionConverters._

import java.nio.file.Paths

object Main extends App {
  def handleMessage(m: String): Unit = {
    val link: DemoLink = read[DemoLink](m)
    println(s"Downloading $link")
    
    val demoExists = s3.listObjects(bucketName).getObjectSummaries().asScala.toList.exists(_.getKey().contains(link.id))
    if(!demoExists) {
      new DemoDownloader(link).download() match {
        case Right(value) => SuccessHandler.handle(link, value)
        case Left(value) => ErrorHandler.handle(link, value)
      }
    }
  }

  longPollMessages(crawlerQueueName)(handleMessage)
}
