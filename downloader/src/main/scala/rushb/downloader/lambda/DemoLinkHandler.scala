package rushb.downloader.lambda

import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import rushb.downloader.DemoDownloader
import rushb.model.DemoLink

import scala.jdk.CollectionConverters._
import upickle.default._

class DemoLinkHandler extends RequestHandler[SQSEvent, Void] {
  override def handleRequest(input: SQSEvent, context: Context): Void = {
    val links = input.getRecords.asScala.toList
      .map(_.getBody)
      .map(json => read[DemoLink](json))
    links.foreach { l => new DemoDownloader(l).download() match {
        case Right(value) => SuccessHandler.handle(l, value)
        case Left(value) => ErrorHandler.handle(l, value)
      }
    }
    null
  }


}
