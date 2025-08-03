package org.apache.http.impl.cookie;

import java.util.StringTokenizer;
import org.apache.http.annotation.Immutable;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieRestrictionViolationException;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.cookie.SetCookie2;














































@Immutable
public class RFC2965PortAttributeHandler
  implements CookieAttributeHandler
{
  private static int[] parsePortAttribute(String portValue)
    throws MalformedCookieException
  {
    StringTokenizer st = new StringTokenizer(portValue, ",");
    int[] ports = new int[st.countTokens()];
    try {
      int i = 0;
      while (st.hasMoreTokens()) {
        ports[i] = Integer.parseInt(st.nextToken().trim());
        if (ports[i] < 0) {
          throw new MalformedCookieException("Invalid Port attribute.");
        }
        i++;
      }
    } catch (NumberFormatException e) {
      throw new MalformedCookieException("Invalid Port attribute: " + e.getMessage());
    }
    
    return ports;
  }
  








  private static boolean portMatch(int port, int[] ports)
  {
    boolean portInList = false;
    int i = 0; for (int len = ports.length; i < len; i++) {
      if (port == ports[i]) {
        portInList = true;
        break;
      }
    }
    return portInList;
  }
  


  public void parse(SetCookie cookie, String portValue)
    throws MalformedCookieException
  {
    if (cookie == null) {
      throw new IllegalArgumentException("Cookie may not be null");
    }
    if ((cookie instanceof SetCookie2)) {
      SetCookie2 cookie2 = (SetCookie2)cookie;
      if ((portValue != null) && (portValue.trim().length() > 0)) {
        int[] ports = parsePortAttribute(portValue);
        cookie2.setPorts(ports);
      }
    }
  }
  



  public void validate(Cookie cookie, CookieOrigin origin)
    throws MalformedCookieException
  {
    if (cookie == null) {
      throw new IllegalArgumentException("Cookie may not be null");
    }
    if (origin == null) {
      throw new IllegalArgumentException("Cookie origin may not be null");
    }
    int port = origin.getPort();
    if (((cookie instanceof ClientCookie)) && (((ClientCookie)cookie).containsAttribute("port")))
    {
      if (!portMatch(port, cookie.getPorts())) {
        throw new CookieRestrictionViolationException("Port attribute violates RFC 2965: Request port not found in cookie's port list.");
      }
    }
  }
  






  public boolean match(Cookie cookie, CookieOrigin origin)
  {
    if (cookie == null) {
      throw new IllegalArgumentException("Cookie may not be null");
    }
    if (origin == null) {
      throw new IllegalArgumentException("Cookie origin may not be null");
    }
    int port = origin.getPort();
    if (((cookie instanceof ClientCookie)) && (((ClientCookie)cookie).containsAttribute("port")))
    {
      if (cookie.getPorts() == null)
      {
        return false;
      }
      if (!portMatch(port, cookie.getPorts())) {
        return false;
      }
    }
    return true;
  }
}
