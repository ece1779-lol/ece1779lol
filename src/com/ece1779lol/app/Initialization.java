package com.ece1779lol.app;

import java.util.logging.*;

import javax.servlet.http.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.*;

import net.enigmablade.riotapi.RiotApi;

@SuppressWarnings("serial")
public class Initialization extends HttpServlet {
    public void init(ServletConfig config) {
    	try {
		    //Initialize RIOT API
    		ServletContext context = config.getServletContext();
    		
    		String apiKey = config.getInitParameter("RiotKey");
    		String apiKey2 = config.getInitParameter("RiotKey2");
    		int LimitPer10Sec = Integer.parseInt(config.getInitParameter("LimitPer10Sec"));
    		int LimitPer10Min = Integer.parseInt(config.getInitParameter("LimitPer10Min"));
    		
    		RiotApi client = new RiotApi(apiKey, null, LimitPer10Sec, LimitPer10Min);
    		context.setAttribute("RiotClient", client);
    		
    		RiotApi client2 = new RiotApi(apiKey2, null, LimitPer10Sec, LimitPer10Min);
    		context.setAttribute("RiotClient2", client2);
    		
    		MemcacheService mc = MemcacheServiceFactory.getMemcacheService();
    		mc.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    	    context.setAttribute("MemCache", mc);
    	    
    	    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    	    context.setAttribute("DataStore", ds);

		}
		catch (Exception ex) {
		    getServletContext().log("SQLGatewayPool Error: " + ex.getMessage());
		}
    }
}
