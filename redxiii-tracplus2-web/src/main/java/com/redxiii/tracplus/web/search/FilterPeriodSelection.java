package com.redxiii.tracplus.web.search;

public enum FilterPeriodSelection {
	all_entries(0),
	last_week(7),
	last_month(30),
	last_quarter(90),
	last_halfyear(180),
	last_year(360),
	;
	int days;
	private FilterPeriodSelection(int days) {
		this.days = days;
	}
}
