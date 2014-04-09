package com.ece1779lol.app;

import java.util.*;

import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.types.Champion;

public class StoredGame
{
	private String summoner_key;
	private long gameId;
	private boolean isWin;
	private Date gameDate;
	private int gameLength;
	private int goldEarned;
	private int championsKilled;
	private int assists;
	private int deaths;
	private String championName;
	
	
	public StoredGame(Map<String, Object> properties)
	{
		this.summoner_key = (String)properties.get("summoner_key");
		this.gameId = (long)properties.get("gameId");
		this.isWin = (boolean)properties.get("isWin");
		this.gameDate = (Date)properties.get("gameDate");
		this.gameLength = (int)properties.get("gameLength");
		this.goldEarned = (int)properties.get("goldEarned");
		this.championsKilled = (int)properties.get("championsKilled");
		this.assists = (int)properties.get("assists");
		this.deaths = (int)properties.get("deaths");
		this.championName = (String)properties.get("championName");
	}


	public String getSummoner_key() {
		return summoner_key;
	}


	public long getGameId() {
		return gameId;
	}


	public boolean isWin() {
		return isWin;
	}


	public Date getGameDate() {
		return gameDate;
	}


	public int getGameLength() {
		return gameLength;
	}


	public int getGoldEarned() {
		return goldEarned;
	}


	public int getChampionsKilled() {
		return championsKilled;
	}


	public int getAssists() {
		return assists;
	}


	public int getDeaths() {
		return deaths;
	}


	public String getChampionName() {
		return championName;
	}
}
