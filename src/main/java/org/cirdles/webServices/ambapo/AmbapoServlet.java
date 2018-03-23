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
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.ServletRequestUtils;
import org.cirdles.webServices.requestUtils.*;
import org.cirdles.ambapo.*;
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
        String uri = request.getRequestURI().toLowerCase();
        String[] pieces = uri.split("/");
        // first item will be "", second item will be "ambapo"
        if (pieces.length >= 4) {
            // UTM -> LatLng
            if (pieces[2].equals("utm") && pieces[3].equals("latlng")) {
                JSONObject json = RequestJSONUtils.extractRequestJSON(request);
                BigDecimal easting = BigDecimal.valueOf(json.getLong("easting"));
                BigDecimal northing = BigDecimal.valueOf(json.getLong("northing"));
                String hemStr = json.getString("hemisphere");
                int zoneNumber = json.getInt("zoneNumber");
                String zoneStr = json.getString("zoneLetter");
                String datum = json.getString("datum");
                if (easting != null && northing != null &&
                        hemStr != null && !hemStr.isEmpty() &&
                        zoneStr != null && !zoneStr.isEmpty() &&
                        datum != null && !datum.isEmpty()) {
                    char hemisphere = hemStr.charAt(0);
                    char zoneLetter = zoneStr.charAt(0);
                    try {
                        UTM utm = new UTM(easting, northing, hemisphere, zoneNumber, zoneLetter);
                        Coordinate coord = UTMToLatLong.convert(utm, datum);
                        JSONObject responseJson = new JSONObject();
                        responseJson.put("latitude", coord.getLatitude());
                        responseJson.put("longitude", coord.getLongitude());
                        responseJson.put("datum", coord.getDatum());
                        response.setContentType("application/json");
                        response.getWriter().println(responseJson);
                    } catch (Exception e) {
                        // TODO: what to send as response?
                        response.sendError(491);
                    }
                } else {
                    // TODO: what to send as response?
                    response.sendError(490);
                }
            }
        }
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
