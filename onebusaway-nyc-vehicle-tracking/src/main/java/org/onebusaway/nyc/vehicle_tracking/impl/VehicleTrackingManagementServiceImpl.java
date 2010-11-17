package org.onebusaway.nyc.vehicle_tracking.impl;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.container.cache.Cacheable;
import org.onebusaway.nyc.transit_data.model.NycVehicleStatusBean;
import org.onebusaway.nyc.transit_data.services.VehicleTrackingManagementService;
import org.onebusaway.nyc.vehicle_tracking.model.VehicleLocationManagementRecord;
import org.onebusaway.nyc.vehicle_tracking.services.VehicleLocationService;
import org.onebusaway.transit_data.model.AgencyBean;
import org.onebusaway.transit_data.model.AgencyWithCoverageBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class VehicleTrackingManagementServiceImpl implements
    VehicleTrackingManagementService {

  private double _vehicleOffRouteDistanceThreshold = 500;

  private int _vehicleStalledTimeThreshold = 2 * 60;

  private VehicleLocationService _vehicleLocationService;

  private TransitDataService _transitDataService;

  @Autowired
  public void setVehicleLocationService(
      VehicleLocationService vehicleLocationService) {
    _vehicleLocationService = vehicleLocationService;
  }

  @Autowired
  public void setTransitDataService(TransitDataService transitDataService) {
    _transitDataService = transitDataService;
  }

  /****
   * {@link VehicleTrackingManagementService} Interface
   ****/

  @Override
  @Cacheable
  public String getDefaultAgencyId() {

    List<AgencyWithCoverageBean> agenciesWithCoverage = _transitDataService.getAgenciesWithCoverage();

    if (agenciesWithCoverage.isEmpty())
      throw new IllegalStateException("No agencies found!");

    for (AgencyWithCoverageBean awc : agenciesWithCoverage) {
      AgencyBean agency = awc.getAgency();
      if (agency.getName().contains("MTA"))
        return agency.getId();
    }

    AgencyWithCoverageBean awc = agenciesWithCoverage.get(0);
    return awc.getAgency().getId();
  }

  @Override
  public double getVehicleOffRouteDistanceThreshold() {
    return _vehicleOffRouteDistanceThreshold;
  }

  @Override
  public int getVehicleStalledTimeThreshold() {
    return _vehicleStalledTimeThreshold;
  }

  @Override
  public void setVehicleOffRouteDistanceThreshold(
      double vehicleOffRouteDistanceThreshold) {
    _vehicleOffRouteDistanceThreshold = vehicleOffRouteDistanceThreshold;
  }

  @Override
  public void setVehicleStalledTimeThreshold(int vehicleStalledTimeThreshold) {
    _vehicleStalledTimeThreshold = vehicleStalledTimeThreshold;
  }

  @Override
  public void setVehicleStatus(String vehicleId, boolean status) {
    _vehicleLocationService.setVehicleStatus(vehicleId, status);
  }

  @Override
  public List<NycVehicleStatusBean> getAllVehicleStatuses() {
    List<VehicleLocationManagementRecord> records = _vehicleLocationService.getVehicleLocationManagementRecords();
    List<NycVehicleStatusBean> beans = new ArrayList<NycVehicleStatusBean>();
    for (VehicleLocationManagementRecord record : records)
      beans.add(getManagementRecordAsStatus(record));
    return beans;
  }

  @Override
  public NycVehicleStatusBean getVehicleStatusForVehicleId(String vehicleId) {
    VehicleLocationManagementRecord record = _vehicleLocationService.getVehicleLocationManagementRecordForVehicle(vehicleId);
    if (record == null)
      return null;
    return getManagementRecordAsStatus(record);
  }

  /****
   * Private Methods
   ****/

  private NycVehicleStatusBean getManagementRecordAsStatus(
      VehicleLocationManagementRecord record) {

    if (record == null)
      return null;
    NycVehicleStatusBean bean = new NycVehicleStatusBean();
    bean.setVehicleId(AgencyAndIdLibrary.convertToString(record.getVehicleId()));
    bean.setEnabled(record.isEnabled());
    bean.setLastUpdateTime(record.getLastUpdateTime());
    bean.setLastGpsTime(record.getLastGpsTime());
    bean.setLastGpsLat(record.getLastGpsLat());
    bean.setLastGpsLon(record.getLastGpsLon());
    bean.setPhase(record.getPhase().toString());
    bean.setStatus(record.getStatus());
    bean.setMostRecentDestinationSignCode(record.getMostRecentDestinationSignCode());
    bean.setInferredDestinationSignCode(record.getInferredDestinationSignCode());
    return bean;
  }

}
