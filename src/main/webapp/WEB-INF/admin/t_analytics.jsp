<%--
  Created by IntelliJ IDEA.
  User: lizhong
  Date: 2017/12/4
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
  <script type="text/javascript" src="<%=basePath%>/js/u8server.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.easyui.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>
  <style>
    .currentData ul{
      margin-top: 0;
      margin-bottom: 0;
      padding-left: 0;
      border-top: 1px solid #CCC;
      border-left: 1px solid #CCC;
      height: 30px;
      width: 748px;
    }
    .currentData ul li{
      float: left;
      list-style-type: none;
      border-bottom: 1px solid #CCC;
      border-right: 1px solid #CCC;
      text-align: center;
      height: 30px;
      line-height: 30px;
      width: 181px;
    }
  </style>
</head>
<body>
<div style="height: 70px;" class="easyui-panel infoBar2">
  <ul>
    <li><span><input id="games" type="text" class="easyui-combobox" style="height: 25px" prompt="选择游戏" name="allgames" maxlength="255"/></span></li>
    <li><span><input id="channels" type="text" class="easyui-combobox" style="height: 25px" prompt="选择渠道" name="allchannels" maxlength="255"/></span></li>
    <li style="padding:5px 5px 0 10px;"><span><input id="beginTime" class="easyui-datebox" style="height: 25px" name="beginCreateTime" prompt="开始时间" data-options="showSeconds:true" style="width:140px"></span><span style="margin-left: 5px;">~</span></li>
    <li style="padding:5px 10px 0 0px;"><span><input id="endTime" class="easyui-datebox" style="height: 25px" name="beginCreateTime" prompt="结束时间" data-options="showSeconds:true" style="width:140px"></span></li>
    <li style="padding:5px 5px 0 10px;"><span><a id="btnUpdate" href="javascript:void(0)" class="easyui-linkbutton c6" style="width:100px">查询</a></span></li>
  </ul>
</div>

<%--<h2>功能待实现</h2>
<p>The tabs height is auto adjusted according to tab panel content.</p>--%>
<div style="margin:20px 0;color: whitesmoke">
  <h1>总收益：￥<span id = "totalMoney"></span></h1>
  <h1>总新增：<span id = "totalReg"></span></h1>
  <h1>总付费用户数：<span id = "totalCostNum"></span></h1>
  <%--<h1>总付费率(今日)：<span id = "totalPayRate"></span></h1>--%>
  <h1>总新增付费用户数：<span id = "totalNewPayNum"></span></h1>
  <h1>总新增付费率：<span id = "totalNewPayRate"></span></h1>
</div>
<table id="secondExpert" class="easyui-datagrid" style="width:750px;"data-options="pagination:false">
  <thead>
  <tr>
    <th data-options="field:'appName',width:100">游戏名称</th>
    <th data-options="field:'channelName',width:100">渠道名称</th>
    <th data-options="field:'totalNewreg',width:100">新增量</th>
    <th data-options="field:'totalCostNum',width:100">付费人数</th>
    <th data-options="field:'totalMoney',width:100">收益</th>
    <th data-options="field:'totalNewCostNum',width:100">新增付费量</th>
    <th data-options="field:'payOfRegistRate',width:100">新增付费率</th>
  </tr>
  </thead>
</table>

<%--<div class="easyui-tabs" style="width:770px;height:auto">
  <div title="收入/付费人数" style="padding:10px" class="currentData">
    <ul id = "profit" style="background-color: #6a7d91;">
      <li>游戏名称</li>
      <li>渠道名称</li>
      <li>收益</li>
      <li>付费人数</li>
    </ul>
  </div>
  <div title="新增/新增付费率" style="padding:10px" class="currentData">
    <ul id = "newreg" style="background-color: #6a7d91">
      <li>游戏名称</li>
      <li>渠道名称</li>
      <li>新增量</li>
      <li>新增付费率</li>
    </ul>
  </div>--%>
  <%--<div title="付费率(今日)" style="padding:10px" class="currentData">
    <ul id = "payrate" style="background-color: #6a7d91">
      <li>游戏名称</li>
      <li>渠道名称</li>
      <li>付费率</li>
      <li>付费率</li>
    </ul>
  </div>--%>
</div>

