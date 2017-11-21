/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cirdles.webServices.ambapo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import org.springframework.web.bind.ServletRequestUtils;

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
        try {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "filename=ambapo.csv");
            OutputStream out = response.getOutputStream();
            
            Part filePart = request.getPart("ambapoFile");
            String typeOfConversion = ServletRequestUtils.getStringParameter(request, "typeOfConversion", LATLONG_TO_UTM);
            
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            File file = new File(fileName);
            InputStream inputStream = filePart.getInputStream();

            AmbapoFileHandlerService handler = new AmbapoFileHandlerService();
            Path path = handler.generateReports(fileName, inputStream);
            
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
