

(function() {

	var pageOrchestrator = new PageOrchestrator();
	var openAuctions, closedAuctions, auctionForm;
	var searchAuctions, offersDetails, visitedAuctions;
	var sellPage;

	document.getElementById("goToBuyPageButton").addEventListener('click', (e) => {
		e.preventDefault();
		pageOrchestrator.start(2);
	});

	document.getElementById("goToSellPageButton").addEventListener('click', (e) => {
		e.preventDefault();
		pageOrchestrator.start(1);
	});

	window.addEventListener("load", () => {

		if (sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {

			if (getCookie('lastAction') == null) {
				pageOrchestrator.start(2);
			} else if (getCookie("lastAction") == "creation") {
				pageOrchestrator.start(2);
			} else {
				pageOrchestrator.start(3);
			}

		}
	}, false);

	function PageOrchestrator() {

		buyPage = new BuyPage();
		sellPage = new SellPage();
		openAuctions = new OpenAuctions();
		auctionDetails = new AuctionDetails();
		closedAuctions = new ClosedAuctions();
		auctionForm = new AuctionForm();
		searchAuctions = new SearchAuctions();
		offersDetails = new OffersDetails();
		visitedAuctions = new VisitedAuctions();

		this.start = function(mode) {
			console.log("orchestrator started");

			if (mode == 1) { //temporary, to be changed

				openAuctions.update();
				closedAuctions.update();
				auctionForm.activateForm();

				auctionDetails.clear();
				auctionDetails.hide();

				sellPage.show();
				buyPage.hide();

			}
			else if (mode == 2) {

				searchAuctions.activateSearch();

				offersDetails.hide();

				visitedAuctions.update();

				buyPage.show();
				sellPage.hide();
			}
		}

		/*		this.refresh = function(mode) {
					if (mode == 1) {
		
						openAuctions.update();
						closedAuctions.update();
						auctionForm.activateForm();
		
						sellPage.show();
						buyPage.hide();
		
					} else {
		
						searchAuctions.activateSearch();
						offersDetails.hide();
		
						buyPage.show();
						sellPage.hide();
					}
				}*/
	}


	function SellPage() {

		this.hide = function() {
			var i = 0;
			var divs = document.getElementsByClassName("sellPage");
			for (i = 0; i < divs.length; i++) {
				divs[i].style.display = "none";
			}
		}


		this.show = function() {
			var divs = document.getElementsByClassName("sellPage");
			for (i = 0; i < divs.length; i++) {
				divs[i].style.display = "block";
			}
		}

	}

	function BuyPage() {

		this.hide = function() {
			var i = 0;
			var divs = document.getElementsByClassName("buyPage");
			for (i = 0; i < divs.length; i++) {
				divs[i].style.display = "none";
			}
		}

		this.show = function() {
			var divs = document.getElementsByClassName("buyPage");
			for (i = 0; i < divs.length; i++) {
				divs[i].style.display = "block";
			}
		}

	}

	function OpenAuctions() {
		var auctionList = null;

		this.update = function() {

			document.getElementById("openAuctions_message").innerHTML = "";
			var body = document.getElementById("openAuctions_body");

			while (body.firstChild) {
				body.removeChild(body.firstChild);
			}

			makeCall("GET", "GetOpenAuctions", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {

							auctionList = JSON.parse(req.responseText);
						} else {
							document.getElementById("openAuctions_message").textContent = message;
							auctionList = null;
						}

						if (auctionList != null && auctionList.length > 0) {

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

								cell = document.createElement("td");
								var button = document.createElement("button");
								button.innerHTML = "show details";

								button.addEventListener('click', (e) => {
									e.preventDefault();
									var auctionDetails = new AuctionDetails();
									auctionDetails.showDetails(auction, 1);
								});

								var att = document.createAttribute("auctionId");
								att.value = auction.auction.auctionId;
								button.setAttributeNode(att);
								cell.appendChild(button);
								row.appendChild(cell);

								document.getElementById("openAuctions_body").appendChild(row);

							});
						} else {
							console.log("no open auctions!!")
							document.getElementById("openAuctions_table").style.visibility = "hidden";
							document.getElementById("openAuctions_message").innerHTML = "You have no open auctions";
						}
					}
				}
			);
		}

		this.hide = function() {
			document.getElementById("openAuctions_div").style.diplay = "none";
		}
	}

	function AuctionDetails() {

		var offers = null;

		this.showDetails = function(auction, isOpen) {
			makeCall("GET", "GetOpenAuctionDetails?auctionId=" + auction.auction.auctionId, null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							offers = JSON.parse(req.responseText);
							document.getElementById("openAuctionDetails_body").innerHTML = "";
							document.getElementById("openAuctionDetails_message").innerHTML = "";
							if (isOpen) {
								document.getElementById("auctionDetails_h2").innerHTML = "Auction " +
									auction.auction.auctionId + " details: offers";
								document.getElementById("openAuctionDetails_div").style.display = "block";
								document.getElementById("closedAuctionDetails").innerHTML = "";

								if (offers != null && offers.length > 0) {
									document.getElementById("openAuctionDetails_table").style.display = "block";
									offers.forEach(function(offer) {

										var row = document.createElement("tr");
										var cell = document.createElement("td");
										cell.innerHTML = offer.offer.amount;
										row.appendChild(cell);

										cell = document.createElement("td");
										cell.innerHTML = offer.user.username;
										row.appendChild(cell);


										cell = document.createElement("td");
										if (auction.remainingDays == 0 && auction.remainingHours == 0 && auction.remainingMinutes == 0) {
											var button = document.createElement("button");
											button.innerHTML = "Close auction";
											button.addEventListener('click', (e) => {
												e.preventDefault();
												var closeAuction = new CloseAuction();
												closeAuction.close(auction.auction.auctionId)
											});
											cell.appendChild(button);
											row.appendChild(cell);

										}

										document.getElementById("openAuctionDetails_body").appendChild(row);

									})
								} else {
									document.getElementById("openAuctionDetails_table").style.display = "none";
									openAuctionDetails_message.innerHTML = "No offers";

									if (auction.remainingDays == 0 && auction.remainingHours == 0 && auction.remainingMinutes == 0) {
										var button = document.createElement("button");
										button.innerHTML = "Close auction";
										button.addEventListener('click', (e) => {
											e.preventDefault();
											var closeAuction = new CloseAuction();
											closeAuction.close(auction.auction.auctionId)
										});
										document.getElementById("openAuctionDetails_div").appendChild(button);
									}
								}

							} else {
								document.getElementById("openAuctionDetails_div").style.display = "none";

								if (offers != null && offers.length > 0) {
									var offer = offers[0];

									document.getElementById("closedAuctionDetails").innerHTML = "The winner of the auction was " +
										offer.user.username + "who offered " + offer.offer.amount + "\n" +
										"The address of the winner is " + offer.user.address;

								} else {
									document.getElementById("closedAuctionDetails").innerHTML = "This auction had no offers";
								}
							}


						} else {
							document.getElementById("auctionDetails_h2").innerHTML = message;

						}
					}
				}
			);
		}

		this.hide = function() {
			document.getElementById("openAuctionDetails_div").style.diplay = "none";
		}
		
		this.clear = function(){
			document.getElementById("openAuctionDetails_div").innerHTML = "";
			document.getElementById("closedAuctionDetails").innerHTML = "";
		}

	}

	function CloseAuction() {

		this.close = function(auctionId) {
			makeCall("GET", "CloseAuction?auctionId=" + auctionId, null,
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;
						switch (req.status) {
							case 200:
								openAuctions.update();
								closedAuctions.update();
								message.innerHTML = "Auction closed succesfully";
								break;
							case 400: // bad request
								document.getElementById("errormessage").textContent = message;
								break;
							case 502: // server error
								document.getElementById("errormessage").textContent = message;
								break;
						}
					}
				});
		}

	}


	function ClosedAuctions() {
		var auctionList = null;

		this.update = function() {

			document.getElementById("closedAuctions_message").innerHTML = "";
			document.getElementById("closedAuctions_body").innerHTML = "";

			makeCall("GET", "GetClosedAuctions", null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							console.log("succesful search\n");
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

								cell = document.createElement("td");
								var button = document.createElement("button");
								button.innerHTML = "show details";

								button.addEventListener('click', (e) => {
									e.preventDefault();
									var auctionDetails = new AuctionDetails();
									auctionDetails.showDetails(auction, 0);
								});

								var att = document.createAttribute("auctionId");
								att.value = auction.auction.auctionId;
								button.setAttributeNode(att);
								cell.appendChild(button);
								row.appendChild(cell);

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

		this.hide = function() {
			document.getElementById("closedAuctions_div").style.diplay = "none";
		}

	}

	function AuctionForm() {

		this.activateForm = function() {

			var message = document.getElementById("sellForm_message");

			document.getElementById("createAuctionButton").addEventListener('click', () => {
				e.preventDefault();
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
			}, false);
		}

		this.hide = function() {
			document.getElementById("sellForm_div").style.diplay = "none";
		}
	}

	function SearchAuctions() {

		this.activateSearch = function() {
			document.getElementById("searchButton").addEventListener('click', (e) => {
				e.preventDefault();
				var form = document.getElementById("searchForm_form");
				var self = this;
				document.getElementById("searchResults_body").innerHTML = "";

				if (form.checkValidity()) {
					makeCall("GET", 'Search?keyword=' + document.getElementById('user_input').value, null,
						function(req) {
							if (req.readyState == XMLHttpRequest.DONE) {
								var message = req.responseText;
								switch (req.status) {
									case 200:
										self.update(JSON.parse(req.responseText));
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

		this.update = function(searchResults) {
			var row, cell, anchor, anchorText;
			if (searchResults != null) {
				searchResults.forEach(function(auction) {
					row = document.createElement("tr");
					// write auction id link
					cell = document.createElement("td");
					anchor = document.createElement("a");
					anchorText = document.createTextNode("Auction" + auction.auctionId);
					anchor.appendChild(anchorText);
					anchor.setAttribute('auctionid', auction.auctionId);
					anchor.addEventListener("click", (e) => {
						e.preventDefault();
						offersDetails.show(auction.auctionId);
					})
					anchor.href = "#";
					cell.appendChild(anchor);
					row.appendChild(cell);
					// write auction ending time
					cell = document.createElement("td");
					endingTime = moment.utc(auction.endingTime.seconds * 1000);
					formattedEndingTime = endingTime.format('YYYY/MM/DD HH:mm');
					cell.innerHTML = formattedEndingTime;
					row.appendChild(cell);
					// compute and write remaining days and hours
					cell = document.createElement("td");
					cell.innerHTML = timeDiffCalc(moment(formattedEndingTime), moment());
					row.appendChild(cell);

					document.getElementById("searchResults_body").appendChild(row);
					document.getElementById("searchResults_table").style.visibility = "visible";
				})

			} else {
				console.log("No auctions found")
				document.getElementById("searchResults_table").style.visibility = "hidden";
				document.getElementById("searchResults_message").innerHTML = "No auctions found";
			}
		}

	}

	function OffersDetails() {
		var self = this;
		var auction;

		this.show = function(auctionId) {
			auction = auctionId;
			makeCall("GET", "ShowOffers?auction=" + auctionId, null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							var response = JSON.parse(message);
							self.updateItems(response[0]);
							self.updateOffers(response[1]);
							self.activateOfferForm(auctionId);
							document.getElementById("offer_page").display = "block";
						}
					}
				}
			);
			addVisitedAuction(auctionId);
		}

		this.update = function() {
			self.show(auction);
		}

		this.activateOfferForm = function(auctionId) {
			document.getElementById("postOffer_auction").value = auctionId;
			document.getElementById("postOffer_button").addEventListener('click', (e) => {
				e.preventDefault();
				var form = document.getElementById("postOffer_form");
				if (form.checkValidity()) {
					makeCall("POST", 'PostOffer', document.getElementById("postOffer_form"),
						function(req) {
							if (req.readyState == XMLHttpRequest.DONE) {
								var message = req.responseText;
								switch (req.status) {
									case 200:
										self.update();
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
			}, false);
		}

		this.updateItems = function(items) {
			document.getElementById("items_body").innerHTML = "";
			// show items
			items.forEach(function(item) {
				row = document.createElement("tr");
				// write auction ending time
				cell = document.createElement("td");
				cell.innerHTML = item.itemId;
				row.appendChild(cell);
				// 
				cell = document.createElement("td");
				cell.innerHTML = item.name;
				row.appendChild(cell);

				cell = document.createElement("td");
				cell.innerHTML = item.description;
				row.appendChild(cell);

				cell = document.createElement("td");
				var image = new Image();
				image.src = 'data:image/jpg;base64,' + item.image;
				image.setAttribute("width", "200");
				cell.appendChild(image);
				row.appendChild(cell);

				document.getElementById("items_body").appendChild(row);
			})
		}

		this.updateOffers = function(offers) {
			document.getElementById("offers_body").innerHTML = "";

			//show offers
			offers.forEach(function(offer) {
				row = document.createElement("tr");
				// write auction ending time
				cell = document.createElement("td");
				cell.innerHTML = offer.offerId;
				row.appendChild(cell);
				// 
				cell = document.createElement("td");
				cell.innerHTML = offer.bidder;
				row.appendChild(cell);
				//
				cell = document.createElement("td");
				cell.innerHTML = offer.amount;
				row.appendChild(cell);
				//
				cell = document.createElement("td");
				offerTime = moment.utc(offer.datetime.seconds * 1000);
				formattedOfferTime = offerTime.format('YYYY/MM/DD HH:mm');
				cell.innerHTML = formattedOfferTime;
				row.appendChild(cell);

				document.getElementById("offers_body").appendChild(row);
			})
		}

		this.hide = function() {
			document.getElementById("offer_page").style.display = "none";
		}

	}

	function VisitedAuctions() {
		var self = this;

		this.update = function() {
			ids = getVisitedAuctions();
			parameter = {
				auctionIds: ids
			}
			if (ids.length > 0) {
				makeJsonPostCall("GetAuctionsById", "auctionIds", ids,
					function(req) {
						if (req.readyState == 4) {
							var message = req.responseText;
							if (req.status == 200) {
								var response = JSON.parse(message);
								self.show(response);
							}
						}
					}
				);
			}
		}

		this.show = function(searchResults) {
			document.getElementById("visitedAuctions_body").innerHTML = "";
			var row, cell, anchor, anchorText;
			if (searchResults != null) {
				searchResults.forEach(function(auction) {
					row = document.createElement("tr");
					// write auction id link
					cell = document.createElement("td");
					anchor = document.createElement("a");
					anchorText = document.createTextNode("Auction" + auction.auctionId);
					anchor.appendChild(anchorText);
					anchor.setAttribute('auctionid', auction.auctionId);
					anchor.addEventListener("click", (e) => {
						e.preventDefault();
						offersDetails.show(auction.auctionId);
					})
					anchor.href = "#";
					cell.appendChild(anchor);
					row.appendChild(cell);
					// write auction ending time
					cell = document.createElement("td");
					endingTime = moment.utc(auction.endingTime.seconds * 1000);
					formattedEndingTime = endingTime.format('YYYY/MM/DD HH:mm');
					cell.innerHTML = formattedEndingTime;
					row.appendChild(cell);
					// compute and write remaining days and hours
					cell = document.createElement("td");
					cell.innerHTML = timeDiffCalc(moment(formattedEndingTime), moment());
					row.appendChild(cell);

					document.getElementById("visitedAuctions_body").appendChild(row);
					document.getElementById("visitedAuctions_table").style.visibility = "visible";
				})

			} else {
				console.log("No auctions found")
				document.getElementById("visitedAuctions_table").style.visibility = "hidden";
			}
		}
	}

}());