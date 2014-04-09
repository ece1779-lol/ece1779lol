package com.ece1779lol.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.*;

@SuppressWarnings("serial")
public class QuerySummoner extends HttpServlet {

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
		
		RiotApi client = (RiotApi)getServletContext().getAttribute("RiotClient");

		Summoner summoner;

		String summonerName = req.getParameter("summonerName");
		Region region = HelperFunctions.getRegionFromString(req.getParameter("region"));

		try {
			summoner = client.getSummoner(region, summonerName);
			out.println("<h1>"+summoner.getName()+" Level :"+summoner.getSummonerLevel());

			//obtain League information
			try {
				League leagueData;
				
				QueueType soloQueueQuery = QueueType.RANKED_5V5_SOLO;
				leagueData = summoner.getLeague(soloQueueQuery);
				
				Map<String, League.Entry> leagueEntryData = leagueData.getEntries();

				League.Entry leagueEntry = leagueEntryData.get(Long.toString(summoner.getId()));
	
				if (leagueEntry != null)
				{
					out.println("</br>");
					out.println("LP: " +leagueEntry.getLeaguePoints()+ " Tier: " +leagueData.getTier()+ " Division: " +leagueEntry.getRank());
					out.println("</br>");					
				}
				else
				{
					out.println("No League Info");
				}

			} catch (RiotApiException e) {
				out.println("No League Info");
			}
			
			out.println("  <form id='addFavorite' name=add_favorite action='/addSummoner' method='post'>");
			out.println("  <input type='hidden' name='summonerName' value="+summoner.getName()+">");
			out.println("  <input type='hidden' name='region' value="+region.getValue()+">");
			out.println("  <input type='submit' value='Add to Favorite'>");
			out.println("  </form>");
			out.println("</h1>");
			

			out.println("</br>");
			out.println("Game Played On" + "Champion Used" + " Outcome " + " Length " + " Total Gold " + " Kills " + " Deaths " + " Assists ");
			out.println("</br>");
			// Match History
			try {
				List<Game> myMatchHistory = summoner.getMatchHistory();
				for (Game game : myMatchHistory)
				{
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
					String dateFormatted = formatter.format(game.getPlayedDate());
					out.println(dateFormatted+" ");
					
					Champion champion = game.getChampion();
					
					out.println("<img src=\"" +champion.getName()+"_Square_0.png\" height=50 width=50>");
					out.println(champion.getName()+ " ");
					
					if (game.isWin())
						out.println("Win ");
					else
						out.println("Loss ");

					int gameLengthInMinutes = game.getLength() / 60;
					out.println(gameLengthInMinutes+" "+game.getGoldEarned());
					
					out.println(game.getChampionsKilled()+"-"+game.getDeaths()+"-"+game.getAssists());
					
					out.println("</br>");
				}
			} catch (RiotApiException e) {
				out.println("No GAMES");
			}

		} catch (RiotApiException e) {
			out.println(summonerName+" is invalid summoner ID");
			out.println("<a href=\"/\">Return to home page.</a></p>");
		} finally {
			out.println("</body>");
		}
	}
}
