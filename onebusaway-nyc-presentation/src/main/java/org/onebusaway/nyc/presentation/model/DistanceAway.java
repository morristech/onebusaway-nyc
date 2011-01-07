package org.onebusaway.nyc.presentation.model;

import java.util.Date;

/**
 * Data transfer object for how far away a vehicle is
 */
public class DistanceAway implements Comparable<DistanceAway> {
  private int atStopThresholdInFeet = 100;
  private int arrivingThresholdInFeet = 500;
  private int arrivingThresholdInStops = 0;
  private int showDistanceInStopsThresholdInStops = 3;
	
  private final int stopsAway;
  private final int feetAway;
  private final int staleTimeoutSeconds;
  private final Date timestamp;
  private Mode currentMode;
    
  public DistanceAway(int stopsAway, int feetAway, Date timestamp, Mode m, int staleTimeoutSeconds) {
    this.stopsAway = stopsAway;
    this.feetAway = feetAway;
    this.timestamp = timestamp;
    this.currentMode = m;
    this.staleTimeoutSeconds = staleTimeoutSeconds;
  }

  public int getStopsAway() {
    return stopsAway;
  }

  public Date getUpdateTimestamp() {
	    return timestamp;
  }

  public int getFeetAway() {
    return feetAway;
  }

  private String addModifiers(String s) {
	StringBuilder b = new StringBuilder(s);
    
	if(new Date().getTime() - timestamp.getTime() > 1000 * this.staleTimeoutSeconds) {
		switch(currentMode) {
			case SMS:			
				b.insert(0, "~");
				break;
			case MOBILE_WEB:
				b.append(" (location data is old)");
				break;
			default:
				break;
		}
	}

	return b.toString();
  }

  public String getPresentableDistance() {
	String r = "";
	
	if(feetAway <= atStopThresholdInFeet)
		r = "at stop";		
	else if(feetAway <= arrivingThresholdInFeet && stopsAway <= arrivingThresholdInStops) {
		r = "approaching";
	} else {
		if(stopsAway <= showDistanceInStopsThresholdInStops) {
			if(stopsAway == 0)
				  r = "< 1 stop away";
			else	  
				  r = stopsAway == 1
				  	? "1 stop away"
				  	: stopsAway + " stops away";			
		} else {
		  double milesAway = feetAway / 5280;
		  r = String.format("%1.2f mi. away", milesAway);
		}
	}
		
	return this.addModifiers(r);
  }
  
  @Override
  public int hashCode() {
    return 31*feetAway + 31*stopsAway;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DistanceAway))
      return false;
    DistanceAway rhs = (DistanceAway) obj;
    if (feetAway != rhs.feetAway)
      return false;
    if (stopsAway != rhs.stopsAway)
      return false;
    return true;
  }

  @Override
  public int compareTo(DistanceAway rhs) {
    if (feetAway < rhs.feetAway) return -1;
    if (feetAway > rhs.feetAway) return 1;
    if (stopsAway < rhs.stopsAway) return -1;
    if (stopsAway > rhs.stopsAway) return 1;
    return 0;
  }
}
