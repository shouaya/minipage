var self = this;
this.on('mount', function(){
    this.refresh();
});
refresh(e){
	$.ajax({
		url : '/line/reply?limit=10&offset=0',
		type : 'GET',
		dataType:"json",
		contentType : 'application/json',
		success : function(data, status) {
			console.log(data);
			opts.replyMessages = data;
			self.update();
		},
		error : function(xhr, type) {
			alert('error!')
		}
	});
}