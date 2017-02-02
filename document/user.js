var self = this;
$.ajax({
	url : '/line/user?limit=10&offset=0',
	type : 'GET',
	dataType:"json",
	contentType : 'application/json',
	success : function(data, status) {
		console.log(data);
		opts.users = data;
		self.update();
	},
	error : function(xhr, type) {
		alert('error!')
	}
});
redirect(page)
{
	return function(e) {
		self.unmount(true);
		riot.mount(page, opts);
	}
}