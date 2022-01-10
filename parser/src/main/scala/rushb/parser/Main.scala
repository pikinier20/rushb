package rushb.parser

import rushb.model.DemoLink
import rushb.parser.lambda.{ErrorHandler, SuccessHandler}
import rushb.utils.AWSUtils.{bucketName, demo, demoLink, downloaderQueueName, longPollMessages, s3}
import upickle.default.read

import java.io.InputStream

object Main extends App {
  def handleMessage(m: String): Unit = {
    val linkIs: InputStream = s3.getObject(bucketName, demoLink(m)).getObjectContent
    val link = read[DemoLink](linkIs)
    val demoS3Obj = s3.getObject(bucketName, demo(m))
    val demoIs: InputStream = demoS3Obj.getObjectContent
    val demoObj = DemoObject(demoIs, ContentType.parse(demoS3Obj.getObjectMetadata.getContentType), link)

    Parser.parseDemo(demoObj).map(demoObj -> _)
      .zipWithIndex
      .foreach {
        case ((demoObj, Left(err)), _) => ErrorHandler.handle(demoObj.link, err)
        case ((demoObj, Right(json)), i) => SuccessHandler.handle(demoObj.link, i, json)
      }
  }

  Prepare.prepare()
  longPollMessages(downloaderQueueName)(handleMessage)
}
