<%--
  Created by IntelliJ IDEA.
  User: ant
  Date: 2016/8/22
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
<div id="menus">

</div>

<div id="easyui_toolbar" region="north" border="false">
  <div class="toolbar">
    <div style="float: left;margin-right: 3px;padding-top: 3px;">
      <input id="roles" type="text" class="easyui-combobox"  name="roles" maxlength="255"/>
    </div>

    <div class="datagrid-btn-separator"></div>

    <div style="float: left;">
      <a class="easyui-linkbutton" plain="true" icon="icon-add" onclick="javascript:showAddDialog();">新增菜单</a>
    </div>

    <div class="datagrid-btn-separator"></div>

    <div style="float: left;">
      <a class="easyui-linkbutton" plain="true" icon="icon-edit" onclick="javascript:showEditDialog();">编辑菜单</a>
    </div>
  </div>
</div>

<div id="dialog_add" class="easyui-dialog u8_form"
     closed="true" buttons="#dlg-buttons" style="height: 240px;">
  <div class="ftitle">系统功能</div>
  <form id="fm" method="post" novalidate>
    <input id="id" type="hidden" name="id" />
    <input id="parentID" type="hidden" name="parentID" />

    <div class="u8_form_row">
      <label >功能名称：</label>
      <input id="name" type="text" class="easyui-textbox" style="height: 25px;" name="name" maxlength="255" required="false"/>
    </div>

    <div class="u8_form_row">
      <label >父功能：</label>
      <input id="parents" type="text" class="easyui-combobox" style="height: 25px;" name="parents" maxlength="255"/>
    </div>

    <div class="u8_form_row">
      <label >访问地址：</label>
      <input id="path" type="text" class="easyui-textbox" style="height: 25px;" name="path" maxlength="255"/>
    </div>

  </form>
</div>

<div id="dialog_add_role" class="easyui-dialog u8_form"
     closed="true" buttons="#dlg-buttons_role" style="height: 240px;">
  <div class="ftitle">系统功能</div>
  <form id="fm_role" method="post" novalidate>
    <input id="topRole" type="hidden" name="topRole"/>
    <div class="u8_form_row">
      <label >角色名称：</label>
      <input id="roleName" type="text" class="easyui-textbox" style="height: 25px;" name="roleName" maxlength="255" required="true"/>
    </div>

    <%--<div class="u8_form_row">
      <label >拥有最高权限：</label>
      <input id="topPermission" type="checkbox" style="height: 25px;width:30px;" name="topPermission" />
    </div>--%>

    <div class="u8_form_row">
      <label >角色描述：</label>
      <input id="roleDesc" type="text" class="easyui-textbox" style="height: 25px;" name="roleDesc" maxlength="255" />
    </div>

  </form>
</div>

<div id="dlg-buttons">
  <a href="javascript:void(0)" class="easyui-linkbutton c6"  onclick="save()" style="width:90px">保 存</a>
  <a href="javascript:void(0)" class="easyui-linkbutton"  onclick="javascript:$('#dialog_add').dialog('close')" style="width:90px">取 消</a>
</div>

<div id="dlg-buttons_role">
  <a href="javascript:void(0)" class="easyui-linkbutton c6"  onclick="saveRole()" style="width:90px">保 存</a>
  <a href="javascript:void(0)" class="easyui-linkbutton"  onclick="javascript:$('#dialog_add_role').dialog('close')" style="width:90px">取 消</a>
</div>


