import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.ReceptionistManager

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bindTypedActor(ReceptionistManager.apply(), "receptionistManager")
  }
}