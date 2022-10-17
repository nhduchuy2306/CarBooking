
package com.stress.dto;

import java.sql.Date;
import java.sql.Time;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trip{
    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;
    public static final int ONGOING = 2;
    public static final int COMPLETE = 3;
    
    private String tripID;
    private String tripName;
    private Date startDateTime;
    private Time startTime;
    private String policy;
    private Route route;
    private Vehicle vehicle;
    private Driver driver;
    private int seatRemain;
    private int status;	
}
