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

<div id="admins">

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
         icon="icon-remove" onclick="javascript:deleteAdmin();">删除</a>
    </div>
  </div>

</div>

<div id="dialog_add" class="easyui-dialog u8_form"
     closed="true" buttons="#dlg-buttons" style="height: 240px;">
  <div class="ftitle">角色信息</div>
  <form id="fm" method="post" novalidate>
    <input type="hidden" name="id" />
    <input type="hidden" id="roleID" name="myRoleID" />

    <div class="u8_form_row">
      <label >用户名：</label>
      <input id="username" type="text" class="easyui-textbox" style="height: 25px;" name="username" maxlength="255" required="false"/>
    </div>

    <div class="u8_form_row">
      <label >密  码：</label>
      <input id="password" type="text" class="easyui-textbox" style="height: 25px;" name="password" maxlength="255" required="false"/>
    </div>

    <div class="u8_form_row">
      <label >角  色：</label>
      <%--<input type="text" class="easyui-textbox" name="permission" maxlength="255" novalidate />--%>
      <input id="roles" type="text" class="easyui-combobox" style="height: 25px;" name="allroles" maxlength="255"/>
    </div>

  </form>
</div>
<div id="dlg-buttons">
  <a href="javascript:void(0)" class="easyui-linkbutton c6"  onclick="saveUser()" style="width:90px">保 存</a>
  <a href="javascript:void(0)" class="easyui-linkbutton" onclick="javascript:$('#dialog_add').dialog('close')" style="width:90px">取 消</a>
</div>


<script type="text/javascript">

  var url;
  function showAddDialog(){
    $("#dialog_add").window({
      top:($(window).height() - 300) * 0.5,
      left:($(window).width() - 420) * 0.5
    });

    $("#dialog_add").dialog('open').dialog('setTitle', '添加管理员角色').css("overflow","hidden");

    $('#fm').form('clear');

    url = '<%=basePath%>/admin/saveAdmin';

  }

  function showEditDialog(){

    $("#dialog_add").window({
      top:($(window).height() - 300) * 0.5,
      left:($(window).width() - 420) * 0.5
    });


    var row = $('#admins').datagrid('getSelected');
    if(row){

      $("#dialog_add").dialog('open').dialog('setTitle', '编辑管理员角色').css("overflow","hidden");
      $('#fm').form('load', row);
      if(row.adminRoleID && row.adminRoleID > 0){
        $("#roles").combobox('setValue', row.adminRoleID);
        $('#roleID').val(row.adminRoleID);
      }

      url = '<%=basePath%>/admin/saveAdmin';

    }else{
      $.messager.show({
        title:'操作提示',
        msg:'请选择一条记录'
      })
    }
  }

  function deleteAdmin(){
    var row = $('#admins').datagrid('getSelected');
    if(row){
      $.messager.confirm(
              '操作确认',
              '确定要删除该管理员角色吗？(操作不可恢复)',
              function(r){
                if(r){
                  $.post('<%=basePath%>/admin/removeAdmin', {id:row.id}, function(result){
                    if (result.state == 1) {
                      $('#dialog_add').dialog('close');
                      $("#admins").datagrid('reload');
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
          $("#admins").datagrid('reload');
        }

        $.messager.show({
          title:'操作提示',
          msg:result.msg
        })
      }
    })

  }


  $("#admins").datagrid({
    height:'98%',
    url:'<%=basePath%>/admin/getAllAdmins',
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
    columns:[[
      {field:'id', title:'ID', width:40, sortable:true},
      {field:'username', title:'用户名', width:40, sortable:true},
      {field:'adminRoleName', title:'权限角色', width:40, sortable:true}
    ]],
    toolbar:'#easyui_toolbar'
  });

  $("#roles").combobox({
    url:'<%=basePath%>/admin/getAllAdminRoles',
    valueField:'id',
    textField:'roleName',
    onSelect:function(rec){
      $('#roleID').val(rec.id);
    }
  });

</script>

</body>
</html>
