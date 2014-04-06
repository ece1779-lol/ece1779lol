package com.ece1779lol.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import ece1779.appengine.datastore.TransactionsServlet;
import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.*;


@SuppressWarnings("serial")
public class AddSummoner extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(AddSummoner.class.getName());
	
	private String favorite = "favorites";
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
		
		out.println("Hello "+user.getNickname());
		
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        String summonerName = req.getParameter("summonerName");
        Date postDate = new Date();

        int retries = 3;
        boolean success = false;
        
        Transaction txn=null;
        
        while (!success && retries > 0) {
            --retries;
            try {
                txn = ds.beginTransaction();

                String keyname = favorite+user.getUserId();
                Entity favorites;
                Key boardKey;
                
                try {
                    boardKey = KeyFactory.createKey("Favorites", keyname);
                    favorites = ds.get(boardKey);

                } catch (EntityNotFoundException e) {
                    favorites = new Entity("Favorites", keyname);
                    favorites.setProperty("count", 0L);
                    boardKey = ds.put(favorites);
                }

                Entity message = new Entity("favorite", boardKey);  // set parent child relationship
                message.setProperty("summoner_name", summonerName);
                message.setProperty("post_date", postDate);
                ds.put(message);

                long count = (Long) favorites.getProperty("count");
                ++count;
                favorites.setProperty("count", count);
                ds.put(favorites);

                log.info("Posting favorite, updating count to " + count +
                         "; " + retries + " retries remaining");

                txn.commit();

                // Break out of retry loop.
                success = true;

            } catch (DatastoreFailureException e) {
            	// Allow retry to occur.
            		
            }
        }

        if (!success) {
            resp.getWriter().println
                ("<p>A Could not add to Favorites.  Try again later." +
                 "<a href=\"/uerPage\">Return to home page.</a></p>");
            txn.rollback();
            
        } else {
            resp.sendRedirect("/userPage");
        }
	}
}
