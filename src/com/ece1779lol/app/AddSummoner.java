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

import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.*;


@SuppressWarnings("serial")
public class AddSummoner extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(AddSummoner.class.getName());
	
	private String globalFavorites = "globalFavorites";
	private String userFavoritePrefix = "favorites";
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		
		HelperFunctions help = (HelperFunctions)getServletContext().getAttribute("HelperFunctions");
		
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
		
		out.println("<p>Hello "+user.getNickname()+"</p>");
		
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        String summonerName = req.getParameter("summonerName");
        String region = req.getParameter("region");

        int retries = 3;
        boolean success = false;
        
        Transaction txn=null;
        
        while (!success && retries > 0) {
            --retries;
            try {
                txn = ds.beginTransaction();

                // Check for favorites for user and create if needed
                String userFavoritesID = userFavoritePrefix+user.getUserId(); //each user has own favorites list
                Key userFavoritesKey;
                Entity userFavoritesEntity;
                
                try {
                    userFavoritesKey = KeyFactory.createKey("Favorites", userFavoritesID);
                    userFavoritesEntity = ds.get(userFavoritesKey);

                } catch (EntityNotFoundException e) {
                    userFavoritesEntity = new Entity("Favorites", userFavoritesID);
                    userFavoritesEntity.setProperty("count", 0L);
                    userFavoritesKey = ds.put(userFavoritesEntity);
                }

                // Add to favorites if it is not in favorites
                Key userSummonerKey;
                Entity summonerEntity;
                try {
                	userSummonerKey = KeyFactory.createKey(userFavoritesKey, "summoner", summonerName+region);
                    summonerEntity = ds.get(userSummonerKey);
                } catch (EntityNotFoundException e) {
                    summonerEntity = new Entity("summoner", summonerName+region, userFavoritesKey);  // key is summonername+region
                    summonerEntity.setProperty("summoner_name", summonerName);
                    summonerEntity.setProperty("region", region);
                    ds.put(summonerEntity);
                    
                    //TODO : Add to global tracking since it doesnt exist
                }
                

                long count = (Long) userFavoritesEntity.getProperty("count");
                ++count;
                userFavoritesEntity.setProperty("count", count);
                ds.put(userFavoritesEntity);

                log.info("Posting summoner, updating count to " + count +
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
