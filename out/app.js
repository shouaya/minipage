(function($)
{  
	var cssEl = document.createElement('style');
	document.documentElement.firstElementChild.appendChild(cssEl);
	function setPxPerRem(){
		var dpr = 1;
		var pxPerRem = document.documentElement.clientWidth * dpr / 20;
		cssEl.innerHTML = 'html{font-size:' + pxPerRem + 'px!important;}';
	}
	setPxPerRem();
	$(window).resize(function() {
		setPxPerRem();
	});
	$.extend($.fn,
	{
		fadeIn: function(ms)
		{
			if(typeof(ms) === 'undefined') ms = 500;
 
			$(this).css(
			{
				display: 'block',
				opacity: 0
			}).animate({
				opacity: 1
			}, ms);
			 
			return this;
		},
		 
		fadeOut: function(ms)
		{
			if(typeof(ms) === 'undefined') ms = 500;
 
			$(this).css(
			{
				opacity: 1
			}).animate({
				opacity: 0
			}, ms, 'linear', function()
			{
				$(this).css('display', 'none');
			});
			 
			return this;
		}
	})
})(Zepto)
function returnHome(){
	if(window.page[0]){
		var opts = window.page[0].opts;
		window.page[0].unmount(true);
		riot.mount('admin', opts);
	}
}
function ajaxget(url, success){
	$('#loadingToast').fadeIn(100);
	$.ajax({
		url : url,
		type : 'GET',
		dataType:"json",
		contentType : 'application/json',
		success : success,
		complete : function(xhr, status){
			$('#loadingToast').fadeOut(100);
		},
		error : function(xhr, type) {
			msgerror();
		}
	});
}
function ajaxpost(url, json, callback){
	$.ajax({
		url : url,
		type : 'POST',
		dataType:"json",
		contentType : 'application/json',
		data : JSON.stringify(json),
		success : function(data, status) {
			callback(data, status);
			msgdone();
		},
		error : function(xhr, type) {
			msgerror();
		}
	});
}
function ajaxupload(url, form, callback){
	$('#loadingToast').fadeIn(100);
	$.ajax({
		url : url,
		type : 'POST',
		processData: false,
		contentType: false,
		dataType: 'json',
		data : form,
		success : function(data, status) {
			callback(data, status);
			msgdone();
		},
		complete : function(xhr, status){
			$('#loadingToast').fadeOut(100);
		},
		error : function(xhr, type) {
			msgerror();
		}
	});
}
function msgdone(){
	$('#doneToast').fadeIn(100);
	setTimeout(function () {
		$('#doneToast').fadeOut(100);
	}, 2000);
}
function msgerror(){
	$('#errorToast').fadeIn(100);
	setTimeout(function () {
		$('#errorToast').fadeOut(100);
	}, 2000);
}
function fileUpload(elem, callback){
	var file = elem.files[0];
    name = file.name;
    size = file.size;
    type = file.type;
	var output = [];
    if(file.name.length < 1) {
    }
    else if(file.size > 100000) {
        //alert("The file is too big");
		output.push('<i class="remove icon"></i><p><strong>ファイルが大きすぎます</strong> <br/>(', file.type || 'n/a', ') <br/> ', file.size, ' bytes', '<p>');
    }
    else if(file.type != 'image/png' && file.type != 'image/jpg' && file.type != 'image/gif' && file.type != 'image/jpeg' ) {
        //alert("The file does not match png, jpg or gif");
		output.push('<i class="remove icon"></i><p><strong>イメージのみ</strong><br/> (', file.type || 'n/a', ') <br/> ', file.size, ' bytes', '<p>');
    }else{
		var formData = new FormData();
        formData.append("file", file);
		$('#loadingToast').fadeIn(100);
		ajaxupload('/file/upload', formData, function(data, status){
			callback(data, status);
		});
		output.push('<i class="checkmark icon"></i><p><strong>', escape(file.name), '</strong><br/> (', file.type || 'n/a', ') <br/> ', file.size, ' bytes', '<p>');
	}
    document.getElementById('list').innerHTML =  output.join('') ;
}
function getconfig(){
	return {gkey:"AIzaSyAcMF_qkLYqc7TSteIhae8sQtvFikFY28w"};
}