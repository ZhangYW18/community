$(function(){
    $("#deleteBtn").click(setDelete);
});

function like(btn, entityType, entityId, entityUserId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {
            "entityType": entityType,
            "entityId": entityId,
            "entityUserId": entityUserId,
            "postId": postId,
        },
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'Liked':"Like");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 置顶
function setSticky(btn, id) {
    $.post(
        CONTEXT_PATH + "/discuss/sticky",
        {
            "id": id,
            "postType": $("#postType").val(),
        },
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $(btn).text(data.type==1?'Undo Pinned':'Set Pinned');
                $("#postType").attr("value", data.type);
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setDigested(btn, id) {
    $.post(
        CONTEXT_PATH + "/discuss/digested",
        {
            "id": id,
            "postStatus": $("#postStatus").val(),
        },
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $(btn).text(data.status==1?'Undo Must-Read':'Set Must-Read');
                $("#postStatus").attr("value", data.status);
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {
            "id":$("#postId").val(),
        },
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}