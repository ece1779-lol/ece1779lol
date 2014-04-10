package com.ece1779lol.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.users.*;

import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.*;


@SuppressWarnings("serial")
public class AddSummoner extends HttpServlet {

	private static final Logger log = Logger.getLogger(AddSummoner.class.getName());

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
		
		if (summonerName.isEmpty() || regionName.isEmpty())
		{
			out.println("<h1>Invalid Params</h1>");
			out.println("<a href=\"/\">Return to home page.</a></p>");
			return;
		}

		HelperFunctions.printLolHeader(out, "Follow Summoner");
		out.println("<body>");
		HelperFunctions.printLolLogo(out);
		HelperFunctions.printLolMenu(out, userService, user);

		/* goto sign in menu if not signed in*/
		if (user == null) {
			HelperFunctions.printLoginPage(out, userService);
			return;
		}

		DatastoreService ds = (DatastoreService)getServletContext().getAttribute("DataStore");
		MemcacheService mc = (MemcacheService)getServletContext().getAttribute("MemCache");

		RiotApi client = (RiotApi)getServletContext().getAttribute("RiotClient");
		
		Region region = HelperFunctions.getRegionFromString(regionName);

		int retries = 3;
		boolean success = false;

		TransactionOptions options = TransactionOptions.Builder.withXG(true);
		Transaction txn=null;

		while (!success && retries > 0) {
			--retries;
			try {
				txn = ds.beginTransaction(options);

				// Add to favorites if it is not in favorites
				String globalSummonerKeyStr = HelperFunctions.addToFavorites(ds, mc, user.getUserId(), summonerName, region.getValue());

				HelperFunctions.getLatestSummonerMatchHistory(client, ds, mc, summonerName, region);

				txn.commit();

				// Break out of retry loop.
				success = true;

			} catch (DatastoreFailureException e) {
				// Allow retry to occur.
				log.info("Transaction failed - retry");
			}
		}

		if (!success) {
			log.info("Transaction failed - rolling back");
			resp.getWriter().println
				("<p>A Could not add to Favorites.  Try again later." +
				 "<a href=\"/uerPage\">Return to home page.</a></p>");
			txn.rollback();

		} else {
			log.info("Transaction success");
			resp.sendRedirect("/");
		}
	}
}
