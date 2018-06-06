
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.sql.*;
import javax.sql.*;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;7
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/*
 * @author Nitigya
 */

public class reqDispatch extends HttpServlet {
	static DataSource ds;                           // dataSource for connection pooling 
	static Queue query;                             // request saving queue
	static final byte BATCH_SIZE = 10;              // size of elements to be queued for one batch
	static boolean processFlag = false;             //flag to check entry in executeBatch() by multiple threads
	static boolean threadFlag = false;              //flag to check timer thread don't run while executeBatch() in use by worker thread



	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		Queue<HttpServletRequest> processable = new LinkedList<>();

		query.add(request);

		if (query.size() > BATCH_SIZE && processFlag == false){
			synchronized (query) {
				processFlag = true;
				processable.addAll(query);
				if(executeBatch(processable)){  
					query.removeAll(processable);
				}
			}
			processFlag = false;
		}



		try (PrintWriter out = response.getWriter()) {

			out.print(query);
			out.println("<!DOCTYPE html>");
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Servlet reqDispatch</title>");            
			out.println("</head>");
			out.println("<body>");
			out.println("<h1>Servlet reqDispatch at " + request.getContextPath() + "</h1>");
			out.println("</body>");
			out.println("</html>");
		}
	}

	public synchronized boolean  executeBatch(Queue<HttpServletRequest> queue){

		threadFlag = true;
		final String DRIVER="com.mysql.jdbc.Driver",
				URL = "jdbc:mysql://localhost:3306/clarotest",
				USER = "root",
				PASS = "root",
				QUERY = "insert into testable values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		boolean status = false;
		HttpServletRequest query;
		Iterator<HttpServletRequest> itr = queue.iterator();
		String[] param = {"param1","param2","param3","param4","param5","param6","param7","param8","param9","param10","param11","param12","param13","param14","param15","param16","param17"};      //replace with parameter name here

		try{
			Connection conn = ds.getConnection(); // data source being used
			conn.setAutoCommit(false);
			PreparedStatement ps = conn.prepareStatement(QUERY); 

			while(itr.hasNext()){
				query = itr.next();
				// insert placeholder parameters here 
				ps.setString(1, query.getParameter(param[0]));
				ps.setString(2, query.getParameter(param[1]));
				ps.setString(3, query.getParameter(param[2]));
				ps.setString(4, query.getParameter(param[3]));

				ps.setLong(5, Long.parseLong(query.getParameter(param[4])));
				ps.setLong(6, Long.parseLong(query.getParameter(param[5])));
				ps.setLong(7, Long.parseLong(query.getParameter(param[6])));
				ps.setLong(8, Long.parseLong(query.getParameter(param[7])));

				ps.setFloat(9, Float.parseFloat(query.getParameter(param[8])));
				ps.setFloat(10, Float.parseFloat(query.getParameter(param[9])));
				ps.setFloat(11, Float.parseFloat(query.getParameter(param[10])));

				ps.setDouble(12, Double.parseDouble(query.getParameter(param[11])));
				ps.setDouble(13, Double.parseDouble(query.getParameter(param[12])));

				ps.setFloat(14, Float.parseFloat(query.getParameter(param[13])));

				ps.setString(15, query.getParameter(param[14]));
				ps.setString(16, query.getParameter(param[15]));

				ps.setInt(17, Integer.parseInt(query.getParameter(param[16])));


				ps.executeUpdate();
			} 

			conn.commit();
			ps.close();
			conn.close();
			status = true;
		}catch(Exception eberror){
			status = false;
		}
		threadFlag = false;
		return status;
	}

	@Override
	public void init() throws ServletException {
		super.init(); //To change body of generated methods, choose Tools | Templates.


		try {
			ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/clarotest");
		} catch (NamingException ex) {
			Logger.getLogger(reqDispatch.class.getName()).log(Level.SEVERE, null, ex);
		}



		query = new LinkedList<>();

		TimerTask timedClean = new TimerTask() {
			Queue<HttpServletRequest> processable = new LinkedList<>();
			@Override
			public void run() {
				synchronized (query) {
					processFlag = true;
					processable.addAll(query);
					if(executeBatch(processable)){                 
						query.removeAll(processable);
						processFlag = false;
					}
				}
			}
		};

		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(timedClean,2*60*60*1000, 2*60*60*1000); 
		System.out.println("timer running");

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
	}

}
