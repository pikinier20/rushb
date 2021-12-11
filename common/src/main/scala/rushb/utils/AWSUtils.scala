package rushb.utils

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.sqs.{AmazonSQS, AmazonSQSClientBuilder}

object AWSUtils {
  val region: Regions = Regions.EU_WEST_1

  val bucketName: String = "csgo-demos-rushb"
  val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(region).build()

  val crawlerQueueName: String = "https://sqs.eu-west-1.amazonaws.com/743262912284/rushb-queue.fifo"
  val downloaderQueueName: String = "https://sqs.eu-west-1.amazonaws.com/743262912284/rushb-parser-queue.fifo"
  val sqs: AmazonSQS = AmazonSQSClientBuilder.standard().withRegion(region).build()

  def demoLink(id: String) = s"demolink${id}.json"
  def demo(id: String) = s"demo${id}"
  def parsedDemo(id: String, index: Int) = s"parsedDemo${id}_$index.json"
}
