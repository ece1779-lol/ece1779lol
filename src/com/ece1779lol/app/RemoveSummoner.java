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
public class RemoveSummoner extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(RemoveSummoner.class.getName());
	
	private void RemoveGlobalFavorites(DatastoreService ds, String summonerKeyStr)
	{
		Entity globalFavoritesEntity;
		Key summonerKey = KeyFactory.stringToKey(summonerKeyStr);
		
		try {
			globalFavoritesEntity = ds.get(summonerKey);
			long refcount = (long)globalFavoritesEntity.getProperty("refcount");
			//decrease refcount to track multiusers
			if (refcount > 1)
			{
				--refcount;
				globalFavoritesEntity.setProperty("refcount", refcount);
				ds.put(globalFavoritesEntity);
			}
			else
			{
				//If last reference then delete entry and also delete game history
				ds.delete(summonerKey);
				
				Query q = new Query(HelperFunctions.gameEntityStr);
				q.setFilter(new FilterPredicate("summoner_key",
						Query.FilterOperator.EQUAL,
						summonerKeyStr));
				
				// Perform the query.
				PreparedQuery pq = ds.prepare(q);
				for (Entity gameEntity : pq.asIterable()) {
					ds.delete(gameEntity.getKey());
				}
			}
		} catch (EntityNotFoundException e) {
			log.info("Couldn't find global favorites summoner");
			// TODO: something really bad going on!
			return;
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

		String userFavoriteKeyStr = req.getParameter("favoritesKey");
		String globalSummonerKeyStr = req.getParameter("summonerKey");

		int retries = 3;
		boolean success = false;
		
		TransactionOptions options = TransactionOptions.Builder.withXG(true);
		Transaction txn=null;
		
		while (!success && retries > 0) {
			--retries;
			try {
				txn = ds.beginTransaction(options);

				Key userFavoriteKey = KeyFactory.stringToKey(userFavoriteKeyStr);
				ds.delete(userFavoriteKey);
				
				RemoveGlobalFavorites(ds, globalSummonerKeyStr);

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
