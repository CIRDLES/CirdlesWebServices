/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cirdles.webServices.squidink;
import java.nio.file.Files;
import java.sql.*;

import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Paths;
import java.nio.file.Path;
import javax.sql.RowSet;

import org.cirdles.webServices.requestUtils.*;
import org.cirdles.ambapo.*;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author ty
 */
public class SquidInkRegisterServlet extends HttpServlet {

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


            //If email does not exist in table, push all values to table
            PreparedStatement prep = connection.prepareStatement("select userID from Users where userID = ?");
            prep.setString(1, holder2[1]);
            ResultSet rs = prep.executeQuery();
            if( rs.next() == false ) {
                PreparedStatement input = connection.prepareStatement("insert into Users (userID, userName, password) values (?, ?, ?)");
                input.setString(1, holder2[3]);
                input.setString(2, holder2[1]);
                input.setString(3, holder2[5]);
                Path path = Paths.get("C:/Users/Richard McCarty/Downloads/dockerout/" + holder2[3].replace(" ", ""));
                Files.createDirectory(path);
                input.execute();
                connection.close();
                response.setContentType("application/json");
                response.getWriter().println("Done");
            }
            else {
                connection.close();
                response.setStatus(400);
                response.getWriter().println("Email Already Exists");
            }
            //json = JSONUtils.createResponseErrorJSON("Invalid URI");

        }
        catch(SQLException | ClassNotFoundException | IOException e) {
            System.err.println(e.getMessage());
            response.getWriter().println(e.getMessage());
        }
    }

    private void createTable(Connection conn) throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + "Users"
                + "  userID       VARCHAR(255),"
                + "  userName     VARCHAR(255),"
                + "  password     VARCHAR(255))";

        Statement stmt = conn.createStatement();
        stmt.execute(sqlCreate);
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
