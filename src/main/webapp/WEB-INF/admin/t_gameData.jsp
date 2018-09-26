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

<div style="width:90%;height: 80px;">
  <table>
    <tr style="height: 30px;">
      <td><input id="games" type="text" class="easyui-combobox" style="height: 25px" prompt="选择游戏" name="allgames" maxlength="255" /></td>
      <td class="u8_search_item"><input id="beginLoginTime" class="easyui-datetimebox" style="height: 25px" name="loginCreateTime" prompt="开始时间" data-options="showSeconds:true" style="width:220px"></td>
      <td class="u8_search_item"><input id="endLoginTime" class="easyui-datetimebox" style="height: 25px" name="loginCreateTime" prompt="结束时间" data-options="showSeconds:true" style="width:220px"></td>
      <td class="u8_search_item"><a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="doSearch();" style="width:80px">查  询</a></td>
      <td class="u8_search_item"><a href="javascript:void(0)" class="easyui-linkbutton c6" onclick="downloadSummary();" style="width:80px">下 载</a></td>
    </tr>
  </table>
</div>

<div class="easyui-panel" style="margin-top: 5px; height: 400px;border:hidden">

  <%--<table id="dg" style="height:400px;"></table>--%>
  <div id="data">

  </div>

</div>



<%--<div id="easyui_toolbar" region="north" border="false">--%>
  <%--<div class="toolbar">--%>
    <%--<div>--%>
      <%--<a class="easyui-linkbutton" plain="true"--%>
         <%--icon="icon-remove" onclick="javascript:exportData();">导出</a>--%>
    <%--</div>--%>
  <%--</div>--%>
<%--</div>--%>


<script type="text/javascript">


  var inited = false;


  function doClear(){

    $("#games").combobox("clear");
    $("#beginLoginTime").datetimebox('setValue', "");
    $("#endLoginTime").datetimebox('setValue', "");

  }

  function doSearch(){

    var appID = $("#games").combobox("getValue");
    var beginLoginTime =  $("#beginLoginTime").datetimebox('getValue');
    var endLoginTime =    $("#endLoginTime").datetimebox('getValue');
    $("#data").datagrid({
      queryParams:{
        appID:appID,
        beginTime:beginLoginTime,
        endTime:endLoginTime
      }
    });

  }

  function updateDataGrid(){

    if(inited){

      doSearch();

    }else{

      var appID = $("#games").combobox("getValue");
      var beginLoginTime =  $("#beginLoginTime").datetimebox('getValue');
      var endLoginTime =    $("#endLoginTime").datetimebox('getValue');

      inited = true;
      $("#data").datagrid({
        height:400,
        url:'<%=basePath%>/analytics/getGameSummaryData',
        queryParams:{
          appID:appID,
          beginTime:beginLoginTime,
          endTime:endLoginTime
        },
        method:'POST',
        idField:'id',
        striped:true,
        fitColumns:true,
        singleSelect:true,
        rownumbers:true,
        pagination:true,
        nowrap:true,
        loadMsg:'数据加载中...',
        pageSize:10,
        pageList:[10,20,50,100],
        showFooter:true,
        columns:[[
          {field:'currTime', title:'日期', width:30, sortable:true},
          //{field:'id', title:'ID', width:30, sortable:true},
          {field:'appName', title:'游戏名称', width:25, sortable:true},
          {field:'deviceNum', title:'新增设备', width:25, sortable:true},
          {field:'userNum', title:'新增用户', width:25, sortable:true},
          {field:'uniUserNum', title:'新增用户(去重)', width:30, sortable:true},
          {field:'totalUserNum', title:'总用户', width:20, sortable:true},
          {field:'dau', title:'活跃用户', width:20, sortable:true},
          {field:'ndau', title:'活跃用户(新)', width:25, sortable:true},
          {field:'wau', title:'7日活跃', width:20, sortable:true},
          {field:'mau', title:'30日活跃', width:20, sortable:true},
          //{field:'avg', title:'平均在线时长', width:30, sortable:true},
          {field:'payUserNum', title:'付费用户', width:20, sortable:true},
          {field:'totalPayUserNum', title:'总付费用户', width:30, sortable:true},
          {field:'newPayUserNum', title:'付费用户(新)', width:30, sortable:true},
          {field:'money', title:'收入(元)', width:20, sortable:true},
            {field:'arppu', title:'平均充值额(arppu)', width:40, sortable:true},
            {field:'arpu', title:'平均收入额(arpu)', width:40, sortable:true},
            {field:'payRate', title:'付费率', width:40, sortable:true}
        ]],
        toolbar:'#easyui_toolbar'
      });
    }



  }

  function downloadSummary(){
      var appID = $("#games").combobox("getValue");
      var beginTime =  $("#beginLoginTime").datetimebox('getValue');
      var endTime =    $("#endLoginTime").datetimebox('getValue');
      function post(URL, PARAMS) {
          var temp = document.createElement("form");
          temp.action = URL;
          temp.method = "post";
          temp.style.display = "none";
          for (var x in PARAMS) {
              var opt = document.createElement("textarea");
              opt.name = x;
              opt.value = PARAMS[x];
              // alert(opt.name)
              temp.appendChild(opt);
          }
          document.body.appendChild(temp);
          temp.submit();
          //return temp;
      }
      post('<%=basePath%>/analytics/downloadSummary',{'appID':appID, 'beginTime':beginTime, 'endTime':endTime});
  }

  $(function(){

    Date.prototype.Format = function(formatStr)
    {
      var str = formatStr;
      var Week = ['日','一','二','三','四','五','六'];

      str=str.replace(/yyyy|YYYY/,this.getFullYear());
      str=str.replace(/yy|YY/,(this.getYear() % 100)>9?(this.getYear() % 100).toString():'0' + (this.getYear() % 100));

      var monthChina = this.getMonth() + 1;
      str=str.replace(/MM/,monthChina>9?monthChina.toString():'0' + monthChina);
      str=str.replace(/M/g,monthChina);

      str=str.replace(/w|W/g,Week[this.getDay()]);

      str=str.replace(/dd|DD/,this.getDate()>9?this.getDate().toString():'0' + this.getDate());
      str=str.replace(/d|D/g,this.getDate());

      str=str.replace(/hh|HH/,this.getHours()>9?this.getHours().toString():'0' + this.getHours());
      str=str.replace(/h|H/g,this.getHours());
      str=str.replace(/mm/,this.getMinutes()>9?this.getMinutes().toString():'0' + this.getMinutes());
      str=str.replace(/m/g,this.getMinutes());

      str=str.replace(/ss|SS/,this.getSeconds()>9?this.getSeconds().toString():'0' + this.getSeconds());
      str=str.replace(/s|S/g,this.getSeconds());

      return str;
    }

    var format = "yyyy-MM-dd HH:mm:ss";
    var d = new Date();
    var lastDate = new Date(d.getFullYear(),d.getMonth(),d.getDate()-30);

    var lastF = lastDate.Format(format);
    var dF = d.Format(format);

    $("#beginLoginTime").datetimebox('setValue', lastF);
    $("#endLoginTime").datetimebox('setValue', dF);

    $("#games").combobox({
      url:'<%=basePath%>/admin/games/getAllGamesSimple',
      valueField:'appID',
      textField:'name',
      onSelect:function(rec){
          inited = false;
        if(!inited){
          updateDataGrid();

        }
      },
      onLoadSuccess: function () { //加载完成后,设置选中第一项
        var data = $(this).combobox('getData')
        if (data.length > 0) {
          $(this).combobox('select', data[0].appID);
        }
      }
    });

  });



</script>

</body>
</html>
