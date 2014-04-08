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
import com.google.appengine.api.datastore.Query.FilterPredicate;
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
	private String globalFavorites = "globalFavorites";
	private String userFavoritePrefix = "favorites";
	
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
		
        HelperFunctions help = (HelperFunctions)getServletContext().getAttribute("HelperFunctions");
        
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

        String userFavoritesKeyName = userFavoritePrefix+user.getUserId();
        
        // Display information about a message board and its messages.
        Key userFavoritesKey = KeyFactory.createKey("Favorites", userFavoritesKeyName);
        out.println("<h1>Tracking Summoners :</h1>");

		Query q = new Query("summoner_ref", userFavoritesKey);
		PreparedQuery pq = ds.prepare(q);
		out.println("<p>Favorites of " + userFavoritesKeyName + " (" + pq.countEntities() + " total):</p>");
		for (Entity favorite_keys : pq.asIterable()) {
			String summoner_key = (String)favorite_keys.getProperty("summoner_key");
			out.println("<h3>"+summoner_key+ "</h3></p>");
			
			Query q2 = new Query("summoner", KeyFactory.stringToKey(summoner_key));
		    PreparedQuery pq2 = ds.prepare(q2);
		    if (pq2.countEntities() != 1)
		    	out.println("<h1>WE GOT BIG ISSUE</h1>");
		    for (Entity summoner : pq2.asIterable()) {
		    	out.println("<p>" + (String)summoner.getProperty("summoner_name") +" "+
	    					help.getStringFromRegion((String)summoner.getProperty("region")) + "</p>");		    	
		    }
		    
		    //TODO: print games here ...

		}
		
		out.println("  </body>");
		out.println("</html>");
	}
}
