package com.slotit.servlets;

import com.slotit.dao.BookingDAO;
import com.slotit.utils.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/owner/dashboard")
public class OwnerDashboardServlet extends HttpServlet {
    private BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.handleOptions(resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.prepare(resp);

        try {
            String ownerIdStr = req.getParameter("owner_id");
            if (ownerIdStr == null) ownerIdStr = req.getParameter("ownerId");
            int ownerId = (ownerIdStr != null && !ownerIdStr.trim().isEmpty()) ? Integer.parseInt(ownerIdStr.trim()) : 4;
            String dashboardJson = bookingDAO.getOwnerDashboard(ownerId);
            resp.getWriter().write(dashboardJson);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters");
        }
    }
}
