package org.sunbird.obj.actors

import org.sunbird.actor.core.BaseActor
import org.sunbird.common.dto.{Request, Response}
import org.sunbird.graph.OntologyEngineContext
import org.sunbird.graph.health.HealthCheckManager

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HealthActor @Inject() (implicit oec: OntologyEngineContext) extends BaseActor {
  implicit val ec: ExecutionContext = getContext().dispatcher

  override def onReceive(request: Request): Future[Response] = {
    HealthCheckManager.checkAllSystemHealth()
  }
}

