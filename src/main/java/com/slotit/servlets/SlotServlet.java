package com.slotit.servlets;

import com.slotit.dao.SlotDAO;
import com.slotit.models.Court;
import com.slotit.models.TimeSlot;
import com.slotit.utils.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(urlPatterns = {"/api/slots", "/api/courts"})
public class SlotServlet extends HttpServlet {
    private SlotDAO slotDAO = new SlotDAO();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.handleOptions(resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.prepare(resp);

        String servletPath = req.getServletPath();
        String venueIdStr = req.getParameter("venue_id");
        if (venueIdStr == null || venueIdStr.trim().isEmpty()) {
            venueIdStr = req.getParameter("venueId");
        }
        if (venueIdStr == null || venueIdStr.trim().isEmpty()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter: venue_id");
            return;
        }

        try {
            int venueId = Integer.parseInt(venueIdStr.trim());

            if ("/api/courts".equalsIgnoreCase(servletPath)) {
                List<Court> courts = slotDAO.getCourtsByVenue(venueId);
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < courts.size(); i++) {
                    json.append(courts.get(i).toJson());
                    if (i < courts.size() - 1) json.append(",");
                }
                json.append("]");
                resp.getWriter().write(json.toString());
                return;
            }

            String date = req.getParameter("date");
            if (date == null || date.trim().isEmpty()) {
                date = LocalDate.now().toString();
            }

            List<TimeSlot> slots = slotDAO.getSlotsByVenueAndDate(venueId, date.trim());

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < slots.size(); i++) {
                json.append(slots.get(i).toJson());
                if (i < slots.size() - 1) json.append(",");
            }
            json.append("]");

            resp.getWriter().write(json.toString());
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric venue_id");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error fetching slots");
        }
    }
}
