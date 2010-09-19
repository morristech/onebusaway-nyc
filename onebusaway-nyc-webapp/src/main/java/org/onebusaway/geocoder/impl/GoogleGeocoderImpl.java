package org.onebusaway.geocoder.impl;

import org.onebusaway.geocoder.model.GeocoderResults;
import org.onebusaway.geocoder.model.GoogleAddressComponent;
import org.onebusaway.geocoder.model.GoogleGeocoderResult;
import org.onebusaway.geocoder.services.GeocoderService;

import org.apache.commons.digester.Digester;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class GoogleGeocoderImpl implements GeocoderService {

  private static final String BASE_URL = "http://maps.google.com/maps/api/geocode/xml";
  
  private boolean sensor = false;
  
  public void setSensor(boolean sensor) {
    this.sensor = sensor;
  }

  public GeocoderResults geocode(String location) {

    StringBuilder b = new StringBuilder();
    b.append(BASE_URL);
    b.append("?");
    b.append("sensor=").append(sensor);
    String encodedLocation;
    try {
      encodedLocation = URLEncoder.encode(location, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      throw new IllegalStateException("unknown encoding: UTF-8");
    }
    b.append("&address=").append(encodedLocation);

    URL url = url(b.toString());

    Digester digester = createDigester();

    GeocoderResults results = new GeocoderResults();
    digester.push(results);

    InputStream inputStream = null;
    try {
      inputStream = url.openStream();
      digester.parse(inputStream);
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    } finally {
      if (inputStream != null) {
        try { inputStream.close(); } catch (Exception ex) {}
      }
    }

    return results;
  }

  private Digester createDigester() {

    Digester digester = new Digester();

    Class<?>[] dType = {Double.class};

    digester.addObjectCreate("GeocodeResponse/result", GoogleGeocoderResult.class);
    
    digester.addObjectCreate("GeocodeResponse/result/address_component", GoogleAddressComponent.class);
    digester.addCallMethod("GeocodeResponse/result/address_component/long_name", "setLongName", 0);
    digester.addCallMethod("GeocodeResponse/result/address_component/short_name", "setShortName", 0);
    digester.addCallMethod("GeocodeResponse/result/address_component/type", "addType", 0);
    digester.addSetNext("GeocodeResponse/result/address_component", "addAddressComponent");
    
    digester.addCallMethod("GeocodeResponse/result/geometry/location/lat", "setLatitude", 0, dType);
    digester.addCallMethod("GeocodeResponse/result/geometry/location/lng", "setLongitude", 0, dType);
    digester.addSetNext("GeocodeResponse/result", "addResult");

    return digester;
  }

  private URL url(String value) {
    try {
      return new URL(value);
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

}