package com.slotit.servlets;

import com.slotit.dao.TournamentDAO;
import com.slotit.utils.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/api/tournaments", "/api/tournaments/*"})
public class TournamentServlet extends HttpServlet {

    private TournamentDAO tournamentDAO;

    @Override
    public void init() throws ServletException {
        tournamentDAO = new TournamentDAO();
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
            response.getWriter().write(tournamentDAO.getAllTournamentsJson());
        } else if (pathInfo.equals("/teams")) {
            String tIdStr = request.getParameter("tournament_id");
            int tId = (tIdStr != null && !tIdStr.isEmpty()) ? Integer.parseInt(tIdStr) : 0;
            response.getWriter().write(tournamentDAO.getTeamsJson(tId));
        } else {
            JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonUtil.prepare(response);
        String pathInfo = request.getPathInfo();
        String body = JsonUtil.readJsonBody(request);

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/register")) {
            try {
                String tIdStr = JsonUtil.paramOrBody(request, body, "tournament_id");
                if (tIdStr == null) tIdStr = JsonUtil.paramOrBody(request, body, "tournamentId");
                String teamName = JsonUtil.paramOrBody(request, body, "team_name");
                if (teamName == null) teamName = JsonUtil.paramOrBody(request, body, "teamName");
                String capStr = JsonUtil.paramOrBody(request, body, "captain_id");
                if (capStr == null) capStr = JsonUtil.paramOrBody(request, body, "captainId");

                int tournamentId = (tIdStr != null) ? Integer.parseInt(tIdStr.trim()) : 1;
                int captainId = (capStr != null) ? Integer.parseInt(capStr.trim()) : 1;
                if (teamName == null || teamName.trim().isEmpty()) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Team name is required.");
                    return;
                }

                boolean success = tournamentDAO.registerTeam(tournamentId, teamName.trim(), captainId);
                if (success) {
                    JsonUtil.sendSuccess(response, "Team registered successfully!");
                } else {
                    JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "Tournament is full or invalid.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid input parameters.");
            }
        } else {
            JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        }
    }
}
