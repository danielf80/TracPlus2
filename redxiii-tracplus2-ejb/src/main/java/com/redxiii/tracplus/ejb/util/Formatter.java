package com.redxiii.tracplus.ejb.util;

import java.text.DecimalFormat;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class Formatter {

	private DecimalFormat dfTimeUnit;
	private DecimalFormat dfBytesUnit;
	
	@PostConstruct
	public void init() {
		dfTimeUnit = new DecimalFormat("00");
		dfBytesUnit = new DecimalFormat("0.00");
	}
	
	public String formatBytes(long bytes) {
		
		String[] powerOfByte = { "B", "KB", "MB", "GB", "TB", "?" };
		
		if (bytes == 0)
			return dfBytesUnit.format(bytes) + " " + powerOfByte[0];
		if (bytes < 0)
			return "-";
		
		
		double potBytes = bytes;
		int potency = 0;
		while (potBytes > 1024d && potency < (powerOfByte.length - 1)) {
			potency++;
			potBytes = potBytes / 1024d;
		}
		return dfBytesUnit.format(potBytes) + " " + powerOfByte[potency];
	}

	public String formatTimeInMillisToHour(long millis) {
		if (millis < 1)
			return "0";
		
		long seconds = millis/1000;
		
		int hour = 0;
		int minute = 0;
		
		if (seconds >= 60) {
			minute = (int)(seconds / 60);
			seconds -= (minute * 60); 
		}
		
		if (minute >= 60) {
			hour = (minute / 60);
			minute -= (hour * 60);
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(dfTimeUnit.format(hour));
		builder.append(":");
		builder.append(dfTimeUnit.format(minute));
		builder.append(":");
		builder.append(dfTimeUnit.format(seconds));
		
		return builder.toString();
	}
}
