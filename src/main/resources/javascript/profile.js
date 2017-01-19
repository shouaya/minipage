$.getJSON('https://api.9jialu.com/profile/1', function(remoteData){
	self.unmount(true);
	riot.mount(page);
})
var self = this;
redirect(page) {
	return function(e) {
		self.unmount(true);
		riot.mount(page);
	}
}