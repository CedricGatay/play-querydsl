package controllers

import com.querydsl.jpa.impl.JPAQuery
import play.api.mvc._
import play.db.jpa.JPA
import models.included.QBook

object Application extends Controller {

  def index = Action {
                       val bookCount = JPA.withTransaction(new play.libs.F.Function0[Long] {
                         override def apply(): Long = {
                           new JPAQuery(JPA.em()).from(QBook.book)
                             .where(QBook.book.title.contains("ipsum")).fetchCount()
                         }
                       })
                       Ok(views.html.index(bookCount))
                     }

}