/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.sql.*;
import javax.sql.*;
import javax.enterprise.context.spi.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 * @author nitigya
 */
public class handleReq extends HttpServlet {
    
    static Queue query;
    static DataSource ds;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet handleReq</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet handleReq at " + request.getContextPath() +" "+request.getParameter("name")+ "</h1>");
            out.println("</body>");
            out.println("</html>");
            query.add(request.getParameter("name"));
            if(query.size() > 2){
            out.println(query.size());
            out.println(query.peek());
            out.println("queued data sent to server");
            query.removeAll(query);
            }
                    String jdbcManager = "com.mysql.jdbc.Driver",
                    url="jdbc:mysql://localhost:3306/clarotest",
                    user="root",
                    pass="root";
            
            try{
                Connection conn = ds.getConnection();
                Statement st = conn.createStatement();
                ResultSet rst = st.executeQuery("select * from init");
                out.println(rst.next());
                conn.close();
            }catch( Exception e){
                out.println(e);
            }
        }
    }
    
    @Override
    public void init()
            throws ServletException {
        super.init(); //To change body of generated methods, choose Tools | Templates.
        query = new LinkedList<>();
        try{
               ds = (DataSource) new InitialContext().lookup("jdbc/mydb");
            
        }catch(Exception e){
            
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
