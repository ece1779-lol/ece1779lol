package com.ece1779lol.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.users.*;

import net.enigmablade.riotapi.*;
import net.enigmablade.riotapi.constants.*;
import net.enigmablade.riotapi.exceptions.RiotApiException;
import net.enigmablade.riotapi.types.*;


@SuppressWarnings("serial")
public class UpdateSummoners extends HttpServlet {

	private static final Logger log = Logger.getLogger(AddSummoner.class.getName());

	public static List<String> logs = new ArrayList<String>();
	private int timesRan = 0;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		DatastoreService ds = (DatastoreService)getServletContext().getAttribute("DataStore");
		MemcacheService mc = (MemcacheService)getServletContext().getAttribute("MemCache");
		RiotApi client2 = (RiotApi)getServletContext().getAttribute("RiotClient2");
		
		if (client2==null || ds==null || mc==null)
		{
			return;
		}
		
		UpdateSummoners.logs.clear();
		
		timesRan++;
		
		log.info("start scraping "+timesRan+"...");
		logs.add("start scraping "+timesRan+"...");
		Query q = new Query("summoner");
		PreparedQuery pq = ds.prepare(q);
		for (Entity summoner : pq.asIterable(FetchOptions.Builder.withLimit(100).offset(0))) {
			String summonerName = (String)summoner.getProperty("summoner_name");
			String region = (String)summoner.getProperty("region");
			log.info("scraping latest history for "+summonerName+" "+region);
			logs.add("scraping latest history for "+summonerName+" "+region);
			HelperFunctions.getLatestSummonerMatchHistory(client2, ds, mc, summonerName, HelperFunctions.getRegionFromString(region));
		}

		logs.add("done scraping...");
		log.info("done scraping...");
	}
}

