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
		
		try {
			summoner = client.getSummoner(region, summonerName);
			out.println("<h1>"+summoner.getName()+" Level :"+summoner.getSummonerLevel());
			out.println("  <form id='addFavorite' name=add_favorite action='/addSummoner' method='post'>");
			out.println("  <input type='hidden' name='summonerName' value="+summoner.getName()+">");
			out.println("  <input type='hidden' name='region' value="+region.getValue()+">");
			out.println("  <input type='submit' value='Add to Favorite'>");
			out.println("  </form>");
			out.println("</h1>");

			try {
				List<Game> myMatchHistory = summoner.getMatchHistory();
				for (Game game : myMatchHistory)
				{
					out.println(game.getGameId()+" "+game.isWin()+" "+game.getEnemyMinionsKilled()+" "+game.getLength()+" "+game.getTotalPlayerScore()+" "+game.getGoldLeft());
					out.println("</br>");
				}
			} catch (RiotApiException e) {
				out.println("No GAMES");
			}
			
		} catch (RiotApiException e) {
			out.println(summonerName+" is invalid summoner ID");
			out.println("<a href=\"/uerPage\">Return to home page.</a></p>");
		}
	}
}
