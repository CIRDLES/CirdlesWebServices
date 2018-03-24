/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cirdles.webServices.ambapo;

import java.io.IOException;
import java.math.BigDecimal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cirdles.webServices.requestUtils.*;
import org.cirdles.ambapo.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author ty
 */
public class AmbapoServlet extends HttpServlet {

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
        JSONObject json = new JSONObject();
        String uri = request.getRequestURI().toLowerCase();
        String[] pieces = uri.split("/");
        // first item will be "", second item will be "ambapo"
        if (pieces.length >= 4) {
            String param1 = pieces[2];
            String param2 = pieces[3];
            // UTM -> LatLng
            if (param1.equals("utm") && param2.equals("latlng")) {
                json = handleUtmToLatlng(request, response);
            } else if (param1.equals("latlng") && param2.equals("utm")) {
                json = handleLatlngToUtm(request, response);
            } else if (param1.equals("latlng") && param2.equals("latlng")) {
                json = handleLatlngToLatlng(request, response);
            }
        } else {
            json = JSONUtils.createResponseErrorJSON("Invalid URI");
        }
        response.setContentType("application/json");
        response.getWriter().println(json);
    }

    private JSONObject handleUtmToLatlng(HttpServletRequest request,
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
                    responseJson = JSONUtils.createResponseErrorJSON(e.getMessage());
                }
            } else {
                responseJson = JSONUtils.createResponseErrorJSON("Invalid request parameters");
            }
        } catch (JSONException e) {
            responseJson = JSONUtils.createResponseErrorJSON("Invalid request parameters: " + e.getMessage());
        }
        return responseJson;
    }

    private JSONObject handleLatlngToUtm(HttpServletRequest request,
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
                    responseJson = JSONUtils.createResponseErrorJSON(e.getMessage());
                }
            } else {
                responseJson = JSONUtils.createResponseErrorJSON("Invalid request parameters");
            }
        } catch (JSONException e) {
            responseJson = JSONUtils.createResponseErrorJSON("Invalid request parameters: " + e.getMessage());
        }
        return responseJson;
    }

    private JSONObject handleLatlngToLatlng(HttpServletRequest request,
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
                    responseJson = JSONUtils.createResponseErrorJSON(e.getMessage());
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
        return "Ambapo Servlet";
    }// </editor-fold>

}
