package org.onebusaway.nyc.webapp.actions.admin;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.nyc.presentation.impl.WebappIdParser;
import org.onebusaway.nyc.presentation.service.ConfigurationBean;
import org.onebusaway.nyc.presentation.service.ConfigurationService;
import org.onebusaway.nyc.transit_data.model.NycVehicleStatusBean;
import org.onebusaway.nyc.transit_data.services.VehicleTrackingManagementService;
import org.onebusaway.nyc.webapp.actions.OneBusAwayNYCActionSupport;
import org.onebusaway.realtime.api.EVehiclePhase;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

@Results( {@Result(type = "redirectAction", name = "redirect", params = {
    "namespace", "/admin", "actionName", "vehicles"})})
public class VehiclesAction extends OneBusAwayNYCActionSupport implements ServletRequestAware {

  private static final long serialVersionUID = 1L;
  
  private static final WebappIdParser idParser = new WebappIdParser();
  
  private String agencyId = "MTA NYCT";

  @Autowired
  private TransitDataService transitService;
  
  @Autowired
  private VehicleTrackingManagementService vehicleTrackingManagementService;

  private List<VehicleBag> vehicles = new ArrayList<VehicleBag>();

  private HttpServletRequest request;

  @Autowired
  private ConfigurationService configurationService;

  @Override
  public void setServletRequest(HttpServletRequest request) {
    this.request = request;
  }
  
  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  @Override
  public String execute() throws Exception {
    ListBean<VehicleStatusBean> vehiclesForAgencyListBean = transitService.getAllVehiclesForAgency(agencyId, System.currentTimeMillis());
    List<VehicleStatusBean> vehicleStatusBeans = vehiclesForAgencyListBean.getList();
    List<NycVehicleStatusBean> nycVehicleStatuses = vehicleTrackingManagementService.getAllVehicleStatuses();
    Map<String, VehicleStatusBean> vehicleMap = new HashMap<String, VehicleStatusBean>();
    Map<String, NycVehicleStatusBean> nycVehicleMap = new HashMap<String, NycVehicleStatusBean>();
    for (VehicleStatusBean vehicleStatusBean : vehicleStatusBeans) {
      String vehicleId = vehicleStatusBean.getVehicleId();
      vehicleMap.put(vehicleId, vehicleStatusBean);
    }
    for (NycVehicleStatusBean nycVehicleStatusBean : nycVehicleStatuses) {
      String vehicleId = nycVehicleStatusBean.getVehicleId();
      nycVehicleMap.put(vehicleId, nycVehicleStatusBean);
    }

    String method = request.getMethod().toUpperCase();
    if (method.equals("POST")) {
      // keep track of vehicles that have been disabled so we can enable the others
      Set<String> disabledVehicles = new HashSet<String>();

      Enumeration<?> parameterNames = request.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        String key = parameterNames.nextElement().toString();
        if (key.startsWith("disable_")) {
          String vehicleId = key.substring("disable_".length());
          NycVehicleStatusBean nycVehicleStatusBean = nycVehicleMap.get(vehicleId);
          if (nycVehicleStatusBean == null) {
            vehicleTrackingManagementService.setVehicleStatus(vehicleId, false);            
          } else {
            // no need to call disable on it if it's already disabled
            boolean isDisabled = !nycVehicleStatusBean.isEnabled();
            if (!isDisabled)
              vehicleTrackingManagementService.setVehicleStatus(vehicleId, false);
          }
          disabledVehicles.add(vehicleId);
        }
      }

      // enable all the vehicles that haven't been explicitly disabled from the interface
      for (NycVehicleStatusBean nycVehicleStatusBean : nycVehicleStatuses) {
        String vehicleId = nycVehicleStatusBean.getVehicleId();
        if (!disabledVehicles.contains(vehicleId) && !nycVehicleStatusBean.isEnabled())
            vehicleTrackingManagementService.setVehicleStatus(vehicleId, true);
      }
      
      return "redirect";
    }
    
