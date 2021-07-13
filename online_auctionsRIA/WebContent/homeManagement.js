(function() {

	var pageOrchestrator = new PageOrchestrator();
	var openAuctions, closedAuctions, auctionForm;
	var searchAuctions, offersDetails, visitedAuctions, winningOffers;
	var sellPage;

	document.getElementById("goToBuyPageButton").addEventListener('click', (e) => {
		e.preventDefault();
		document.getElementById("winningOffers_error_message").innerHTML = "";
		document.getElementById("postOffer_form_message").innerHTML = "";
		document.getElementById("searchForm_message").innerHTML = "";
		pageOrchestrator.start(2);
	});

	document.getElementById("goToSellPageButton").addEventListener('click', (e) => {
		e.preventDefault();
		document.getElementById("sellForm_message").innerHTML = "";
		pageOrchestrator.start(1);
	});

	document.getElementById("logoutButton").addEventListener('click', (e) => {
		window.sessionStorage.removeItem('username');
	});

	window.addEventListener("load", () => {
		user = sessionStorage.getItem("username");
		if (user == null) {
			window.location.href = "index.html";
		} else {
			lastAction = getLastAction();
			if (lastAction == "creation") {
				// delete last action cookie
				cookieString = user.trim() + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
				document.cookie = cookieString;
				pageOrchestrator.start(1);
			} else {
				// if last action != createAuction
				pageOrchestrator.start(2);
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
		winningOffers = new WinningOffers();

		this.start = function(mode) {

			if (mode == 1) {
				// sell page mode
				
				openAuctions.update();
				closedAuctions.update();
				auctionForm.activateForm();

				auctionDetails.clear();
				auctionDetails.hide();

				sellPage.show();
				buyPage.hide();

			}
			else if (mode == 2) {
				// buy page mode
				
				sellPage.hide();
				buyPage.show();

				searchAuctions.activateSearch();

				offersDetails.update();

				visitedAuctions.update();
				winningOffers.show();

			}
		}

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
			document.getElementById("openAuctionDetails_div").style.display = "none";
			document.getElementById("sellForm_message").innerHTML = "";
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
								document.getElementById("openAuctions_table").style.display = "block";

							});
						} else {
							document.getElementById("openAuctions_table").style.display = "none";
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

			makeCall("GET", "GetAuctionDetails?auctionId=" + auction.auction.auctionId, null,
				function(req) {
					if (req.readyState == 4) {
						var message = req.responseText;
						if (req.status == 200) {
							offers = JSON.parse(req.responseText);
							document.getElementById("openAuctionDetails_message").innerHTML = "";
							document.getElementById("openAuctionDetails_body").innerHTML = "";
							if (isOpen) {
								if (document.getElementById("openAuctionDetails_div").style.display == "none") {
									document.getElementById("openAuctionDetails_div").style.display = "block";
								}
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

									});
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
								document.getElementById("closedAuctionDetails").style.display = "block";
							}


						} else {
							document.getElementById("openAuctionDetails_message").innerHTML = message;

						}
					}
				}
			);
		}

		this.hide = function() {
			document.getElementById("openAuctionDetails_div").style.display = "none";
			document.getElementById("openAuctionDetails_table").style.display = "none";
		}

		this.clear = function() {

			document.getElementById("auctionDetails_h2").innerHTML = "";
			document.getElementById("openAuctionDetails_message").innerHTML = "";
			document.getElementById("openAuctionDetails_body").innerHTML = "";
			document.getElementById("closedAuctionDetails").innerHTML = "";
		}

	}

	function CloseAuction() {

		this.close = function(auctionId) {
			document.getElementById("closedAuctions_message").textContent = "";
			makeCall("GET", "CloseAuction?auctionId=" + auctionId, null,
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;
						switch (req.status) {
							case 200:
								openAuctions.update();
								closedAuctions.update();
								document.getElementById("closedAuctions_message").innerHTML = "Auction closed succesfully";
								break;
							case 400: // bad request
								document.getElementById("closedAuctions_message").textContent = message;
								break;
							case 502: // server error
								document.getElementById("closedAuctions_message").textContent = message;
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
							auctionList = JSON.parse(req.responseText);;
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

								document.getElementById("closedAuctions_tr").style.display = "block";
								document.getElementById("closedAuctions_table").style.display = "block";
							});

						} else {
							document.getElementById("closedAuctions_tr").style.display = "none";
							document.getElementById("closedAuctions_table").style.display = "none";
							document.getElementById("closedAuctions_message").innerHTML = "You have no closed auctions";
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

			document.getElementById("sellForm_message").textContent = "";
			document.getElementById("createAuctionButton").addEventListener('click', (e) => {
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
										document.getElementById("sellForm_message").textContent = "Auction created succesfully";
										setLastAction("creation");
										break;
									case 400: // bad request
										document.getElementById("sellForm_message").textContent = message;
										break;
									case 401: // unauthorized
										document.getElementById("sellForm_message").textContent = message;
										break;
									case 500: // server error
										document.getElementById("sellForm_message").textContent = message;
										break;
									case 502:
										document.getElementById("sellForm_message").textContent = message;
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

	function WinningOffers() {
		var self = this;

		this.show = function() {
			makeCall("GET", "GetWinningOffers", null,
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;
						switch (req.status) {
							case 200:
								self.update(JSON.parse(req.responseText));
								break;
							case 400: // bad request
								document.getElementById("winningOffers_error_message").textContent = message;
								break;
							case 401: // unauthorized
								document.getElementById("winningOffers_error_message").textContent = message;
								break;
							case 500: // server error
								document.getElementById("winningOffers_error_message").textContent = message;
								break;
						}
					}
				}
			);
		}

		this.update = function(winningOffers) {
			document.getElementById("winningOffers_body").innerHTML = "";
			if (winningOffers != null) {
				document.getElementById("winningOffers_error_message").style.display = "none";
				winningOffers.forEach(function(winningOffer) {
					row = document.createElement("tr");

					// write offer id
					cell = document.createElement("td");
					cell.innerHTML = winningOffer.offer.offerId;
					row.appendChild(cell);

					// write final amount
					cell = document.createElement("td");
					cell.innerHTML = winningOffer.offer.amount;
					row.appendChild(cell);

					// write auction id
					cell = document.createElement("td");
					cell.innerHTML = winningOffer.offer.auction;
					row.appendChild(cell);

					// write item name
					cell = document.createElement("td");
					cell.innerHTML = winningOffer.item.name;
					row.appendChild(cell);

					// write item description
					cell = document.createElement("td");
					cell.innerHTML = winningOffer.item.description;
					row.appendChild(cell);

					// print image
					cell = document.createElement("td");
					var image = new Image();
					image.src = 'data:image/jpg;base64,' + winningOffer.item.image;
					image.setAttribute("width", "200");
					cell.appendChild(image);
					row.appendChild(cell);

					document.getElementById("winningOffers_body").appendChild(row);
					document.getElementById("winning_offers").style.display = "block";
				})

			} else {
				document.getElementById("winning_offers").style.display = "none";
				document.getElementById("winningOffers_error_message").textContent = "No winning offers so far";
			}
		}

	}

	function SearchAuctions() {
		var form = document.getElementById("searchForm_form");
		var self = this;

		this.activateSearch = function() {
			var rowCount = document.getElementById("searchResults_body").rows.length;
			if (rowCount == 0) {
				document.getElementById("search_results").style.display = "none";
			}

			document.getElementById("searchButton").addEventListener('click', (e) => {
				e.preventDefault();

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
										document.getElementById("searchForm_message").textContent = message;
										break;
									case 500: // server error
										document.getElementById("searchForm_message").textContent = message;
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
			document.getElementById("searchResults_body").innerHTML = "";
			document.getElementById("searchForm_message").innerHTML = "";
			if (searchResults.length > 0) {
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
					// write initial price
					cell = document.createElement("td");
					startingPrice = auction.initialPrice;
					cell.innerHTML = startingPrice;
					row.appendChild(cell);
					// write minimum bid
					cell = document.createElement("td");
					minBid = auction.minimumBid;
					cell.innerHTML = minBid;
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
					document.getElementById("search_results").style.display = "block";
				})

			} else {
				document.getElementById("searchForm_message").textContent = "No results were found";
				document.getElementById("search_results").style.display = "none";
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
						switch (req.status) {
							case 200:
								var response = JSON.parse(message);
								self.updateItems(response[0]);
								self.updateOffers(response[1]);
								self.activateOfferForm(auctionId);
								document.getElementById("offer_page").style.display = "block";
								document.getElementById("offerPage_error_message").style.display = "none";
								break;
							case 400:
								document.getElementById("offerPage_error_message").textContent = message;
								document.getElementById("offerPage_error_message").style.display = "block";
								break;
							case 404:
								document.getElementById("offerPage_error_message").textContent = message;
								document.getElementById("offerPage_error_message").style.display = "block";
								break;
							case 502:
								document.getElementById("offerPage_error_message").textContent = message;
								document.getElementById("offerPage_error_message").style.display = "block";
								break;
						}
					}
				}
			);
			addVisitedAuction(auctionId);
		}

		this.update = function() {
			if (auction != null) {
				self.show(auction);
			} else {
				this.hide();
			}
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
										document.getElementById("postOffer_form_message").innerHTML = "";
										break;
									case 400: // bad request
										document.getElementById("postOffer_form_message").textContent = message;
										document.getElementById("postOffer_form_message").style.display = "block";
										break;
									case 403: // forbidden
										document.getElementById("postOffer_form_message").textContent = message;
										document.getElementById("postOffer_form_message").style.display = "block";
										break;
									case 500: // server error
										document.getElementById("postOffer_form_message").textContent = message;
										document.getElementById("postOffer_form_message").style.display = "block";
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

			if (offers != null && offers.length > 0) {
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
					document.getElementById("offers_div").style.display = "block";
				})

			} else {
				document.getElementById("offers_div").style.display = "none";
				document.getElementById("postOffer_form_message").textContent = "No offers for this auction yet";
				document.getElementById("postOffer_form_message").style.display = "block";
			}


		}

		this.hide = function() {
			document.getElementById("offer_page").style.display = "none";
			document.getElementById("postOffer_form_message").style.display = "none";
			document.getElementById("offerPage_error_message").style.display = "none";
		}

	}

	function VisitedAuctions() {
		var self = this;

		this.update = function() {
			ids = getVisitedAuctions();
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
					document.getElementById("visited_auctions").style.display = "block";
				})

			}

		}


	}

}());