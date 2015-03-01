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

import retrofit.RetrofitError;

public class RequestReceivedEvent<T> {
	private T response;
	private RetrofitError err;
	private boolean invalidError;

	protected RequestReceivedEvent(T response) {
		this.response = response;
	}

	protected RequestReceivedEvent(RetrofitError r) {
		this.err = r;
	}

	protected RequestReceivedEvent(boolean invalid) {
		this.invalidError = invalid;
	}

	public RetrofitError getErr() {
		return err;
	}

	public String getErrorMessage() {
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
			return "You need an internet connection to load data";
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
