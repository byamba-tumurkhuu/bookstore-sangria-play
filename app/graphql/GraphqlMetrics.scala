package graphql

import play.api.Logger
import sangria.execution.{Middleware, MiddlewareQueryContext}

object GraphqlMetrics extends Middleware[Any] {
  
  override type QueryVal = Long
  
  def beforeQuery(context: MiddlewareQueryContext[Any, _, _]): Long =
    System.currentTimeMillis()
  
  def afterQuery(startMs: QueryVal, context: MiddlewareQueryContext[Any, _, _]): Unit = {
    val duration = System.currentTimeMillis() - startMs
    Logger.info(s"Bookstore: Query execution time ${duration}ms")
  }
  
}
