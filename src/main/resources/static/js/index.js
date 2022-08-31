$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//发送ajax请求前，将crsf令牌设置到请求头中
	/*var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function (e, xhr, options) {
		xhr.setRequestHeader(header, token);
	});*/

	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	$.post(CONTEXT_PATH + "/discuss/display", {"title":title, "content":content}, function (data) {
		data = $.parseJSON(data);
		$("#hintBody").text(data.msg);
		$("#hintModal").modal("show");
		setTimeout(function(){
			$("#hintModal").modal("hide");
			if (data.code == 0) {
				window.location.reload();
			}
		}, 2000);
	});
}