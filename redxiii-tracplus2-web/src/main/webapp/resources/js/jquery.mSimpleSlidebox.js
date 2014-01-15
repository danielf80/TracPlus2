/*
Slidebox jQuery banner rotator and image slideshow plugin
Author: malihu [http://manos.malihu.gr]
Homepage: http://manos.malihu.gr/slidebox-jquery-banner-rotator
*/
(function($){
	$(document).data("slideboxID",0);
	$.fn.mSlidebox=function(options){
		var defaults={ //default options
			autoPlayTime:7000, //milliseconds
			animSpeed:1000, //milliseconds
			easeType:"easeInOutQuint", //see jQuery UI easing types
			controlsPosition:{buttonsPosition:"inside",thumbsPosition:"inside"}, //inside/outside
			numberedThumbnails:false, //display numbers inside thumbnails
			pauseOnHover:true //boolean
		};
		var options=$.extend(defaults,options);
		return this.each(function(){
			var slidebox=$(this);
			var slideboxID=$(document).data("slideboxID");
			slideboxID++;
			$(document).data("slideboxID",slideboxID);
			slidebox.wrap("<div class='slideboxContainer slideboxContainer_"+$(document).data("slideboxID")+"' />");
			var slideboxContainer=slidebox.parent(".slideboxContainer");
			var autoPlayTimer;
			var slideboxWidth=slidebox.width();
			var slideboxSlides=slidebox.find("ul");
			slideboxSlides.addClass("slideboxSlides");
			var slideboxSlide=slideboxSlides.children("li");
			var slideboxTotalWidth;
			var slideboxEnd;
			var slideboxStart="0";
			if(slideboxSlide.length>1){ //if more than 1 slides
				autoPlayTimer=setInterval(autoPlay,options.autoPlayTime);
				var autoPlayState="on";
				if(options.controlsPosition.buttonsPosition=="outside"){
					slidebox.after("<a href='#' class='slideboxPrevious'></a><a href='#' class='slideboxNext'></a>");
				}else{
					slidebox.append("<a href='#' class='slideboxPrevious'></a><a href='#' class='slideboxNext'></a>");
				}
				var slideboxPrevious=slideboxContainer.find(".slideboxPrevious");
				var slideboxNext=slideboxContainer.find(".slideboxNext");
				if(options.controlsPosition.thumbsPosition=="outside"){
					slidebox.after("<div class='slideboxThumbs' />");
				}else{
					slidebox.append("<div class='slideboxThumbs' />");
				}
				var slideboxThumbs=slideboxContainer.find(".slideboxThumbs");
				slideboxSlide.each(function(index){
					if(options.numberedThumbnails){
						slideboxThumbs.append("<a href='#' class='slideboxThumb' rel='"+(index+1)+"'>"+(index+1)+"</a>");
					}else{
						slideboxThumbs.append("<a href='#' class='slideboxThumb' rel='"+(index+1)+"' />");
					}
					$(this).attr("rel",index+1).addClass("slideboxSlide slideboxSlide_"+(index+1)).children().addClass("slideboxCaption");
					slideboxTotalWidth=(index+1)*slideboxWidth;
					slideboxSlides.css("width",slideboxTotalWidth);
					slideboxEnd=index*slideboxWidth;
				});
				var slideboxThumb=slideboxThumbs.children(".slideboxThumb");
				slideboxThumb.click(function(e){
					e.preventDefault();
					SlideboxAction($(this).attr("rel"));
				});
				slideboxNext.click(function(e){
					e.preventDefault();
					SlideboxAction("next","stop");
				});
				slideboxPrevious.click(function(e){
					e.preventDefault();
					SlideboxAction("previous","stop");
				});
				if(options.pauseOnHover){
					slidebox.hover(function(){
						clearInterval(autoPlayTimer);
					},function(){
						if(autoPlayState!="off"){
							autoPlayTimer=setInterval(autoPlay,options.autoPlayTime);
						}
					});
				}
				slideboxThumb.first().addClass("selectedSlideboxThumb");
			}else{ //if less than 1 slides
				slideboxSlide.each(function(){
					$(this).addClass("slideboxSlide slideboxSlide_1").children().addClass("slideboxCaption");
				});
			}
			function autoPlay(){
				SlideboxAction("next");
			}
			slideboxSlides.css("left",0);
			function SlideboxAction(slideTo,autoPlay){
				var leftPosition=parseInt(slideboxSlides.css("left"));
				if(!slideboxSlides.is(":animated")){
					var selectedSlideboxThumb=slideboxThumbs.children(".selectedSlideboxThumb");
					if(slideTo=="next"){ //next
						if(autoPlay=="stop"){
							clearInterval(autoPlayTimer);
							autoPlayState="off";
						}
						if(leftPosition==-slideboxEnd){
							if(!slideboxSlides.data("carouselFirst")){
								slideboxSlide.first().clone().appendTo(slideboxSlides);
								slideboxSlides.css("width",slideboxSlides.width()+slideboxWidth).data("carouselFirst","duplicated");
							}
							slideboxSlides.animate({left:-(slideboxSlides.width()-slideboxWidth)},options.animSpeed,options.easeType,function(){
								slideboxSlides.css("left",slideboxStart);
							});
							slideboxThumb.first().addClass("selectedSlideboxThumb");
							slideboxThumb.last().removeClass("selectedSlideboxThumb");
						}else{
							slideboxSlides.animate({left:"-="+slideboxWidth},options.animSpeed,options.easeType); //next
							selectedSlideboxThumb.removeClass("selectedSlideboxThumb").next().addClass("selectedSlideboxThumb");
						}
					}else if(slideTo=="previous"){ //previous
						if(autoPlay=="stop"){
							clearInterval(autoPlayTimer);
							autoPlayState="off";
						}
						if(leftPosition==slideboxStart){
							if(!slideboxSlides.data("carouselLast")){
								slideboxSlide.last().clone().prependTo(slideboxSlides);
								slideboxSlides.css({"width":slideboxSlides.width()+slideboxWidth,"left":-slideboxWidth}).data("carouselLast","duplicated");
							}
							slideboxSlides.animate({left:0},options.animSpeed,options.easeType,function(){
								slideboxSlides.css("left",-slideboxTotalWidth);
								slideboxStart=-slideboxWidth;
								slideboxEnd=slideboxTotalWidth;
							});
							slideboxThumb.first().removeClass("selectedSlideboxThumb");
							slideboxThumb.last().addClass("selectedSlideboxThumb");
						}else{
							slideboxSlides.animate({left:"+="+slideboxWidth},options.animSpeed,options.easeType); //previous
							selectedSlideboxThumb.removeClass("selectedSlideboxThumb").prev().addClass("selectedSlideboxThumb");
						}
					}else{ //go to slide
						var slide2;
						if(!slideboxSlides.data("carouselLast")){
							slide2=(slideTo-1)*slideboxWidth;
						}else{
							slide2=slideTo*slideboxWidth;
						}
						if(leftPosition!=-slide2){
							clearInterval(autoPlayTimer);
							autoPlayState="off";
							slideboxSlides.animate({left:-slide2},options.animSpeed,options.easeType); 
							selectedSlideboxThumb.removeClass("selectedSlideboxThumb");
							slideboxThumb.eq((slideTo-1)).addClass("selectedSlideboxThumb");
						}
					}
				}
			}
		});
	};
})(jQuery);