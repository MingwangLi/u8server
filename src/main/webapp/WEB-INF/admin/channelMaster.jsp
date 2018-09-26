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
    <div id="masters">

    </div>

    <div id="easyui_toolbar" region="north" border="false"
          >

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
             icon="icon-remove" onclick="javascript:deleteMaster();">删除</a>
        </div>

        <div id="tb" style="float: right;">
          <input id="search_box" class="easyui-searchbox" style="width: 250px;height: 25px;"  data-options="searcher:doSearch,prompt:'请输入查询词',menu:'#search_menu'" />
          <div id="search_menu" style="width:100px">
            <div data-options="name:'channel_name'">渠道名称</div>
            <div data-options="name:'channel_id'">渠道商ID</div>
            <div data-options="name:'name_suffix'">名称后缀</div>
            <div data-options="name:'verify_class'">处理类</div>
          </div>
        </div>
      </div>

    </div>

    <div id="dialog_add" class="easyui-dialog u8_form"
         closed="true" buttons="#dlg-buttons">
      <div class="ftitle">渠道商信息</div>
      <form id="fm" method="post" novalidate>
        <input type="hidden" name="masterID" value="0"/>
        <div class="u8_form_row">
          <label >渠道商名称：</label>
          <input type="text" class="easyui-textbox" style="height: 25px;" name="masterName" maxlength="255" required="false" />
        </div>

        <div class="u8_form_row">
          <label >SDK名称：</label>
          <input type="text" class="easyui-textbox" style="height: 25px;" name="sdkName" maxlength="255" required="false" />
        </div>
        <div class="u8_form_row">
          <label >用户名后缀：</label>
          <input type="text" class="easyui-textbox" style="height: 25px;" name="nameSuffix" maxlength="255" required="false" />
        </div>

        <div class="u8_form_row">
          <label >登录认证地址：</label>
          <input type="text" class="easyui-textbox" style="height: 25px;" name="authUrl" maxlength="1024" required="false" />
        </div>

        <div class="u8_form_row">
          <label >支付回调地址：</label>
          <input type="text" class="easyui-textbox" style="height: 25px;" name="payCallbackUrl" maxlength="1024" required="false" />
        </div>

        <div class="u8_form_row">
          <label >渠道下单地址：</label>
          <input type="text" class="easyui-textbox" style="height: 25px;" name="orderUrl" maxlength="1024" novalidate />
        </div>

        <div class="u8_form_row">
          <label >脚本类路径：</label>
          <input type="text" class="easyui-textbox" style="height: 25px;" name="verifyClass" maxlength="1024" required="false" />
        </div>
      </form>
    </div>
    <div id="dlg-buttons">
      <a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="saveUser()" style="width:90px">保 存</a>
      <a href="javascript:void(0)" class="easyui-linkbutton" onclick="javascript:$('#dialog_add').dialog('close')" style="width:90px">取 消</a>
    </div>


    <script type="text/javascript">

      var url;
      function showAddDialog(){
        $("#dialog_add").window({
          top:($(window).height() - 300) * 0.5,
          left:($(window).width() - 400) * 0.5
        });

        $("#dialog_add").dialog('open').dialog('setTitle', '添加渠道商');

        $('#fm').form('clear');

        url = '<%=basePath%>/admin/channelMaster/saveMaster';

      }

      function showEditDialog(){

        $("#dialog_add").window({
          top:($(window).height() - 300) * 0.5,
          left:($(window).width() - 400) * 0.5
        });


        var row = $('#masters').datagrid('getSelected');
        if(row){

          $("#dialog_add").dialog('open').dialog('setTitle', '编辑渠道商');
          $('#fm').form('load', row);
          url = '<%=basePath%>/admin/channelMaster/saveMaster';

        }else{
          $.messager.show({
            title:'操作提示',
            msg:'请选择一条记录'
          })
        }
      }

      function deleteMaster(){
        var row = $('#masters').datagrid('getSelected');
        if(row){
          $.messager.confirm(
            '操作确认',
            '确定要删除该渠道商吗？(操作不可恢复)',
            function(r){
              if(r){
                $.post('<%=basePath%>/admin/channelMaster/removeMaster', {currMasterID:row.masterID}, function(result){
                  if (result.state == 1) {
                    $('#dialog_add').dialog('close');
                    $("#masters").datagrid('reload');
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
              $("#masters").datagrid('reload');
            }

            $.messager.show({
              title:'操作提示',
              msg:result.msg
            })
          }
        })

      }



      function doSearch(value, name){

        if(name == "channel_name"){
          $("#masters").datagrid({
            queryParams:{
              masterName:value
            }
          });
        }else if(name == "channel_id"){
          $("#masters").datagrid({
            queryParams:{
              masterID:value
            }
          });
        }else if(name == "name_suffix"){
          $("#masters").datagrid({
            queryParams:{
              nameSuffix:value
            }
          });
        }else if(name == "verify_class"){
          $("#masters").datagrid({
            queryParams:{
              verifyClass:value
            }
          });
        }

      }


      $("#masters").datagrid({
        height:'98%',
        url:'<%=basePath%>/admin/channelMaster/getAllChannelMasters',
        method:'POST',
        idField:'masterID',
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
          {field:'masterID', title:'ID', width:40, sortable:true},
          {field:'sdkName', title:'使用的SDK', width:60, sortable:true},
          {field:'masterName', title:'渠道商名称', width:60, sortable:true},
          {field:'nameSuffix', title:'用户名后缀', width:60, sortable:true},
          {field:'verifyClass', title:'SDK脚本路径', width:120, sortable:true}
        ]],
        toolbar:'#easyui_toolbar'
      });

    </script>

  </body>
  </html>
