window.locationService = {
	execute: function(action, successCallback, errorCallback) {
		cordova.exec(    
			function(pos) {
				var errcode = pos.code;
				if (errcode == 0) {
					successCallback(pos);
				} else {
					if (typeof(errorCallback) != "undefined") {
						errorCallback(pos)
					};
				}
			}, 
			function(err){},
			"QQLocation",
			action,
			[]
		)
	},
	getCurrentPosition: function(successCallback, errorCallback) {
		this.execute("getCurrentPosition", successCallback, errorCallback);
	},
	stop: function(action, successCallback, errorCallback) {
		this.execute("stop", successCallback, errorCallback);
	}
}
module.exports = locationService;