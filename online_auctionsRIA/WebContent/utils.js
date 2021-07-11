/**
 * AJAX call management
 */

function makeCall(method, url, formElement, cback, reset = true) {
	var req = new XMLHttpRequest(); // visible by closure
	req.onreadystatechange = function() {
		cback(req)
	}; // closure
	req.open(method, url);
	if (formElement == null) {
		req.send();
	} else {
		req.send(new FormData(formElement));
	}
	if (formElement !== null && reset === true) {
		formElement.reset();
	}
}

/**
 * AJAX call management
 */

function makeJsonPostCall(url, paramName, parameter, callback) {
	var req = new XMLHttpRequest(); // visible by closure
	req.onreadystatechange = function() {
		callback(req)
	}; // closure
	req.open("POST", url);
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	req.send(paramName + "=" + encodeURIComponent(JSON.stringify(parameter)));
}

/**
 * This function returns a String containing the time difference 
 * between the moments end and start in days, hours and minutes
 */

function timeDiffCalc(end, start) {
	let diffInMilliSeconds = Math.abs(end - start) / 1000;

	// calculate days
	const days = Math.floor(diffInMilliSeconds / 86400);
	diffInMilliSeconds -= days * 86400;

	// calculate hours
	const hours = Math.floor(diffInMilliSeconds / 3600) % 24;
	diffInMilliSeconds -= hours * 3600;

	// calculate minutes
	const minutes = Math.floor(diffInMilliSeconds / 60) % 60;
	diffInMilliSeconds -= minutes * 60;

	let difference = '';
	if (days > 0) {
		difference += (days === 1) ? `${days} day, ` : `${days} days, `;
	}

	difference += (hours === 0 || hours === 1) ? `${hours} hour, ` : `${hours} hours, `;

	difference += (minutes === 0 || hours === 1) ? `${minutes} minutes` : `${minutes} minutes`;

	return difference;

}

/**
 * Returns the cookie with the given name, or undefined if not found.
 */

function getCookie(cookieName) {
	var ret;
	document.cookie.split(';').forEach(function(el) {
		var [key, value] = el.split('=');
		if (key.trim() == cookieName) {
			ret = value;
		}
	})
	return ret;
}

/**
 * This function sets a cookie, giving it a name, a value and an expiration time
 */
function setCookie(cname, cvalue) {
	exdays = 30;
	const d = new Date();
	d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
	let expires = "expires=" + d.toUTCString();
	document.cookie = cname + "=" + cvalue + ";" + expires;
}

/**
 * This function returns a vector containing all the ids of the visited auctions
 */
function getVisitedAuctions() {
	var user = sessionStorage.getItem("username").replace(/(\r\n|\n|\r)/gm, "");
	let ids = [];
	document.cookie.split(';').forEach(function(el) {
		let [key, value] = el.split('=');
		key = key.trim(); //trim() removes white spaces
		if (key.includes(user) && key != user) {
			value = parseInt(value);
			ids.push(value);
		}
	})
	return ids;
}

function addVisitedAuction(auctionId) {
	var user = sessionStorage.getItem("username").replace(/(\r\n|\n|\r)/gm, "");
	var auction = user + auctionId;
	setCookie(auction, auctionId);
}

function setLastAction (lastAction) {
	var user = sessionStorage.getItem("username").replace(/(\r\n|\n|\r)/gm, "");
	setCookie(user, lastAction);
}

function getLastAction() {
	var user = sessionStorage.getItem("username").replace(/(\r\n|\n|\r)/gm, "");
	return getCookie(user);
}