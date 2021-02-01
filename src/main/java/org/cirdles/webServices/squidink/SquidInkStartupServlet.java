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
import java.util.Stack;

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
       //@TODO convert source string to generic with passed formdata for final user path
        try {
        String body = IOUtils.toString(request.getReader()).replace("\"","");
        System.out.println(body);
        Stack<Integer> portStack = (Stack<Integer>) this.getServletConfig().getServletContext().getAttribute("portStack");
        int portNum = portStack.pop();
        Process process = Runtime.getRuntime()
                .exec("docker run --mount type=bind,source=\"//c/Users/Richard McCarty/Downloads/dockerout/" + body + "\",target=\"/usr/local/user_files\" " +
                        "-p " + portNum + ":8080 squidboys");
        this.getServletConfig().getServletContext().setAttribute("portStack", portStack);
        response.getWriter().println(portNum);
        }
        catch (IOException | NullPointerException | SecurityException | IllegalArgumentException e) {
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
