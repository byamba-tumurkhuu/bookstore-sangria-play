package service

import models._

import scala.concurrent.Future

class BookstoreRepo {
  
  import BookstoreRepo._
  
  def findBooks(bookId: Option[String]): Future[Seq[Book]] =
    Future.successful {
      bookId
        .map(bookId => books.filter(_.id == bookId))
        .getOrElse(books)
    }
  
  def findComments(commentIds: Seq[String]): Future[Seq[Comment]] =
    Future.successful {
      println(s"Fetching comments: $commentIds")
      comments.filter(c => commentIds.contains(c.id))
    }
  
  
  def findAuthors(authorIds: Seq[String]): Future[Seq[Author]] =
    Future.successful {
      println(s"Fetching authors: $authorIds")
      authors.filter(a => authorIds.contains(a.id))
    }
  
  def findUser(userId: String): Option[User] = users.find(_.id == userId)
}

object BookstoreRepo {
  
  private val author1 = Author("author1", "FirstName1", "LastName1")
  private val author2 = Author("author2", "FirstName2", "LastName2")
  
  private val authors = Seq(author1, author2)
  
  private val users = Seq(
    User("user1", Set(Permission.viewBook)),
    User("user2", Set(Permission.viewBook, Permission.viewComment))
  )
  
  private val books = Seq(
    Book(
      id = "b1",
      name = "Book1",
      authorId = "author1",
      comments = Seq("c1", "c2", "c3")
    ),
    Book(
      id = "b2",
      name = "Book2",
      authorId = "author2",
      comments = Seq("c4", "c5")
    )
  )
  
  private val comments = Seq(
    Comment(
      id = "c1",
      text = "Comment 1",
      userName = "Reader1"
    ),
    Comment(
      id = "c2",
      text = "Comment 2",
      userName = "Reader2"
    ),
    Comment(
      id = "c3",
      text = "Comment 3",
      userName = "Reader3"
    ),
    Comment(
      id = "c4",
      text = "Comment 4",
      userName = "Reader4"
    ),
    Comment(
      id = "c5",
      text = "Comment 5",
      userName = "Reader5"
    )
  )
  
  
}