    for (NycVehicleStatusBean nycVehicleStatusBean : nycVehicleStatuses) {
      String vehicleId = nycVehicleStatusBean.getVehicleId();
      VehicleStatusBean vehicleStatusBean = vehicleMap.get(vehicleId);
      VehicleBag vehicleBag = new VehicleBag(nycVehicleStatusBean,
          vehicleStatusBean, configurationService.getConfiguration());
      vehicles.add(vehicleBag);
    }
    return SUCCESS;
  }

  public List<VehicleBag> getVehicles() {
    return vehicles;
  }

  // vehicle data bag suitable for use in vehicles jsp
  private static class VehicleBag {
    private NycVehicleStatusBean nycVehicleStatusBean;
    private VehicleStatusBean vehicleStatusBean;
    private ConfigurationBean configuration;

    public VehicleBag(NycVehicleStatusBean nycVehicleStatusBean,
        VehicleStatusBean vehicleStatusBean, ConfigurationBean configuration) {
      this.nycVehicleStatusBean = nycVehicleStatusBean;
      this.vehicleStatusBean = vehicleStatusBean;
      this.configuration = configuration;
    }
    
    @SuppressWarnings("unused")
    public String getVehicleId() {
      String vehicleIdWithAgency = nycVehicleStatusBean.getVehicleId();
      String idWithoutAgency = idParser.parseIdWithoutAgency(vehicleIdWithAgency);
      return idWithoutAgency;
    }
    
    @SuppressWarnings("unused")
    public String getStatusClass() {
      if (vehicleStatusBean == null)
        return "status red";
      String status = vehicleStatusBean.getStatus();
      TripBean tripBean = vehicleStatusBean.getTrip();
      if (tripBean == null)
        return "status red";
      String tripHeadsign = tripBean.getTripHeadsign();
      long lastUpdateTime = vehicleStatusBean.getLastUpdateTime();
      long now = System.currentTimeMillis();
      long timeDiff = now - lastUpdateTime;
      long redMillisThreshold = configuration.getNoProgressTimeout() * 1000;
      long orangeMillisThreshold = configuration.getHideTimeout() * 1000;
      if (timeDiff > redMillisThreshold)
        return "status red";
      if (timeDiff > orangeMillisThreshold)
        return "status orange";
      if (status == null || !status.equals(EVehiclePhase.IN_PROGRESS.toString()))
        return "status orange";
      return "status normal";
    }
    
    @SuppressWarnings("unused")
    public String getLastUpdateTime() {
      long lastUpdateTime = nycVehicleStatusBean.getLastUpdateTime();
      long now = System.currentTimeMillis();
      long timeDiff = now - lastUpdateTime;
      long seconds = timeDiff / 1000;
      if (seconds < 60)
        return seconds == 1 ? "1 second" : seconds + " seconds";
      if (seconds >= 60 && seconds < 120)
        return "1 minute";
      long minutes = seconds / 60;
      return minutes + " minutes";
    }
    
    @SuppressWarnings("unused")
    public String getLastCommTime() {
      long lastUpdateTime = nycVehicleStatusBean.getLastGpsTime();
      long now = System.currentTimeMillis();
      long timeDiff = now - lastUpdateTime;
      long seconds = timeDiff / 1000;
      if (seconds < 60)
        return seconds == 1 ? "1 second" : seconds + " seconds";
      if (seconds >= 60 && seconds < 120)
        return "1 minute";
      long minutes = seconds / 60;
      return minutes + " minutes";
    }
    
    @SuppressWarnings("unused")
    public String getHeadsign() {
      if (vehicleStatusBean == null)
        return "Disabled";
      TripBean trip = vehicleStatusBean.getTrip();
      if (trip == null)
        return "Not In Service";
      String tripHeadsign = trip.getTripHeadsign();
      String inferredDestinationSignCode = nycVehicleStatusBean.getInferredDestinationSignCode();
      String mostRecentDestinationSignCode = nycVehicleStatusBean.getMostRecentDestinationSignCode();
      if (inferredDestinationSignCode.equals(mostRecentDestinationSignCode)) {
        return inferredDestinationSignCode + ": " + tripHeadsign;
      } else {
        return inferredDestinationSignCode + ": " + tripHeadsign + " (bus sent " + mostRecentDestinationSignCode + ")";
      }
    }
    
    public String getInferredState() {
      if (vehicleStatusBean == null)
        return "Disabled";
      TripBean trip = vehicleStatusBean.getTrip();
      if (trip == null)
        return "No Trip";
      String status = vehicleStatusBean.getStatus();
      if (status != null && status.equals(EVehiclePhase.IN_PROGRESS.toString()))
        return "Normal";
      return "Unknown";
    }
    
    @SuppressWarnings("unused")
    public String getInferredStateClass() {
      String inferredState = getInferredState();
      return inferredState != "Normal" ? "inferred-state error" : "inferred-state";
    }
    
    @SuppressWarnings("unused")
    public String getLocation() {
      CoordinatePoint location = vehicleStatusBean.getLocation();
      double lat = location.getLat();
      double lon = location.getLon();
      return lat + "," + lon;
    }
    
    @SuppressWarnings("unused")
    public String getDisabledName() {
      String vehicleId = vehicleStatusBean.getVehicleId();
      return "disable_" + vehicleId;
    }

    @SuppressWarnings("unused")
    public boolean isDisabled() {
      return !nycVehicleStatusBean.isEnabled();
    }
  }
}
