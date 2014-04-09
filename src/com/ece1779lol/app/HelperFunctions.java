package com.ece1779lol.app;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.FilterPredicate;
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
	

	public static boolean isFavoriteAlready(DatastoreService ds, String userId, String summonerName, Region region)
	{
		String regionName = region.getValue();
		Key userSummonerKey = getUserSummonerKey(userId, summonerName, regionName);
		try {
			//Check if it exists or not
			ds.get(userSummonerKey);
			return true;

		} catch(EntityNotFoundException e) {
			return false;
		}
	}
	
	//Retrieves key of summoner from global favorites or adds it
	public static String addToFavorites(DatastoreService ds, String userId, String summonerName, Region region)
	{
		// Get/Create Global Favorites
		Key globalFavoritesKey;
		Entity globalFavoritesEntity;

		try {
			globalFavoritesKey = getGlobalFavoritesKey();
			globalFavoritesEntity = ds.get(globalFavoritesKey);

		} catch (EntityNotFoundException e) {
			globalFavoritesEntity = getGlobalFavoritesEntity();
			globalFavoritesEntity.setProperty("count", 0L);
			globalFavoritesKey = ds.put(globalFavoritesEntity);
		}

		// Get/Create User Favorites. Each user has own favorites list
		String userFavoritesID = getUserFavoritesStr(userId); //ID is prefix and google ID
		Key userFavoritesKey;
		Entity userFavoritesEntity;

		try {
			userFavoritesKey = KeyFactory.createKey("Favorites", userFavoritesID);
			userFavoritesEntity = ds.get(userFavoritesKey);

		} catch (EntityNotFoundException e) {
			userFavoritesEntity = new Entity("Favorites", userFavoritesID);
			userFavoritesEntity.setProperty("count", 0L);
			userFavoritesKey = ds.put(userFavoritesEntity);
		}


		// Get/Create global summoner properties
		String favoritesEntryName = getGlobalSummonerStr(summonerName, region.getValue());   // key is summonername+region
		Key favoritesEntryKey, userSummonerKey;
		Entity favoritesEntryEntity, userSummonerEntity;
		String globalSummonerKeyStr;
		String userSummonerName = getUserSummonerStr(userId, summonerName, region.getValue());

		try {
			long refcount;
			favoritesEntryKey = getGlobalSummonerKey(summonerName, region.getValue());
			KeyFactory.createKey(globalFavoritesKey, "summoner", favoritesEntryName);
			favoritesEntryEntity = ds.get(favoritesEntryKey);

			globalSummonerKeyStr = KeyFactory.keyToString(favoritesEntryEntity.getKey());
			
			userSummonerKey = KeyFactory.createKey(userFavoritesKey, "summoner_ref", userSummonerName);

			try {
				//Check if it exists in user favorites and move on if it already exists
				userSummonerEntity = ds.get(userSummonerKey);

			} catch(EntityNotFoundException e) {
				//If not then increment refcount and create new one
				refcount = (Long) favoritesEntryEntity.getProperty("refcount");
				++refcount;
				favoritesEntryEntity.setProperty("refcount", refcount);
				ds.put(favoritesEntryEntity);

				userSummonerEntity = new Entity("summoner_ref", userSummonerName, userFavoritesKey);
				userSummonerEntity.setProperty("summoner_key", globalSummonerKeyStr);
				userSummonerKey = ds.put(userSummonerEntity);
			}


		} catch (EntityNotFoundException e) {
			favoritesEntryEntity = new Entity("summoner", favoritesEntryName, globalFavoritesKey);
			favoritesEntryEntity.setProperty("summoner_name", summonerName);
			favoritesEntryEntity.setProperty("region", region.getValue());
			favoritesEntryEntity.setProperty("refcount", 1L);
			favoritesEntryKey = ds.put(favoritesEntryEntity);

			globalSummonerKeyStr = KeyFactory.keyToString(favoritesEntryKey);

			userSummonerEntity = new Entity("summoner_ref", userSummonerName, userFavoritesKey);
			userSummonerEntity.setProperty("summoner_key", globalSummonerKeyStr);
			userSummonerKey = ds.put(userSummonerEntity);
		}

		return globalSummonerKeyStr;
	}

	public static void RemoveGlobalFavorites(DatastoreService ds, String summonerKeyStr)
	{
		Entity globalFavoritesEntity;
		Key summonerKey = KeyFactory.stringToKey(summonerKeyStr);
		
		try {
			globalFavoritesEntity = ds.get(summonerKey);
			long refcount = (long)globalFavoritesEntity.getProperty("refcount");
			//decrease refcount to track multiusers
			if (refcount > 1)
			{
				--refcount;
				globalFavoritesEntity.setProperty("refcount", refcount);
				ds.put(globalFavoritesEntity);
			}
			else
			{
				//If last reference then delete entry and also delete game history
				ds.delete(summonerKey);
				
				Query q = new Query(gameEntityStr);
				q.setFilter(new FilterPredicate("summoner_key",
						Query.FilterOperator.EQUAL,
						summonerKeyStr));
				
				// Perform the query.
				PreparedQuery pq = ds.prepare(q);
				for (Entity gameEntity : pq.asIterable()) {
					ds.delete(gameEntity.getKey());
				}
			}
		} catch (EntityNotFoundException e) {
			log.info("Couldn't find global favorites summoner");
			// TODO: something really bad going on!
			return;
		}
	}
	
	public static void printLastMatchHistory()
	{
		
	}
	
	public static void printMatchHistory()
	{
		
	}
	
	public static void addMatchHistory(DatastoreService ds, String globalSummonerKey, String summonerName, Region region, RiotApi client)
	{
		Summoner summoner;

		try {
			summoner = client.getSummoner(region, summonerName);

			try {
				List<Game> myMatchHistory = summoner.getMatchHistory();
				for (Game game : myMatchHistory)
				{
					Key GamesKey;
					Entity GamesEntity;
					try {
						GamesKey = KeyFactory.createKey("Games", globalGames);
						GamesEntity = ds.get(GamesKey);

					} catch (EntityNotFoundException e) {
						GamesEntity = new Entity("Games", globalGames);
						GamesKey = ds.put(GamesEntity);
					}

					String gameIdName = getGameIDName(game.getGameId(), globalSummonerKey);
					Key gameIdKey;
					Entity gameIdEntity;
					try {
						gameIdKey = KeyFactory.createKey(GamesKey, gameEntityStr, gameIdName);
						gameIdEntity = ds.get(gameIdKey);

						//game exists in DB, hence no longer need to store more of the history
						break;
					} catch (EntityNotFoundException e) {
						//game doesnt exist, add it to DB
						gameIdEntity = new Entity(gameEntityStr, gameIdName, GamesKey);
						gameIdEntity.setProperty("summoner_key", globalSummonerKey);
						gameIdEntity.setProperty("gameId", game.getGameId());
						gameIdEntity.setProperty("isWin", game.isWin());
						gameIdEntity.setProperty("gameDate", game.getPlayedDate());
						gameIdEntity.setProperty("gameLength", game.getLength());
						gameIdEntity.setProperty("goldEarned", game.getGoldEarned());
						gameIdEntity.setProperty("championsKilled", game.getChampionsKilled());
						gameIdEntity.setProperty("assists", game.getAssists());
						gameIdEntity.setProperty("deaths", game.getDeaths());
						
						Champion champion = game.getChampion();
						gameIdEntity.setProperty("championName", champion.getName());

						gameIdKey = ds.put(gameIdEntity);
					}
				}
			} catch (RiotApiException e) {
				log.info("No match history found");
			}

		} catch (RiotApiException e) {
			log.info(summonerName+" is invalid summoner ID");
		}
	}
	
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
	
	public static Key getUserFavoritesKey(String userId)
	{
		return KeyFactory.createKey("Favorites", getUserFavoritesStr(userId));
	}
	
	public static Key getGlobalSummonerKey(String summonerName, String region)
	{
		Key globalFavoritesKey = getGlobalFavoritesKey();
		return KeyFactory.createKey(globalFavoritesKey, "summoner", getGlobalSummonerStr(summonerName, region));
	}
	
	public static Key getUserSummonerKey(String userId, String summonerName, String regionName)
	{
		return KeyFactory.createKey(getUserFavoritesKey(userId), "summoner_ref", getUserSummonerStr(userId, summonerName, regionName));
	}
	
	
	public static String getGlobalSummonerStr(String summonerName, String region)
	{
		return summonerName+region;    // key is summonername+region
	}
	
	public static String getUserSummonerStr(String userId, String summonerName, String region)
	{
		return userId+summonerName+region;    // key is userid+summonername+region
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
		Region regionQuery = getRegionFromString(region);
		
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
