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

<div style="height: 22%;" class="easyui-panel infoBar2" style="height: 800px">
    <form id="conditionForn" method="post" action="<%=basePath%>/admin/orders/downloadOrdersWithConditions">
  <ul><li>
  <table>
    <tr style="height: 30%;">
      <td><input id="games" type="text" class="easyui-combobox" style="height: 25px" prompt="选择游戏" name="allgames" maxlength="255" /></td>
      <td class="u8_search_item"><input id="channelID" type="text" class="easyui-textbox" style="height: 25px" prompt="渠道号" name="channelID" maxlength="255" /></td>
      <td class="u8_search_item"><input id="orderID" type="text" class="easyui-textbox" style="height: 25px" prompt="订单号" name="orderID" maxlength="255" /></td>
      <td class="u8_search_item"><input id="channelOrderID" type="text" class="easyui-textbox" style="height: 25px" prompt="渠道订单号" name="channelOrderID" maxlength="255" /></td>
      <td class="u8_search_item"><input id="beginCreateTime" class="easyui-datetimebox" style="height: 25px" name="beginCreateTime" prompt="订单创建开始时间" data-options="showSeconds:true" style="width:140px"></td>
      <td class="u8_search_item"><input id="endCreateTime" class="easyui-datetimebox" style="height: 25px" name="endCreateTime" prompt="订单创建结束时间" data-options="showSeconds:true" style="width:140px"></td>
      <%--<td class="u8_search_item"><a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="doSearch();" style="width:80px">查  询</a></td>--%>
    <%--  <td class="u8_download_item"><a href="<%=basePath%>/admin/orders/downloadOrders" class="easyui-linkbutton c6"  style="width:80px">今天订单</a></td>--%>
        <td class="u8_download_item"><a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="downloadOrders();" style="width:80px">订单下载</a><font color="red">仅针对游戏、渠道号、订单状态、创建时间等条件搜索下载</font></td>
    </tr>
    <tr><td colspan="6"><br></td></tr>
    <tr>
      <td><input id="minMoney" name="minMoney" prompt="最小金额(分)" type="text" class="easyui-numberbox" style="height: 25px" data-options="min:0,precision:0"/></td>
      <td class="u8_search_item"><input id="maxMoney" name="maxMoney" prompt="最大金额(分)" type="text" class="easyui-numberbox" style="height: 25px" data-options="min:0,precision:0"/></td>
      <td class="u8_search_item"><input id="minRealMoney" name="minRealMoney" prompt="最小实际金额(分)" type="text" class="easyui-numberbox" style="height: 25px" data-options="min:0,precision:0"/></td>
      <td class="u8_search_item"><input id="maxRealMoney" name="maxRealMoney" prompt="最大实际金额(分)" type="text" class="easyui-numberbox" style="height: 25px" data-options="min:0,precision:0"/></td>
      <td class="u8_search_item">
        <input id="state" type="text" class="easyui-combobox" style="height: 25px" prompt="订单状态" name="state" maxlength="255"
                data-options="
                  valueField:'value',
                  textField:'label',
                  data:[
                    {label:'支付中',value:'1'},
                    {label:'处理成功',value:'2'},
                    {label:'完成',value:'3'},
                    {label:'失败',value:'0'}
                  ]
                "/>
        <%--<select id="state" class="easyui-combobox" prompt="订单状态" name="state" style="width: 140px;height: 25px">--%>
          <%--<option value="3">完成</option>--%>
          <%--<option value="1">支付中</option>--%>
          <%--<option value="2">处理成功</option>--%>
          <%--<option value="4">失败</option>--%>
        <%--</select>--%>

      </td>
      <td class="u8_search_item"><input id="serverID" type="text" class="easyui-textbox" style="height: 25px" prompt="服务器ID" name="serverID" maxlength="255" /></td>
      <td class="u8_search_item"><a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="doSearch();" style="width:80px">查  询</a></td>
    </tr>
    <tr><td colspan="6"><br></td></tr>
    <tr>
      <td><input id="userID" type="text" class="easyui-textbox" style="height: 25px" prompt="用户ID" name="userID" maxlength="255" /></td>
      <td class="u8_search_item"><input id="username" type="text" class="easyui-textbox" style="height: 25px" prompt="用户名" name="username" maxlength="255" /></td>
      <td class="u8_search_item"><input id="productID" type="text" class="easyui-textbox" style="height: 25px" prompt="商品ID" name="productID" maxlength="255" /></td>
      <td class="u8_search_item"><input id="productName" type="text" class="easyui-textbox" style="height: 25px" prompt="商品名" name="productName" maxlength="255" /></td>
      <td class="u8_search_item"><input id="roleID" type="text" class="easyui-textbox" style="height: 25px" prompt="角色ID" name="roleID" maxlength="255" /></td>
      <td class="u8_search_item"><input id="roleName" type="text" class="easyui-textbox" style="height: 25px" prompt="角色名称" name="roleName" maxlength="255" /></td>
      <td class="u8_search_item"><a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="doClear();" style="width:80px">清  空</a></td>
    </tr>

    <%--<tr style="height: 30px;">--%>
      <%--<td><input id="channelUserID" type="text" class="easyui-textbox" prompt="渠道用户ID" name="channelUserID" maxlength="255" /></td>--%>
      <%--<td class="u8_search_item"><input id="channelUserName" type="text" class="easyui-textbox" prompt="渠道用户名" name="channelUserName" maxlength="255" /></td>--%>
      <%--<td class="u8_search_item"><input id="channelNickName" type="text" class="easyui-textbox" prompt="渠道昵称" name="channelNickName" maxlength="255" /></td>--%>
      <%--<td class="u8_search_item"><input id="beginLoginTime" class="easyui-datetimebox" name="loginCreateTime" prompt="登录开始时间" data-options="showSeconds:true" style="width:150px"></td>--%>
      <%--<td class="u8_search_item"><input id="endLoginTime" class="easyui-datetimebox" name="loginCreateTime" prompt="登录结束时间" data-options="showSeconds:true" style="width:150px"></td>--%>
      <%--<td class="u8_search_item"><a href="javascript:void(0)" class="easyui-linkbutton" onclick="doClear();" style="width:80px">清  空</a></td>--%>
    <%--</tr>--%>
  </table>
  </li>
    </ul>
    </form>
