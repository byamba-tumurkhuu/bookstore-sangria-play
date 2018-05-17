package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import graphql._
import models.User
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc._
import sangria.execution._
import sangria.execution.deferred.DeferredResolver
import sangria.marshalling.playJson._
import sangria.parser.{QueryParser, SyntaxError}
import service.BookstoreRepo

import scala.concurrent.Future
import scala.util.{Failure, Success}

class BookstoreController @Inject() (system: ActorSystem, config: Configuration) extends InjectedController {
  import system.dispatcher
  
  private val bookstoreRepo = new BookstoreRepo
  
  private val authAction = AuthenticatedBuilder[User](
    userinfo = _.headers.get("X-UserId").flatMap(bookstoreRepo.findUser),
    defaultParser = BodyParsers.parse.defaultBodyParser
  )
  
  def graphiql = Action {
    Ok(views.html.graphiql())
  }

  def graphql = authAction.async(parse.json) { request ⇒
    val query = (request.body \ "query").as[String]
    val operationName = (request.body \ "operationName").asOpt[String]

    val variables = (request.body \ "variables").toOption.flatMap {
      case JsString(vars) if vars.trim == "" || vars.trim == "null" => Some(Json.obj())
      case JsString(vars) => Some(Json.parse(vars).as[JsObject])
      case obj: JsObject => Some(obj)
      case _ => None
    }

    val userContext = UserContext(request.user, bookstoreRepo)
    executeBookstoreQuery(userContext, query, variables, operationName)
  }
  
  /**
    * Executes the provided graphql query with [[BookstoreSchema]] and [[BookstoreRepo]].
    */
  private def executeBookstoreQuery(userContext: UserContext, query: String, variables: Option[JsObject], operationName: Option[String]) =
    QueryParser.parse(query) match {

      // query parsed successfully, time to execute it!
      case Success(queryAst) ⇒
        Executor.execute(
          schema = BookstoreSchema.schema,
          queryAst = queryAst,
          userContext = userContext,
          operationName = operationName,
          deferredResolver = DeferredResolver.fetchers(Fetchers.comments, Fetchers.authors),
          exceptionHandler = errorHandler,
          middleware = GraphqlMetrics :: Nil, // SlowLog(Logger.underlying(), threshold = 10.millis) :: Nil,
          variables = variables getOrElse Json.obj()
        ).map(Ok(_))
          .recover {
            case error: QueryAnalysisError ⇒ BadRequest(error.resolveError)
            case error: ErrorWithResolver ⇒ InternalServerError(error.resolveError)
          }

      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) ⇒
        Future.successful(BadRequest(Json.obj(
          "syntaxError" → error.getMessage,
          "locations" → Json.arr(Json.obj(
            "line" → error.originalError.position.line,
            "column" → error.originalError.position.column)))))

      case Failure(error) ⇒
        throw error
    }
  
  
  val errorHandler = ExceptionHandler {
    case (m, UnauthorizedException(message)) => HandledException(message)
  }
  
  
}
