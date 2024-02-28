package org.cirdles.webServices.squidink;

import org.apache.commons.io.IOUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.Runtime;
import java.lang.Process;
import java.io.IOException;
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
            generatePortStack();
        String body = IOUtils.toString(request.getReader()).replace("\"","");
        System.out.println(body);
        Stack<Integer> portStack = (Stack<Integer>) this.getServletConfig().getServletContext().getAttribute("portStack");
        int portNum = portStack.pop();
        Process process = Runtime.getRuntime()
                .exec("docker run --mount type=bind,source=\"//c/Users/Richard McCarty/Downloads/aaaFileBrowser/filebrowser/users/" + body + "\",target=\"/usr/local/user_files\" " +
                        "-p " + portNum + ":8080 squidboys");
        this.getServletConfig().getServletContext().setAttribute("portStack", portStack);
        response.getWriter().println(portNum);
        }
        catch (IOException | NullPointerException | SecurityException | IllegalArgumentException e) {
            System.out.println(e);
            response.getWriter().println(e);
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
