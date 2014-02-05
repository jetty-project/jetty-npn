package sun.security.ssl;

import javax.net.ssl.SNIServerName;
import java.util.List;

public interface ServerNameSelector {
  public List<SNIServerName> getServerNamesFor(List<SNIServerName> clientAsks);
}
