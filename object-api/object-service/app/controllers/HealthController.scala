package controllers

import akka.actor.{ActorRef, ActorSystem}
import handlers.SignalHandler

import javax.inject._
import org.sunbird.common.JsonUtils
import org.sunbird.common.dto.ResponseHandler
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class HealthController @Inject()(@Named("healthActor") healthActor: ActorRef, cc: ControllerComponents, actorSystem: ActorSystem, signalHandler: SignalHandler)(implicit exec: ExecutionContext) extends BaseController(cc) {

  def health() = Action.async { implicit request =>
    if (signalHandler.isShuttingDown) {
      Future { ServiceUnavailable }
    } else {
      getResult("api.content.health", healthActor, new org.sunbird.common.dto.Request())
    }
  }

  def serviceHealth() = Action.async { implicit request =>
    if (signalHandler.isShuttingDown)
      Future { ServiceUnavailable }
    else {
      val response = ResponseHandler.OK().setId("api.content.service.health").put("healthy", true)
      Future { Ok(JsonUtils.serialize(response)).as("application/json") }
    }
  }
}
