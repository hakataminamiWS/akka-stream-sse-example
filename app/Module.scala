import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import services.ActorRefManager

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bindTypedActor(ActorRefManager.apply(Set.empty), "manager-actor")
  }
}