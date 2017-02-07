var self = this;
this.page = 1;
this.on('mount', function(){
    this.refresh();
});
refresh(e){
	$('#loadingToast').fadeIn(100);
	$.ajax({
		url : '/line/reply?limit=5&offset=' + (this.page-1)*5,
		type : 'GET',
		dataType:"json",
		contentType : 'application/json',
		success : function(data, status) {
			console.log(data);
			opts.replyMessages = data;
			self.update();
		},
		complete : function(xhr, status){
			$('#loadingToast').fadeOut(100);
		},
		error : function(xhr, type) {
			alert('error!')
		}
	});
}