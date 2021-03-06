/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.nyc.vehicle_tracking.model;

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.onebusaway.gtfs.csv.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nyc.vehicle_tracking.impl.AgencyIdFieldMappingFactory;

/**
 * Raw vehicle location record to be persisted to the database
 * 
 * @author bdferris
 */
@Entity
@Table(name = "oba_nyc_raw_location")
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class NycVehicleLocationRecord {

  @Id
  @GeneratedValue
  private long id;

  private long time;

  @CsvField(name = "timereceived")
  private long timeReceived;

  private double latitude;

  private double longitude;

  private double bearing;

  @CsvField(name = "destinationsigncode")
  private String destinationSignCode;

  @CsvField(name = "deviceid")
  private String deviceId;

  /** raw GPS sentences */
  @CsvField(optional = true)
  private String gga;

  @CsvField(optional = true)
  private String rmc;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "agencyId", column = @Column(name = "vehicle_agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "vehicle_id"))})
  @CsvField(name = "vehicle", mapping = AgencyIdFieldMappingFactory.class)
  private AgencyAndId vehicleId;

  @Column(name = "rawData", length = 3000)
  @CsvField(name = "rawdata", optional = true)
  private String rawData;

  public void setId(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public long getTime() {
    return time;
  }
  
  public Date getTimeAsDate() {
    return new Date(time);
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setBearing(double bearing) {
    this.bearing = bearing;
  }

  public double getBearing() {
    return bearing;
  }

  public String getDestinationSignCode() {
    return destinationSignCode;
  }

  public void setDestinationSignCode(String destinationSignCode) {
    this.destinationSignCode = destinationSignCode;
  }

  public AgencyAndId getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(AgencyAndId vehicleId) {
    this.vehicleId = vehicleId;
  }

  /**
   * Location data is considered missing if the values are NaN or if both are
   * zero
   */
  public boolean locationDataIsMissing() {
    return (Double.isNaN(this.latitude) || Double.isNaN(this.longitude))
        || (this.latitude == 0.0 && this.longitude == 0.0);
  }

  public void setGga(String gga) {
    this.gga = gga;
  }

  public String getGga() {
    return gga;
  }

  public void setRmc(String rmc) {
    this.rmc = rmc;
  }

  public String getRmc() {
    return rmc;
  }

  public void setTimeReceived(long timeReceived) {
    this.timeReceived = timeReceived;
  }

  public long getTimeReceived() {
    return timeReceived;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setRawData(String rawData) {
    this.rawData = rawData;
  }

  public String getRawData() {
    return rawData;
  }
  
  @Override
  public String toString() {
    return latitude + " " + longitude + " " + destinationSignCode + " " + timeReceived;
  }
}
