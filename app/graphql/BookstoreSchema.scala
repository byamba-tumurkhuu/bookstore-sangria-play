package graphql

import models.{Author, Book, Comment, Permission}
import sangria.macros.derive.{ObjectTypeDescription, _}
import sangria.schema._

object BookstoreSchema {
  
  implicit val CommentType = deriveObjectType[UserContext, Comment](
    ObjectTypeDescription("Comment schema")
  )
  
  implicit val AuthorType = deriveObjectType[UserContext, Author](
    ObjectTypeDescription("Author")
  )
  
  implicit val BookType = ObjectType(
    "Book",
    description = "",
    fields[UserContext, Book](
      Field("id", StringType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("author", OptionType(AuthorType), resolve = ctx => {
        Fetchers.authors.deferOpt(ctx.value.authorId)
      }),
      Field("comments", OptionType(ListType(CommentType)), resolve = ctx => {
        ctx.ctx.authorized(Permission.viewComment) { _ =>
          Fetchers.comments.deferSeqOpt(ctx.value.comments)
        }
      }),
      Field("price", IntType, resolve = _.value.price)
    )
  )
  
  val BookIdArg = Argument("bookId", OptionInputType(StringType), "Book id")
  
  val Query = ObjectType(
    "Query",
    fields[UserContext, Unit](
      Field("books", ListType(BookType),
        description = Some("Returns a list books"),
        arguments = BookIdArg :: Nil,
        resolve = (ctx) => {
          ctx.ctx.authorized(Permission.viewBook) { _ =>
            ctx.ctx.bookstoreRepo.findBooks(ctx.arg(BookIdArg))
          }
        }
      )
    )
  )
  
  lazy val schema = Schema(Query)
}
