package org.sunbird.obj.actors

import akka.actor.Props
import org.scalamock.scalatest.MockFactory
import org.sunbird.cloudstore.StorageService
import org.sunbird.common.dto.{Property, Request, Response}
import org.sunbird.common.exception.ResponseCode
import org.sunbird.graph.dac.model.{Node, SearchCriteria}
import org.sunbird.graph.{GraphService, OntologyEngineContext}
import java.util
import scala.collection.JavaConverters.mapAsJavaMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestObjectActor extends BaseSpec with MockFactory{

  "ObjectActor" should "return failed response for 'unknown' operation" in {
    implicit val ss = mock[StorageService]
    implicit val oec: OntologyEngineContext = new OntologyEngineContext
    testUnknownOperation(Props(new ObjectActor()), getRequest())
  }

  it should "return success response for 'readObject'" in {
    implicit val oec: OntologyEngineContext = mock[OntologyEngineContext]
    val graphDB = mock[GraphService]
    (oec.graphService _).expects().returns(graphDB)
    val node = getNode("Content", None)
    (graphDB.getNodeByUniqueId(_: String, _: String, _: Boolean, _: Request)).expects(*, *, *, *).returns(Future(node))
    implicit val ss = mock[StorageService]
    val request = getRequest()
    request.getContext.put("identifier","do1234")
    request.putAll(mapAsJavaMap(Map("identifier" -> "do_1234", "fields" -> "")))
    request.setOperation("readObject")
    val response = callActor(request, Props(new ObjectActor()))
    assert("successful".equals(response.getParams.getStatus))
  }

  it should "return success response for 'createObject'" in {
    implicit val ss = mock[StorageService]
    implicit val oec: OntologyEngineContext = mock[OntologyEngineContext]
    val graphDB = mock[GraphService]
    (oec.graphService _).expects().returns(graphDB).anyNumberOfTimes()
    // Uncomment below line if running individual file in local.
    (graphDB.readExternalProps(_: Request, _: List[String])).expects(*, *).returns(Future(new Response()))
    (graphDB.addNode(_: String, _: Node)).expects(*, *).returns(Future(getValidNode())).anyNumberOfTimes()
    (graphDB.getNodeByUniqueIds(_: String, _: SearchCriteria)).expects(*, *).returns(Future(new util.ArrayList[Node]() {
      {
        add(getBoardNode())
      }
    }))

    val request = getContentRequest()
    request.getRequest.putAll( mapAsJavaMap(Map("channel"-> "in.ekstep","name" -> "New" ,"code" -> "1234", "mimeType"-> "application/vnd.ekstep.content-collection", "contentType" -> "Course", "primaryCategory" -> "Learning Resource", "channel" -> "in.ekstep", "targetBoardIds" -> new util.ArrayList[String](){{add("ncf_board_cbse")}})))
    request.setOperation("createObject")
    val response = callActor(request, Props(new ObjectActor()))
    assert(response.get("identifier") != null)
    assert(response.get("versionKey") != null)
  }

  it should "return error response for 'createObject'" in {
    implicit val ss = mock[StorageService]
    implicit val oec: OntologyEngineContext = mock[OntologyEngineContext]
    val graphDB = mock[GraphService]
    val request = getContentErrorRequest()
    request.getRequest.putAll( mapAsJavaMap(Map("name" -> "New Content", "mimeType"-> "application/vnd.ekstep.plugin-archive", "contentType" -> "Course", "primaryCategory" -> "Learning Resource", "channel" -> "in.ekstep")))
    request.setOperation("createObject")
    val response = callActor(request, Props(new ObjectActor()))
    assert(response.getResponseCode == ResponseCode.CLIENT_ERROR)
  }

  it should "create a plugin node with invalid request, should through client exception" in {
    implicit val ss = mock[StorageService]
    implicit val oec: OntologyEngineContext = mock[OntologyEngineContext]
    val graphDB = mock[GraphService]
    val request = getContentRequest()
    request.getRequest.putAll( mapAsJavaMap(Map("name" -> "New Content", "mimeType"-> "application/vnd.ekstep.plugin-archive", "contentType" -> "Course", "primaryCategory" -> "Learning Resource", "channel" -> "in.ekstep")))
    request.setOperation("createObject")
    val response = callActor(request, Props(new ObjectActor()))
    assert(response.getResponseCode == ResponseCode.CLIENT_ERROR)
  }


  it should "return success response for 'updateObject'" in {
    implicit val oec: OntologyEngineContext = mock[OntologyEngineContext]
    val graphDB = mock[GraphService]
    (oec.graphService _).expects().returns(graphDB).anyNumberOfTimes()
    val node = getNode()
    (graphDB.getNodeByUniqueId(_: String, _: String, _: Boolean, _: Request)).expects(*, *, *, *).returns(Future(node)).anyNumberOfTimes()
    (graphDB.getNodeProperty(_: String, _: String, _: String)).expects(*, *, *).returns(Future(new Property("versionKey", new org.neo4j.driver.internal.value.StringValue("test_123"))))
    (graphDB.upsertNode(_: String, _: Node, _: Request)).expects(*, *, *).returns(Future(node))
    val nodes: util.List[Node] = getCategoryNode()
    (graphDB.getNodeByUniqueIds(_: String, _: SearchCriteria)).expects(*, *).returns(Future(nodes)).anyNumberOfTimes()

    implicit val ss = mock[StorageService]
    val request = getContentRequest()
    request.getContext.put("identifier","do_1234")
    request.putAll(mapAsJavaMap(Map("description" -> "test desc", "versionKey" -> "test_123")))
    request.setOperation("updateObject")
    val response = callActor(request, Props(new ObjectActor()))
    assert("successful".equals(response.getParams.getStatus))
    assert("test_123".equals(response.get("versionKey")))
  }

  it should "return client exception for 'updateObject' with invalid versionKey" in {
    implicit val oec: OntologyEngineContext = mock[OntologyEngineContext]
    val graphDB = mock[GraphService]
    (oec.graphService _).expects().returns(graphDB).anyNumberOfTimes()
    val node = getNode()
    (graphDB.getNodeByUniqueId(_: String, _: String, _: Boolean, _: Request)).expects(*, *, *, *).returns(Future(node)).anyNumberOfTimes()
    (graphDB.getNodeProperty(_: String, _: String, _: String)).expects(*, *, *).returns(Future(new Property("versionKey", new org.neo4j.driver.internal.value.StringValue("test_xyz"))))
    val nodes: util.List[Node] = getCategoryNode()
    (graphDB.getNodeByUniqueIds(_: String, _: SearchCriteria)).expects(*, *).returns(Future(nodes)).anyNumberOfTimes()

    implicit val ss = mock[StorageService]
    val request = getContentRequest()
    request.getContext.put("identifier","do_1234")
    request.putAll(mapAsJavaMap(Map("description" -> "test desc", "versionKey" -> "test_123")))
    request.setOperation("updateObject")
    val response = callActor(request, Props(new ObjectActor()))
    assert("failed".equals(response.getParams.getStatus))
    assert("CLIENT_ERROR".equals(response.getParams.getErr))
    assert("Invalid version Key".equals(response.getParams.getErrmsg))
  }

  it should "return success response for retireObject" in {
    implicit val oec: OntologyEngineContext = mock[OntologyEngineContext]
    val graphDB = mock[GraphService]
    (oec.graphService _).expects().returns(graphDB).repeated(2)
    val node = getNode("Content", None)
    (graphDB.getNodeByUniqueId(_: String, _: String, _: Boolean, _: Request)).expects(*, *, *, *).returns(Future(node)).anyNumberOfTimes()
    (graphDB.updateNodes(_: String, _: util.List[String], _: util.HashMap[String, AnyRef])).expects(*, *, *).returns(Future(new util.HashMap[String, Node]))
    implicit val ss = mock[StorageService]
    val request = getContentRequest()
    request.getContext.put("identifier","do1234")
    request.getRequest.putAll(mapAsJavaMap(Map("identifier" -> "do_1234")))
    request.setOperation("retireObject")
    val response = callActor(request, Props(new ObjectActor()))
    assert("successful".equals(response.getParams.getStatus))
  }

  it should "return client error response for retireObject" in {
    implicit val oec: OntologyEngineContext = mock[OntologyEngineContext]
    implicit val ss = mock[StorageService]
    val request = getContentRequest()
    request.getContext.put("identifier","do_1234.img")
    request.getRequest.putAll(mapAsJavaMap(Map("identifier" -> "do_1234.img")))
    request.setOperation("retireObject")
    val response = callActor(request, Props(new ObjectActor()))
    assert(response.getResponseCode == ResponseCode.CLIENT_ERROR)
  }

  private def getContentRequest(): Request = {
    val request = new Request()
    request.setContext(new util.HashMap[String, AnyRef]() {
      {
        put("graph_id", "domain")
        put("version", "1.0")
        put("objectType", "Content")
        put("schemaName", "content")
        put("X-Channel-Id", "in.ekstep")
      }
    })
    request.setObjectType("Content")
    request
  }

  private def getContentErrorRequest(): Request = {
    val request = new Request()
    request.setContext(new util.HashMap[String, AnyRef]() {
      {
        put("graph_id", "domain")
        put("version", "1.0")
        put("objectType", "Content")
        put("schemaName", "contents")
        put("X-Channel-Id", "in.ekstep")
      }
    })
    request.setObjectType("Content")
    request
  }

  private def getRetireRequest(): Request = {
    val request = new Request()
    request.setContext(new util.HashMap[String, AnyRef]() {
      {
        put("graph_id", "domain")
        put("version", "1.0")
        put("objectType", "Content")
        put("schemaName", "content")
        put("X-Channel-Id", "in.ekstep")
        put("status" , "Live")
      }
    })
    request.setObjectType("Content")
    request
  }

  private def getNode(): Node = {
    val node = new Node()
    node.setIdentifier("obj-agri:1234")
    node.setNodeType("DATA_NODE")
    node.setObjectType("Content")
    node.setMetadata(new util.HashMap[String, AnyRef]() {
      {
        put("identifier", "obj-agri:1234")
        put("mimeType", "application/pdf")
        put("status", "Live")
        put("contentType", "Resource")
        put("name", "Resource_1")
        put("versionKey", "12340988")
        put("channel", "in.ekstep")
        put("code", "Resource_1")
        put("primaryCategory", "Learning Resource")
      }
    })
    node
  }
  def getBoardNode(): Node = {
    val node = new Node()
    node.setIdentifier("ncf_board_cbse")
    node.setNodeType("DATA_NODE")
    node.setObjectType("Term")
    node.setGraphId("domain")
    node.setMetadata(mapAsJavaMap(Map("name"-> "CBSE")))
    node
  }
  private def getValidNode(): Node = {
    val node = new Node()
    node.setIdentifier("obj-agri:1234")
    node.setNodeType("DATA_NODE")
    node.setObjectType("ObjectCategory")
    node.setMetadata(new util.HashMap[String, AnyRef]() {
      {
        put("identifier", "obj-agri:1234")
        put("objectType", "ObjectCategory")
        put("name", "1234")
        put("versionKey","12340988")
      }
    })
    node
  }

  override def getCategoryNode(): util.List[Node] = {
    val node = new Node()
    node.setIdentifier("board")
    node.setNodeType("DATA_NODE")
    node.setObjectType("Category")
    node.setMetadata(new util.HashMap[String, AnyRef]() {
      {
        put("code", "board")
        put("orgIdFieldName", "boardIds")
        put("targetIdFieldName", "targetBoardIds")
        put("searchIdFieldName", "se_boardIds")
        put("searchLabelFieldName", "se_boards")
        put("status", "Live")
      }
    })
    util.Arrays.asList(node)
  }

  private def getRequest(): Request = {
    val request = new Request()
    request.setContext(new util.HashMap[String, AnyRef]() {
      {
        put("graph_id", "domain")
        put("version", "1.0")
        put("objectType", "Content")
        put("schemaName", "content")
        put("X-Channel-Id", "in.ekstep")
      }
    })
    request.setObjectType("Content")
    request
  }
}