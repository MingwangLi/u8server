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
  <div id="chargePoint">

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

      <div class="datagrid-btn-separator"></div>

      <div style="float: left;">
        <a class="easyui-linkbutton" plain="true"
           icon="icon-remove" onclick="javascript:deleteGame();">删除</a>
      </div>

      <div id="tb" style="float: right;">
        <input id="search_box" class="easyui-searchbox" style="width: 250px;height: 25px;"  data-options="searcher:doSearch,prompt:'请输入查询词',menu:'#search_menu'" />
        <div id="search_menu" style="width:100px;">
          <div data-options="name:'channelID'">渠道号</div>
          <div data-options="name:'ganme'">游戏名称</div>
          <div data-options="name:'channelMaster'">渠道名称</div>
        </div>
      </div>
    </div>

  </div>

  <div id="dialog_add" class="easyui-dialog u8_form"
       closed="true" buttons="#dlg-buttons" style="height: 450px;width: 500px;">
    <div class="ftitle">计费点信息</div>
    <form id="fm" method="post" novalidate>
      <input id="id" type="hidden" name="id" />
      <div class="u8_form_row">
        <label >渠道号：</label>
        <input id = "channelID" type="text" class="easyui-textbox" style="height: 25px;" name="channelID" maxlength="255" required="false" />
      </div>

      <div class="u8_form_row">
        <label >金额(元)：</label>
        <input id="money" type="text" class="easyui-textbox" style="height: 25px;" name="money" maxlength="255"/>
      </div>

      <div class="u8_form_row">
        <label >CP商品代码：</label>
        <input id="chargeCode" type="text" class="easyui-textbox" style="height: 25px;" name="chargeCode" maxlength="255"/>
      </div>

      <div class="u8_form_row">
        <label >渠道商品代码：</label>
        <input id="channelChargeCode" type="text" class="easyui-textbox" style="height: 25px;" name="channelChargeCode" maxlength="255"/>
      </div>

      <div class="u8_form_row">
        <label >商品名称：</label>
        <input id="chargeName" type="text" class="easyui-textbox" style="height: 25px;" name="chargeName" maxlength="1024" novalidate />
      </div>

      <div class="u8_form_row">
        <label >商品描述：</label>
        <input id = "chargeDesc" type="text" class="easyui-textbox" style="height: 25px;" name="chargeDesc" maxlength="1024" novalidate />
      </div>

    </form>
  </div>
  <div id="dlg-buttons">
    <a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="saveGame()" style="width:90px">保 存</a>
    <a href="javascript:void(0)" class="easyui-linkbutton" onclick="javascript:$('#dialog_add').dialog('close')" style="width:90px">取 消</a>
  </div>

  <script type="text/javascript">

    var url;
    function showAddDialog(){
      $("#dialog_add").window({
        top:($(window).height() - 300) * 0.5,
        left:($(window).width() - 400) * 0.5
      });

      $("#dialog_add").dialog('open').dialog('setTitle', '添加计费点');
      //refreshAdmins();
      $('#fm').form('clear');
      url = '<%=basePath%>/admin/chargePointAdd';

    }

    function showEditDialog(){

      $("#dialog_add").window({
        top:($(window).height() - 300) * 0.5,
        left:($(window).width() - 400) * 0.5
      });


      var row = $('#chargePoint').datagrid('getSelected');
      if(row){

        $("#dialog_add").dialog('open').dialog('setTitle', '编辑计费点').css("overflow","hidden");
        $('#fm').form('load', row);
        url = '<%=basePath%>/admin/chargePointEdit';
        //refreshAdmins();
          //数据回显
          $.post(url, {id:row.id}, function(result){
            $("#channelID").val(result.channelID);
              $("#money").val(result.money);
              $("#chargeCode").val(result.chargeCode);
              $("#channelChargeCode").val(result.channelChargeCode);
              $("#chargeName").val(result.chargeName);
              $("#chargeDesc").val(result.chargeDesc);
              $("#id").val(result.id);
          });
          url = '<%=basePath%>/admin/chargePointAdd';

      }else{
        $.messager.show({
          title:'操作提示',
          msg:'请选择一条记录'
        })
      }
    }

    function deleteGame(){
      var row = $('#chargePoint').datagrid('getSelected');
      if(row){
        $.messager.confirm(
          '操作确认',
          '确定要删除该渠道商吗？(操作不可恢复)',
          function(r){
            if(r){
              $.post('<%=basePath%>/admin/chargePointDelete', {id:row.id}, function(result){
                if (result.state == 1) {
                  $('#dialog_add').dialog('close');
                  $("#chargePoint").datagrid('reload');
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

    function saveGame(){

      $('#fm').form(
          'submit', {
            url:url,
            onSubmit:function(){
              return $(this).form('validate');
            },
            success:function(result){
              var result = eval('('+result+')');

              if (result.state == 1) {
               $('#dialog_add').dialog('close');
                $("#chargePoint").datagrid('reload');
              }

              $.messager.show({
               title:'操作提示',
                  msg:result.msg
              })
            }
          }
      )

    }

    function refreshAdmins(){
      $("#admins").combobox({
        url:'<%=basePath%>/admin/getAllAdminList?appID='+$("#appID").val(),
        valueField:'id',
        textField:'username',
        multiple:true,
//        onSelect:function(rec){
//          //$('#parentID').val(rec.id);
//          //$("#menus").treegrid('reload');
//          //reload();
//        }
      onLoadSuccess:function(){
        var data = $(this).combobox('getData');
        if(data.length > 0){
          var ids = "";
          var realIndex = 0;
          for(var i=0;i<data.length;i++){

            if(data[i].checkedState == 1){

              if(realIndex == 0){
                ids = ids + data[i].id;
              }else{
                ids = ids + "," + data[i].id;
              }

              realIndex = realIndex + 1;
            }
          }
          $(this).combobox("setValues", ids.split(","));
        }
      }
      });
    }

    function doSearch(value, name){
      if(name == "channelID"){
        $("#chargePoint").datagrid({
          queryParams:{
              channelID:value
          }
        });
      }else if (name == "ganme") {
          $("#chargePoint").datagrid({
              queryParams:{
                  ganme:value
              }
          });
      }else if (name == "channelMaster") {
          $("#chargePoint").datagrid({
              queryParams:{
                  channelMaster:value
              }
          });
      }
    }

    $("#chargePoint").datagrid({
      height:'77%',
      url:'<%=basePath%>/admin/chargePointList',
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
        {field:'channelID', title:'渠道号', width:20, sortable:true},
          {field:'ganme', title:'游戏', width:60, sortable:true},
          {field:'channelMaster', title:'渠道商', width:60, sortable:true},
        {field:'money', title:'金额(元)', width:40, sortable:true},
          {field:'chargeCode', title:'CP商品代码', width:40, sortable:true},
          {field:'channelChargeCode', title:'渠道商品代码', width:40, sortable:true},
        {field:'chargeName', title:'商品名称', width:60, sortable:true},
        {field:'chargeDesc', title:'商品描述', width:60, sortable:true},
        {field:'createTime', title:'创建时间', width:100, sortable:true,formatter:function(val){
                return formattime(val);
            }},
        {field:'updateTime', title:'修改时间', width:100, sortable:true,formatter:function(val){
                return formattime(val);
            }},
        {field:'createBy', title:'创建人', width:50, sortable:true},
        {field:'status', title:'状态', width:30, sortable:true,formatter:function(val) {
            return val == 1?"启用":"关闭";
            }}
      ]],
      toolbar:'#easyui_toolbar'
    });

    function formattime(val) {

        var year=parseInt(val.year)+1900;

        var month=(parseInt(val.month)+1);

        month=month>9?month:('0'+month);

        var date=parseInt(val.date);

        date=date>9?date:('0'+date);

        var hours=parseInt(val.hours);

        hours=hours>9?hours:('0'+hours);

        var minutes=parseInt(val.minutes);

        minutes=minutes>9?minutes:('0'+minutes);

        var seconds=parseInt(val.seconds);

        seconds=seconds>9?seconds:('0'+seconds);

        var time=year+'-'+month+'-'+date+' '+hours+':'+minutes+':'+seconds;

        return time;

    }


  </script>

</body>
</html>
