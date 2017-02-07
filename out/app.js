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
	window.onresize = setPxPerRem;
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