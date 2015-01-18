package com.rbardini.carteiro.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.net.URL;
import java.net.URLConnection;

public class TimeCostCalculator {
  public enum Service {SEDEX, SEDEX_COBRAR, SEDEX_10, SEDEX_HOJE, PAC}
  public enum Packing {BOX, ENVELOPE, ROLL};

  private Service mService;
  private Packing mPacking;
  private String mOriginZip;
  private String mDestinationZip;
  private float mWidth;
  private float mHeight;
  private float mLength;
  private float mDiameter;
  private float mWeight;
  private float mDeclaredValue;
  private boolean mSigned;
  private boolean mDeliveryNotice;

  public TimeCostCalculator() {}

  public void setService(Service service) {
    mService = service;
  }

  public void setPacking(Packing packing) {
    mPacking = packing;
  }

  public void setOriginZip(String zip) {
    mOriginZip = zip;
  }

  public void setDestinationZip(String zip) {
    mDestinationZip = zip;
  }

  public void setDimensions(float width, float height, float length, float diameter) {
    mWidth = width;
    mHeight = height;
    mLength = length;
    mDiameter = diameter;
  }

  public void setDimensions(float width, float height, float length) {
    setDimensions(width, height, length, 0);
  }

  public void setDimensions(float width, float height) {
    setDimensions(width, height, 0, 0);
  }

  public void setWeight(float weight) {
    mWeight = weight;
  }

  public void setDeclaredValue(float declaredValue) {
    mDeclaredValue = declaredValue;
  }

  public void setSigned(boolean signed) {
    mSigned = signed;
  }

  public void setDeliveryNotice(boolean deliveryNotice) {
    mDeliveryNotice = deliveryNotice;
  }

  public PrecoPrazo fetch() throws Exception {
    URL url = new URL("http://ws.correios.com.br/calculador/CalcPrecoPrazo.aspx");
    URLConnection conn = url.openConnection();

    Serializer serializer = new Persister();
    PrecoPrazo pp = serializer.read(PrecoPrazo.class, conn.getInputStream(), false);

    return pp;
  }

  @Root
  private static class PrecoPrazo {
    @Element(name="cServico")
    private Service mService;

    public int getCode() { return mService.code; }
    public String getValue() { return mService.value; }
    public int getDeliveryTime() { return mService.deliveryTime; }
    public String getCostNoExtras() { return mService.costNoExtras; }
    public String getCostSigned() { return mService.costSigned; }
    public String getCostDeliveryNotice() { return mService.costDeliveryNotice; }
    public String getCostDeclaredValue() { return mService.costDeclaredValue; }
    public String getHomeDelivery() { return mService.homeDelivery; }
    public String getSaturdayDelivery() { return mService.saturdayDelivery; }
    public int getError() { return mService.error; }
    public String getErrorMessage() { return mService.errorMessage; }

    private static class Service {
      @Element(name="Codigo") private int code;
      @Element(name="Valor") private String value;
      @Element(name="PrazoEntrega") private int deliveryTime;
      @Element(name="ValorSemAdicionais") private String costNoExtras;
      @Element(name="ValorMaoPropria") private String costSigned;
      @Element(name="ValorAvisoRecebimento") private String costDeliveryNotice;
      @Element(name="ValorValorDeclarado") private String costDeclaredValue;
      @Element(name="EntregaDomiciliar") private String homeDelivery;
      @Element(name="EntregaSabado") private String saturdayDelivery;
      @Element(name="Erro") private int error;
      @Element(name="MsgErro", required = false) private String errorMessage;
    }
  }

}
