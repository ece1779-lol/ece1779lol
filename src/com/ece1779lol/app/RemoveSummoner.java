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
		
		String summonerName = req.getParameter("summonerName");
		String regionName = req.getParameter("region");
		
		HelperFunctions.printLolHeader(out, "Unfollow Summoner");
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

		if (userFavoriteKeyStr == null && globalSummonerKeyStr == null)
		{
			if (summonerName == null && regionName == null)
			{
				out.println("<h1>Invalid Params</h1>");
				out.println("<a href=\"/\">Return to home page.</a></p>");
				return;
			}
			else
			{
				globalSummonerKeyStr = KeyFactory.keyToString(HelperFunctions.getGlobalSummonerKey(summonerName, regionName));
				userFavoriteKeyStr = KeyFactory.keyToString(HelperFunctions.getUserSummonerKey(user.getUserId(), summonerName, regionName));
			}
		}
		
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
				
				HelperFunctions.RemoveGlobalFavorites(ds, globalSummonerKeyStr);

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
