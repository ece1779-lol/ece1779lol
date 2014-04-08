package com.ece1779lol.app;

import java.io.PrintWriter;

import net.enigmablade.riotapi.constants.Region;

public class HelperFunctions {

	public HelperFunctions ()
	{
		
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
		out.println("<img height=100 src ='/css/lol_logo.png'>");
		out.println("<b>ECE1779 LOL Tracker</b>");
		out.println("</div>");
	}
	
	public Region getRegionFromString(String region)
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
	
	public String getStringFromRegion(String region)
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
