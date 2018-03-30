/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cirdles.webServices.ambapo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import org.json.HTTP;
import org.json.JSONException;
import org.springframework.web.bind.ServletRequestUtils;
import org.json.JSONObject;
import org.springframework.web.bind.ServletRequestBindingException;
import org.cirdles.webServices.requestUtils.*;
import org.cirdles.ambapo.*;

/**
 *
 * @author elaina cole
 */
@MultipartConfig
public class AmbapoServlet extends HttpServlet {
    private static final String LATLONG_TO_UTM = "latlongtoutm";
    private static final String LATLONG_TO_LATLONG = "latlongtolatlong";
    private static final String UTM_TO_LATLONG = "utmtolatlong";
    
    private InputStream inputStream;
    private BufferedReader bufferedReader;
    private StringBuilder stringBuilder;
    private String prefix = "outputfile";
    private String suffix = ".csv";
    
    private Datum fromDatum;
    private Datum toDatum;
    
    private BigDecimal fromLongitude;
    private BigDecimal fromLatitude;
    private BigDecimal toLongitude;
    private BigDecimal toLatitude;
    
    private char hemisphere;
    private char zoneLetter;
    private int zoneNumber;
    private BigDecimal easting;
    private BigDecimal northing;
    
    private UTM utm;
    private Coordinate latAndLong;
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
    }

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
            throws ServletException, IOException 
    {
        boolean isBulk = true;
        boolean isJSON = false;        
        
        String contentType = request.getHeader("content-type");
        
        if(contentType.equals("application/json")){
            isBulk = false;
            isJSON = true;
        }else{
            isBulk = ServletRequestUtils.getBooleanParameter(request, "isBulk", true);
        }
            
        
        if(isBulk) 
        {
            doPostBulk(request, response);
        }
        else if(!isJSON && !isBulk) 
        {
            try {
                doSoloPostTextFile(request, response);
            } catch (Exception ex) {
                Logger.getLogger(AmbapoServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            try {
                doPostJSON(request, response);
            } catch (Exception ex) {
                Logger.getLogger(AmbapoServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

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
    

    private void doPostBulk(HttpServletRequest request, HttpServletResponse response) {
        try {
            String typeOfConversion = ServletRequestUtils.getStringParameter(request, "typeOfConversion", LATLONG_TO_UTM);    
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "filename=ambapo.csv");

            OutputStream out = response.getOutputStream();
            Part filePart = request.getPart("ambapoFile");
                
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            File file = new File(fileName);
            InputStream inputStream = filePart.getInputStream();

            AmbapoFileHandlerService handler = new AmbapoFileHandlerService();
            Path path = handler.generateBulkOutput(fileName, inputStream);

            ConversionFileHandler fileHandler = new ConversionFileHandler(path.toFile().getCanonicalPath());
            File tempFile = File.createTempFile(prefix, suffix);

            switch (typeOfConversion)
            {
                case LATLONG_TO_UTM:
                    fileHandler.writeConversionsLatLongToUTM(tempFile);
                    break;
                case LATLONG_TO_LATLONG:
                    fileHandler.writeConversionsLatLongToLatLong(tempFile);
                        break;
                    case UTM_TO_LATLONG:
                        fileHandler.writeConversionsUTMToLatLong(tempFile);
                        break;   
                }

                response.setContentLengthLong(fileHandler.getOutputFile().length());
                IOUtils.copy(new FileInputStream(fileHandler.getOutputFile()), response.getOutputStream());

        } catch (Exception ex) {
            Logger.getLogger(AmbapoServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doSoloPostTextFile(HttpServletRequest request, HttpServletResponse response) 
            throws ServletRequestBindingException, Exception {
        
        String typeOfConversion = ServletRequestUtils.getStringParameter(request, "typeOfConversion", LATLONG_TO_UTM);    
        OutputStream out = response.getOutputStream();
        File outputFile = null;
        BufferedReader br = null;
        AmbapoFileHandlerService handler = new AmbapoFileHandlerService(); 
        
        switch (typeOfConversion){
            case LATLONG_TO_UTM:
                fromLatitude = new BigDecimal(ServletRequestUtils.getRequiredDoubleParameter(request, "latitude"));
                fromLongitude = new BigDecimal(ServletRequestUtils.getRequiredDoubleParameter(request, "longitude"));
                fromDatum = Datum.valueOf(ServletRequestUtils.getStringParameter(request, "fromDatum", "WGS84"));
                utm = LatLongToUTM.convert(fromLatitude, fromLongitude, fromDatum.getDatum());
                
                outputFile = handler.generateSoloOutputUTM(utm);
                br = new BufferedReader(new FileReader(outputFile));

                break;
            
            case LATLONG_TO_LATLONG:
                fromLatitude = new BigDecimal(ServletRequestUtils.getRequiredDoubleParameter(request, "latitude"));
                fromLongitude = new BigDecimal(ServletRequestUtils.getRequiredDoubleParameter(request, "longitude"));
                fromDatum = Datum.valueOf(ServletRequestUtils.getStringParameter(request, "fromDatum", "WGS84"));
                toDatum = Datum.valueOf(ServletRequestUtils.getStringParameter(request, "toDatum", "WGS84"));
                latAndLong = LatLongToLatLong.convert(fromLatitude, fromLongitude, fromDatum.toString(), toDatum.toString());

                outputFile = handler.generateSoloOutputLatLong(latAndLong);
                br = new BufferedReader(new FileReader(outputFile));

                break;
            case UTM_TO_LATLONG:
                toDatum = Datum.valueOf(ServletRequestUtils.getStringParameter(request, "toDatum", "WGS84"));
                easting = new BigDecimal(ServletRequestUtils.getRequiredDoubleParameter(request, "easting"));
                northing = new BigDecimal(ServletRequestUtils.getRequiredDoubleParameter(request, "northing"));
                zoneNumber = ServletRequestUtils.getRequiredIntParameter(request, "zoneNumber");
                zoneLetter = ServletRequestUtils.getStringParameter(request, "zoneLetter", "*").charAt(0);
                hemisphere = ServletRequestUtils.getStringParameter(request, "hemisphere", "*").charAt(0);
                utm = new UTM(easting, northing, hemisphere, zoneNumber, zoneLetter);
                latAndLong = UTMToLatLong.convert(utm, toDatum.getDatum());
                
                outputFile = handler.generateSoloOutputLatLong(latAndLong);
                br = new BufferedReader(new FileReader(outputFile));
                break;
        }
        
        response.setContentLengthLong(outputFile.length());
        IOUtils.copy(br, out);
    
    }

    private void doPostJSON(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, Exception {
        
        StringBuffer jb = new StringBuffer();
        String line = null;
        JSONObject jsonObject = null;
        try {
          BufferedReader reader = request.getReader();
          while ((line = reader.readLine()) != null)
            jb.append(line);
        } catch (Exception e) { /*report an error*/ }

        try {
          jsonObject =  new JSONObject(jb.toString());
        } catch (JSONException e) {
          // crash and burn
          throw new IOException("Error parsing JSON request string");
        }
        
        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "application/json name=ambapo");
        OutputStream out = response.getOutputStream();
                
        File outputFile = null;
        BufferedReader br = null;
        AmbapoFileHandlerService handler = new AmbapoFileHandlerService();                
        
        String typeOfConversion = jsonObject.getString("typeOfConversion");
        
        switch (typeOfConversion)
        {
            case LATLONG_TO_UTM:
                JSONObject jsonToReturn = new JSONObject();
                fromLatitude = new BigDecimal(jsonObject.getDouble("latitude"));
                fromLongitude = new BigDecimal(jsonObject.getDouble("longitude"));
                fromDatum = Datum.valueOf(jsonObject.getString("fromDatum"));
                utm = LatLongToUTM.convert(fromLatitude, fromLongitude, fromDatum.getDatum());
                
                jsonToReturn.append("fromLatitude", fromLatitude);
                jsonToReturn.append("fromLongitude", fromLongitude);
                jsonToReturn.append("fromDatum", fromDatum);
                
                break;
            
            case LATLONG_TO_LATLONG:
                fromLatitude = fromLatitude = new BigDecimal(jsonObject.getDouble("latitude"));
                fromLongitude = new BigDecimal(jsonObject.getDouble("longitude"));
                fromDatum = Datum.valueOf(jsonObject.getString("fromDatum"));
                toDatum = Datum.valueOf(jsonObject.getString("toDatum"));
                latAndLong = LatLongToLatLong.convert(fromLatitude, fromLongitude, fromDatum.toString(), toDatum.toString());
                
                outputFile = handler.generateSoloOutputLatLong(latAndLong);
                br = new BufferedReader(new FileReader(outputFile));
                
                break;
            
            case UTM_TO_LATLONG:
                toDatum = Datum.valueOf(jsonObject.getString("toDatum"));
                easting = new BigDecimal(jsonObject.getDouble("easting"));
                northing = new BigDecimal(jsonObject.getDouble("northing"));
                zoneNumber = jsonObject.getInt("zoneNumber");
                zoneLetter = jsonObject.getString("zoneLetter").charAt(0);
                hemisphere = jsonObject.getString("hemisphere").charAt(0);
                utm = new UTM(easting, northing, hemisphere, zoneNumber, zoneLetter);
                latAndLong = UTMToLatLong.convert(utm, toDatum.getDatum());
                
                outputFile = handler.generateSoloOutputLatLong(latAndLong);
                br = new BufferedReader(new FileReader(outputFile));
                
                break;
        }
        
        
    }
}
