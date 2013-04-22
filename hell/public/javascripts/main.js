$(function(){
    $('.dropdown-toggle').dropdown();
    $('.dropdown-menu').on('click',function(e){
       e.stopPropagation();
    });
});