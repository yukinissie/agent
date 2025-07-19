import com.thoughtworks.gauge.BeforeSuite
import com.uzabase.playtest2.core.config.Configuration.Companion.playtest2
import com.uzabase.playtest2.http.config.http
import java.net.URI

class ExecutionHook {
  @BeforeSuite
  fun beforeSuite() {
    playtest2 {
      listOf(http(URI("http://localhost:3000").toURL()))
    }
  }
}
