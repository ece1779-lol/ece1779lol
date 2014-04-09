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
	private long gameLength;
	private long goldEarned;
	private long championsKilled;
	private long assists;
	private long deaths;
	private String championName;
	
	
	public StoredGame(Map<String, Object> properties)
	{
		this.summoner_key = (String)properties.get("summoner_key");
		this.gameId = (long)properties.get("gameId");
		this.isWin = (boolean)properties.get("isWin");
		this.gameDate = (Date)properties.get("gameDate");
		this.gameLength = (long)properties.get("gameLength");
		this.goldEarned = (long)properties.get("goldEarned");
		this.championsKilled = (long)properties.get("championsKilled");
		this.assists = (long)properties.get("assists");
		this.deaths = (long)properties.get("deaths");
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


	public long getGameLength() {
		return gameLength;
	}


	public long getGoldEarned() {
		return goldEarned;
	}


	public long getChampionsKilled() {
		return championsKilled;
	}


	public long getAssists() {
		return assists;
	}


	public long getDeaths() {
		return deaths;
	}


	public String getChampionName() {
		return championName;
	}
}
