package com.ece1779lol.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
		
		out.println("Hello "+user.getNickname());
		
		RiotApi client = (RiotApi)getServletContext().getAttribute("RiotClient");
		HelperFunctions help = (HelperFunctions)getServletContext().getAttribute("HelperFunctions");
		
		Summoner summoner;
	
		String summonerName = req.getParameter("summonerName");
		Region region = help.getRegionFromString(req.getParameter("region"));
		
		//Region region = Region.NA;
		QueueType soloQueueQuery = QueueType.RANKED_5V5_SOLO;
		League leagueData;
		LeagueTier leagueTierData;

		try {
			summoner = client.getSummoner(region, summonerName);
			out.println("<h1>"+summoner.getName()+" Level :"+summoner.getSummonerLevel());
			
			//obtain League information
			leagueData = summoner.getLeague(soloQueueQuery);
			leagueTierData = leagueData.getTier();
            List<League.Entry> leagueEntryData = leagueData.getEntries();
            
            League.Entry leagueEntry = leagueEntryData.get(0);

		    out.println("</br>");
		    out.println("LP: " +leagueEntry.getLeaguePoints()+ " Tier: " +leagueData.getTier()+ " Division: " +leagueEntry.getRank());
		    out.println("</br>");
			
			out.println("  <form id='addFavorite' name=add_favorite action='/addSummoner' method='post'>");
			out.println("  <input type='hidden' name='summonerName' value="+summoner.getName()+">");
			out.println("  <input type='hidden' name='region' value="+region.getValue()+">");
			out.println("  <input type='submit' value='Add to Favorite'>");
			out.println("  </form>");
			out.println("</h1>");
			
			out.println("Champion Used" + " Outcome " + " Length " + "Total Gold");
		    out.println("</br>");

			try {
				List<Game> myMatchHistory = summoner.getMatchHistory();
				for (Game game : myMatchHistory)
				{
					Champion champion = game.getChampion();

					out.println(champion.getName()+ " ");
					out.println("<img src=\"" +champion.getName()+"_Square_0.png\" height=50 width=50>");
					if (game.isWin())
						out.println("Win ");
					else
						out.println("Loss ");
					
					int gameLengthInMinutes = game.getLength() / 60;
					out.println(gameLengthInMinutes+" "+game.getGoldEarned());
					out.println("</br>");
				}
			} catch (RiotApiException e) {
				out.println("No GAMES");
			}
			
		} catch (RiotApiException e) {
			out.println(summonerName+" is invalid summoner ID");
			out.println("<a href=\"/userPage\">Return to home page.</a></p>");
		}
	}
}
