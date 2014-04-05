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
public class AddSummoner extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
		
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
		
		out.println("Hello "+user.getNickname());
		
		RiotApi client = (RiotApi)getServletContext().getAttribute("RiotClient");
		
		Region REGION = Region.NA;
		Summoner summoner;
		
		String summonerName = req.getParameter("summonerName");
		
		try {
			summoner = client.getSummoner(REGION, summonerName);
			out.println(summoner.getName()+" "+summoner.getSummonerLevel());

			List<Game> myMatchHistory = summoner.getMatchHistory();
			for (Game game : myMatchHistory)
			{
				out.println(game.getGameId()+" "+game.isWin()+" "+game.getEnemyMinionsKilled()+" "+game.getLength()+" "+game.getTotalPlayerScore()+" "+game.getGoldLeft());
			}
			
		} catch (RiotApiException e) {
			out.println(summonerName+" is invalid summoner ID");
		}
	}
}
