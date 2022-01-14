package rushb.transformer

import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest}
import org.apache.commons.io.IOUtils

import java.io.{ByteArrayInputStream, File, FileReader, FileWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import rushb.utils.AWSUtils._

object Main extends App {
  s3.listObjects(parsedBucketName).getObjectSummaries.asScala.toList
    .map(os => os.getKey -> s3.getObject(parsedBucketName, os.getKey).getObjectContent)
    .map {
      case (str, stream) => str -> IOUtils.toString(stream, StandardCharsets.UTF_8)
    }
    .map {
      case (str, str1) => str -> ParsedDemoTransformer.transformJsonToCsv(str1)
    }
    .foreach {
      case (objectKey, map) => map.foreach {
        case (eventName, csv) =>
          val id = objectKey.stripPrefix("parsedDemo").stripSuffix(".json")
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
//  val jsonString = IOUtils.toString(new FileReader("parsedDemo.json"))
//  Files.createDirectory(Paths.get("csvs"))
//  ParsedDemoTransformer.transformJsonToCsv(jsonString).foreach {
//    case (str, str1) =>
//      IOUtils.write(str1, Files.newOutputStream(Paths.get("csvs", s"$str.csv")), StandardCharsets.UTF_8)
//  }
}
