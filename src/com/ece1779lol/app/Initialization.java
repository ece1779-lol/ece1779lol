package com.ece1779lol.app;

import javax.servlet.http.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import net.enigmablade.riotapi.RiotApi;

@SuppressWarnings("serial")
public class Initialization extends HttpServlet {
    public void init(ServletConfig config) {
    	try {
		    //Initialize RIOT api
  			
    		ServletContext context = config.getServletContext();
    		
    		String apiKey = config.getInitParameter("RiotKey");
    		int LimitPer10Sec = Integer.parseInt(config.getInitParameter("LimitPer10Sec"));
    		int LimitPer10Min = Integer.parseInt(config.getInitParameter("LimitPer10Min"));
    		
    		RiotApi client = new RiotApi(apiKey, null, LimitPer10Sec, LimitPer10Min);
    		
    		context.setAttribute("RiotClient", client);
		}
		catch (Exception ex) {
		    getServletContext().log("SQLGatewayPool Error: " + ex.getMessage());
		}
    }
}
