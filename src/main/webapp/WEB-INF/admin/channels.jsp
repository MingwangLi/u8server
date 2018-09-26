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
<div id="channels">

</div>

<div id="easyui_toolbar" region="north" border="false">
  <div class="toolbar">
    <div style="float: left;">
      <a class="easyui-linkbutton" plain="true" icon="icon-add" onclick="javascript:showAddDialog();">新增</a>
    </div>

    <div class="datagrid-btn-separator"></div>

    <div style="float: left;">
      <a class="easyui-linkbutton" plain="true" icon="icon-edit" onclick="javascript:showEditDialog();">编辑</a>
    </div>

    <div class="datagrid-btn-separator"></div>

    <div style="float: left;">
      <a class="easyui-linkbutton" plain="true"
         icon="icon-remove" onclick="javascript:deleteChannel();">删除</a>
    </div>

    <div id="tb" style="float: right;">
      <input id="search_box" class="easyui-searchbox" style="width: 250px;height: 25px;"  data-options="searcher:doSearch,prompt:'请输入查询词',menu:'#search_menu'" />
      <div id="search_menu" style="width:100px">
        <div data-options="name:'channel_name'">渠道名称</div>
        <div data-options="name:'channel_id'">渠道号</div>
        <div data-options="name:'game_name'">游戏名称</div>
      </div>
    </div>
  </div>

</div>

