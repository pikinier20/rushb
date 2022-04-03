package rushb.transformer.lambda

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.model.ObjectMetadata
import org.apache.commons.io.IOUtils
import rushb.transformer.ParsedDemoTransformer
import rushb.utils.AWSUtils.{dataLakeBucketName, parsedBucketName, s3}

import java.io.ByteArrayInputStream
import java.nio.charset.{Charset, StandardCharsets}
import scala.jdk.CollectionConverters._



class LambdaHandler extends RequestHandler[S3Event, String] {
  override def handleRequest(input: S3Event, context: Context): String = {
    input.getRecords.asScala.toList
      .map(_.getS3.getObject.getKey)
      .map(k => k -> s3.getObject(parsedBucketName, k))
      .map {
        case (k, obj) => k -> obj.getObjectContent
      }
      .map {
        case (k, stream) => k -> IOUtils.toString(stream, StandardCharsets.UTF_8)
      }
      .foreach {
        case (str, value) => Handler.handle(str, value)
      }
      "Success"
  }
}
