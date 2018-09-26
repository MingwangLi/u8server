<%--
  Created by IntelliJ IDEA.
  User: chenjie.chen
  Date: 2016/8/9
  Time: 18:51
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path;

%>
<base href="<%=basePath%>">
<html>
<head>
  <%--<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />--%>
  <%--<link rel="stylesheet" type="text/css" href="<%=basePath%>/js/plugins/easyui/themes/default/easyui.css">--%>
  <%--<link rel="stylesheet" type="text/css" href="<%=basePath%>/js/plugins/easyui/themes/icon.css">--%>
  <%--<link rel="stylesheet" type="text/css" href="<%=basePath%>/js/plugins/easyui/themes/color.css">--%>

  <link rel="icon" href="<%=basePath%>/images/favicon.ico">
  <link rel="stylesheet" type="text/css" href="<%=basePath%>/css/login.css">

  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.easyui.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/jquery.md5.js"></script>
</head>
<body>
<div class="login_wrap">
  <div style="overflow: hidden;height:100%;display: block;">
    <div style="width: 680px; height: 100px; margin-left:auto;margin-right: auto; margin-top: 180px;background:url('<%=basePath%>/images/login_logo.png') top center no-repeat;">
    </div>
    <div style="margin-left:auto;margin-right: auto;margin-top: 30px;">
      <form id="login2" method="post">
        <label>用户名：<input id="username" type="text" name="userName"/></label>
        <label style="margin-top: 20px;">密　码：<input id="pwd" type="password" name="pass"/></label>
        <button id="btnLogin" type="button" onclick="javascript:login();">登　录</button>
      </form>
    </div>
  </div>


</div>


<script type="text/javascript">
    $(function() {

        var top = getTopWinow(); //获取当前页面的顶层窗口对象
        if(top != window){
            top.location.href = location.href; //跳转到登陆页面
            //document.parent.ReLogin();
        }

    });
    /*
    *这个方法用来获取当前页面的最顶层对象
    */
    function getTopWinow(){
        var p = window;
        while(p != p.parent){
            p = p.parent;
        }
        return p;
    }
    function login(){

    var username = $("#username").val();
    var pwd = $("#pwd").val();
    pwd = $.md5(pwd);

    $.post('<%=basePath%>/admin/doLogin', {username:username, password:pwd}, function(result){
      if (result.state == 1) {

        location.href="<%=basePath%>/admin/index"

      }else{
        alert(result.msg)
      }

    }, 'json');



  }


  $("#pwd").keyup(function(event){
    if(event.keyCode == 13){
      $("#btnLogin").click();
    }
  });

</script>

</body>
</html>
