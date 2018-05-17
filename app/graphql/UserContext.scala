package graphql

import models.{Permission, User}
import service.BookstoreRepo

case class UserContext(user: User, bookstoreRepo: BookstoreRepo) {
  
  def authorized[T](permission: Permission)(fn: User => T): T = {
    if (user.permissions.contains(permission))
      fn(user)
    else
      throw UnauthorizedException(s"${user.id} doesn't have $permission")
  }
}

case class UnauthorizedException(message: String) extends Exception(message)