<div id="dialog_add" class="easyui-dialog u8_form"
     closed="true" buttons="#dlg-buttons" style="height: 350px;width: 500px;">
  <div class="ftitle">渠道信息</div>
  <form id="fm" method="post" novalidate>
    <input id="id" type="hidden" name="id" />
    <input id="appID" type="hidden" name="appID" />
    <input id="masterID" type="hidden" name="masterID" />
    <div class="u8_form_row">
      <label >所属游戏：</label>
      <input id="games" type="text" class="easyui-combobox" style="height: 25px;" name="allgames" maxlength="255" required="true"/>
    </div>

    <div class="u8_form_row">
      <label >渠道商：</label>
      <input id="masters" type="text" class="easyui-combobox" style="height: 25px;" name="allmasters" maxlength="255" required="true"/>
    </div>
    <div class="u8_form_row">
      <label >渠道号：</label>
      <input id="channelID" type="text" value="111" class="easyui-textbox" style="height: 25px;" name="channelID" maxlength="255" required="true" />
      <a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="recommendChannelID()" style="width:70px">推荐</a>
    </div>
    <div class="u8_form_row">
      <label >充值状态(0:开放/1:关闭)：</label>
      <input id="openPayFlag" type="text" class="easyui-textbox" style="height: 25px;" name="openPayFlag" prompt="关闭后，下单会返回错误" maxlength="255" novalidate />
    </div>
    <div class="u8_form_row">
      <label >充值关闭时间(开始)：</label>
      <input id="chargeCloseTimeBegin" type="text" class="easyui-datetimebox" id="chargeCloseTimeBegin" style="height: 25px;" name="chargeCloseTimeBegin" prompt="时间范围内无法下单" data-options="showSeconds:true" maxlength="255" novalidate />
    </div>
    <div class="u8_form_row">
      <label >充值关闭时间(结束)：</label>
      <input id="chargeCloseTimeEnd" type="text" class="easyui-datetimebox" id="chargeCloseTimeEnd" style="height: 25px;" name="chargeCloseTimeEnd" prompt="时间范围内无法下单" data-options="showSeconds:true" maxlength="255" novalidate />
    </div>
    <div class="u8_form_row">
      <label >CPID：</label>
      <input id="cpID" type="text" class="easyui-textbox" style="height: 25px;" name="cpID" maxlength="255" novalidate />
    </div>

    <div class="u8_form_row">
      <label >AppID：</label>
      <input id="cpAppID" type="text" class="easyui-textbox" style="height: 25px;" name="cpAppID" maxlength="255" novalidate />
    </div>
    <div class="u8_form_row">
      <label >AppKey：</label>
      <input id="cpAppKey" type="text" class="easyui-textbox" style="height: 25px;" name="cpAppKey" maxlength="255" novalidate />
    </div>

    <div class="u8_form_row">
      <label >AppSecret：</label>
      <input id="cpAppSecret" type="text" class="easyui-textbox" style="height: 25px;" name="cpAppSecret" maxlength="1024" novalidate />
    </div>

    <div class="u8_form_row">
      <label >PayID：</label>
      <input id="cpPayID" type="text" class="easyui-textbox" style="height: 25px;" name="cpPayID" maxlength="255" novalidate />
    </div>

    <div class="u8_form_row">
      <label >PayPublicKey：</label>
      <input id="cpPayKey" type="text" class="easyui-textbox" style="height: 25px;" name="cpPayKey" maxlength="1024" novalidate />
    </div>

    <div class="u8_form_row">
      <label >PayPrivateKey：</label>
      <input id=cpPayPriKey"" type="text" class="easyui-textbox" style="height: 25px;" name="cpPayPriKey" maxlength="1024" novalidate />
    </div>

    <div class="u8_form_row">
      <label >特殊配置(登陆:注册)：</label>
      <input id="cpConfig" type="text" class="easyui-textbox" style="height: 25px;" name="cpConfig" maxlength="1024" novalidate />
    </div>

    <div class="u8_form_row">
      <label >版本号：</label>
      <input id="version_add" type="text" class="easyui-textbox" style="height: 25px;" name="version" maxlength="1024" novalidate />
    </div>

    <div class="u8_form_row">
      <label >下载地址：</label>
      <input id="lastVersionUrl" type="text" class="easyui-textbox" style="height: 25px;" name="lastVersionUrl" maxlength="1024" novalidate />
    </div>

    <div class="u8_form_row">
      <label >登录认证地址：</label>
      <input id="authUrl" type="text" class="easyui-textbox" style="height: 25px;" name="authUrl" prompt="这里会覆盖渠道商配置中的" maxlength="1024" novalidate />
    </div>

    <div class="u8_form_row">
      <label >支付回调地址：</label>
      <input id="payCallbackUrl" type="text" class="easyui-textbox" style="height: 25px;" name="payCallbackUrl" prompt="这里会覆盖渠道商配置中的" maxlength="1024" novalidate />
    </div>

    <div class="u8_form_row">
      <label >渠道下单地址：</label>
      <input id="orderUrl" type="text" class="easyui-textbox" style="height: 25px;" name="orderUrl" prompt="这里会覆盖渠道商配置中的" maxlength="1024" novalidate />
    </div>

    <div class="u8_form_row">
      <label >脚本类路径：</label>
      <input id="verifyClass" type="text" class="easyui-textbox" style="height: 25px;" name="verifyClass" prompt="这里会覆盖渠道商配置中的" maxlength="1024" novalidate />
    </div>

  </form>
</div>
<div id="dlg-buttons">
  <a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="saveUser()" style="width:90px">保 存</a>
  <a href="javascript:void(0)" class="easyui-linkbutton" onclick="javascript:$('#dialog_add').dialog('close')" style="width:90px">取 消</a>
</div>

<div id="upload-buttons">
  <a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="uploadFormSubmit()" style="width:90px">保 存</a>
  <a href="javascript:void(0)" class="easyui-linkbutton" onclick="javascript:$('#apk_upload').dialog('close')" style="width:90px">取 消</a>
</div>


<div id="apk_upload" class="easyui-dialog u8_form"
     closed="true" buttons="#upload-buttons" style="height: 200px;width: 400px;">
  <form id="apkForm" action="<%=basePath%>/admin/uploadApk" method="post" enctype="multipart/form-data">
    <table>
      <tr>
        <td>请选择要上传的文件</td>
        <td><input type="file" name="apkFile" id="apkFileId"></td>
      </tr>
      <tr>
        <td>请输入渠道号</td>
        <td><input type="text" name="channelIDApk" id="channelIDApk"></td>
      </tr>
      <tr>
        <td>请输入版本号</td>
        <td><input type="text" name="version" id="version"></td>
      </tr>
    <%--  <tr>
        <td colspan="2"><input type="submit" name="提交"></td>
      </tr>--%>
    </table>
  </form>
