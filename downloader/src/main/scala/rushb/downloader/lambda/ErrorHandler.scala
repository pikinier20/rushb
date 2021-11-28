package rushb.downloader.lambda

import rushb.model.DemoLink
import org.apache.logging.log4j.{LogManager, Logger}

object ErrorHandler {
  val log = LogManager.getLogger(ErrorHandler.getClass)
  def handle(link: DemoLink, err: String): Unit = {
    log.error(s"Error occurred while downloading from $link. Error message: $err")
  }
}
