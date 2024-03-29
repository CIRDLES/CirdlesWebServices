package org.cirdles.webServices.topsoil;

import org.cirdles.topsoil.data.DataTable;
import org.cirdles.topsoil.data.DataTemplate;
import org.cirdles.topsoil.file.Delimiter;
import org.cirdles.topsoil.file.TableFileExtension;
import org.cirdles.topsoil.file.TopsoilFileUtils;
import org.cirdles.topsoil.file.parser.DataParser;
import org.json.JSONObject;
import org.springframework.web.bind.ServletRequestUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;

@MultipartConfig
public class TopsoilServlet extends HttpServlet {

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
        response.setContentType("text/plain");
        response.getWriter().println("Topsoil Data Conversion Servlet");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Check that required values are not null
        Part filePart = request.getPart("tableFile");
        if (filePart == null) {
            response.sendError(400, "No table file provided.");
            return;
        }
        String templateName = request.getParameter( "template");
        if (templateName == null) {
            response.sendError(400, "No data template provided.");
            return;
        }

        // Determine DataTemplate
        DataTemplate template;
        try {
            template = DataTemplate.valueOf(templateName);
        } catch (IllegalArgumentException e) {
            response.sendError(422, "Invalid data template.");
            return;
        }

        // Convert data into a string
        Path filePath = Paths.get(filePart.getSubmittedFileName());
        String fileName = filePath.getFileName().toString();
        String dataString = IOUtils.toByteArray(filePart.getInputStream()).toString();

        // Determine the data delimiter based on the file extension, or by guessing from the text
        TableFileExtension extension = TableFileExtension.getExtensionFromPath(filePath);
        Delimiter delimiter;
        switch (extension) {
            case CSV:
                delimiter = Delimiter.COMMA;
                break;
            case TSV:
                delimiter = Delimiter.TAB;
                break;
            default:
                delimiter = TopsoilFileUtils.guessDelimiter(dataString);
                if (delimiter == null) {
                    response.sendError(422, "Unable to determine data delimiter from text.");
                    return;
                }
                break;
        }

        // Parse data string into a DataTable
        DataParser parser = template.getParser();
        DataTable table = parser.parseDataTable(dataString, delimiter.asString(), fileName);
        if (table == null) {
            response.sendError(422, "Unable to parse data.");
            return;
        }

        // Return data table as JSON
        response.getWriter().println(new JSONObject(table.toJSONString()));
    }

}
