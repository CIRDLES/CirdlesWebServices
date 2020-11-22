package org.cirdles.webServices.squidink;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.apache.commons.io.IOUtils;
import org.cirdles.ambapo.*;
import org.cirdles.webServices.requestUtils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.Runtime;
import java.lang.Process;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class SquidInkStartupServlet extends HttpServlet {
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {


            System.out.println(response);
        Process process = Runtime.getRuntime()
                .exec("docker run -p 8081:8080 squidboys");
        response.getWriter().println(process.isAlive());
        }
        catch (IOException | NullPointerException | SecurityException | IllegalArgumentException e) {
            System.out.println(e);
            response.getWriter().println(e);
        }

    }

    private JSONObject handleUtmToLatlong(HttpServletRequest request,
                                          HttpServletResponse response) throws IOException {
        JSONObject json = JSONUtils.extractRequestJSON(request);
        JSONObject responseJson = new JSONObject();
        try {
            BigDecimal easting = BigDecimal.valueOf(json.getLong("easting"));
            BigDecimal northing = BigDecimal.valueOf(json.getLong("northing"));
            String hemStr = json.getString("hemisphere");
            int zoneNumber = json.getInt("zoneNumber");
            String zoneStr = json.getString("zoneLetter");
            String datum = json.getString("datum");
            if (easting != null && northing != null
                    && hemStr != null && !hemStr.isEmpty()
                    && zoneStr != null && !zoneStr.isEmpty()
                    && datum != null && !datum.isEmpty()) {
                char hemisphere = hemStr.charAt(0);
                char zoneLetter = zoneStr.charAt(0);
                try {
                    UTM utm = new UTM(easting, northing, hemisphere, zoneNumber, zoneLetter);
                    Coordinate coord = UTMToLatLong.convert(utm, datum);
                    responseJson.put("latitude", coord.getLatitude());
                    responseJson.put("longitude", coord.getLongitude());
                    responseJson.put("datum", coord.getDatum());
                } catch (Exception e) {
                    responseJson = JSONUtils.createResponseErrorJSON("Error converting: " + e.toString());
                }
            } else {
                responseJson = JSONUtils.createResponseErrorJSON("Invalid request parameters");
            }
        } catch (JSONException e) {
            responseJson = JSONUtils.createResponseErrorJSON("Invalid request parameters: " + e.getMessage());
        }
        return responseJson;
    }

    private JSONObject handleLatlongToUtm(HttpServletRequest request,
                                          HttpServletResponse response) throws IOException {
        JSONObject json = JSONUtils.extractRequestJSON(request);
        JSONObject responseJson = new JSONObject();
        try {
            BigDecimal latitude = BigDecimal.valueOf(json.getDouble("latitude"));
            BigDecimal longitude = BigDecimal.valueOf(json.getDouble("longitude"));
            String datum = json.getString("datum");
            if (latitude != null && longitude != null
                    && datum != null && !datum.isEmpty()) {
                try {
                    UTM utm = LatLongToUTM.convert(latitude, longitude, datum);
                    responseJson.put("easting", utm.getEasting());
                    responseJson.put("northing", utm.getNorthing());
                    responseJson.put("hemisphere", utm.getHemisphere());
                    responseJson.put("zoneNumber", utm.getZoneNumber());
                    responseJson.put("zoneLetter", utm.getZoneLetter());
                } catch (Exception e) {
                    responseJson = JSONUtils.createResponseErrorJSON("Error converting: " + e.toString());
                }
            } else {
                responseJson = JSONUtils.createResponseErrorJSON("Invalid request parameters");
            }
        } catch (JSONException e) {
            responseJson = JSONUtils.createResponseErrorJSON("Invalid request parameters: " + e.getMessage());
        }
        return responseJson;
    }

    private JSONObject handleLatlongToLatlong(HttpServletRequest request,
                                              HttpServletResponse response) throws IOException {
        JSONObject json = JSONUtils.extractRequestJSON(request);
        JSONObject responseJson = new JSONObject();
        try {
            BigDecimal latitude = BigDecimal.valueOf(json.getDouble("latitude"));
            BigDecimal longitude = BigDecimal.valueOf(json.getDouble("longitude"));
            String fromDatum = json.getString("fromDatum");
            String toDatum = json.getString("toDatum");
            if (latitude != null && longitude != null
                    && fromDatum != null && !fromDatum.isEmpty()
                    && toDatum != null && !toDatum.isEmpty()) {
                try {
                    Coordinate coord = LatLongToLatLong.convert(latitude, longitude, fromDatum, toDatum);
                    responseJson.put("latitude", coord.getLatitude());
                    responseJson.put("longitude", coord.getLongitude());
                    responseJson.put("datum", coord.getDatum());
                } catch (Exception e) {
                    responseJson = JSONUtils.createResponseErrorJSON("Error converting: " + e.toString());
                }
            } else {
                responseJson = JSONUtils.createResponseErrorJSON("Invalid request parameters");
            }
        } catch (JSONException e) {
            responseJson = JSONUtils.createResponseErrorJSON("Invalid request parameters: " + e.getMessage());
        }
        return responseJson;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "SquidInkLogin Servlet";
    }// </editor-fold>

}
