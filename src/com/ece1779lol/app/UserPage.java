package com.ece1779lol.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.*;
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
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		doGet(req, resp);
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		RiotApi client = (RiotApi)getServletContext().getAttribute("RiotClient");

		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		out.println("<html>");
		/* header */
		HelperFunctions.printLolHeader(out, "Welcome to LOL tracker");

		/* logo and menu */
		out.println("  <body>");
		HelperFunctions.printLolLogo(out);
		HelperFunctions.printLolMenu(out, userService, user);
		
		out.println("<div id='container'>");

		/* goto sign in menu if not signed in*/
		if (user == null) {
			HelperFunctions.printLoginPage(out, userService);
			out.println("</div>");
			return;
		}
		
		/* Form to query a summoner */
		out.println("<div class='highlight' id='container elem'>");
		
		out.println("<section class='elem elem-green'>");
		
		out.println("<form action='/querySummoner' method='post'>");
		out.println("  <h1 class='content'>Query a Summoner</h1>");
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
		out.println("  <input class='actionbutton' type='submit' value='Go'>");
		out.println("</form>");
		out.println("</section>");
		out.println("</div>");

		
		/* show favorite summoners */
		out.println("<br>");
		out.println("<div class='highlight' id='container elem'>");
		out.println("<section class='elem elem-green'>");

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

		String userFavoritesKeyName = HelperFunctions.getUserFavoritesStr(user.getUserId());

		// Display information about a message board and its messages.
		Key userFavoritesKey = KeyFactory.createKey("Favorites", userFavoritesKeyName);
		out.println("<h1 class='content'>Followed Summoners Last Game</h1>");

		Query q = new Query("summoner_ref", userFavoritesKey);
		PreparedQuery pq = ds.prepare(q);
		//out.println("<p>Favorites of " + userFavoritesKeyName + " (" + pq.countEntities() + " total):</p>");
		
		/* print summoners table header */
		out.println("<table class='pretty'><tbody>");
		out.println("<tr>");
		out.println("<th>Summoner</th><th>Region</th>");
		out.println("<th>Date</th><th>Champion</th><th>Outcome</th><th>Minutes</th>");
		out.println("<th>Gold</th><th>Kills</th><th>Assists</th><th>Deaths</th><th></th>");
		out.println("</tr>");
		
		for (Entity favorite_keys : pq.asIterable()) {

			String summoner_key = (String)favorite_keys.getProperty("summoner_key");
			Query q2 = new Query("summoner", KeyFactory.stringToKey(summoner_key));
			PreparedQuery pq2 = ds.prepare(q2);
			if (pq2.countEntities() != 1)
				out.println("<h1>WE GOT BIG ISSUE</h1>");

			for (Entity summoner : pq2.asIterable()) {
				
				out.println("<tr>");

				String summonerName = (String)summoner.getProperty("summoner_name");
				String region = (String)summoner.getProperty("region");
				HelperFunctions.printFavoriteSummunerTitle(out, summonerName, region);
				
				HelperFunctions.printUserPageStats(out, (String)summoner.getProperty("summoner_name"), 
						HelperFunctions.getStringFromRegion((String)summoner.getProperty("region")), client);

				out.println("  <td><form id='addFavorite' name=add_favorite action='/removeSummoner' method='post'>");
				out.println("  <input type='hidden' name='favoritesKey' value="+KeyFactory.keyToString(favorite_keys.getKey())+">");
				out.println("  <input type='hidden' name='summonerKey' value="+summoner_key+">");
				out.println("  <input class='actionbutton' style='width: inherit;' type='submit' value='Unfollow'>");
				out.println("  </form></td>");
				
				out.println("</tr>");
			}

		}
		out.println("</tbody></table>"); /* table */
		out.println("</section></div>"); /* end of favoirtes div */
		
		out.println("</div>"); /* end of container */ 

		out.println("  </body>");
		out.println("</html>");
	}
}
