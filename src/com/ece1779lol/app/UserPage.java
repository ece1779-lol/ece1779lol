package com.ece1779lol.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.*;


@SuppressWarnings("serial")
public class UserPage extends HttpServlet {
	
	// Hard code the message board name for simplicity.  Could support
    // multiple boards by getting this from the URL.
	private String favorite = "favorites";
	
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
		out.println("<form action='/querySummoner' method='post'>");
		out.println("  <p>Query a Summoner</p>");
		out.println("  Summoner Name <input type='text' name='summonerName'/>");
		out.println("  <select name='region'>");
		out.println("  <option value='na' selected>N.A.</option>");
		out.println("  <option value='euw'>Europe W.</option>");
		out.println("  <option value='eune'>Europe N. & E.</option>");
		out.println("  <option value='br'>Brazil</option>");
		out.println("  <option value='tr'>Turkey</option>");
		out.println("  <option value='ru'>Russia</option>");
		out.println("  <option value='lan'>L.A. North</option>");
		out.println("  <option value='las'>L.A. South</option>");
		out.println("  <option value='oce'>Oceania</option>");
		out.println("  </select>");
		out.println("  <input type='submit' value='Send'>");
		out.println("</form>");
		
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        String keyname = favorite+user.getUserId();
        
        // Display information about a message board and its messages.
        Key favoriteKey = KeyFactory.createKey("Favorites", keyname);
        try {
        	out.println("<h1>Tracking Summoners :</h1>");
            Entity favorites = ds.get(favoriteKey);
            long count = (Long) favorites.getProperty("count");
            out.println("<p>Favorites of " + keyname + " (" + count + " total):</p>");

            Query q = new Query("favorite", favoriteKey);
            PreparedQuery pq = ds.prepare(q);
            for (Entity result : pq.asIterable()) {
                out.println("<h3>" + (String) result.getProperty("summoner_name") + "</h3></p>");
            }
        } catch (EntityNotFoundException e) {
            out.println("<p>No Favorites Saved.</p>");
        }
		
		out.println("  </body>");
		out.println("</html>");
	}
}
