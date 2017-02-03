var self = this;
$.ajax({
	url : '/line/message?limit=10&offset=0',
	type : 'GET',
	dataType:"json",
	contentType : 'application/json',
	success : function(data, status) {
		console.log(data);
		opts.messages = data;
		self.update();
	},
	error : function(xhr, type) {
		alert('error!')
	}
});