package com.ece1779lol.app;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;

import net.enigmablade.riotapi.RiotApi;
import net.enigmablade.riotapi.constants.Region;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.Champion;
import net.enigmablade.riotapi.types.Game;
import net.enigmablade.riotapi.types.Summoner;

public class HelperFunctions {
	
	private static final Logger log = Logger.getLogger(HelperFunctions.class.getName());
	
	public static String globalGames = "globalGames";
	public static String globalFavorites = "globalFavorites";
	public static String userFavoritePrefix = "userFavorites_";
	public static String gameEntityStr = "lol_game";
	
	public static Key getGlobalFavoritesKey()
	{
		return KeyFactory.createKey("Favorites", globalFavorites);
	}
	
	public static Entity getGlobalFavoritesEntity()
	{
		return new Entity("Favorites", globalFavorites);
	}
	
	public static String getUserFavoritesStr(String userId)
	{
		return userFavoritePrefix+userId;
	}
	
	public static String getGameIDName(long gameId, String globalSummonerKeyStr)
	{
		return gameId+"_"+globalSummonerKeyStr;
	}
	
	public static void printLoginPage(PrintWriter out, UserService userService)
	{
		String navBar = "<nav>Welcome! <a href=\""+ userService.createLoginURL("/") +
				"\">Sign in or register</a> to customize.</nav>";
		out.println(navBar);
		out.println("  </body>");
		out.println("</html>");
		return;
	}
	
	public static void printLolHeader(PrintWriter out, String title)
	{
		out.println("<head>");
		out.println("<title>" + title + "</title>");
		out.println("<meta name='viewport' content='width=600'>");
		out.println("<link rel='stylesheet' href='/css/style.css'>");
		out.println("<link rel='stylesheet' href='/css/code.css'>");
		out.println("</head>");
	}
	
	public static void printLolLogo(PrintWriter out)
	{
		out.println("<div id='logo'>");
		out.println("<img height=140 src ='/css/lol_logo.png'>");
		out.println("<span>ECE1779 LOL Tracker</span>");
		out.println("</div>");
	}
	
	public static void printFavoriteSummunerTitle(PrintWriter out, String summonerName, String region)
	{
		String formId = "form_"+summonerName+region;
		/*
		out.println("<form action='/querySummoner' id="+formId+" method='post' style='display: none;'>");
		out.println("<input type='text' name='summonerName' value="+summonerName+" />");
		out.println("<input type='text' name='region' value="+getStringFromRegion(region)+" />");
		out.println("</form>");
		out.println("<a href='javascript:;' onclick='javascript:document.getElementById("+formId+").submit()'><h4>"+summonerName+" "+region+"</h4></a>");
		*/
		out.println("<td><form name="+formId+" method='post' action='/querySummoner'>");
		out.println("<input type='hidden' name='summonerName' value="+summonerName+">");
		out.println("<input type='hidden' name='region' value="+getStringFromRegion(region)+" />");
		out.println("<a href='javascript:document."+formId+".submit()'><h4>"+summonerName+"</h4></a>");
		out.println("</form></td>");
		out.println("<td>" + region.toUpperCase() + "</td>");
	}
	
	public static void printUserPageStats(PrintWriter out, String summonerName, String region, RiotApi client)
	{
		Summoner summoner;
		Region regionQuery = HelperFunctions.getRegionFromString(region);
		
		Game game;
		
		try {
			summoner = client.getSummoner(regionQuery, summonerName);
			
			List<Game> myMatchHistory = summoner.getMatchHistory();
			
			game = myMatchHistory.get(0);
			
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String dateFormatted = formatter.format(game.getPlayedDate());
			out.println("<td>" + dateFormatted + "</td>");
			
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
		}
		catch (RiotApiException e) {
			out.println("No GAMES");
		}
	
	}
	
	private static Object getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void printLolMenu(PrintWriter out, UserService userService, User user)
	{
		out.println("<div id='menu'>");
		out.println("<a class='home-link' href='/'>Home</a>");
		
		if (user != null) {
			out.println("<p>" + user.getNickname() + "</p>");
			out.println("<a href=\"" + userService.createLogoutURL("/") + "\">Sign Out</a>");
		}

		out.println("</div>");
	}
	
	public static Region getRegionFromString(String region)
	{
		Region REGION;
		switch(region)
		{
			case "na":  REGION = Region.NA; break;
			case "euw":  REGION = Region.EUW; break;
			case "eune":  REGION = Region.EUNE; break;
			case "br":  REGION = Region.BR; break;
			case "tr":  REGION = Region.TR; break;
			case "ru":  REGION = Region.RU; break;
			case "lan":  REGION = Region.LAN; break;
			case "las":  REGION = Region.LAS; break;
			case "oce":  REGION = Region.OCE; break;
			default: REGION = Region.NA; break;
		}
		return REGION;
	}
	
	public static String getStringFromRegion(String region)
	{
		String REGION;
		switch(region)
		{
			case "na":  REGION = "N.A."; break;
			case "euw":  REGION = "Europe W."; break;
			case "eune":  REGION = "Europe N. & E."; break;
			case "br":  REGION = "Brazil"; break;
			case "tr":  REGION = "Turkey"; break;
			case "ru":  REGION = "Russia"; break;
			case "lan":  REGION = "L.A. North"; break;
			case "las":  REGION = "L.A. South"; break;
			case "oce":  REGION = "Oceania"; break;
			default: REGION = "N.A."; break;
		}
		return REGION;
	}
			
}
