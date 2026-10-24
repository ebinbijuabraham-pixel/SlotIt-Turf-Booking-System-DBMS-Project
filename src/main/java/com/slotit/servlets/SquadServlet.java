package com.slotit.servlets;

import com.slotit.dao.SquadDAO;
import com.slotit.utils.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/squads", "/api/squads/*"})
public class SquadServlet extends HttpServlet {

    private SquadDAO squadDAO;

    @Override
    public void init() throws ServletException {
        squadDAO = new SquadDAO();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.handleOptions(resp);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonUtil.prepare(response);
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.getWriter().write(squadDAO.getAllSquadsJson());
        } else {
            JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonUtil.prepare(response);
        String pathInfo = request.getPathInfo();
        String body = JsonUtil.readJsonBody(request);

        if (pathInfo != null && pathInfo.equals("/join")) {
            try {
                String reqIdStr = JsonUtil.paramOrBody(request, body, "request_id");
                if (reqIdStr == null) reqIdStr = JsonUtil.paramOrBody(request, body, "requestId");
                String userIdStr = JsonUtil.paramOrBody(request, body, "user_id");
                if (userIdStr == null) userIdStr = JsonUtil.paramOrBody(request, body, "userId");

                int requestId = Integer.parseInt(reqIdStr.trim());
                int userId = (userIdStr != null) ? Integer.parseInt(userIdStr.trim()) : 1;

                Map<String, Object> result = squadDAO.joinSquadWithMessage(requestId, userId);
                if (Boolean.TRUE.equals(result.get("success"))) {
                    JsonUtil.sendSuccess(response, (String) result.get("message"));
                } else {
                    JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, (String) result.get("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid input parameters.");
            }
        } else if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/create")) {
            try {
                String userIdStr = JsonUtil.paramOrBody(request, body, "user_id");
                if (userIdStr == null) userIdStr = JsonUtil.paramOrBody(request, body, "userId");
                String slotIdStr = JsonUtil.paramOrBody(request, body, "slot_id");
                if (slotIdStr == null) slotIdStr = JsonUtil.paramOrBody(request, body, "slotId");
                String sport = JsonUtil.paramOrBody(request, body, "sport");
                String neededStr = JsonUtil.paramOrBody(request, body, "players_needed");
                if (neededStr == null) neededStr = JsonUtil.paramOrBody(request, body, "playersNeeded");
                String message = JsonUtil.paramOrBody(request, body, "message");

                int userId = (userIdStr != null && !userIdStr.isEmpty()) ? Integer.parseInt(userIdStr.trim()) : 1;
                int slotId = (slotIdStr != null && !slotIdStr.isEmpty()) ? Integer.parseInt(slotIdStr.trim()) : 0;
                int playersNeeded = (neededStr != null && !neededStr.isEmpty()) ? Integer.parseInt(neededStr.trim()) : 5;

                boolean ok = squadDAO.createRequest(userId, slotId, sport, playersNeeded, message);
                if (ok) {
                    JsonUtil.sendSuccess(response, "Squad request posted!");
                } else {
                    JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not create squad request.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid parameters for creating squad request.");
            }
        } else {
            JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }
}
