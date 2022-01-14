package rushb.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, Message, ReceiveMessageRequest}
import com.amazonaws.services.sqs.{AmazonSQS, AmazonSQSClientBuilder}
import org.apache.logging.log4j.LogManager

import scala.jdk.CollectionConverters._

object AWSUtils {
  val region: Regions = Regions.EU_WEST_1
  private val log = LogManager.getLogger(getClass)

  def bucketName: String = "csgo-demos-rushb"
  def parsedBucketName: String = "csgo-parsed-demos-rushb"
  def dataLakeBucketName: String = "csgo-datalake-rushb"
  val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(region).build()

  def crawlerQueueName: String = "https://sqs.eu-west-1.amazonaws.com/743262912284/rushb-queue.fifo"
  def downloaderQueueName: String = "https://sqs.eu-west-1.amazonaws.com/743262912284/rushb-parser-queue.fifo"
  val sqs: AmazonSQS = AmazonSQSClientBuilder.standard().withRegion(region).build()

  def demoLink(id: String) = s"demolink${id}.json"
  def demo(id: String) = s"demo${id}"
  def parsedDemo(id: String, index: Int) = s"parsedDemo${id}_$index.json"

  def longPollMessages(queueLink: String)(op: String => Unit): Unit = {
    while(true) {
      val receiveReceipt = new ReceiveMessageRequest()
        .withQueueUrl(queueLink)
        .withWaitTimeSeconds(20)
        .withMaxNumberOfMessages(1)
      println("Polling messages...")
      val messages = sqs.receiveMessage(receiveReceipt).getMessages.asScala.toList
      println(s"Message polled. Number of messages: ${messages.length}")
      messages.foreach { m =>
        try {
          val start = System.currentTimeMillis() / 1000
          println(s"START: $start")
          op(m.getBody)
          val end = System.currentTimeMillis() / 1000
          println(s"END: $end")
          val deleteReceipt = new DeleteMessageRequest()
            .withQueueUrl(queueLink)
            .withReceiptHandle(m.getReceiptHandle)
          sqs.deleteMessage(deleteReceipt)
        } catch {
          case e: Exception =>
            println(e)
            log.error(e)
        }
      }
      println("Message processed")
    }
  }
}
