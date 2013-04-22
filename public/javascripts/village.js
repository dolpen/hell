$(function () {
    var limit = $('#timeLimit').val();
    if (limit == "0"){
        $('#time').text('-');
        return;
    }
    var f = function () {
        var time = Math.floor((limit - new Date()) / 1000);
        var resp = '';
        if (time >= 3600) {
            resp += Math.floor(time / 3600) + '時間';
            time %= 60;
        }
        if (time >= 60 || resp.length > 0) {
            resp += Math.floor(time / 60) + '分';
            time %= 60;
        }
        if (time >= 0) {
            $('#time').text(resp + time + '秒');
            setTimeout(f, 1000);
        } else {
            $('#time').text('更新してください');
        }
    };
    f();
});