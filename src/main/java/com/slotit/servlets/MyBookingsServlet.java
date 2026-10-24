package com.slotit.servlets;

import com.slotit.dao.BookingDAO;
import com.slotit.models.Booking;
import com.slotit.utils.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/bookings/my")
public class MyBookingsServlet extends HttpServlet {
    private BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.handleOptions(resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.prepare(resp);

        try {
            String userIdStr = req.getParameter("user_id");
            if (userIdStr == null) userIdStr = req.getParameter("userId");
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing user_id parameter");
                return;
            }
            int userId = Integer.parseInt(userIdStr.trim());
            String type = req.getParameter("type");
            if (type == null || type.trim().isEmpty()) type = "upcoming";

            List<Booking> bookings = bookingDAO.getUserBookings(userId, type);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < bookings.size(); i++) {
                json.append(bookings.get(i).toJson());
                if (i < bookings.size() - 1) json.append(",");
            }
            json.append("]");

            resp.getWriter().write(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
        }
    }
}
