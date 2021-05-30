

(function() {

	window.addEventListener("load", () => {
		if (sessionStorage.getItem("username") == null) {
			window.location.href = "index.html";
		} else {

			if (localStorage.getItem("lastAction") == null) {
				pageOrchestrator.start(1);
			} else if (localStorage.getItem("lastAction") == "creation") {
				pageOrchestrator.start(2);
			} else {
				pageOrchestrator.start(3);
			}

		}
	}, false);

	

}());