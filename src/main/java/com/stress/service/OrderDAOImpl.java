
package com.stress.service;

import com.stress.dao.OrderDAO;
import com.stress.dto.Order;
import com.stress.dto.User;
import com.stress.utils.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

/**
 *
 * @author Huy
 */
public class OrderDAOImpl implements OrderDAO{

    @Override
    public Order getOderByID(String orderID) throws SQLException {
        Connection conn = null;
        PreparedStatement ptm = null;
        ResultSet rs = null;
        Order order = null;
        String sql = "SELECT [createDate], [PaymentMode],[userID],[Status] FROM tblOrders WHERE [OrderID] = ?";
       
        try {
            conn = DBConnection.getConnection();
            if(conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, orderID);
                rs = ptm.executeQuery();
                if(rs.next()) {
                    Date createDate = rs.getDate("CreateDate");
                    String paymentMode = rs.getString("PaymentMode");
                    String userID = rs.getString("userID");
                    boolean status = rs.getBoolean("Status");
                    User user = new UserDAOImpl().getUserByID(userID);
                    order = new Order(orderID, createDate, paymentMode, user, status);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(rs != null) rs.close();
            if(ptm != null) ptm.close();
            if(conn != null) conn.close();
        }
        return order;
    }

    @Override
    public Order createOrder(Order order) throws SQLException {
       Connection conn = null;
       PreparedStatement ptm = null;
       
       String sql = "INSERT INTO tblOrders([OrderID],[CreateDate],[PaymentMode],[UserID],[Status]) VALUES (?,?,?,?,?)";
        try {
            conn = DBConnection.getConnection();
            if(conn != null) {
                ptm = conn.prepareStatement(sql);
                ptm.setString(1, order.getOrderID());
                ptm.setDate(2, new Date(Calendar.getInstance().getTimeInMillis()));
                ptm.setString(3, order.getPaymentMode());
                ptm.setString(4, order.getUser().getUserID());
                ptm.setBoolean(5, order.isStatus());
                if(ptm.executeUpdate() > 0) {
                    order = getOderByID(order.getOrderID()); 
                }
            }
        } catch (Exception e) {
        } finally {
            if(ptm != null) ptm.close();
            if(conn != null) conn.close();
        }
        return order;
    }
    public static void main(String[] args) {
        System.out.println(new Date(Calendar.getInstance().getTimeInMillis()));
    }
}
