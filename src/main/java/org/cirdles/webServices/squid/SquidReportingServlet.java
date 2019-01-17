/*
 * Copyright 2019 James F. Bowring and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.webServices.squid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.IOUtils;
import org.cirdles.squid.web.SquidReportingService;
import org.springframework.web.bind.ServletRequestUtils;
import org.xml.sax.SAXException;

/**
 *
 * @author ty
 */
@MultipartConfig
public class SquidReportingServlet extends HttpServlet {

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

        // this seems to hang needs work HttpSession session = request.getSession();
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=squid-reports.zip");

        boolean useSBM = ServletRequestUtils.getBooleanParameter(request, "useSBM", true);
        boolean useLinFits = ServletRequestUtils.getBooleanParameter(request, "userLinFits", false);
        String refMatFilter = ServletRequestUtils.getStringParameter(request, "refMatFilter", "");
        String concRefMatFilter = ServletRequestUtils.getStringParameter(request, "concRefMatFilter", "");
        String preferredIndexIsotopeName = ServletRequestUtils.getStringParameter(request, "prefIndexIso", "PB_204");
        Part filePart = request.getPart("prawnFile");
        Part filePart2 = request.getPart("taskFile");

        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        InputStream fileStream = filePart.getInputStream();
        InputStream fileStream2 = filePart2.getInputStream();

        SquidReportingService handler = new SquidReportingService();

        try {
            File report = null;
            report = handler.generateReports(
                    fileName, fileStream, fileStream2, useSBM, useLinFits, refMatFilter, concRefMatFilter,
                    preferredIndexIsotopeName).toFile();

//            // now if Linux. we are going to assume cirdles.cs.cofc.edu and write to Google Drive for now
//            // note: gdrive runs as root: sudo chmod +s gdrive
//            Thread thread = new Thread() {
//                public void run() {
//                    System.out.println("Thread Running");
//                    try {
//                        String[] arguments = new String[]{
//                            System.getenv("CATALINA_HOME") + "/gdrive", "mkdir", "JIMMY"};
//                        //"/home/gdrive", "upload", "--parent", "19RHlWggIw5fqWQUO1xs3M2iWjD82Ph3m", "/opt/tomcat9/temp/reports.zip"};
//                        List<String> argList = Arrays.asList(arguments);
//                         
//                        ProcessBuilder processBuilder = new ProcessBuilder(argList);
//                        Process process = processBuilder.start();
//
//                        int exitCode = process.waitFor();
//                        assert exitCode == 0;
//
//                    } catch (IOException | InterruptedException iOException) {
//                    }
//                }
//            };
//
//            thread.start();

            response.setContentLengthLong(report.length());
            IOUtils.copy(new FileInputStream(report), response.getOutputStream());

        } catch (IOException | JAXBException | SAXException e) {
            System.err.println(e);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Squid Reporting Servlet";
    }// </editor-fold>
}
