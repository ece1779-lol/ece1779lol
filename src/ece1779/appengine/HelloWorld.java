package ece1779.appengine;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class HelloWorld extends HttpServlet {
	
	private static final Logger log=Logger.getLogger(HelloWorld.class.getName());
	
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, world version 3");
		
		log.warning("Hello World 3!");
		
	}
}
