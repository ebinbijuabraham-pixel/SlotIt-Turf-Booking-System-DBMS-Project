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

/**
 * Handles user registration.
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/api/auth/register"})
public class RegisterServlet extends HttpServlet {
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

            String name = JsonUtil.paramOrBody(request, jsonBody, "name");
            String email = JsonUtil.paramOrBody(request, jsonBody, "email");
            String phone = JsonUtil.paramOrBody(request, jsonBody, "phone");
            String password = JsonUtil.paramOrBody(request, jsonBody, "password");
            String role = JsonUtil.paramOrBody(request, jsonBody, "role");

            if (name == null || email == null || phone == null || password == null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "All fields are required.");
                return;
            }

            // Restrict self-registration roles strictly to 'player' or 'owner'
            String safeRole = "owner".equalsIgnoreCase(role) ? "owner" : "player";

            User existingUser = userDAO.findByEmail(email.trim());
            if (existingUser != null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "Email already registered.");
                return;
            }

            String passwordHash = PasswordUtil.hashPassword(password);
            int userId = userDAO.register(name.trim(), email.trim(), phone.trim(), passwordHash, safeRole);

            if (userId > 0) {
                // Give new players a starter wallet balance of ₹2,500 so they can test bookings immediately
                if ("player".equals(safeRole)) {
                    userDAO.updateWallet(userId, 2500.00);
                }
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write("{\"success\":true,\"userId\":" + userId + ",\"message\":\"Account created successfully\"}");
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create account.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error occurred.");
        }
    }
}
