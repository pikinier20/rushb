package rushb.parser.lambda

import com.amazonaws.services.s3.model.ObjectMetadata
import rushb.model.DemoLink

object SuccessHandler {
  import rushb.utils.AWSUtils._
  def handle(link: DemoLink, index: Int, res: String): Unit = {
    val metadata = new ObjectMetadata()
    metadata.setContentType("application/json")
    s3.putObject(bucketName, parsedDemo(link.id, index), res)
  }
}
