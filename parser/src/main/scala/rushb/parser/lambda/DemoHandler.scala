package rushb.parser.lambda

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import rushb.model.DemoLink
import rushb.parser.{ContentType, DemoObject, Parser, Prepare}

import java.io.InputStream
import scala.jdk.CollectionConverters._
import upickle.default._


class DemoHandler extends RequestHandler[SQSEvent, Void] {
  import rushb.utils.AWSUtils._
  override def handleRequest(input: SQSEvent, context: Context): Void = {
    Prepare.prepare()
    val ids = input.getRecords.asScala.toList
      .map(_.getBody)

    val demos: Seq[DemoObject] = ids.map { id =>
      val linkIs: InputStream = s3.getObject(bucketName, demoLink(id)).getObjectContent
      val link = read[DemoLink](linkIs)
      val demoS3Obj = s3.getObject(bucketName, demo(id))
      val demoIs: InputStream = demoS3Obj.getObjectContent
      DemoObject(demoIs, ContentType.parse(demoS3Obj.getObjectMetadata.getContentType), link)
    }

    demos.flatMap { d => Parser.parseDemo(d).map(d -> _) }
      .zipWithIndex
      .foreach {
        case ((demoObj, Left(err)), _) => ErrorHandler.handle(demoObj.link, err)
        case ((demoObj, Right(json)), i) => SuccessHandler.handle(demoObj.link, i, json)
      }
    null
  }


}
