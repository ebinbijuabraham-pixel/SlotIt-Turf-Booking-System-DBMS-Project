package com.slotit.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Simple JSON Utility since we are avoiding external libraries like Gson/Jackson
 * for this student project. Note: In production, use Gson or Jackson.
 */
public class JsonUtil {

    /**
     * Reads the JSON body from an HttpServletRequest.
     */
    public static String readJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Extracts a string field value from a flat JSON string.
     * Naive implementation for simple key-value pairs without nesting.
     */
    public static String parseField(String jsonString, String fieldName) {
        if (jsonString == null || jsonString.isEmpty()) return null;
        
        String keyToFind = "\"" + fieldName + "\"";
        int keyIndex = jsonString.indexOf(keyToFind);
        if (keyIndex == -1) return null;
        
        int colonIndex = jsonString.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;
        
        int valueStart = -1;
        int valueEnd = -1;
        
        // Find the start of the value
        for (int i = colonIndex + 1; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);
            if (c == '"') {
                valueStart = i + 1;
                break;
            } else if (!Character.isWhitespace(c)) {
                // Number or boolean
                valueStart = i;
                break;
            }
        }
        
        if (valueStart == -1) return null;
        
        // Find the end of the value
        if (jsonString.charAt(valueStart - 1) == '"') {
            valueEnd = jsonString.indexOf("\"", valueStart);
        } else {
            for (int i = valueStart; i < jsonString.length(); i++) {
                char c = jsonString.charAt(i);
                if (c == ',' || c == '}') {
                    valueEnd = i;
                    break;
                }
            }
        }
        
        if (valueEnd == -1) return null;
        
        String value = jsonString.substring(valueStart, valueEnd).trim();
        return "null".equals(value) ? null : value;
    }

    /**
     * Returns a query parameter if present, otherwise the field from the given JSON body.
     */
    public static String paramOrBody(HttpServletRequest request, String body, String name) {
        String v = request.getParameter(name);
        if (v != null && !v.trim().isEmpty()) return v.trim();
        return parseField(body, name);
    }

    /**
     * Creates a simple JSON response string.
     */
    public static String toJsonResponse(String key, String value) {
        return "{\"" + key + "\":\"" + escapeJson(value) + "\"}";
    }

    public static String toJsonResponse(String key, boolean value) {
        return "{\"" + key + "\":" + value + "}";
    }

    /**
     * Escapes a string for safe inclusion inside a JSON string literal (null -> "").
     */
    public static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"")
                  .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    /**
     * Applies CORS headers (used by every servlet, including OPTIONS preflight).
     */
    public static void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    /**
     * Sets CORS headers and the JSON content type.
     */
    public static void prepare(HttpServletResponse resp) {
        setCors(resp);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }

    /**
     * Standard OPTIONS preflight response.
     */
    public static void handleOptions(HttpServletResponse resp) {
        setCors(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Writes {"success":false,"message":...} with the given status.
     */
    public static void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write("{\"success\":false,\"message\":\"" + escapeJson(message) + "\"}");
    }

    /**
     * Writes {"success":true,"message":...}.
     */
    public static void sendSuccess(HttpServletResponse resp, String message) throws IOException {
        resp.getWriter().write("{\"success\":true,\"message\":\"" + escapeJson(message) + "\"}");
    }
}
