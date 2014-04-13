package com.ece1779lol.app;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.enigmablade.riotapi.RiotApi;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.memcache.MemcacheService;

public class BackgroundWorker extends TimerTask {

	private static final Logger log = Logger.getLogger(BackgroundWorker.class.getName());
	
	// public
	public static String accessKey;
	public static String secretKey;

	public static int test_counter;

	// PRIVATE
	private static int timerStarted;
	private final static long fONCE_PER_Day = 1000 * 60 * 60 * 24; // in
																	// milliseconds

	private static DatastoreService ds;
	private static RiotApi client;
	private static MemcacheService mc;
	
	public static void setRiotClient(RiotApi riotClient)
	{
		client = riotClient;
	}
	public static void setDatastoreService(DatastoreService dataService)
	{
		ds = dataService;
	}
	public static void setMemcacheService(MemcacheService memcacheService)
	{
		mc = memcacheService;
	}
	
	/** Construct and use a TimerTask and Timer. */
	public static void startTimer(long period) {

		if (timerStarted != 0)
			return;

		timerStarted = 1; /* only run a single timer */

		TimerTask backgroundWorker = new BackgroundWorker();

		if (period > fONCE_PER_Day || period < 0)
			period = fONCE_PER_Day;

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(backgroundWorker, 0, period);
	}

	/**
	 * Implements TimerTask's abstract run method.
	 */
	@Override
	public void run() {

		if (client==null || ds==null || mc==null)
		{
			return;
		}
		
		log.info("start scraping...");
		Query q = new Query("summoner");
		PreparedQuery pq = ds.prepare(q);
		for (Entity summoner : pq.asIterable(FetchOptions.Builder.withLimit(100).offset(0))) {
			String summonerName = (String)summoner.getProperty("summoner_name");
			String region = (String)summoner.getProperty("region");
			log.info("scraping latest history for "+summonerName+" "+region);
			HelperFunctions.getLatestSummonerMatchHistory(client, ds, mc, summonerName, HelperFunctions.getRegionFromString(region));
		}

		log.info("done scraping...");
		
		test_counter++;

	}

}
