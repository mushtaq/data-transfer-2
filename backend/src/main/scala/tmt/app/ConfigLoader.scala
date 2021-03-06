package tmt.app

import com.typesafe.config.{ConfigResolveOptions, ConfigParseOptions, ConfigFactory}
import collection.JavaConverters._

class ConfigLoader {

  def load(name: String, env: String) = parse(name).withFallback(parse(env)).resolve()

  def parse(name: String) = ConfigFactory.load(
    name,
    ConfigParseOptions.defaults(),
    ConfigResolveOptions.defaults().setAllowUnresolved(true)
  )
}
