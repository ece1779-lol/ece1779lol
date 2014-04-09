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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.*;

@SuppressWarnings("serial")
public class QuerySummoner extends HttpServlet {

	private static final Logger log = Logger.getLogger(QuerySummoner.class.getName());
	
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
		
		Region region = HelperFunctions.getRegionFromString(regionName);
		
		HelperFunctions.printLolHeader(out, "LOL Tracker - "+summonerName);
		out.println("<body>");
		HelperFunctions.printLolLogo(out);
		HelperFunctions.printLolMenu(out, userService, user);

		/* goto sign in menu if not signed in*/
		if (user == null) {
			HelperFunctions.printLoginPage(out, userService);
			return;
		}
		
		out.println("<div id='container'>");
		
		RiotApi client = (RiotApi)getServletContext().getAttribute("RiotClient");

		Summoner summoner;

		try {
			out.println("<div class='highlight' id='container elem'>");
			out.println("<section class='elem elem-green'>");
			
			summoner = client.getSummoner(region, summonerName);
			out.println("<h1>Summoner : " + summoner.getName() + "</h1>");
			out.println("<h1>Level : " + summoner.getSummonerLevel() + "</h1>");

			//obtain League information
			try {
				League leagueData;
				
				QueueType soloQueueQuery = QueueType.RANKED_5V5_SOLO;
				leagueData = summoner.getLeague(soloQueueQuery);
				
				Map<String, League.Entry> leagueEntryData = leagueData.getEntries();

				League.Entry leagueEntry = leagueEntryData.get(Long.toString(summoner.getId()));
	
				if (leagueEntry != null)
				{
					out.println("<h2>");
					out.println("<p>League Points : " + leagueEntry.getLeaguePoints() + "</p>");
					out.println("<p>Tier : " + leagueEntry.getTier() + "</p>");
					out.println("<p>Division : " + leagueEntry.getRank() + "</p>");
					out.println("</h2>");
				}
				else
				{
					log.info("No League Info");
				}

			} catch (RiotApiException e) {
				log.info("RiotApiException encountered while retrieving league data");
			}
			
			DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
			
			if ( HelperFunctions.isFavoriteAlready(ds, user.getUserId(), summoner.getName(), region) )
			{
				out.println("  <form id='removeFavorite' name=remove_favorite action='/removeSummoner' method='post'>");
				out.println("  <input type='hidden' name='summonerName' value="+summoner.getName()+">");
				out.println("  <input type='hidden' name='region' value="+region.getValue()+">");
				out.println("  <input class='actionbutton' type='submit' value='UnFollow'>");
				out.println("  </form>");
			}
			else
			{
				out.println("  <form id='addFavorite' name=add_favorite action='/addSummoner' method='post'>");
				out.println("  <input type='hidden' name='summonerName' value="+summoner.getName()+">");
				out.println("  <input type='hidden' name='region' value="+region.getValue()+">");
				out.println("  <input class='actionbutton' type='submit' value='Follow'>");
				out.println("  </form>");
			}
			out.println("</h1>");
			out.println("</section></div>");
			
			/* Match history table */
			out.println("<br>");
			out.println("<div class='highlight' id='container elem'>");
			out.println("<section class='elem elem-green'>");
			out.println("<h1 class='content'>Match History</h1>");
			out.println("<table class='pretty'><tbody>");
			out.println("<tr><th>Date</th><th>Champion</th><th>Outcome</th><th>Minutes</th>");
			out.println("<th>Gold</th><th>Kills</th><th>Assists</th><th>Deaths</th></tr>");
			// Match History
			try {
				List<Game> myMatchHistory = summoner.getMatchHistory();
				for (Game game : myMatchHistory)
				{
					out.println("<tr>");

					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
					String dateFormatted = formatter.format(game.getPlayedDate());
					out.println("<td>" + dateFormatted +"</td>");
					
					Champion champion = game.getChampion();

					out.println("<td>" + "<img src=\"" +champion.getName()+"_Square_0.png\" height=50 width=50> " + champion.getName() +"</td>");

					if (game.isWin())
						out.println("<td>Win</td>");
					else
						out.println("<td>Loss</td>");

					int gameLengthInMinutes = game.getLength() / 60;
					out.println("<td>" + gameLengthInMinutes +"</td>");
					out.println("<td>" + game.getGoldEarned() +"</td>");
					out.println("<td>" + game.getChampionsKilled() +"</td>");
					out.println("<td>" + game.getAssists() +"</td>");
					out.println("<td>" + game.getDeaths() +"</td>");
					
					out.println("</tr>");
				}
			} catch (RiotApiException e) {
				out.println("No GAMES");
			} finally {
				out.println("</tbody></table></section></div>");
			}

		} catch (RiotApiException e) {
			out.println(summonerName+" is invalid summoner ID");
			out.println("<a href=\"/\">Return to home page.</a></p>");
		} finally {
			out.println("</div>");
			out.println("</body>");
		}
	}
}
