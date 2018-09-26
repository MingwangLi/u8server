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
  <link rel="stylesheet" type="text/css" href="<%=basePath%>/css/login.css">

  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.easyui.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/jquery.md5.js"></script>
</head>
<body>
<div id="home">
  <form id="login" method="post">
    <h3>用户登录</h3>
    <img class="avator" src="<%=basePath%>/images/avatar.png" width="96" height="96"/>
    <label>用户名<input id="username" type="text" name="userName"/></label>
    <label>密　码<input id="pwd" type="password" name="pass"/></label>
    <button id="btnLogin" type="button" onclick="javascript:login();">登　录</button>
  </form>
</div>

<script type="text/javascript">

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
