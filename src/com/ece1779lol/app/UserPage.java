package com.ece1779lol.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.*;

import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.*;


@SuppressWarnings("serial")
public class UserPage extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		
		out.println("Hello, world");
		
		String apiKey = "fc237e42-4071-4272-a723-c98bc3ddd7ef";
		int numPerTenSeconds = 10;
		int numPerTenMinutes = 500;
		
		
		RiotApi api = new RiotApi(apiKey);
		
		Region REGION = Region.NA;
		Summoner summoner;
		try {
			summoner = api.getSummoner(REGION, "hatakekakashi");
			out.println(summoner.getName()+" "+summoner.getSummonerLevel());

			List<Game> myMatchHistory = summoner.getMatchHistory();
			for (Game game : myMatchHistory)
			{
				out.println(game.getGameId()+" "+game.isWin()+" "+game.getEnemyMinionsKilled()+" "+game.getLength()+" "+game.getTotalPlayerScore()+" "+game.getGoldLeft());
			}
			
		} catch (RiotApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
