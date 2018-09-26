<%--
  Created by IntelliJ IDEA.
  User: xiaohei
  Date: 2015/8/22
  Time: 14:01
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
  <title></title>

  <link rel="stylesheet" type="text/css" href="<%=basePath%>/js/plugins/easyui/themes/black/easyui.css">
  <link rel="stylesheet" type="text/css" href="<%=basePath%>/js/plugins/easyui/themes/icon.css">
  <link rel="stylesheet" type="text/css" href="<%=basePath%>/js/plugins/easyui/themes/color.css">
  <link rel="stylesheet" type="text/css" href="<%=basePath%>/css/u8server.css">

  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.easyui.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>

</head>
<body>

<div style="height: 19%;" style="width:90%;height: 80%;" class="easyui-panel infoBar2">
<ul><li>
  <table>
    <tr style="height:30%;">
      <td><input id="games" type="text" class="easyui-combobox" style="height: 25px" prompt="选择游戏" name="appID" maxlength="255" /></td>
      <td class="u8_search_item"><input id="userID" type="text" class="easyui-textbox" style="height: 25px" prompt="用户id" name="userID" maxlength="255" /></td>
      <td class="u8_search_item"><input id="roleName" type="text" class="easyui-textbox" style="height: 25px" prompt="角色名" name="roleName" maxlength="255" /></td>
      <td class="u8_search_item"><input id="beginRegTime" class="easyui-datetimebox" style="height: 25px" name="beginCreateTime" prompt="创建开始时间" data-options="showSeconds:true" style="width:150px"></td>
      <td class="u8_search_item"><input id="endRegTime" class="easyui-datetimebox" style="height: 25px" name="endCreateTime" prompt="创建结束时间" data-options="showSeconds:true" style="width:150px"></td>
      <td class="u8_search_item"><a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="doSearch();" style="width:80px">查  询</a></td>
      <td class="u8_search_item"><a href="javascript:void(0)" class="easyui-linkbutton" onclick="doClear();" style="width:80px">清  空</a></td>
    </tr>
    <tr><td colspan="5"><font color="red">注意:此功能是2018-06-27新增的功能 只有在这之后创建的角色才会记录 适用于之后接入的游戏</font></tr>
  </table>
</li></ul>
</div>


<div class="easyui-panel" style="margin-top: 5px; height: 78%;border:hidden;overflow:hidden;">

  <div id="users" style="width: 100%;">

  </div>

</div>


<script type="text/javascript">


  function doClear(){
    $("#games").combobox("clear");
    $("#userID").textbox("setValue", "");
    $("#beginRegTime").datetimebox("setValue", "");
    $("#endRegTime").datetimebox("setValue", "");
    $("#roleName").textbox("setValue", "");
  }

  function doSearch(){

    var appID = $("#games").combobox("getValue");
    var userID = $("#userID").val();
    var beginRegTime = $("#beginRegTime").datetimebox('getValue');
    var endRegTime = $("#endRegTime").datetimebox('getValue');
    var roleName = $("#roleName").val();
    $("#users").datagrid({
      queryParams:{
        appID:appID,
          userID:userID,
          roleName:roleName,
          beginCreateTime:beginRegTime,
          endCreateTime:endRegTime
      }
    });

  }

  $("#users").datagrid({
    height:'99%',
    url:'<%=basePath%>/admin/uuserinfolist',
    method:'POST',
    idField:'id',
    striped:true,
    fitColumns:true,
    singleSelect:true,
    rownumbers:true,
    pagination:true,
    nowrap:true,
    loadMsg:'数据加载中...',
    pageSize:20,
    pageList:[20,50,100],
    showFooter:true,
    remoteSort:false,
    columns:[[
      {field:'id', title:'编号', width:30, sortable:true},
      {field:'userID', title:'用户id', width:30, sortable:true},
      {field:'roleID', title:'角色id', width:30, sortable:true},
      {field:'roleName', title:'角色名称', width:30, sortable:true},
      {field:'roleLevel', title:'角色等级', width:30, sortable:true},
      {field:'serverID', title:'大区编号', width:30, sortable:true},
      {field:'serverName', title:'大区名称', width:30, sortable:true},
      {field:'createdTime', title:'创建时间', width:30, sortable:true},
        {field:'channelID', title:'渠道编号', width:30, sortable:true},
        {field:'appID', title:'所属游戏', width:30, sortable:true}


    ]],
    toolbar:'#easyui_toolbar'
  });

  $("#games").combobox({
    url:'<%=basePath%>/admin/games/getAllGamesSimple',
    valueField:'appID',
    textField:'name',
    onSelect:function(rec){
      $('#appID').val(rec.appID);
    },
    loadFilter:function (data) {
        var a = {"appID":0,"name":"全部游戏"};
        data.unshift(a);
        return data;
    }
  });

</script>

</body>
</html>
