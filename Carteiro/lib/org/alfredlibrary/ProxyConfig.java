package org.alfredlibrary;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

public class ProxyConfig {
  private String host;
  private int port;
  private Proxy proxy;

  public ProxyConfig(final String host, final int port, final Type proxyType) {
    this.host = host;
    this.port = port;
    this.proxy = new Proxy(proxyType, new InetSocketAddress(host, port));
  }

  public void setProxy(final Proxy proxy) {
    this.proxy = proxy;
  }

  public Proxy getProxy() {
    return proxy;
  }

  public void setHost(final String host) {
    this.host = host;
  }

  public String getHost() {
    return host;
  }

  public void setPort(final int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

}