</div>

<div class="easyui-panel" style="margin-top: 5px; height: 74%;border:hidden;overflow:hidden;">
  <div id="orders" style="width: 100%;">

  </div>
</div>



<div id="easyui_toolbar" region="north" border="false">

  <div class="toolbar">

    <div style="float: left;">
      <a class="easyui-linkbutton" plain="true" icon="icon-filter" onclick="javascript:resend();">补单</a>
    </div>

    <div class="datagrid-btn-separator"></div>

    <div style="float: left;">
      <a class="easyui-linkbutton" plain="true"
         icon="icon-remove" onclick="javascript:deleteOrder();">删除</a>
    </div>

  </div>


  <%--<div id="tb" style="float: right;">--%>
    <%--<input id="search_box" class="easyui-searchbox" style="width: 250px"  data-options="searcher:doSearch,prompt:'请输入查询词',menu:'#search_menu'" />--%>
    <%--<div id="search_menu" style="width:120px">--%>
      <%--<div data-options="name:'order_id'">订单号</div>--%>
      <%--<div data-options="name:'order_channelID'">渠道订单号</div>--%>
      <%--<div data-options="name:'order_username'">用户名</div>--%>
      <%--<div data-options="name:'order_channel'">渠道名称</div>--%>
      <%--<div data-options="name:'order_game'">所属游戏</div>--%>
    <%--</div>--%>
  <%--</div>--%>

</div>


