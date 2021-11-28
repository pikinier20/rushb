package rushb.downloader
package lambda

import rushb.model.DemoLink
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.sqs.{AmazonSQS, AmazonSQSClientBuilder}
import upickle.default._

object SuccessHandler {
  private val bucketName: String = "csgo-demos-rushb"
  private val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build()

  private val queueName: String = "rushb-queue.fifo"
  private val sqs: AmazonSQS = AmazonSQSClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build()

  def handle(link: DemoLink, res: Response): Unit = {
    val linkJson = write(link)
    s3.putObject(bucketName, s"demolink${link.id}.json", linkJson)
    val metadata = new ObjectMetadata()
    metadata.setContentType(res.contentType)
    s3.putObject(bucketName, s"demo${link.id}", res.is, new ObjectMetadata())

    sqs.sendMessage(queueName, link.id)
  }
}
