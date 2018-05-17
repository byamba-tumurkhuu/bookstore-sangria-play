package models

import scala.util.Random

case class Comment(
  id: String,
  text: String,
  userName: String
)

case class Book(
  id: String,
  name: String,
  authorId: String,
  comments: Seq[String] = Nil,
  price: Int = Random.nextInt
)

case class Author(
  id: String,
  firstName: String,
  lastName: String
)

case class User(id: String, permissions: Set[Permission])

case class Permission(value: String) extends AnyVal

object Permission {
  
  lazy val viewBook = Permission("view-book")
  lazy val viewComment = Permission("view-comment")
}
