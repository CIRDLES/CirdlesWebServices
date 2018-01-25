/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cirdles.webServices.ambapo;

import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import org.cirdles.ambapo.ConversionFileHandler;
import org.cirdles.ambapo.Coordinate;
import org.cirdles.ambapo.Datum;
import org.cirdles.ambapo.LatLongToLatLong;
import org.cirdles.ambapo.LatLongToUTM;
import org.cirdles.ambapo.UTM;
import org.cirdles.ambapo.UTMToLatLong;
import org.springframework.web.bind.ServletRequestUtils;
import org.json.JSONObject;


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
        processRequest(request, response);
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
        boolean isBulk = ServletRequestUtils.getBooleanParameter(request, "isBulk", true);
        String typeOfConversion = ServletRequestUtils.getStringParameter(request, "typeOfConversion", LATLONG_TO_UTM);
        
        if(isBulk) {
        
            try {
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
        } else {
            
            try {
                response.setContentType("text/plain");
                response.setHeader("Content-Disposition", "filename=ambapo.txt");
                OutputStream out = response.getOutputStream();
                
                File outputFile = null;
                BufferedReader br = null;
                
                AmbapoFileHandlerService handler = new AmbapoFileHandlerService();
                
                
                String jsonStr = ServletRequestUtils.getStringParameter(request, "json", "");
                if(!jsonStr.isEmpty()){
                    JSONObject json = new JSONObject(jsonStr);
                    
                    typeOfConversion = json.getString("typeOfConversion");
                    
                    
                    switch (typeOfConversion)
                    {
                        case LATLONG_TO_UTM:
                            fromLatitude = new BigDecimal(json.getDouble("latitude"));
                            fromLongitude = new BigDecimal(json.getDouble("longitude"));
                            fromDatum = Datum.valueOf(json.getString("fromDatum"));

                            utm = LatLongToUTM.convert(fromLatitude, fromLongitude, fromDatum.getDatum());

                            outputFile = handler.generateSoloOutputUTM(utm);
                            br = new BufferedReader(new FileReader(outputFile));

                            break;
                        case LATLONG_TO_LATLONG:
                            fromLatitude = fromLatitude = new BigDecimal(json.getDouble("latitude"));
                            fromLongitude = new BigDecimal(json.getDouble("longitude"));
                            fromDatum = Datum.valueOf(json.getString("fromDatum"));
                            toDatum = Datum.valueOf(json.getString("toDatum"));

                            latAndLong = LatLongToLatLong.convert(fromLatitude, fromLongitude, fromDatum.toString(), toDatum.toString());

                            outputFile = handler.generateSoloOutputLatLong(latAndLong);
                            br = new BufferedReader(new FileReader(outputFile));

                            break;
                        case UTM_TO_LATLONG:
                            toDatum = Datum.valueOf(json.getString("toDatum"));
                            easting = new BigDecimal(json.getDouble("easting"));
                            northing = new BigDecimal(json.getDouble("northing"));
                            zoneNumber = json.getInt("zoneNumber");

                            zoneLetter = json.getString("zoneLetter").charAt(0);
                            hemisphere = json.getString("hemisphere").charAt(0);

                            utm = new UTM(easting, northing, hemisphere, zoneNumber, zoneLetter);

                            latAndLong = UTMToLatLong.convert(utm, toDatum.getDatum());

                            outputFile = handler.generateSoloOutputLatLong(latAndLong);
                            br = new BufferedReader(new FileReader(outputFile));

                            break;
                    }
                    
                }else{
                    
                    switch (typeOfConversion)
                    {


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
                    
                }
                
                
                
                response.setContentLengthLong(outputFile.length());
                IOUtils.copy(br, out);
                
            } catch (Exception ex) {
                Logger.getLogger(AmbapoServlet.class.getName()).log(Level.SEVERE, null, ex);
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
        return "Short description";
    }// </editor-fold>

}
