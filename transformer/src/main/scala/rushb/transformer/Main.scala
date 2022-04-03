package rushb.transformer

import com.amazonaws.services.s3.model.{ObjectListing, ObjectMetadata, PutObjectRequest, S3ObjectSummary}
import monix.eval.Task
import monix.execution.{Cancelable, Scheduler}
import monix.execution.schedulers.SchedulerService
import monix.reactive.observers.Subscriber
import monix.reactive.subjects.BehaviorSubject
import monix.reactive.{Observable, OverflowStrategy}
import org.apache.commons.io.IOUtils
import rushb.transformer.lambda.Handler

import java.io.{ByteArrayInputStream, File, FileReader, FileWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.jdk.CollectionConverters._
import rushb.utils.AWSUtils._

import java.util.concurrent.Executors
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  implicit val scheduler: SchedulerService = Scheduler(Executors.newFixedThreadPool(8))
  def listings(prev: Option[ObjectListing]): List[ObjectListing] = {
    prev.fold(listings(Some(s3.listObjects(parsedBucketName)))) { prev =>
      if (prev.isTruncated) {
        val next = s3.listNextBatchOfObjects(prev)
        prev :: listings(Some(next))
      } else List(prev)
    }
  }
  val f = Observable.fromIterable(listings(None))
    .flatMap(l => Observable.fromIterable(l.getObjectSummaries.asScala.toList))
    .mapParallelUnordered(parallelism = 8) { os =>
      Task {
        val id = os.getKey.stripPrefix("parsedDemo").stripSuffix(".json")
        val stream = s3.getObject(parsedBucketName, os.getKey).getObjectContent
        val json = IOUtils.toString(stream, StandardCharsets.UTF_8)
        println(s"Started $id")
        Handler.handle(id, json)
        println(s"Finished $id")
      }
    }
    .foreach(_ => ())
  Await.result(f, Duration.Inf)
}
