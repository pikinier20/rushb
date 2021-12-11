package rushb.downloader
package lambda

import rushb.model.DemoLink
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.sqs.model.{ReceiveMessageRequest, SendMessageRequest}
import com.amazonaws.services.sqs.{AmazonSQS, AmazonSQSClientBuilder}
import upickle.default._

object SuccessHandler {
  import rushb.utils.AWSUtils._

  def handle(link: DemoLink, res: Response): Unit = {
    val linkJson = write(link)
    s3.putObject(bucketName, demoLink(link.id), linkJson)
    val metadata = new ObjectMetadata()
    metadata.setContentType(res.contentType)
    s3.putObject(bucketName, demo(link.id), res.is, metadata)

    val request = new SendMessageRequest()
    request.setMessageGroupId("1")
    request.setMessageBody(link.id)
    request.setQueueUrl(downloaderQueueName)

    sqs.sendMessage(request)
  }
}
