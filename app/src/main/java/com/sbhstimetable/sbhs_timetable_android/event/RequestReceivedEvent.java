/*
 * SBHS-Timetable-Android: Countdown and timetable all at once (Android app).
 * Copyright (C) 2015 Simon Shields, James Ye
 *
 * This file is part of SBHS-Timetable-Android.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sbhstimetable.sbhs_timetable_android.event;

import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;

import retrofit.RetrofitError;

public class RequestReceivedEvent<T> {
	private T response;
	private RetrofitError err;
	private boolean invalidError;
	private String type;

	protected RequestReceivedEvent(T response, String type) {
		this.response = response;
		this.type = type;
	}

	protected RequestReceivedEvent(RetrofitError r, String type) {
		this.err = r;
		this.type = type;
	}

	protected RequestReceivedEvent(boolean invalid, String type) {
		this.invalidError = invalid;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public RetrofitError getErr() {
		return err;
	}

	public String getErrorMessage() {
		if (ApiWrapper.getApi() == null) {
			return "You need an internet connection to load data";
		}
		if (invalidError) {
			return "Invalid response from server";
		}

		if (err == null) {
			return "Success";
		}

		if (err.getKind() == RetrofitError.Kind.CONVERSION) {
			return "Error parsing response from server.";
		}

		if (err.getKind() == RetrofitError.Kind.HTTP) {
			if (err.getResponse().getStatus() == 502) {
				return "Server is down.";
			} else if (err.getResponse().getStatus() == 401) {
				return "Not logged in.";
			} else {
				return "Server error occurred.";
			}
		}

		if (err.getKind() == RetrofitError.Kind.NETWORK) {
			return "Connection to the server failed";
		}

		return "Unexpected error occurred.";
	}

	public T getResponse() {
		return response;
	}

	public boolean successful() {
		return response != null;
	}
}
