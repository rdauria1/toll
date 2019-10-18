package com.amaxzadigital.tollpays.checkin;

public class ModelAxles {
  String vehicle_type_id, axle_id, name;

  public ModelAxles(String vehicle_type_id, String axle_id, String name) {
    setAxleId(axle_id);
    setName(name);
    setVehicle_type_id(vehicle_type_id);
  }

  public String getAxleId() {
    return axle_id;
  }

  public void setAxleId(String id) {
    this.axle_id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVehicle_type_id() {
    return vehicle_type_id;
  }

  public void setVehicle_type_id(String vehicle_type_id) {
    this.vehicle_type_id = vehicle_type_id;
  }
}
