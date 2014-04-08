package com.ece1779lol.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.*;

import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.*;


@SuppressWarnings("serial")
public class AddSummoner extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(AddSummoner.class.getName());
	
	private String globalGames = "globalGames";
	private String globalFavorites = "globalFavorites";
	private String userFavoritePrefix = "favorites";
	
	//Retrieves key of summoner from global favorites or adds it
	private Key addToGlobalFavorites(DatastoreService ds, String summonerName, Region region)
	{
        Key globalFavoritesKey;
        Entity globalFavoritesEntity;
        
        try {
        	globalFavoritesKey = KeyFactory.createKey("Favorites", globalFavorites);
        	globalFavoritesEntity = ds.get(globalFavoritesKey);

        } catch (EntityNotFoundException e) {
        	globalFavoritesEntity = new Entity("Favorites", globalFavorites);
        	globalFavoritesEntity.setProperty("count", 0L);
        	globalFavoritesKey = ds.put(globalFavoritesEntity);
        }
        
        String favoritesEntryName = summonerName+region.getValue();
        Key favoritesEntryKey;
        Entity favoritesEntryEntity;
        
        try {
        	long refcount;
        	favoritesEntryKey = KeyFactory.createKey(globalFavoritesKey, "summoner", favoritesEntryName);
            favoritesEntryEntity = ds.get(favoritesEntryKey);
            refcount = (Long) favoritesEntryEntity.getProperty("refcount");
            ++refcount;
            favoritesEntryEntity.setProperty("refcount", refcount);
            ds.put(favoritesEntryEntity);

        } catch (EntityNotFoundException e) {
            favoritesEntryEntity = new Entity("summoner", favoritesEntryName, globalFavoritesKey);
            favoritesEntryEntity.setProperty("summoner_name", summonerName);
            favoritesEntryEntity.setProperty("region", region.getValue());
            favoritesEntryEntity.setProperty("refcount", 1L);
            favoritesEntryKey = ds.put(favoritesEntryEntity);
        }
        
        return favoritesEntryKey;
	}
	
	private void RemoveGlobalFavorites(DatastoreService ds, String summonerName, Region region)
	{
	}
	
	private void addMatchHistory(DatastoreService ds, String globalSummonerKey, String summonerName, Region region)
	{
		RiotApi client = (RiotApi)getServletContext().getAttribute("RiotClient");
		HelperFunctions help = (HelperFunctions)getServletContext().getAttribute("HelperFunctions");
		
		Summoner summoner;
	
		try {
			summoner = client.getSummoner(region, summonerName);

			try {
				List<Game> myMatchHistory = summoner.getMatchHistory();
				for (Game game : myMatchHistory)
				{
					Key GamesKey;
					Entity GamesEntity;
					try {
	                    GamesKey = KeyFactory.createKey("Games", globalGames);
	                    GamesEntity = ds.get(GamesKey);

	                } catch (EntityNotFoundException e) {
	                	GamesEntity = new Entity("Games", globalGames);
	                	GamesKey = ds.put(GamesEntity);
	                }
					
					String gameIdName = globalSummonerKey+game.getGameId();
					Key gameIdKey;
					Entity gameIdEntity;
					try {
						gameIdKey = KeyFactory.createKey(GamesKey, "lol_game", gameIdName);
						gameIdEntity = ds.get(gameIdKey);
						
						//game exists in DB, hence no longer need to store more of the history
						break;
					} catch (EntityNotFoundException e) {
						gameIdEntity = new Entity("lol_game", gameIdName, GamesKey);
						gameIdEntity.setProperty("summoner_key", globalSummonerKey);
						gameIdEntity.setProperty("gameId", game.getGameId());
						gameIdEntity.setProperty("isWin", game.isWin());
						//TODO: add more game stats here...
						gameIdKey = ds.put(gameIdEntity);
					}
					//out.println(game.getGameId()+" "+game.isWin()+" "+game.getEnemyMinionsKilled()+" "+game.getLength()+" "+game.getTotalPlayerScore()+" "+game.getGoldLeft());
					
				}
			} catch (RiotApiException e) {
				//out.println("No GAMES");
			}
			
		} catch (RiotApiException e) {
			//out.println(summonerName+" is invalid summoner ID");
			//out.println("<a href=\"/uerPage\">Return to home page.</a></p>");
		}
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.sendRedirect("/");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
		
		HelperFunctions.printLolHeader(out, "Query LOL Summoner");
		out.println("<body>");
		HelperFunctions.printLolLogo(out);
		HelperFunctions.printLolMenu(out, userService, user);

		/* goto sign in menu if not signed in*/
		if (user == null) {
			HelperFunctions.printLoginPage(out, userService);
			return;
		}
		
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        String summonerName = req.getParameter("summonerName");
        Region region = HelperFunctions.getRegionFromString(req.getParameter("region"));

        int retries = 3;
        boolean success = false;
        
        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn=null;
        
        while (!success && retries > 0) {
            --retries;
            try {
                txn = ds.beginTransaction(options);

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
                Key globalSummonerKey = addToGlobalFavorites(ds, summonerName, region);
                String globalSummonerKeyStr = KeyFactory.keyToString(globalSummonerKey);
                String userSummoneName = user.getUserId()+globalSummonerKeyStr;
                Key userSummonerKey;
                Entity userSummonerEntity;

            	userSummonerEntity = new Entity("summoner_ref", userSummoneName, userFavoritesKey);  // key is summonername+region
                userSummonerEntity.setProperty("summoner_key", globalSummonerKeyStr);
                userSummonerKey = ds.put(userSummonerEntity);
                   
                addMatchHistory(ds, globalSummonerKeyStr, summonerName, region);
               
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
            resp.sendRedirect("/");
        }
	}
}
