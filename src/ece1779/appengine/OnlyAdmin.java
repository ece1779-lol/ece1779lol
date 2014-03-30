package ece1779.appengine;

import java.io.IOException;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class OnlyAdmin extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("If you can read this, it means you are the admin!");
	}
}
