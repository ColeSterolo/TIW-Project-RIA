

(function() {

	var pageOrchestrator = new PageOrchestrator();

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {

			if (localStorage.getItem('lastAction') == null) {
				pageOrchestrator.start(1);
			} else if (localStorage.getItem("lastAction") == "creation") {
				pageOrchestrator.start(2);
			} else {
				pageOrchestrator.start(3);
			}

		}
	}, false);

	function PageOrchestrator() {

		this.start = function(mode) {
			console.log("orchestrator started");
			if (mode == 1) {

				//TODO
				//buyPage = newBuyPage();
				//buyPage.hide();

				//TODO 
				openAuctions = new OpenAuctions();
				openAuctions.update();

				//TODO
				closedAuctions = new ClosedAuctions();
				closedAuctions.update();

				//TODO
				//auctionForm = new AuctionForm();
				//auctionForm.show();

			}
		}
	}



	function OpenAuctions() {
		var auctionList = null;

		this.update = function() {
			console.log("\n update started")
			makeCall("GET", "GetOpenAuctions", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							console.log("request ajax succesful\n");
							auctionList = JSON.parse(req.responseText);
							console.log("number of auctions: " + auctionList.length);
						} else {
							//document.getElementById("openAuctions_message").textContent = message;
							auctionList = null;
						}

						if (auctionList != null && auctionList.length > 0) {
							console.log("open auctions != null");
							auctionList.forEach(function(auction) {

								var row = document.createElement("tr");
								var cell = document.createElement("td");
								cell.innerHTML = auction.item.name;
								row.appendChild(cell);

								cell = document.createElement("td");
								cell.innerHTML = auction.item.itemId;
								row.appendChild(cell);

								cell = document.createElement("td");
								cell.innerHTML = auction.item.description;
								row.appendChild(cell);

								cell = document.createElement("td");
								if (auction.remainingDays == 0 && auction.remainingHours == 0 && auction.remainingMinutes == 0) {
									cell.innerHTML = "No time remaining";
								}
								else {
									cell.innerHTML = auction.remainingDays + ' days ' + auction.remainingHours + ' hours ' + auction.remainingMinutes + ' minutes';
								}
								row.appendChild(cell);

								cell = document.createElement("td");
								var image = new Image();
								image.src = 'data:image/jpg;base64,' + auction.item.image;
								image.setAttribute("width", "200");
								cell.appendChild(image);
								row.appendChild(cell);

								cell = document.createElement("td");
								if (auction.maxOffer != -1) {
									cell.innerHTML = auction.maxOffer;
								} else {
									cell.innerHTML = "No offers";
								}
								row.appendChild(cell);
								document.getElementById("openAuctions_body").appendChild(row);

							})
						} else {
							console.log("no open auctions!!")
							document.getElementById("openAuctions_table").style.visibility = "hidden";
							document.getElementById("openAuctions_message").innerHTML = "You have no open auctions";
						}
					}
				}
			);
		}
	}



	function ClosedAuctions() {
		var auctionList = null;

		this.update = function() {
			//console.log("\n update started")
			makeCall("GET", "GetClosedAuctions", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							//console.log("request ajax succesful\n");
							auctionList = JSON.parse(req.responseText);
							//console.log("number of auctions: " + auctionList.length);
						} else {
							//document.getElementById("openAuctions_message").textContent = message;
							auctionList = null;
						}

						if (auctionList != null && auctionList.length > 0) {
							//console.log("open auctions != null");
							auctionList.forEach(function(auction) {

								var row = document.createElement("tr");
								var cell = document.createElement("td");
								cell.innerHTML = auction.item.name;
								row.appendChild(cell);

								cell = document.createElement("td");
								cell.innerHTML = auction.item.itemId;
								row.appendChild(cell);

								cell = document.createElement("td");
								cell.innerHTML = auction.item.description;
								row.appendChild(cell);

								cell = document.createElement("td");
								var image = new Image();
								image.src = 'data:image/jpg;base64,' + auction.item.image;
								image.setAttribute("width", "200");
								cell.appendChild(image);
								row.appendChild(cell);

								if (auction.winner != null) {
									cell = document.createElement("td");
									cell.innerHTML = auction.winner.username;
									row.appenChild(cell);

									cell = document.createElement("td");
									cell.innerHTML = auction.winner.address;
									row.appenChild(cell);

								} else {
									cell = document.createElement("td");
									cell.innerHTML = "No offers";
									row.appendChild(cell);

									cell = document.createElement("td");
									cell.innerHTML = "No offers";
									row.appendChild(cell);
								}

								document.getElementById("closedAuctions_body").appendChild(row);
							});

						} else {
							console.log("no closed auctions!!")
							document.getElementById("closedAuctions_table").style.visibility = "hidden";
							document.getElementById("openAuctions_message").innerHTML = "You have no closed auctions";
						}
					}
				});
		}
	}

	function AuctionForm() {

		this.activateForm = function() {
			
			var message = document.getElementById("sellForm_message");

			document.getElementById("createAuctionButton").addEventListener('click', () => {
				var form = document.getElementById("sellForm_form");
				if (form.checkValidity()) {
					makeCall("POST", 'CreateAuction', document.getElementById("sellForm_form"),
						function(req) {
							if (req.readyState == XMLHttpRequest.DONE) {
								var message = req.responseText;
								switch (req.status) {
									case 200:
										openAuctions.update();
										message.innerHTML = "Auction created succesfully";
										break;
									case 400: // bad request
										document.getElementById("errormessage").textContent = message;
										break;
									case 401: // unauthorized
										document.getElementById("errormessage").textContent = message;
										break;
									case 500: // server error
										document.getElementById("errormessage").textContent = message;
										break;
								}
							}
						}
					);
				} else {
					form.reportValidity();
				}
			});


		}

	}
	
	function SearchForm() {

		this.activateForm = function() {
			
			document.getElementById("searchButton").addEventListener('click', () => {
				var form = document.getElementById("searchForm_form");
				if (form.checkValidity()) {
					makeCall("GET", 'Search', document.getElementById("searchForm_form"),
						function(req) {
							if (req.readyState == XMLHttpRequest.DONE) {
								var message = req.responseText;
								switch (req.status) {
									case 200:
										openAuctions.update();
										message.innerHTML = "Auction created succesfully";
										break;
									case 400: // bad request
										document.getElementById("errormessage").textContent = message;
										break;
									case 401: // unauthorized
										document.getElementById("errormessage").textContent = message;
										break;
									case 500: // server error
										document.getElementById("errormessage").textContent = message;
										break;
								}
							}
						}
					);
				} else {
					form.reportValidity();
				}
			});


		}

	}
	
	function Search() {
		var auctionList = null;

		this.update = function() {
			console.log("\n update started")
			makeCall("GET", "Search", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							console.log("request ajax succesful\n");
							auctionList = JSON.parse(req.responseText);
							console.log("number of auctions: " + auctionList.length);
						} else {
							auctionList = null;
						}

						if (auctionList != null && auctionList.length > 0) {
							console.log("open auctions != null");
							auctionList.forEach(function(auction) {

								var row = document.createElement("tr");
								var cell = document.createElement("td");
								cell.innerHTML = auction.item.name;
								row.appendChild(cell);

								cell = document.createElement("td");
								cell.innerHTML = auction.item.itemId;
								row.appendChild(cell);

								cell = document.createElement("td");
								cell.innerHTML = auction.item.description;
								row.appendChild(cell);

								cell = document.createElement("td");
								if (auction.remainingDays == 0 && auction.remainingHours == 0 && auction.remainingMinutes == 0) {
									cell.innerHTML = "No time remaining";
								}
								else {
									cell.innerHTML = auction.remainingDays + ' days ' + auction.remainingHours + ' hours ' + auction.remainingMinutes + ' minutes';
								}
								row.appendChild(cell);

								cell = document.createElement("td");
								var image = new Image();
								image.src = 'data:image/jpg;base64,' + auction.item.image;
								image.setAttribute("width", "200");
								cell.appendChild(image);
								row.appendChild(cell);

								cell = document.createElement("td");
								if (auction.maxOffer != -1) {
									cell.innerHTML = auction.maxOffer;
								} else {
									cell.innerHTML = "No offers";
								}
								row.appendChild(cell);
								document.getElementById("openAuctions_body").appendChild(row);

							})
						} else {
							console.log("no open auctions!!")
							document.getElementById("openAuctions_table").style.visibility = "hidden";
							document.getElementById("openAuctions_message").innerHTML = "You have no open auctions";
						}
					}
				}
			);
		}
	}


}());