</div>

<script type="text/javascript">

  var url;
  function showAddDialog(){
    $("#dialog_add").window({
      top:($(window).height() - 450) * 0.5,
      left:($(window).width() - 450) * 0.5
    });

    $("#dialog_add").dialog('open').dialog('setTitle', '添加渠道');

    $('#fm').form('clear');

    url = '<%=basePath%>/admin/channels/addChannel';

  }

  function showEditDialog(){

    $("#dialog_add").window({
      top:($(window).height() - 450) * 0.5,
      left:($(window).width() - 450) * 0.5
    });

    url = '<%=basePath%>/admin/channels/saveChannel';
    var row = $('#channels').datagrid('getSelected');
    if(row){

      $("#dialog_add").dialog('open').dialog('setTitle', '编辑渠道');
      //$('#fm').form('load', row);  存在错位问题
        $("#id").val(row.id);
      $("#channelID").textbox("setValue", row.channelID);
        $("#openPayFlag").textbox("setValue", row.openPayFlag);
        $("#chargeCloseTimeBegin").textbox("setValue", row.chargeCloseTimeBegin);
        $("#chargeCloseTimeEnd").textbox("setValue", row.chargeCloseTimeEnd);
        $("#cpID").textbox("setValue", row.cpID);
        $("#cpAppID").textbox("setValue", row.cpAppID);
        $("#cpAppKey").textbox("setValue", row.cpAppKey);
        $("#cpAppSecret").textbox("setValue", row.cpAppSecret);
        $("#cpPayID").textbox("setValue", row.cpPayID);
        $("#cpPayKey").textbox("setValue", row.cpPayKey);
        $("#cpPayPriKey").textbox("setValue", row.cpPayPriKey);
        $("#cpConfig").textbox("setValue", row.cpConfig);
        $("#version_add").textbox("setValue", row.version);
        $("#lastVersionUrl").textbox("setValue", row.lastVersionUrl);
        $("#authUrl").textbox("setValue", row.authUrl);
        $("#payCallbackUrl").textbox("setValue", row.payCallbackUrl);
        $("#orderUrl").textbox("setValue", row.orderUrl);
        $("#verifyClass").textbox("setValue", row.verifyClass);
        $('#games').combobox('select', row.appID);
      $('#masters').combobox('select', row.masterID);

      var time = row.chargeCloseTime;
      if (typeof(time) != "undefined") {
          var array = row.chargeCloseTime.split("_");
          $("#chargeCloseTimeBegin").datetimebox('setValue', array[0]);
          $("#chargeCloseTimeEnd").datetimebox('setValue', array[1]);
      }

    }else{
      $.messager.show({
        title:'操作提示',
        msg:'请选择一条记录'
      })
    }
  }

  function deleteChannel(){
    var row = $('#channels').datagrid('getSelected');
    if(row){
      $.messager.confirm(
              '操作确认',
              '确定要删除该渠道吗？(操作不可恢复)',
              function(r){
                if(r){
                  $.post('<%=basePath%>/admin/channels/removeChannel', {currChannelID:row.channelID}, function(result){
                    if (result.state == 1) {
                      $('#dialog_add').dialog('close');
                      $("#channels").datagrid('reload');
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

  function saveUser(){
    $('#fm').form('submit', {
      url:url,
      onSubmit:function(){
        return $(this).form('validate');
      },
      success:function(result){
        var result = eval('('+result+')');
        if (result.state == 1) {
          $('#dialog_add').dialog('close');
          $("#channels").datagrid('reload');
            $("#chargeCloseTimeBegin").datetimebox('setValue', "");
            $("#chargeCloseTimeEnd").datetimebox('setValue', "");
        }

        $.messager.show({
          title:'操作提示',
          msg:result.msg
        })
      }
    })

  }


  function recommendChannelID(){
    $.post('<%=basePath%>/admin/channels/recommendChannelID', {}, function(result){
      if (result.state == 1) {
        $("#channelID").textbox().textbox('setValue', result.data);
          //$("#channelID").val(result.data);
      }else{
        alert(result.msg);
      }
    });

  }

  function doSearch(value, name){
    if(name == "channel_name"){
      $("#channels").datagrid({
        queryParams:{
          searchMaserName:value
        }
      });
    }else if(name == "channel_id"){
      $("#channels").datagrid({
        queryParams:{
          channelID:value
        }
      });
    }else if(name == "game_name"){
      $("#channels").datagrid({
        queryParams:{
          searchGameName:value
        }
      });
    }

  }

  $("#channels").datagrid({
    height:'98%',
    url:'<%=basePath%>/admin/channels/getAllChannels',
    method:'POST',
    idField:'channelID',
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
    columns:[[
      //{field:'id', title:'ID', width:20, sortable:true},
      {field:'channelID', title:'渠道号', width:20, sortable:true},
      {field:'appName',title:'所属游戏',width:40,sortable:true},
      {field:'masterName', title:'渠道名称', width:40, sortable:true},
      {field:'cpID', title:'CPID', width:20, sortable:true},
      {field:'cpAppID', title:'CPAppID', width:40, sortable:true},
      {field:'cpAppKey', title:'CPAppKey', width:40, sortable:true},
      {field:'cpAppSecret', title:'CPAppSecret', width:40, sortable:true},
      {field:'cpPayID', title:'CPPayID', width:40, sortable:true},
      {field:'cpPayKey', title:'CPPayPublicKey', width:40, sortable:true},
      {field:'cpPayPriKey', title:'CPPayPrivateKey', width:40, sortable:true},
      {field:'cpConfig', title:'特殊配置(登陆:注册)', width:40, sortable:true},
      {field:'openPayFlag', title:'充值状态', width:20, sortable:true,formatter:function(v){
              var s = {0:"开放",1:"关闭"};
              return s[v];
          }},
        {field:'chargeCloseTime', title:'充值关闭时间', width:80, sortable:true},
        {field:'version', title:'最新版本', width:40, sortable:true},
        {field:'lastVersionUrl', title:'下载地址', width:100, sortable:true},
        { field: 'op', title: '操作', width: 50,formatter:function(v,d,i){return '<a href="javascript:;" onclick="uploadApk('+i+')">上传</a>'}},
    ]],
    toolbar:'#easyui_toolbar'
  });

  $("#games").combobox({
    url:'<%=basePath%>/admin/games/getAllGamesSimple',
    valueField:'appID',
    textField:'name',
    onSelect:function(rec){
      $('#appID').val(rec.appID);
    }
  });


  $("#masters").combobox({
    url:'<%=basePath%>/admin/channelMaster/getAllMastersSimple',
    valueField:'masterID',
    textField:'masterName',
    onSelect:function(rec){
      $('#masterID').val(rec.masterID);
    }
  });

  function uploadApk(i) {
      //alert(i);
      var value = $("#channels").datagrid('getData').rows[i];
      //alert(value);
      $("#apk_upload").dialog('open').dialog('setTitle', '上传最新版本');
      $("#channelIDApk").val(value.channelID);
  }

  function uploadFormSubmit() {
      $('#apkForm').form('submit', {
          url:"<%=basePath%>/admin/uploadApk",
          onSubmit:function(){
              return $(this).form('validate');
          },
          success:function(result){
              var result = eval('('+result+')');
              if (result.state == 1) {
                  $('#apk_upload').dialog('close');
                  $("#channels").datagrid('reload');
              }
              $.messager.show({
                  title:'操作提示',
                  msg:result.msg
              })
          }
      })
  }

</script>

</body>
</html>