<script type="text/javascript">

  function resend(){
    var row = $('#orders').datagrid('getSelected')
    if(row){

      $.messager.confirm(
              '操作确认',
              '确定要补单吗？',
              function(r){
                if(r){

                  $.post('<%=basePath%>/admin/orders/resendOrder', {currOrderID:row.orderID}, function(result){
                    if(result.state == 1){
                      $("#orders").datagrid('reload');

                    }

                    $.messager.show({
                      title:'操作提示',
                      msg:result.msg
                    })
                  }, 'json');
                }
              }
      );

    }else{
      $.messager.show({
        title:'操作提示',
        msg:'请选择一条纪录'
      })
    }
  }

  function deleteOrder(){
    var row = $('#orders').datagrid('getSelected');
    if(row){
      $.messager.confirm(
              '操作确认',
              '确定要删除该用户吗？(操作不可恢复)',
              function(r){
                if(r){
                  $.post('<%=basePath%>/admin/orders/removeOrder', {currOrderID:row.orderID}, function(result){
                    if (result.state == 1) {
                      $("#orders").datagrid('reload');
                    }

                    $.messager.show({
                      title:'操作提示',
                      msg:result.msg
                    })

                  }, 'json');
                }
              }
      );
    }else{
      $.messager.show({
        title:'操作提示',
        msg:'请选择一条记录'
      })
    }
  }

  function doClear(){

    $("#games").combobox("clear");
    $("#channelID").textbox("setValue", "");
    $("#orderID").textbox("setValue", "");
    $("#userID").textbox("setValue", "");
    $("#username").textbox("setValue", "");
    $("#productID").textbox("setValue", "");
    $("#productName").textbox("setValue", "");
    $("#minMoney").textbox("setValue", "");
    $("#maxMoney").textbox("setValue", "");
    $("#minRealMoney").numberbox("clear");
    $("#maxRealMoney").numberbox("clear");
    $("#roleID").textbox("setValue", "");
    $("#roleName").textbox("setValue", "");
    $("#serverID").textbox("setValue", "");
    $("#state").textbox("setValue", "");
    $("#channelOrderID").textbox("setValue", "");
    $("#beginCreateTime").datetimebox('setValue', "");
    $("#endCreateTime").datetimebox('setValue', "");

  }

  function doSearch(){

    var appID = $("#games").combobox("getValue");
    var channelID = $("#channelID").val();
    var orderID = $("#orderID").val();
    var userID = $("#userID").val();
    var username = $("#username").val();
    var productID = $("#productID").val();
    var productName = $("#productName").val();

    var minMoney= $("#minMoney").numberbox("getValue");
    var maxMoney = $("#maxMoney").numberbox("getValue");
    var minRealMoney = $("#minRealMoney").numberbox("getValue");
    var maxRealMoney = $("#maxRealMoney").numberbox("getValue");

    var roleID = $("#roleID").val();
    var roleName = $("#roleName").val();
    var serverID = $("#serverID").val();
    var state = $("#state").combobox("getValue");
    var channelOrderID = $("#channelOrderID").val();

    var beginCreateTime = $("#beginCreateTime").datetimebox('getValue');
    var endCreateTime = $("#endCreateTime").datetimebox('getValue');


    $("#orders").datagrid({
      queryParams:{
        orderID         : orderID        ,
        appID           : appID          ,
        channelID       : channelID      ,
        userID          : userID         ,
        username        : username       ,
        productID       : productID      ,
        productName     : productName    ,
        minMoney        : minMoney       ,
        maxMoney        : maxMoney       ,
        minRealMoney    : minRealMoney   ,
        maxRealMoney    : maxRealMoney   ,
        roleID          : roleID         ,
        roleName        : roleName       ,
        serverID        : serverID       ,
        state           : state          ,
        channelOrderID  : channelOrderID ,
        beginCreateTime : beginCreateTime,
        endCreateTime   : endCreateTime
      }
    });

  }

  $("#orders").datagrid({
    height:'98%',
    url:'<%=basePath%>/admin/orders/searchOrders',
    method:'POST',
    idField:'orderID',
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
      {field:'orderID', title:'订单号', width:25, sortable:true},
      {field:'username', title:'用户名', width:20, sortable:true},
      {field:'money', title:'金额(分)', width:10, sortable:true,align:'center'},
      {field:'realMoney', title:'实际金额(分)', width:10, sortable:true,align:'center'},
      {field:'stateName', title:'状态', width:10, sortable:true},
      {field:'channelOrderID', title:'渠道订单号', width:20, sortable:true},
      {field:'channelID', title:'渠道号', width:10, sortable:true,align:'center'},
      {field:'channelName', title:'渠道名称', width:10, sortable:true,align:'center'},
      {field:'appName', title:'所属游戏', width:20, sortable:true,align:'center'},
      {field:'createdTime', title:'下单时间', width:20, sortable:true}
    ]],
    toolbar:'#easyui_toolbar'
  });

  $("#games").combobox({
    url:'<%=basePath%>/admin/games/getAllGamesSimple',
    valueField:'appID',
    textField:'name',
    onSelect:function(rec){
      //$('#appID').val(rec.appID);
    }
  });

  function downloadOrders() {
      $("#conditionForn").submit();
  }


</script>

</body>
</html>
