var self = this;
this.user = opts;
redirect(page)
{
	return function(e) {
		self.unmount(true);
		riot.mount(page, self.user);
	}
}
this.mode = "read";
this.modeName = "編集";
edit(e)
{
	if (this.mode == "read") {
		this.mode = "edit";
		this.modeName = "保存";
		this.update();
	} else if (this.mode = "edit") {
		this.mode = "read";
		this.modeName = "編集";
		this.user.displayName = document.getElementById('in6').value;
		$.ajax({
			url : '/profile/edit',
			type : 'POST',
			dataType:"json",
			contentType : 'application/json',
			data : JSON.stringify(this.user),
			success : function(data, status) {
				console.log(data);
				self.user = data;
				self.update();
			},
			error : function(xhr, type) {
				alert('error!')
			}
		});
	}
}