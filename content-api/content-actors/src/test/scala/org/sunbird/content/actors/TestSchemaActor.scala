
package org.sunbird.content.actors

import akka.actor.Props
import org.scalamock.scalatest.MockFactory
import org.sunbird.cloudstore.StorageService
import org.sunbird.common.dto.{Request, Response}
import org.sunbird.graph.dac.model.{Node, SearchCriteria}
import org.sunbird.graph.{GraphService, OntologyEngineContext}

import java.util
import scala.collection.JavaConversions.mapAsJavaMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestSchemaActor extends BaseSpec with MockFactory{

  "SchemaActor" should "return failed response for 'unknown' operation" in {
    implicit val ss = mock[StorageService]
    implicit val oec: OntologyEngineContext = new OntologyEngineContext
    testUnknownOperation(Props(new ObjectActor()), getRequest())
  }


  it should "return success response for 'readSchema'" in {
    implicit val oec: OntologyEngineContext = mock[OntologyEngineContext]
    val graphDB = mock[GraphService]
    (oec.graphService _).expects().returns(graphDB)
    val node = getNode("Schema", None)
    (graphDB.getNodeByUniqueId(_: String, _: String, _: Boolean, _: Request)).expects(*, *, *, *).returns(Future(node))
    implicit val ss = mock[StorageService]
    val request = getRequest()
    request.getContext.put("identifier", "emp_1234")
    request.putAll(mapAsJavaMap(Map("identifier" -> "emp_1234", "fields" -> "")))
    request.setOperation("readSchema")
    val response = callActor(request, Props(new SchemaActor()))
    assert("successful".equals(response.getParams.getStatus))
  }

  it should "return success response for 'createSchema'" in {
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
    request.getRequest.putAll( mapAsJavaMap(Map("name"-> "Agri")))
    request.setOperation("createSchema")
    val response = callActor(request, Props(new ContentActor()))
    assert("successful".equals(response.getParams.getStatus))
  }

  private def getContentRequest(): Request = {
    val request = new Request()
    request.setContext(new util.HashMap[String, AnyRef]() {
      {
        put("graph_id", "domain")
        put("version", "1.0")
        put("objectType", "Schema")
        put("schemaName", "schema")
        put("X-Channel-Id", "in.ekstep")
      }
    })
    request.setObjectType("Schema")
    request
  }

  def getBoardNode(): Node = {
    val node = new Node()
    node.setIdentifier("ncf_board_cbse")
    node.setNodeType("DATA_NODE")
    node.setObjectType("Term")
    node.setGraphId("domain")
    node.setMetadata(mapAsJavaMap(Map("name"-> "Agri")))
    node
  }

  private def getValidNode(): Node = {
    val node = new Node()
    node.setIdentifier("agr_11404059476158873611")
    node.setNodeType("DATA_NODE")
    node.setObjectType("Schema")
    node.setMetadata(new util.HashMap[String, AnyRef]() {
      {
        put("identifier", "do_1234")
        put("mimeType", "application/vnd.ekstep.content-collection")
        put("status", "Draft")
        put("contentType", "Course")
        put("name", "Course_1")
        put("versionKey", "1878141")
        put("primaryCategory", "Learning Resource")
      }
    })
    node
  }

  private def getRequest(): Request = {
    val request = new Request()
    request.setContext(new util.HashMap[String, AnyRef]() {
      {
        put("graph_id", "domain")
        put("version", "1.0")
        put("objectType", "Schema")
        put("schemaName", "schema")
      }
    })
    request.setObjectType("Schema")
    request
  }

  override def getNode(objectType: String, metadata: Option[util.Map[String, AnyRef]]): Node = {
    val node = new Node("domain", "DATA_NODE", objectType)
    node.setGraphId("domain")
    val nodeMetadata = metadata.getOrElse(new util.HashMap[String, AnyRef]() {
      {
        put("name", "Schema Node")
        put("code", "Schema-node")
        put("status", "Draft")
      }
    })
    node.setMetadata(nodeMetadata)
    node.setObjectType(objectType)
    node.setIdentifier("emp_1234")
    node
  }

}

