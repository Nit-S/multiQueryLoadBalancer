// the servlet that will receive parameters and handles data base functionality


import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.sql.*;
import javax.sql.*;

import clarotest.DataObj;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
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
	static final byte BUFFER_SIZE = 10;             // size of elements to be queued for one batch
	static boolean processFlag = false;             //flag to check entry in executeBatch() by multiple threads
	static boolean threadFlag = false;              //flag to check timer thread don't run while executeBatch() in use by worker thread



	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		Queue<DataObj> processable = new LinkedList<>();          // queue to store batch to be processed 
		
		// change name of parameters here as per required
		String[] param = {"param1","param2","param3","param4","param5","param6","param7","param8","param9","param10","param11","param12","param13","param14","param15","param16","param17"};      //replace with parameter name here

		// creation of object to store requested query 
		DataObj dataObj = new DataObj(request.getParameter(param[0]),
				request.getParameter(param[0]),
				request.getParameter(param[0]),
				request.getParameter(param[0]),
				Long.parseLong(request.getParameter(param[4])),
				Long.parseLong(request.getParameter(param[4])),
				Long.parseLong(request.getParameter(param[4])),
				Long.parseLong(request.getParameter(param[4])),
				Float.parseFloat(request.getParameter(param[8])),
				Float.parseFloat(request.getParameter(param[8])),
				Float.parseFloat(request.getParameter(param[8])),
				Double.parseDouble(request.getParameter(param[11])),
				Double.parseDouble(request.getParameter(param[11])),
				Float.parseFloat(request.getParameter(param[8])),
				request.getParameter(param[0]),
				request.getParameter(param[0]),
				Integer.parseInt(request.getParameter(param[16]))
				
				);

		query.add(dataObj);

		if (query.size() > BUFFER_SIZE && processFlag == false){
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

	public synchronized boolean  executeBatch(Queue<DataObj> queue){  // function which parses the queue and sends to data base

		threadFlag = true;
		final String  QUERY = "insert into testable values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		boolean status = false;
		DataObj query;
		Iterator<DataObj> itr = queue.iterator();
		try{
			Connection conn = ds.getConnection(); // data source being used
			conn.setAutoCommit(false);
			PreparedStatement ps = conn.prepareStatement(QUERY); 

			while(itr.hasNext()){
				query = itr.next();
				// insert placeholder parameters here 
				ps.setString(1, query.getVar1());
				ps.setString(2, query.getVar2());
				ps.setString(3, query.getVar3());
				ps.setString(4, query.getVar4());

				ps.setLong(5, query.getVar5());
				ps.setLong(6, query.getVar6());
				ps.setLong(7, query.getVar7());
				ps.setLong(8, query.getVar8());

				ps.setFloat(9, query.getVar9());
				ps.setFloat(10,query.getVar10());
				ps.setFloat(11,query.getVar11());

				ps.setDouble(12, query.getVar12());
				ps.setDouble(13, query.getVar13());

				ps.setFloat(14, query.getVar14());

				ps.setString(15, query.getVar15());
				ps.setString(16, query.getVar16());

				ps.setInt(17, query.getVar17());


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

		// data source datasource fetching
		try {
			ds = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/clarotest");
		} catch (NamingException ex) {
			Logger.getLogger(reqDispatch.class.getName()).log(Level.SEVERE, null, ex);
		}

		
		query = new LinkedList<>();

		
		
		// timer task
		TimerTask timedClean = new TimerTask() {
			Queue<DataObj> processable = new LinkedList<>();
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
