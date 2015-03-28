import javax.persistence.EntityManager

import models.included.Book
import play.api.{Application, GlobalSettings}
import play.db.jpa.JPA
import play.libs.F.Function0

/**
 * @author cgatay
 */
object Global extends GlobalSettings{
  override def onStart(app: Application): Unit = {
    val book = new Book()
    book.title="lorem ipsum dolor"
    JPA.withTransaction(new Function0[Unit] {
      override def apply(): Unit = {
        val em: EntityManager = JPA.em()
        em.merge(book)
      }
    })
  }
}
