package models

import models.datastore.DataStore
import play.api.libs.json.{ Writes, Reads }

import scala.concurrent.Future
import scala.reflect.ClassTag

trait FakeDataStore extends DataStore {

  override def find[T](id: String)(implicit ct: ClassTag[T], reader: Reads[T]): Future[Option[T]] =
    Future.successful(Some(fakeIntanceOfT[T](ct)))

  override def find[T](fields: (String, String)*)(implicit ct: ClassTag[T], reader: Reads[T]): Future[Seq[T]] =
    Future.successful(Seq(fakeIntanceOfT[T](ct)))

  override def findAll[T](implicit ct: ClassTag[T], reader: Reads[T]): Future[Seq[T]] =
    Future.successful(Seq(fakeIntanceOfT[T](ct)))

  override def persist[T](obj: T)(implicit ct: ClassTag[T], reader: Reads[T], writer: Writes[T]): Future[Boolean] =
    Future.successful(true)

  override def modify[T <: Persistable](obj: T)(implicit ct: ClassTag[T], reader: Reads[T], writer: Writes[T]): Future[Boolean] =
    Future.successful(true)

  override def remove[T](obj: T)(implicit ct: ClassTag[T], reader: Reads[T]): Future[Boolean] = Future.successful(true)

  private def fakeIntanceOfT[T](ct: ClassTag[T]): T = {
    val targetClazz = ct.runtimeClass
    val ctors = targetClazz.getConstructors()
    val parameterTypes = ctors(0).getParameterTypes

    val parameterValues = parameterTypes.map { pClass =>
      if (pClass.getClass == classOf[String]) ""
      else if (pClass.getClass == classOf[Boolean]) true
    }

    val instance = ctors(0).newInstance(parameterValues).asInstanceOf[T]
    instance
  }

}
