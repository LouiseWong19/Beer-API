package controllers

import models.{BeerItem, NewBeerItem}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import play.api.libs.json._

@Singleton
class BeerAPIController @Inject()(val controllerComponents: ControllerComponents)
  extends BaseController {
  private val beerList = new ListBuffer[BeerItem]()
  beerList += BeerItem(1,"Estrella Galicia", 4.7)
  beerList += BeerItem(2,"Old Crafty Hen", 6.5)
  implicit val beerListJson = Json.format[BeerItem]
  implicit val newBeerListJson = Json.format[NewBeerItem]

  // getAll request curl localhost:9000/beer
  def getAll(): Action[AnyContent] = Action {
    if (beerList.isEmpty){
      NoContent
    }else{
      Ok(Json.toJson(beerList))
    }
  }

  // getById request curl localhost:9000/beer/1
  def getById(itemId: Long) = Action {
    val foundItem = beerList.find(_.id == itemId)
    foundItem match{
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

  // deleteById request curl -X DELETE localhost:9000/beer/1
  def deleteById(itemId: Long) = Action{
    beerList.filterInPlace(_.id != itemId)
    Accepted
  }

  // post request curl -H "Content-Type: application/json" -X POST -d "{\"description\": \"another new\",\"unit\": 6.0}" "localhost:9000/beer"
  def addNewItem() = Action{
    implicit request =>
      val content = request.body
      val jsonObject = content.asJson
      val beerListItem: Option[NewBeerItem] = jsonObject.flatMap(
        Json.fromJson[NewBeerItem](_).asOpt
      )
      beerListItem match {
        case Some(newItem) =>
          val nextId = beerList.map(_.id).max + 1
          val toBeAdded = BeerItem(nextId,newItem.description, newItem.unit)
          beerList += toBeAdded
          Created(Json.toJson(toBeAdded))
        case None => BadRequest
      }
  }
}
