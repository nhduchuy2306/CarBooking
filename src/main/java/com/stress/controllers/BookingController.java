package com.stress.controllers;

import com.stress.dao.CityDAO;
import com.stress.dao.OrderDAO;
import com.stress.dao.RouteDAO;
import com.stress.dao.SeatDAO;
import com.stress.dao.TicketDAO;
import com.stress.dao.TripDAO;
import com.stress.dao.UserDAO;
import com.stress.dto.Order;
import com.stress.dto.Seat;
import com.stress.dto.Ticket;
import com.stress.dto.Trip;
import com.stress.dto.User;
import com.stress.service.CityDAOImpl;
import com.stress.service.OrderDAOImpl;
import com.stress.service.RouteDAOImpl;
import com.stress.service.SeatDAOImpl;
import com.stress.service.TicketDAOImpl;
import com.stress.service.TripDAOImpl;
import com.stress.service.UserDAOImpl;
import com.stress.utils.CommonFunction;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Huy
 */
@WebServlet(name = "BookingController", urlPatterns = {"/book"})
public class BookingController extends HttpServlet {

    private TripDAO tripDAO = new TripDAOImpl();
    private RouteDAO routeDAO = new RouteDAOImpl();
    private CityDAO cityDAO = new CityDAOImpl();
    private SeatDAO seatDAO = new SeatDAOImpl();
    private UserDAO userDAO = new UserDAOImpl();
    private TicketDAO ticketDAO = new TicketDAOImpl();
    private OrderDAO orderDAO = new OrderDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            switch (action) {
                case "Search":
                    showTrip(request, response);
                    break;
                case "createTrip":
                    createTrip(request, response);
                    break;
                case "choose-ticket":
                    ChoosingTicket(request, response);
                    break;
                default:
                    throw new AssertionError();
            }
        } catch (Exception e) {
            System.out.println("Error " + e.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    private void showTrip(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        try {
            int routeID = Integer.parseInt(request.getParameter("routeID"));
            String startDay = request.getParameter("start");          
            List<Trip> listTrip = tripDAO.getAllTripByRouteAndStartDay(routeID, startDay);
            request.setAttribute("LIST_ALL_TRIP_BY_LOCATION", listTrip);
            String []a=startDay.split("/");
            String checkStartDate=a[2]+"-"+a[1]+"-"+a[0];
            SimpleDateFormat formater=new SimpleDateFormat("yyyy-MM-dd");
            Date dateInput=formater.parse(checkStartDate);
            String test= java.time.LocalDate.now().toString();
            Date currentDate=formater.parse(test);
            List<Trip> listTrip=null;
            if (dateInput.compareTo(currentDate)==0) {
                listTrip=tripDAO.getAllTripByRouteAndSameStartDay(routeID, startDay);
            }else{
                listTrip = tripDAO.getAllTripByRouteAndStartDay(routeID, startDay);
            }
            System.out.println("List " + listTrip);
            request.setAttribute("LIST_ALL_TRIP_BY_LOCATION", listTrip);         
        } catch (Exception e) {
            System.out.println("Error at BookingController - showTrip" + e.toString());
        }
        finally{
            request.getRequestDispatcher("./client/route.jsp").forward(request, response);
        }
    }


    private void createTrip(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String url = "./client/404.jsp";
//        }
        try {
            HttpSession session = request.getSession();
            String tripID = request.getParameter("tripID");
            String seatIDs = request.getParameter("seatID");
            String[] seatID = seatIDs.split(",");
            int quantity = seatID.length; // quantity of Seat that Customer choose 
            double price = 0; // totalPrice
            double accountBalance = 0; // Account balance of Active Customer
            User loginUser = (User) session.getAttribute("LOGIN_USER");
            if (loginUser != null) {
                // Starting checkout
                if(quantity > 0) {
                    
                    Trip choosingTrip = tripDAO.getTripByID(tripID);
                    String orderID = CommonFunction.generateID("tblOrders", "Order");
                    Order order = new Order(orderID, null, "ABL", loginUser, true);
                    order = orderDAO.createOrder(order);
                    for(int i = 0; i < quantity; i++) {
                        Seat seat = seatDAO.getSeatByID(seatID[i], tripID);
                       
                        
                        if(seatDAO.lockSeat(seat.getSeatID(), tripID)) {
                            Ticket ticket = new Ticket(0, seat, choosingTrip, order);
                            ticketDAO.addNewTicket(ticket);
                            price += seat.getPrice();
                        }
                    }
                    accountBalance = Double.parseDouble(loginUser.getAccountBalance());
                    if(accountBalance >= price) {
                        //accountBalance -= price;
                        // update Account Balance again
                        request.setAttribute("SUCCESS", "Check Out Success!");
                        url = "./client/order.jsp";
                    }else {
                        request.setAttribute("ERROR", "Account Balance is not Enough!");
                    }
                }else {
                    request.setAttribute("ERROR", "You not Book any Seat!");
                    
                }
            } else {
                request.setAttribute("ERROR_FOR_LOGIN", "You have to login First");
                url = "./client/index.jsp";
            }
        } catch (Exception e) {
            System.out.println("Error at CheckOut Controller"+e.toString());
        } finally {
            request.getRequestDispatcher(url).forward(request, response);
        }

    }

    private void ChoosingTicket(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String tripID = request.getParameter("tripID");
            String totalSeat = request.getParameter("totalSeat");
            List<Seat> list = seatDAO.getAllUnAvailbeSeatByTripID(tripID);
            List<String> unavailableSeat = new ArrayList<>();
            for (Seat s : list) {
                unavailableSeat.add(s.getSeatID().trim());
            }
            String seat = "";
            for (String s : unavailableSeat) {
                seat+=s+",";
            }
            System.out.println(seat);
            request.setAttribute("totalSeat", totalSeat);
            request.setAttribute("unavailabelSeat", seat);
            request.setAttribute("tripID", tripID);
            request.getRequestDispatcher("/client/ticket-detail.jsp").forward(request, response);
        } catch (Exception e) {
        }
    }

}
