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

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		DatastoreService ds = (DatastoreService)getServletContext().getAttribute("DataStore");
		MemcacheService mc = (MemcacheService)getServletContext().getAttribute("MemCache");
		RiotApi client = (RiotApi)getServletContext().getAttribute("RiotClient");
		
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
	}
}
