package com.slotit.servlets;

import com.slotit.dao.UserDAO;
import com.slotit.models.User;
import com.slotit.utils.JsonUtil;
import com.slotit.utils.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Handles user login.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/api/auth/login"})
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonUtil.handleOptions(resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonUtil.prepare(response);

        try {
            String jsonBody = JsonUtil.readJsonBody(request);
            String email = JsonUtil.paramOrBody(request, jsonBody, "email");
            String password = JsonUtil.paramOrBody(request, jsonBody, "password");

            if (email == null || password == null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Email and password required.");
                return;
            }

            User user = userDAO.findByEmail(email.trim());

            if (user != null && PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                String jsonResponse = String.format(Locale.US,
                    "{\"success\":true,\"user\":{\"id\":%d,\"userId\":%d,\"name\":\"%s\",\"email\":\"%s\"," +
                    "\"phone\":\"%s\",\"role\":\"%s\",\"walletBalance\":%.2f,\"streakCount\":%d," +
                    "\"totalBookings\":%d,\"badge\":\"%s\"}}",
                    user.getUserId(),
                    user.getUserId(),
                    JsonUtil.escapeJson(user.getName()),
                    JsonUtil.escapeJson(user.getEmail()),
                    JsonUtil.escapeJson(user.getPhone()),
                    JsonUtil.escapeJson(user.getRole()),
                    user.getWalletBalance(),
                    user.getStreakCount(),
                    user.getTotalBookings(),
                    JsonUtil.escapeJson(user.getBadge() != null ? user.getBadge() : "none")
                );

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(jsonResponse);
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error occurred.");
        }
    }
}
