/**
 * Created by lvs on 17-7-12.
 */
$.ajaxSetup({
    //设置ajax请求结束后的执行动作
    complete: function (XMLHttpRequest, textStatus) {
        if ("REDIRECT" === XMLHttpRequest.getResponseHeader("REDIRECT")) { //若HEADER中含有REDIRECT说明后端想重定向，
            var win = window;
            while (win !== win.top) {
                win = win.top;
            }
            win.location.href = XMLHttpRequest.getResponseHeader("CONTENTPATH");//将后端重定向的地址取出来,使用win.location.href去实现重定向的要求
        }
    }
});