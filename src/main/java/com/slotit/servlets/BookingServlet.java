package com.slotit.servlets;

import com.slotit.dao.BookingDAO;
import com.slotit.utils.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/bookings", "/api/bookings/cancel"})
public class BookingServlet extends HttpServlet {
    private BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.handleOptions(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.prepare(resp);

        // Allow POST /api/bookings/cancel as well as DELETE /api/bookings
        if (req.getServletPath().endsWith("/cancel")) {
            handleCancel(req, resp);
            return;
        }

        try {
            String jsonBody = JsonUtil.readJsonBody(req);
            String slotIdStr = JsonUtil.paramOrBody(req, jsonBody, "slot_id");
            if (slotIdStr == null) slotIdStr = JsonUtil.paramOrBody(req, jsonBody, "slotId");
            String userIdStr = JsonUtil.paramOrBody(req, jsonBody, "user_id");
            if (userIdStr == null) userIdStr = JsonUtil.paramOrBody(req, jsonBody, "userId");

            if (slotIdStr == null || userIdStr == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing slot_id or user_id");
                return;
            }

            int slotId = Integer.parseInt(slotIdStr.trim());
            int userId = Integer.parseInt(userIdStr.trim());

            Map<String, Object> result = bookingDAO.createBooking(userId, slotId);

            boolean success = Boolean.TRUE.equals(result.get("success"));
            if (success) {
                double newBal = result.get("newWalletBalance") instanceof Number
                    ? ((Number) result.get("newWalletBalance")).doubleValue() : 0.0;
                resp.getWriter().write(String.format(Locale.US,
                    "{\"success\":true,\"bookingId\":%d,\"newWalletBalance\":%.2f,\"message\":\"%s\"}",
                    (Integer) result.get("bookingId"),
                    newBal,
                    JsonUtil.escapeJson((String) result.get("message"))
                ));
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, (String) result.get("message"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Booking failed: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.prepare(resp);
        handleCancel(req, resp);
    }

    private void handleCancel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String jsonBody = JsonUtil.readJsonBody(req);
            String bookingIdStr = JsonUtil.paramOrBody(req, jsonBody, "booking_id");
            if (bookingIdStr == null) bookingIdStr = JsonUtil.paramOrBody(req, jsonBody, "bookingId");
            String userIdStr = JsonUtil.paramOrBody(req, jsonBody, "user_id");
            if (userIdStr == null) userIdStr = JsonUtil.paramOrBody(req, jsonBody, "userId");

            if (bookingIdStr == null || userIdStr == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing booking_id or user_id");
                return;
            }

            int bookingId = Integer.parseInt(bookingIdStr.trim());
            int userId = Integer.parseInt(userIdStr.trim());

            boolean success = bookingDAO.cancelBooking(bookingId, userId);

            if (success) {
                JsonUtil.sendSuccess(resp, "Booking cancelled and wallet refunded!");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Cannot cancel this booking (already refunded or past date).");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error cancelling booking: " + e.getMessage());
        }
    }
}
