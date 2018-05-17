package graphql

import models.Comment
import sangria.execution.deferred.{Fetcher, HasId}
import service.BookstoreRepo

object Fetchers {
  
  val comments = {
    implicit val commentId = HasId[Comment, String](_.id)
    Fetcher.caching(
      (ctx: UserContext, commentIds: Seq[String]) => ctx.bookstoreRepo.findComments(commentIds)
    )
  }
  
  val authors = Fetcher.caching(
    (ctx: UserContext, authorIds: Seq[String]) =>
      ctx.bookstoreRepo.findAuthors(authorIds))(HasId(_.id)
  )
}
