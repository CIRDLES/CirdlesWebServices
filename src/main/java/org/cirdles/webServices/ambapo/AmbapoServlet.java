/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cirdles.webServices.ambapo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.cirdles.ambapo.ConversionFileHandler;
import org.cirdles.ambapo.Coordinate;
import org.cirdles.ambapo.Datum;
import org.cirdles.ambapo.LatLongToLatLong;
import org.cirdles.ambapo.LatLongToUTM;
import org.cirdles.ambapo.UTM;
import org.cirdles.ambapo.UTMToLatLong;
import org.cirdles.webServices.calamari.PrawnFileHandlerService;
import org.springframework.web.bind.ServletRequestUtils;

/**
 *
 * @author ty
 */
public class AmbapoServlet extends HttpServlet {
    private static final String LATLONG_TO_UTM = "latlongtoutm";
    private static final String LATLONG_TO_LATLONG = "latlongtolatlong";
    private static final String UTM_TO_LATLONG = "utmtolatlong";

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
            throws ServletException, IOException {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=ambapo-conversion.zip");
        
        String typeOfConversion = (ServletRequestUtils.getStringParameter(request, "typeOfConversion", "utmtolatlong")).toLowerCase();
        //boolean isBulk = ServletRequestUtils.getBooleanParameter(request, "isBulk", true);

        //if(isBulk){
            File fileToConvert = null;
            String convertedFileStr = ServletRequestUtils.getStringParameter(request, "convertedFile", "result.csv");
            Part filePart = request.getPart("ambapoFile");
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            InputStream fileStream = filePart.getInputStream();
            File myFile = new File(fileName);
        
            ConversionFileHandler handler = new ConversionFileHandler(fileName);
            String fileExt = FilenameUtils.getExtension(fileName);
            
            try {
                File convertedFile = null;
                
                switch(typeOfConversion){
                    case UTM_TO_LATLONG:
                        handler.writeConversionsUTMToLatLong(convertedFileStr);
                        break;
                    case LATLONG_TO_LATLONG:
                        handler.writeConversionsLatLongToLatLong(convertedFileStr);
                        break;
                    case LATLONG_TO_UTM:
                        handler.writeConversionsLatLongToUTM(convertedFileStr);
                        break;  
                }
                        
                response.setContentLengthLong(convertedFile.length());
                IOUtils.copy(new FileInputStream(convertedFile), response.getOutputStream());
            
            } catch (Exception e) {
                if(fileExt.equals("csv"))
                    System.out.println("Must upload a csv file with .csv extension.");
                else
                    System.out.println(e);
            }
            
        /*}else{
            try {
                BigDecimal fromLatitude = null;
                BigDecimal fromLongitude = null;
                
                Datum toDatum = null;
                Datum fromDatum = null;
                
                UTM utm = null;
                BigDecimal easting;
                BigDecimal northing;
                int zoneNumber;
                char zoneLetter;
                char hemisphere;
                
                Coordinate latAndLong = null;
                
                if(typeOfConversion.equalsIgnoreCase(LATLONG_TO_UTM) ||
                        typeOfConversion.equalsIgnoreCase(LATLONG_TO_LATLONG)){
                    fromLatitude = new BigDecimal(ServletRequestUtils.getDoubleParameter(request, "fromLatitude", 0));
                    fromLongitude = new BigDecimal(ServletRequestUtils.getDoubleParameter(request, "fromLongitude", 0));
                    fromDatum = Datum.valueOf(ServletRequestUtils.getStringParameter(request, "fromDatum", "WGS84"));
                    
                    if(typeOfConversion.equalsIgnoreCase(LATLONG_TO_LATLONG))
                        toDatum = Datum.valueOf(ServletRequestUtils.getStringParameter(request, "toDatum", "NAD83"));
                }else{
                    easting = new BigDecimal(ServletRequestUtils.getDoubleParameter(request, "easting", 0));
                    northing = new BigDecimal(ServletRequestUtils.getDoubleParameter(request, "northing", 0));
                    zoneNumber = ServletRequestUtils.getIntParameter(request, "zoneNumber", 1);
                    zoneLetter = ServletRequestUtils.getStringParameter(request, "zoneLetter").charAt(0);
                    hemisphere = ServletRequestUtils.getStringParameter(request, "hemisphere").charAt(0);
                    toDatum = Datum.valueOf(ServletRequestUtils.getStringParameter(request, "toDatum", "WGS84"));
                    
                    try {
                        utm = new UTM(easting, northing, hemisphere, zoneNumber, zoneLetter);
                        
                    } catch (Exception ex) {
                        Logger.getLogger(AmbapoServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                switch(typeOfConversion){
                    case UTM_TO_LATLONG:
                        latAndLong = UTMToLatLong.convert(utm, toDatum.getDatum());
                        response.
                        break;
                    case LATLONG_TO_LATLONG:
                        latAndLong = LatLongToLatLong.convert(fromLatitude, fromLongitude, fromDatum.getDatum(), toDatum.getDatum());
                        break;
                    case LATLONG_TO_UTM:
                        utm = LatLongToUTM.convert(fromLatitude, fromLongitude, fromDatum.getDatum());
                        break; 
                }
            } catch (Exception ex) {
                Logger.getLogger(AmbapoServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
        
        
        
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
