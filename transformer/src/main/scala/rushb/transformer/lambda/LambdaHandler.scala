package rushb.transformer.lambda

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.S3Event
import org.apache.commons.io.IOUtils
import rushb.utils.AWSUtils.{parsedBucketName, s3}

import java.nio.charset.{Charset, StandardCharsets}
import scala.jdk.CollectionConverters._



class LambdaHandler extends RequestHandler[S3Event, String] {
  override def handleRequest(input: S3Event, context: Context): String = {
    val objs = input.getRecords.asScala.toList
      .map(_.getS3.getObject.getKey)
      .map(s3.getObject(parsedBucketName, _))
      .map(_.getObjectContent)
      .map(IOUtils.toString(_, StandardCharsets.UTF_8))
    ???
  }
}
