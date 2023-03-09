package org.baklanovsoft.shoppingcart.error
import scala.util.control.NoStackTrace

trait DomainError extends NoStackTrace {
  val code: String
  val status: Int
  val description: Option[String]
}