<script type="text/javascript">

  function reload(){
    var adminRoleID = $("#roles").combobox("getValue");
    if(adminRoleID){
      $.post('<%=basePath%>/admin/getAllMenus',{adminRoleID:adminRoleID},function(data){
        $('#menus').treegrid('loadData',data);
      },'json');
    }

  }

  var url;
  function showAddDialog(){
    $("#dialog_add").window({
      top:($(window).height() - 300) * 0.5,
      left:($(window).width() - 420) * 0.5
    });

    $("#dialog_add").dialog('open').dialog('setTitle', '添加功能').css("overflow","hidden");

    $('#fm').form('clear');

    url = '<%=basePath%>/admin/saveSysMenu';

  }

  function showEditDialog(){

    $("#dialog_add").window({
      top:($(window).height() - 300) * 0.5,
      left:($(window).width() - 420) * 0.5
    });


    var row = $('#menus').treegrid('getSelected');
    if(row){

      $("#dialog_add").dialog('open').dialog('setTitle', '编辑功能').css("overflow","hidden");
      $('#fm').form('load', row);
      url = '<%=basePath%>/admin/saveSysMenu';

      if(row.parentID && row.parentID > 0){
        $("#parents").combobox('setValue', row.parentID);
        $('#parentID').val(row.parentID);
      }


    }else{
      $.messager.show({
        title:'操作提示',
        msg:'请选择一条记录'
      })
    }
  }


  function save(){
    $('#fm').form('submit', {
      url:url,
      onSubmit:function(){
        return $(this).form('validate');
      },
      success:function(result){
        var result = eval('('+result+')');

        if (result.state == 1) {
          $('#dialog_add').dialog('close');
          $("#menus").treegrid('reload' ,{
            onLoadSuccess:function(){
              reload();
              refreshParents();
            }
          });

        }

        $.messager.show({
          title:'操作提示',
          msg:result.msg
        })
      }
    })

  }

  function refreshParents(){
    $("#parents").combobox({
      url:'<%=basePath%>/admin/getAllRootMenus',
      valueField:'id',
      textField:'showName',
      onSelect:function(rec){
        $('#parentID').val(rec.id);
      }
    });
  }

  function refreshRoles(){
    $("#roles").combobox({
      url:'<%=basePath%>/admin/getAllAdminRoles',
      valueField:'id',
      textField:'roleName',
      prompt:"选择角色",
      icons:[
        {
          iconCls:'icon-add',
          handler:function(e){
            addRole();
          }
        },
        {
          iconCls:'icon-remove',
          handler: function (e) {
            removeRole();
          }
        }
      ],
      onSelect:function(rec){
        //$('#parentID').val(rec.id);
        //$("#menus").treegrid('reload');
        reload();
      }
//      onLoadSuccess:function(){
//        var data = $(this).combobox('getData');
//        if(data.length > 0){
//          $(this).combobox("setValue", data[data.length - 1].id);
//          reload();
//        }
//      }
    });
  }


  function onSysMenuChanged(currID){

    var adminRoleID = $("#roles").combobox("getValue");

    if(!adminRoleID){
      $.messager.show({
        title:'操作提示',
        msg:'请先选择一个角色'
      });

      $("input:checked").each(function(){
        var id = $(this).val();
        if(id == currID){
          $(this).attr("checked",false);
        }

      });

      return
    }

    var idLst = "";
    $("input:checked").each(function(){
      var id = $(this).val();
      idLst += id + ",";

    });


    $.post('<%=basePath%>/admin/updateRolePermission', {adminRoleID:adminRoleID,rolePermission:idLst}, function(result){
      if (result.state == 1) {
        reload();
      }else{
        alert(result.msg);
      }
    },'json');
  }

  function addRole(){

    $("#dialog_add_role").window({
      top:($(window).height() - 300) * 0.5,
      left:($(window).width() - 420) * 0.5
    });

    $("#dialog_add_role").dialog('open').dialog('setTitle', '添加角色').css("overflow","hidden");

    $('#fm_role').form('clear');


  }

  function removeRole(){

    var roleID = $("#roles").combobox("getValue");
    if (roleID == null || roleID <= 0){
      $.messager.show({
        title:'操作提示',
        msg:'请选择一个角色'
      })
      return;
    }

    $.messager.confirm(
            '操作确认',
            '确定要删除该角色吗？(删除之后，管理员信息可能需要变更)',
            function(r){
              if(r){
                $.post('<%=basePath%>/admin/removeAdminRole', {id:roleID}, function(result){
                  if (result.state == 1) {
                    refreshRoles();
                  }

                  $.messager.show({
                    title:'操作提示',
                    msg:result.msg
                  })

                }, 'json');
              }
            }
    );
  }


  function saveRole(){

    if($('#topPermission').is(':checked')){
      $("#topRole").val(1);
    }else{
      $("#topRole").val(0);
    }

    $('#fm_role').form('submit', {

      url:'<%=basePath%>/admin/saveAdminRole',
      onSubmit:function(){
        return $(this).form('validate');
      },
      success:function(result){
        var result = eval('('+result+')');

        if (result.state == 1) {
          $('#dialog_add_role').dialog('close');
          //$("#menus").treegrid('reload');
          refreshRoles();
          //reload();
        }

        $.messager.show({
          title:'操作提示',
          msg:result.msg
        })
      }
    })

  }

  $("#menus").treegrid({
    height:'98%',
    url:'<%=basePath%>/admin/getAllMenus',
    method:'POST',
    idField:'id',
    treeField:'name',
    checkbox:true,
    striped:true,
    fitColumns:true,
    singleSelect:true,
    pagination:true,
    nowrap:true,
    loadMsg:'数据加载中...',
    pageSize:20,
    pageList:[20,50,100],
    showFooter:true,
    columns:[[
//      {field:'id', title:'ID', width:40, sortable:true},
      {field:'name', title:'功能名称', width:40, sortable:true},
      {field:'selected', title:'选择', width:40, sortable:true,
//        editor:{
//          type:'checkbox',
//          options:{on:'1',off:'0'}
//        },
        formatter:function(value, row, index){

          if(row.isChecked){
            return '<input type="checkbox" checked="checked" value="'+row.id+'" onclick="javascript:onSysMenuChanged('+row.id+','+row.parentID+');"/>';
          }else{
            return '<input type="checkbox" value="'+row.id+'" onclick="javascript:onSysMenuChanged('+row.id+','+row.parentID+');"/>';
          }
        }
      }
    ]],
    toolbar:'#easyui_toolbar'
  });

  refreshParents();
  refreshRoles();

</script>

</body>
</html>
