package com.stress.controllers;

import com.stress.dao.CityDAO;
import com.stress.dao.DriverDAO;
import com.stress.dao.LocationDAO;
import com.stress.dao.RouteDAO;
import com.stress.dao.SeatDAO;
import com.stress.dao.TripDAO;
import com.stress.dao.VehicleDAO;
import com.stress.dto.City;
import com.stress.dto.DistanceAndTime;
import com.stress.dto.Driver;
import com.stress.dto.Location;
import com.stress.dto.Route;
import com.stress.dto.Trip;
import com.stress.dto.Vehicle;
import com.stress.service.CityDAOImpl;
import com.stress.service.DriverDAOImpl;
import com.stress.service.LocationDAOImpl;
import com.stress.service.RouteDAOImpl;
import com.stress.service.SeatDAOImpl;
import com.stress.service.TripDAOImpl;
import com.stress.service.VehicleDAOImpl;
import com.stress.utils.CommonFunction;
import com.stress.utils.DistanceUtils;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "RouteController", urlPatterns = {"/admin/route"})
public class RouteController extends HttpServlet {

    private VehicleDAO vehicleDAO = new VehicleDAOImpl();
    private TripDAO tripDAO = new TripDAOImpl();
    private DriverDAO driverDAO = new DriverDAOImpl();
    private RouteDAO routeDAO = new RouteDAOImpl();
    private SeatDAO seatDAO = new SeatDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        try {
//            String action=null;
//            if(request.getAttribute("action")!=null){
//                action=(String)request.getAttribute("action");
//            }else{
            String action = request.getParameter("action");
//            }

            System.out.println("action:" + action);
            switch (action) {
                case "show":
                    viewRoute(request, response);
                    break;
                case "showDelete":
                    showDeleteHistory(request, response);
                    break;

            }
        } catch (Exception e) {
            System.out.println("Error at Do Get " + e.toString());
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        try {
            String action = request.getParameter("action");
            System.out.println("action:" + action);
            switch (action) {
                case "create":
                    addRoute(request, response);
                    break;
                case "update":
                    updateRoute(request, response);
                    break;
                case "delete":
                    deleteRoute(request, response);
                    break;
                case "recover":
                    recoverRoute(request, response);
                    break;
                case "add":
                    addTrip(request, response);
                    break;
                case "add_continue":
                    addTripContinue(request, response);
                    break;
            }
        } catch (Exception e) {

        }
    }

