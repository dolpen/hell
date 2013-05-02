$(function () {
    var limit = $('#timeLimit').val();
    $('#start').submit(function() {
        if (confirm("村を手動開始しますか？")) {
            return true;
        }
        return false;
    });
    if (limit == null || limit == "" || limit == "0"){
        $('#time').text('-');
        return;
    }
    var pad = function(n){
        return ("00"+n).slice(-2);
    };
    var f = function () {
        var time = Math.floor((limit - new Date()) / 1000);
        var resp = '';
        if (time >= 3600) {
            resp += Math.floor(time / 3600) + '時間';
            time %= 60;
        }
        if (time >= 60 || resp.length > 0) {
            resp += pad(Math.floor(time / 60)) + '分';
            time %= 60;
        }
        if (time >= 0) {
            $('#time').text(resp + pad(time) + '秒');
            setTimeout(f, 1000);
        } else {
            $('#time').text('更新してください');
        }
    };
    f();

});