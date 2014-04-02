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
public class Ece1779lolServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		
		out.println("Hello, world");
		
		String apiKey = null;
		int numPerTenSeconds = 10;
		int numPerTenMinutes = 500;
		
		
		RiotApi api = new RiotApi(apiKey);
		
		Region REGION = Region.NA;
		Summoner me;
		try {
			me = api.getSummoner(REGION, "TheEnigmaBlade");
			out.println(me.getName()+" "+me.getSummonerLevel());

			List<Game> myMatchHistory = me.getMatchHistory();
			for (Game game : myMatchHistory)
			{
				out.println(game.getTotalPlayerScore()+" "+game.getGoldLeft());
			}
			
		} catch (RiotApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