    private void recoverRoute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        String url = "/admin/404.jsp";
        try {
            String routeID = request.getParameter("routeID");
            RouteDAO routeDAO = new RouteDAOImpl();
            if (routeDAO.recoverRoute(routeID)) {
                request.setAttribute("SUCCESS", "Recover Route " + routeID + " Success!");
                Route recoverItem = routeDAO.getRouteByID(Integer.parseInt(routeID));

                request.setAttribute("ROUTE_ID", recoverItem.getRouteName());
                viewRoute(request, response);
            }
        } catch (Exception e) {
            System.out.println("Error at Recover Route Controller " + e.toString());
        } finally {
            request.getRequestDispatcher(url).forward(request, response);
        }
    }

    private void showDeleteHistory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        String url = "/admin/404.jsp";
        try {
            RouteDAO routeDAO = new RouteDAOImpl();
            List<Route> list = routeDAO.getDeleteHistory();
            System.out.println(list.size());
            request.setAttribute("DELETE_LIST", list);
            request.getRequestDispatcher("/admin/routeTable.jsp").forward(request, response);
        } catch (Exception e) {
            System.out.println("Error at Show Delete Route History " + e.toString());
        }
    }

    private void viewRoute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        String url = "./404.jsp";
        try {
            RouteDAO dao = new RouteDAOImpl();
            List<Route> list = dao.getAllRoute();
            List<Location> allLocation = new LocationDAOImpl().getAllLocation();
            List<City> cityList = new CityDAOImpl().getAllCity();

            request.setAttribute("ROUTE_LIST", list);
            request.setAttribute("CITY_LIST", cityList);

            request.setAttribute("LOCATION_LIST", allLocation);
            url = "./routeTable.jsp";

        } catch (Exception e) {
            log("Error at RouteController - viewRoute: " + e.toString());
        } finally {
            request.getRequestDispatcher(url).forward(request, response);
        }
    }

    private void addRoute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        try {

            String startLocID = request.getParameter("startLocation");
            String endLocID = request.getParameter("endLocation");
            String description = request.getParameter("description");

            //==================Conversion process======================//
            int sLID = Integer.parseInt(startLocID);
            int eLID = Integer.parseInt(endLocID);
            LocationDAO LDAO = new LocationDAOImpl();
            Location startLocation = LDAO.getLocationById(sLID);
            Location endLocation = LDAO.getLocationById(eLID);

            String routeName = startLocation.getLocationName() + "_" + endLocation.getLocationName();

            //===========================================================//
            RouteDAO RDAO = new RouteDAOImpl();
            Route createItem = new Route(0, routeName, startLocation, endLocation, description, true);
            if (RDAO.checkDuplicate(createItem)) {
                request.setAttribute("ERROR", "The Start and End Location has exist! Try Again!");
                viewRoute(request, response);
            } else if (createItem.getStartLocation().getLocationID() == createItem.getEndLocation().getLocationID()) {
                request.setAttribute("ERROR", "Start and End Location is the Same! Can't Create");
                viewRoute(request, response);
            } else {
                int routeID = RDAO.addRoute(createItem);
                createItem.setRouteID(routeID);

                if (routeID > 0) {
                    request.setAttribute("ROUTE_ID", createItem.getRouteName());
                    request.setAttribute("SUCCESS", "Create " + createItem.getRouteName() + " Success");
                    viewRoute(request, response);
                }
            }

        } catch (Exception e) {
            log("Error at RouteController - addRoute:" + e.toString());
        }
    }

    private void updateRoute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        try {
            String tmpRouteID = request.getParameter("RouteID");

            String startLocID = request.getParameter("StartLocation");
            String endLocID = request.getParameter("EndLocation");
            String Description = request.getParameter("Description");
            String tmpStatus = request.getParameter("Status");
            //==================Conversion process======================//
            int RouteID = Integer.parseInt(tmpRouteID);
            int sLID = Integer.parseInt(startLocID);
            int eLID = Integer.parseInt(endLocID);
            LocationDAO LDAO = new LocationDAOImpl();
            Location StartLocation = LDAO.getLocationById(sLID);
            Location EndLocation = LDAO.getLocationById(eLID);
            String RouteName = StartLocation.getLocationName() + "_" + EndLocation.getLocationName();
            boolean Status = Boolean.parseBoolean(tmpStatus);
            //===========================================================//
            RouteDAO dao = new RouteDAOImpl();
            boolean checkUpdate = dao.updateRoute(RouteID, RouteName, StartLocation, EndLocation, Description, Status);
            if (checkUpdate) {
                request.setAttribute("ROUTE_ID", RouteName);
                viewRoute(request, response);
            }
        } catch (Exception e) {
            log("Error at RouteController - updateRoute: " + e.toString());
        }
    }

    private void deleteRoute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String RouteID = request.getParameter("routeID");
            RouteDAO dao = new RouteDAOImpl();
            boolean check = dao.deleteRoute(RouteID);
            if (check) {
                viewRoute(request, response);
            }
        } catch (Exception e) {
            log("Error at RouteController - deleteRoute: " + e.toString());
        }
    }

    private void addTrip(HttpServletRequest request, HttpServletResponse response) {
        try {
            String tripID = CommonFunction.generateID("tblTrips", "Trip");
            String tripName = request.getParameter("tripName").trim();
            String startdate = request.getParameter("startdate");
            String startTime = request.getParameter("startTime") + ":00";
            String policy = request.getParameter("policy").trim();
            String routeID = request.getParameter("routeID").trim();

            if (tripID != null && tripName != null && startdate != null && startTime != null && policy != null && routeID != null) {
                request.setAttribute("tripID", tripID);
                request.setAttribute("tripName", tripName);
                request.setAttribute("startdate", startdate);
                request.setAttribute("startTime", startTime);
                request.setAttribute("policy", policy);
                request.setAttribute("routeID", routeID);
                request.setAttribute("ADD_TRIP_CONTINUE", "add_trip_continue");
                RouteDAO routeDAO = new RouteDAOImpl();
                LocationDAO locationDAO = new LocationDAOImpl();
                CityDAO cityDAO = new CityDAOImpl();

                Route route = routeDAO.getRouteByID(Integer.parseInt(routeID));
                Location startLoc = route.getStartLocation();
                Location endLoc = route.getEndLocation();
                // Get New Trip Date and Time
                Date newTripDate = java.sql.Date.valueOf(startdate);
                LocalTime time = Time.valueOf(startTime).toLocalTime();
                long hours = (long) time.getHour();
                LocalDateTime newTripTime = newTripDate.toLocalDate().atStartOfDay().plusHours(hours);
                // quangtm Modify
                List<Vehicle> vList = vehicleDAO.getAllVehicle();
                List<Driver> dList = driverDAO.getAllDriver();
                for(int i = 0; i < vList.size(); i++){
                    Vehicle vehicle = vList.get(i);
                    if (vehicle.getStatus() == 2) {
                        Trip onGoingTrip = tripDAO.getOnGoingTripByVehicle(vehicle.getVehicleID());
                        // Get the Time when Trip is finish
                        DistanceAndTime tripTime = new DistanceUtils().getDistanceAndTime(
                                onGoingTrip.getRoute().getStartLocation().getCity().getCityName(),
                                onGoingTrip.getRoute().getEndLocation().getCity().getCityName());
                        // double the Time going to make sure Vehicle return
                        long realTimeGoing = (long) tripTime.getTime() * 2;
                        LocalDateTime finishTrip = onGoingTrip.getStartDateTime().toLocalDate().
                                atStartOfDay().plusHours(realTimeGoing);
                        if (finishTrip.isBefore(newTripTime)) {
                            vList.remove(vehicle);
                        }

                    }
                }

                for(int i = 0; i < dList.size(); i++) {
                    Driver driver = dList.get(i);
                    if (driver.getStatus() == Driver.ONGOING) {
                        Trip onGoingTrip = tripDAO.getOngoingTripByDriver(driver.getDriverID());

                        DistanceAndTime tripTime = new DistanceUtils().getDistanceAndTime(
                                onGoingTrip.getRoute().getStartLocation().getCity().getCityName(),
                                onGoingTrip.getRoute().getEndLocation().getCity().getCityName());
                        // double the Time going to make sure Vehicle return
                        long realTimeGoing = (long) tripTime.getTime() * 2;
                        LocalDateTime finishTrip = onGoingTrip.getStartDateTime().toLocalDate().
                                atStartOfDay().plusHours(realTimeGoing);
                        if (finishTrip.isBefore(newTripTime)) {
                            dList.remove(driver);
                        }

                    }
                }
                request.setAttribute("LIST_ACTIVE_VEHICLE", vList);
                request.setAttribute("LIST_ACTIVE_DRIVER", dList);
                viewRoute(request, response);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void addTripContinue(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String tripID = CommonFunction.generateID("tblTrips", "Trip");
            String tripName = request.getParameter("tripName").trim();
            String startdate = request.getParameter("startdate");
            String startTime = request.getParameter("startTime");
            String policy = request.getParameter("policy").trim();
            String routeID = request.getParameter("routeID").trim();
            String vehicleID = request.getParameter("vehicleID").trim();
            String driverID = request.getParameter("driverID").trim();

            Vehicle v = vehicleDAO.getVehicleByID(vehicleID);
            v.setStatus(2);
            Route r = routeDAO.getRouteByID(Integer.parseInt(routeID));
            Driver d = driverDAO.getDriverByID(driverID);
            d.setStatus(2);

            Trip trip = new Trip(tripID, tripName, Date.valueOf(startdate), Time.valueOf(startTime),
                    policy, r, v, d, v.getVehicleType().getTotalSeat(), 1);

            boolean check = tripDAO.addTrip(trip);

            if (check) {
                List<String> setMap = seatDAO.setMap(v.getVehicleType().getTotalSeat());
                boolean checkAddSeat = false;
                for (String s : setMap) {
                    checkAddSeat = seatDAO.addSeat(tripID, s);
                }
                if (checkAddSeat) {
                    vehicleDAO.updateVehicle(v);
                    driverDAO.updateDriver(d);
                    request.setAttribute("SUCCESS", "ADD TRIP SUCCESSFULLY");
                    request.setAttribute("tripID", tripID);
                    response.sendRedirect("trip?action=show");
                } else {
                    request.setAttribute("ADD_ERROR", "ADD TRIP ERROR");
                    viewRoute(request, response);
                }
            } else {
                request.setAttribute("ID_EXIST", "create-" + tripID);
                request.setAttribute("action", "show");
                viewRoute(request, response);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
