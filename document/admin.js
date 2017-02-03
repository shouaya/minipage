var self = this;
redirect(page)
{
	return function(e) {
		self.unmount(true);
		window.page = riot.mount(page, opts);
	}
}