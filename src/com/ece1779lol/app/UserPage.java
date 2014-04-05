package com.ece1779lol.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.*;


@SuppressWarnings("serial")
public class UserPage extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();

        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        
		out.println("<html>");
		out.println("  <head><title>Welcome to LOL tracker</title></head>");
		out.println("  <body>");
        
        String navBar;
        if (user != null) {
            navBar = "<p>Welcome, " + user.getNickname() + "! You can <a href=\"" +
                     userService.createLogoutURL("/") +
                     "\">sign out</a>.</p>";
        } else {
            navBar = "<p>Welcome! <a href=\"" + userService.createLoginURL("/userPage") +
                     "\">Sign in or register</a> to customize.</p>";
            out.println(navBar);
    		out.println("  </body>");
    		out.println("</html>");
            return;
        }
		
        
		out.println(navBar);
		out.println("</br>");
		out.println("    <form action='/addSummoner' method='post'>");
		out.println("	 <p>Query a Summoner</p>");
		out.println("	    Summoner Name <input type='text' name='summonerName'/><br />");
		out.println("   	<input type='submit' value='Send'>");
		out.println("    </form>");
		out.println("  </body>");
		out.println("</html>");
	}
}
