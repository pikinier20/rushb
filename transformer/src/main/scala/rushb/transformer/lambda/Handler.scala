package rushb.transformer.lambda

import com.amazonaws.services.s3.model.ObjectMetadata
import rushb.transformer.ParsedDemoTransformer
import rushb.utils.AWSUtils.{dataLakeBucketName, s3}

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

object Handler {
  def handle(id: String, json: String) = {
    val csvMap: Map[String, String] = ParsedDemoTransformer.transformJsonToCsv(json)
    csvMap.foreach {
      case (eventName, csv) =>
        val fileName = s"$eventName/$id.csv"
        val metadata = new ObjectMetadata()
        val bytes = csv.getBytes(StandardCharsets.UTF_8)
        metadata.setContentType("text/csv")
        metadata.setContentLength(bytes.length)
        s3.putObject(
          dataLakeBucketName,
          fileName,
          new ByteArrayInputStream(bytes),
          metadata
        )
    }
  }
}
