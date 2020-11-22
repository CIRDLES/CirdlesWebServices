/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cirdles.webServices.squidink;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import org.cirdles.squid.utilities.FileUtilities;
import org.cirdles.squid.utilities.fileUtilities.ZipUtility;
import org.zeroturnaround.zip.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;


/**
 *
 * @author
 */
public class FileSender extends HttpServlet {
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
        //On Post Request that carries userID, Take user id, find user ID folder, zip, send

        String user = request.getPathInfo();
        ZipUtil.pack(new File("C:/Users/Richard McCarty/Downloads/CirdlesWeb/", user.replace("/","")),
                new File("C:/Users/Richard McCarty/Downloads/CirdlesWeb/", user.replace("/","") + ".zip"));
        System.out.println(user);
        response.getWriter().println(user);
        }
        catch (InvalidPathException | IOException e) {
            System.out.println(e);
            response.getWriter().println(e);
        }
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
