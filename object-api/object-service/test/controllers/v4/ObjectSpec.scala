package controllers.v4

import controllers.base.BaseSpec
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{OK, defaultAwaitTimeout, status}

@RunWith(classOf[JUnitRunner])
class ObjectSpec extends BaseSpec {

  "Object controller" should {
    "return success response for read API" in {
      val controller = app.injector.instanceOf[controllers.v4.ObjectController]
      val result = controller.read("content","do_1234", None)(FakeRequest())
      isOK(result)
      status(result) must equalTo(OK)
    }
  }

  "return success response for create API" in {
    val controller = app.injector.instanceOf[controllers.v4.ObjectController]
    val json: JsValue = Json.parse("""{"request": {"content": {"primaryCategory": "Learning Resource"}}}""")
    val fakeRequest = FakeRequest("POST", "/content/v1/create ").withJsonBody(json)
    val result = controller.create("content")(fakeRequest)
    isOK(result)
    status(result) must equalTo(OK)
  }

  "return success response for update API" in{
    val controller = app.injector.instanceOf[controllers.v4.ObjectController]
    val json: JsValue = Json.parse("""{"request": {"agri": {"primaryCategory": "Learning Resource"}}}""")
    val fakeRequest = FakeRequest("POST", "/agri/v1/update/do_1234 ").withJsonBody(json)
    val result = controller.update("content","do_1234")(fakeRequest)
    isOK(result)
    status(result) must equalTo(OK)
  }

  "return success response for retire API" in{
    val controller = app.injector.instanceOf[controllers.v4.ObjectController]
    val json: JsValue = Json.parse("""{"request": {"agri": {"primaryCategory": "Learning Resource"}}}""")
    val fakeRequest = FakeRequest("POST", "/agri/v1/retire/do_1234 ").withJsonBody(json)
    val result = controller.retire("content","do_1234")(fakeRequest)
    isOK(result)
    status(result) must equalTo(OK)
  }
}
