package com.ece1779lol.app;

import java.io.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;


public class BackgroundWorker extends TimerTask {
	
	//public
	public static String accessKey;
	public static String secretKey;
	
	public static int test_counter;
	
	//PRIVATE
	private static int timerStarted;
	private final static long fONCE_PER_Day = 1000*60*60*24; //in milliseconds
	

	/** Construct and use a TimerTask and Timer. */
	public static void startTimer (long period) {
		
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
  @Override public void run(){
	  
	  test_counter++;

  }

}

