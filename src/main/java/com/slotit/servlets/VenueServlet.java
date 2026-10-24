package com.slotit.servlets;

import com.slotit.dao.VenueDAO;
import com.slotit.models.Venue;
import com.slotit.utils.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Handles venue fetching and searching.
 */
@WebServlet(name = "VenueServlet", urlPatterns = {"/api/venues"})
public class VenueServlet extends HttpServlet {
    private VenueDAO venueDAO = new VenueDAO();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.handleOptions(resp);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonUtil.prepare(response);

        String idParam = request.getParameter("id");
        String latParam = request.getParameter("lat");
        String lngParam = request.getParameter("lng");
        String radiusParam = request.getParameter("radius");
        String sportParam = request.getParameter("sport");
        String cityParam = request.getParameter("city");

        try {
            if (idParam != null && !idParam.trim().isEmpty()) {
                int venueId = Integer.parseInt(idParam.trim());
                Venue v = venueDAO.findById(venueId);
                if (v == null) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Venue not found");
                } else {
                    response.getWriter().write(v.toJson());
                }
                return;
            }

            List<Venue> venues;
            if (latParam != null && lngParam != null && !latParam.isEmpty() && !lngParam.isEmpty()) {
                double lat = Double.parseDouble(latParam);
                double lng = Double.parseDouble(lngParam);
                double radius = (radiusParam != null && !radiusParam.isEmpty()) ? Double.parseDouble(radiusParam) : 25.0;
                venues = venueDAO.findNearby(lat, lng, radius);
                if (venues.isEmpty()) {
                    venues = venueDAO.findAll();
                }
            } else if (sportParam != null && !sportParam.trim().isEmpty() && !"all".equalsIgnoreCase(sportParam.trim())) {
                venues = venueDAO.findBySport(sportParam.trim());
            } else if (cityParam != null && !cityParam.trim().isEmpty()) {
                venues = venueDAO.findByCity(cityParam.trim());
            } else {
                venues = venueDAO.findAll();
            }

            StringBuilder jsonResponse = new StringBuilder("[");
            for (int i = 0; i < venues.size(); i++) {
                jsonResponse.append(venues.get(i).toJson());
                if (i < venues.size() - 1) {
                    jsonResponse.append(",");
                }
            }
            jsonResponse.append("]");

            response.getWriter().write(jsonResponse.toString());
        } catch (NumberFormatException e) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid numeric parameters");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
    }
}
