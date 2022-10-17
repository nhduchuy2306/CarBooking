
package com.stress.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    public static final boolean PENDING  = false;
    public static final boolean COMPLETE  = true;
    
    private String orderID;
    private Date createDate;
    private String paymentMode;
    private User user;
    private boolean status;
}
