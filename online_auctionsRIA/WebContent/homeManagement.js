

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
				//closedAuctions = new ClosedAuctions();
				//closedAuctions.show();

				//TODO
				//auctionForm = new AuctionForm();
				//auctionForm.show();

			}
		}
	}


	function OpenAuctions() {
		var auctionList = null;
		var ul = document.getElementById("openAuctions");

		this.show = function() {
			this.update();
			if (auctionList.lenght > 0) {
				var li = document.createElement("li");
			}
		}

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

						}
					} else {
						document.getElementById("openAuctions").textContent = message;
					}

					if (auctionList != null && auctionList.length > 0) {
						console.log("open auctions != null")
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
							if(auction.remainingDays == 0 && auction.remainingHours == 0 && auction.remainingMinutes == 0){
								cell.innerHTML = "No time remaining";
							}
							else{
								cell.innerHTML = auction.remainingDays + ' days ' + auction.remainingHours + ' hours ' + auction.remainingMinutes + ' minutes';
							}
							row.appendChild(cell);
							
							cell = document.createElement("td");
							var image = new Image();
							image.source ='data:image/png;base64,' + auction.item.image;
							cell.appendChild(image);
							row.appendChild(cell);
							
							cell = document.createElement("td");
							if(auction.maxOffer != -1){
								cell.innerHTML = auction.maxOffer;
							}else{
								cell.innerHTML = "No offers";
							}
							row.appendChild(cell);
							document.getElementById("openAuctions_body").appendChild(row);
						})
					}else{
						document.getElementById("openAuctions_table").style.visibility = "hidden";
						document.getElementById("openAuctions_message").innerHTML = "You have no open auctions";
					}
				}
			);


		}

	}


}());