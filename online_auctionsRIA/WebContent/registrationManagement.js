
(function() {

	document.getElementById("registerbutton").addEventListener('click', () => {
		var form = document.getElementById("regform");
		if (form.checkValidity()) {
			makeCall("POST", 'RegisterUser', document.getElementById("regform"),
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						
						if (req.status == 200) {
							document.getElementById("regmessage").textContent = "registration correctly executed!";
						} else if(req.status == 400){
							document.getElementById("regmessage").textContent = "username already in use, please choose another one";
						}else {
							document.getElementById("regmessage").textContent = "error in the registration, please try again!";
						}
					}
				});
		}else{
			form.reportValidity();
			}
		}
	);

}());
