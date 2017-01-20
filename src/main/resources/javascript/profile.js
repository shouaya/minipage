var self = this;
console.log(opts);
redirect(page) {
	return function(e) {
		self.unmount(true);
		riot.mount(page, opts);
	}
}