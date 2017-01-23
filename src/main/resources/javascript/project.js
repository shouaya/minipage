var self = this;
redirect(page) {
	return function(e) {
		self.unmount(true);
		riot.mount(page, opts);
	}
}
