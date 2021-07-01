import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import service.ActorRefManager

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bindTypedActor(ActorRefManager, "manager-actor")
  }
}
