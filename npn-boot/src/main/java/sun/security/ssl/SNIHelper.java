package sun.security.ssl;

import javax.net.ssl.SNIServerName;
import java.util.List;

public class SNIHelper {

  public static void setServerNameSelector(SSLEngineImpl engine, ServerNameSelector selector) {
    engine.setServerNameSelector(selector);
  }

  public static List<SNIServerName> getServerNames(SSLEngineImpl engine) {
    return engine.serverNames;
  }
}
