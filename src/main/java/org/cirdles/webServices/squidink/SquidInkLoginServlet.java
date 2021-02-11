/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cirdles.webServices.squidink;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Stack;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.apache.commons.io.IOUtils;
import org.cirdles.webServices.requestUtils.*;
import org.cirdles.ambapo.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author ty
 */
public class SquidInkLoginServlet extends HttpServlet {

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
            generatePortStack();
            //Initialize DB Connection and Statement loading
            Class.forName("org.sqlite.JDBC");
            //@TODO CHANGE THE DB LOCATION
            Connection connection = DriverManager.getConnection("jdbc:sqlite:C:/Users/Richard McCarty/Downloads/CirdlesWeb/CirdlesWebServices-master/users.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            //Separate response data
            String body = IOUtils.toString(request.getReader());
            body = body.replace("\"","");
            String[] array = body.split(":");
            String holder = Arrays.toString(array);
            String[] holder2 = holder.split(",");
            PreparedStatement prep = connection.prepareStatement("select * from Users where userID = ?");
            prep.setString(1, holder2[1]);
            ResultSet rs = prep.executeQuery();
            if( !rs.next() ) {
                connection.close();
                response.getWriter().println("Email Doesn't Exist");
            }
            else {
                holder2[1] = holder2[1].replace(" ","");
                holder2[3] = holder2[3].replace("}","").replace("]","").replace(" ", "");
                //Check Login presence in DB
                //Calendar for date-referenced JWT
                Calendar calNow = Calendar.getInstance();
                Calendar calFuture = Calendar.getInstance();
                Date curDate = new Date();
                calNow.setTime(curDate);
                calFuture.add(Calendar.YEAR, 1);
                if(rs.getString("password").replace(" ", "").equals(holder2[3])) {

                    try {
                        Algorithm algorithm = Algorithm.HMAC256("$B&E)H+MbQeThWmZq4t7w!z%C*F-JaNc");
                        String token = JWT.create()
                                .withClaim("id", holder2[1])
                                .withIssuedAt(calNow.getTime())
                                .withExpiresAt(calFuture.getTime())
                                .withIssuer("auth0")
                                .sign(algorithm);
                        connection.close();
                        response.setContentType("application/json");
                        response.getWriter().println(token);
                    }
                    catch (JWTCreationException exception){
                        System.out.println(exception);
                        response.getWriter().println(exception.getMessage());
                    }
                }

                else{
                    connection.close();
                    response.getWriter().println("Password is Incorrect");
                }
            }
            //json = JSONUtils.createResponseErrorJSON("Invalid URI");

        }
        catch(SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            response.getWriter().println(e.getMessage());
        }
    }
    private void generatePortStack() {
        if(this.getServletConfig().getServletContext().getAttribute("portStack") == null) {
            Stack<Integer> portStack = new Stack<>();
            for(int i = 8081; i < 8086; i++) {
                portStack.push(i);
            }
            this.getServletConfig().getServletContext().setAttribute("portStack", portStack);
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
