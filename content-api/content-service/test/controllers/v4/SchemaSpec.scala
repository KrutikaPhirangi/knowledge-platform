package controllers.v4

import controllers.base.BaseSpec
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.{BadPart, FilePart}
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, defaultAwaitTimeout, status}
import java.io.File

class SchemaSpec extends BaseSpec {

  "Schema controller" should {
    "return success response for read API" in {
      val controller = app.injector.instanceOf[controllers.v4.SchemaController]
      val result = controller.read("do_1234", None)(FakeRequest())
      isOK(result)
      status(result) must equalTo(OK)
    }
  }

    "return success response for create API" in {
      val controller = app.injector.instanceOf[controllers.v4.SchemaController]
      val result = controller.create()(FakeRequest())
      isOK(result)
      status(result) must equalTo(OK)
    }

    "return success response for upload API with file" in {
    val controller = app.injector.instanceOf[controllers.v4.SchemaController]
    val file = new File("content-api/content-service/test/resources/sample.pdf")
    val files = Seq[FilePart[TemporaryFile]](FilePart("file", "sample.pdf", None, SingletonTemporaryFileCreator.create(file.toPath)))
    val multipartBody = MultipartFormData(Map[String, Seq[String]](), files, Seq[BadPart]())
    val fakeRequest = FakeRequest().withMultipartFormDataBody(multipartBody)
    val result = controller.upload("01234", None, None)(fakeRequest)
    isOK(result)
    status(result) must equalTo(OK)
  }

  "return success response for publish API" in {
    val controller = app.injector.instanceOf[controllers.v4.SchemaController]
    val result = controller.publish("content","do_1234")(FakeRequest())
    isOK(result)
    status(result) must equalTo(OK)
  }
}
