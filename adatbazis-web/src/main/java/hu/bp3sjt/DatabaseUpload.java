package hu.bp3sjt;

import hu.bp3sjt.dao.DataBaseDaoImp;
import hu.bp3sjt.model.DataBase;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.*;
import java.util.Vector;

@WebServlet("/db-upload")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024, // 1 MB
        maxFileSize = 1024 * 1024 * 5, // 5 MB
        maxRequestSize = 1024 * 1024 * 5 * 5 // 25 MB
)
public class DatabaseUpload extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Vector<InputStream> allParts = new Vector<>();

        for (Part part : req.getParts()) {
            allParts.add(part.getInputStream());
        }

        SequenceInputStream appended = new SequenceInputStream(allParts.elements());
        byte[] bytes = appended.readAllBytes();

        File file = new File("database.db");

        try {
            OutputStream stream = new FileOutputStream(file);
            stream.write(bytes);
            stream.close();
        }catch (Exception e){

        }

        if (file != null){
            ServletContext servletContext = getServletContext();
            DataBase db = new DataBase();
            db.setUrl(file.getAbsolutePath());
            DataBaseDaoImp dao = new DataBaseDaoImp();

            servletContext.setAttribute("db", db);
            servletContext.setAttribute("dao", dao);

            resp.sendRedirect("pages/main.jsp");
        }else {

        }

    }
}