<script  type="text/javascript">
 $(function () {
       $("#btnUpdate").click(function () {
            reqChartData();
        });
        function reqChartData(){
            var beginTime = $("#beginTime").datebox("getValue") ;
            var endTime = $("#endTime").datebox("getValue");
            var appID = $("#games").combobox("getValue");
            if(!appID) {
                return;
            }
            var appIDList = new Array() ;
            if(appID == 0){
                var data = $('#games').combobox('getData');
                for(var i = 1 ;i<data.length; i++){
                   appIDList.push(data[i].appID);
                }
            }
            var masterName = $("#channels").combobox("getText");
            $.post('<%=basePath%>/analytics/currTimeData', {appID:appID, beginTime:beginTime,endTime:endTime,game_datas:appIDList.join(","),masterName:masterName}, function(result){
                if (result.state == 1) {
                    //showBaseInfo(result.data)
                    //alert(result.data.profitAndNewReg);
                    $("#totalCostNum").text(result.data.totalCostNum);
                    $("#totalMoney").text(result.data.totalMoney);
                    $("#totalNewPayNum").text(result.data.totalNewCostNum);
                    $("#totalReg").text(result.data.totalNewreg);
                    $("#totalNewPayRate").text((result.data.totalNewCostNum*100/result.data.totalNewreg).toFixed(2) + "%");
                    $('#secondExpert').datagrid('loadData',result.data.profitAndNewReg);
                }else{
                    $.messager.show({
                        title: '操作提示',
                        msg: '操作失败'
                    });
                }
            }, 'json');

        }
      /* function showBaseInfo(data){
           $("#profit").siblings().remove();
           var profit_len = data.profit_json.length;
           $("#totalCostNum").text(data.profit_json[profit_len - 1]);
           $("#totalMoney").text(data.profit_json[profit_len - 2]);
           data.profit_json.splice(profit_len - 2,2);
           $.each(data.profit_json,function (index,item) {
               var ul = '<ul><li>';
               ul += item.appName + '</li><li>';
               ul += item.channelName + '</li><li>';
               ul += Math.round(parseFloat(item.totalMoney)*100)/100 + '</li><li>';
               ul += item.totalCostNum + '</li></ul>';
               $("#profit").after(ul);
           });
           $("#newreg").siblings().remove();
           var newreg_len = data.newreg_json.length;
           $("#totalNewPayRate").text((data.newreg_json[newreg_len - 1]*100/data.newreg_json[newreg_len - 2]).toFixed(2) + "%");
           $("#totalNewPayNum").text(data.newreg_json[newreg_len - 1]);
           $("#totalReg").text(data.newreg_json[newreg_len - 2]);

           data.newreg_json.splice(newreg_len - 2,2);
           $.each(data.newreg_json,function (index,item) {
               var ul = '<ul><li>';
               ul += item.appName + '</li><li>';
               ul += item.channelName + '</li><li>';
               ul += item.totalNewreg + '</li><li>';
               ul += (item.totalNewCostNum*100/item.totalNewreg).toFixed(2) + '%' + '</li></ul>'
               $("#newreg").after(ul);
           });
       }*/
      $("#games").combobox({
          url: '<%=basePath%>/admin/games/getAllGamesSimple',
          valueField: 'appID',
          textField: 'name',
          onSelect: function (rec) {
              reqChartData();
          },
          loadFilter:function (data) {
              var a = {"appID":0,"name":"全部游戏"};
              data.unshift(a);
            return data;
          },
          onLoadSuccess: function () {
              $("#beginTime").datebox("setValue", currFormatTime());
              $("#endTime").datebox("setValue", currFormatTime());
              var data = $('#games').combobox('getData');
              $("#games").combobox('select', data[0].appID);
              //reqChartData();

          }
      });
      $("#channels").combobox({
          url: '<%=basePath%>/admin/channelMaster/getAllMastersSimple',
          valueField: 'masterID',
          textField: 'masterName',
          onSelect: function (rec) {
              reqChartData();
          },
          loadFilter:function (data) {
              var a = {"masterID":0,"masterName":"全部渠道"};
              data.unshift(a);
            return data;
          },
          onLoadSuccess: function () {
              $("#beginTime").datebox("setValue", currFormatTime());
              $("#endTime").datebox("setValue", currFormatTime());
              var data = $('#channels').combobox('getData');
              $("#channels").combobox('select', data[0].masterName);
              //reqChartData();
          }
      });

 });

</script>
</body>
</html>
