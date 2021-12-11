package rushb.parser.lambda

import org.apache.logging.log4j.LogManager
import rushb.model.DemoLink

object ErrorHandler {
  private val log = LogManager.getLogger(ErrorHandler.getClass)
  def handle(link: DemoLink, err: String): Unit = {
    log.error(s"Error occurred while downloading from $link. Error message: $err")
  }
}
