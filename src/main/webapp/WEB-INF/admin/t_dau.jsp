<%--
  Created by IntelliJ IDEA.
  User: xiaohei
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

  <script type="text/javascript" src="<%=basePath%>/js/u8server.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.easyui.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>

  <script type="text/javascript" src="<%=basePath%>/js/highcharts.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/dark-unica.js"></script>


</head>
<body>

<div class="easyui-panel infoBar2" style="height: 4%;">

  <ul>
    <li>
      <span><input id="games" type="text" class="easyui-combobox" style="width: 120px;height: 25px;" prompt="选择游戏" name="allgames"  /></span>
    </li>
    <li style="padding:5px 5px 0 10px;"><span><input id="beginTime" class="easyui-datebox" style="height: 25px" name="beginCreateTime" prompt="开始时间" data-options="showSeconds:true" style="width:140px"></span><span style="margin-left: 5px;">~</span></li>
    <li style="padding:5px 10px 0 0px;"><span><input id="endTime" class="easyui-datebox" style="height: 25px" name="beginCreateTime" prompt="结束时间" data-options="showSeconds:true" style="width:140px"></span></li>
    <li style="padding:5px 10px 0 10px;">
      <a id="btnUpdate" href="javascript:void(0)" class="easyui-linkbutton c6" style="width:80px">刷新数据</a>
    </li>
  </ul>

</div>

<div style="margin-top: 20px;">
  <div id="dau" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="玩家日活跃"></div>
</div>

<div style="margin-top: 20px;">
  <div id="wau" class="easyui-panel" style="height: 300px;" title="玩家周活跃"></div>
</div>

<div style="margin-top: 20px;">
  <div id="mau" class="easyui-panel" style="height: 300px;" title="玩家月活跃"></div>
</div>

<div style="margin-top: 20px;">
  <div id="daumau" class="easyui-panel" style="height: 300px;" title="玩家总体粘度"></div>
</div>

<div style="margin-top: 20px;">
  <div id="avg" class="easyui-panel" style="height: 300px;" title="玩家平均在线时长"></div>
</div>

<script type="text/javascript">

  $(function(){

    $("#btnUpdate").click(function(){
      reqChartData();
    });

    function reqChartData(){

      var appID = $("#games").combobox("getValue");
      var beginTime = $("#beginTime").datebox("getValue");
      var endTime = $("#endTime").datebox("getValue");

      if(!appID) {
        return;
      }

      $.post('<%=basePath%>/analytics/dauData', {appID:appID, beginTime:beginTime,endTime:endTime}, function(result){
        if (result.state == 1) {
          showBaseInfo(result.data)
        }else{
          $.messager.show({
            title: '操作提示',
            msg: '操作失败'
          });
        }

      }, 'json');
    }

    function showBaseInfo(data){

      showDAUChart(data.keyCategory, data.dauData, data.ndauData, data.dauAvg);
      showWAUChart(data.keyCategory, data.wauData, data.wauAvg);
      showMAUChart(data.keyCategory, data.mauData, data.mauAvg);
      showDAUMAUChart(data.keyCategory, data.dauMauData, data.dauMauAvg);
      showAVGChart(data.keyCategory, data.avgData, data.timeAvg);

    }

    function showDAUChart(categories, dauData, ndauData, avg){
      $('#dau').highcharts({
        title: {
          text: '平均日活跃用户数:'+Highcharts.numberFormat(avg,0)+'个',
          x: -20 //center
        },
        xAxis: {
          categories:eval(categories),
          tickInterval:Math.ceil(categories.length / 15)
        },
        yAxis: {
          title: {
            text: ' '
          }
        },
        tooltip: {
          shared: true,
          formatter:function(){

            var html = '' + this.points[0].x + ":";
            var index;
            for(index=0;index<this.points.length;index += 1){
              if(this.points[index].series.name == '所有玩家'){
                html +=  '<br/>所有玩家: ' + this.points[index].y + ' 个';

              }else{
                html += '<br/>新玩家: ' + this.points[index].y + ' 个';
              }
            }

            return html;
          }
        },
        legend: {
          layout: 'vertical',
          align: 'right',
          verticalAlign: 'middle',
          borderWidth: 0
        },
        series: [
          {
            name:'所有玩家',
            data:eval(dauData)
          },
          {
            name:'新玩家',
            data:eval(ndauData)
          }
        ]
      });
    }

    function showWAUChart(categories, wauData, avg){
      $('#wau').highcharts({
        title: {
          text: '平均周活跃用户数:'+Highcharts.numberFormat(avg,0)+"个",
          x: -20 //center
        },
        xAxis: {
          categories:eval(categories),
          tickInterval:Math.ceil(categories.length / 15)
        },
        yAxis: {
          title: {
            text: ' '
          }
        },
        tooltip: {
          valueSuffix: '个'
        },
        legend: {
          layout: 'vertical',
          align: 'right',
          verticalAlign: 'middle',
          borderWidth: 0
        },
        series: [
          {
            name:'所有玩家',
            data:eval(wauData)
          }
        ]
      });
    }

    function showMAUChart(categories, mauData,avg){
      $('#mau').highcharts({
        title: {
          text: '平均月活跃用户数:'+Highcharts.numberFormat(avg,0)+'个',
          x: -20 //center
        },
        xAxis: {
          categories:eval(categories),
          tickInterval:Math.ceil(categories.length / 15)
        },
        yAxis: {
          title: {
            text: ' '
          }
        },
        tooltip: {
          valueSuffix: '个'
        },
        legend: {
          layout: 'vertical',
          align: 'right',
          verticalAlign: 'middle',
          borderWidth: 0
        },
        series: [
          {
            name:'所有玩家',
            data:eval(mauData)
          }
        ]
      });
    }

    function showDAUMAUChart(categories, dauMauData, avg){
      $('#daumau').highcharts({
        title: {
          text: '玩家平均粘度:'+Highcharts.numberFormat(avg,2),
          x: -20 //center
        },
        xAxis: {
          categories:eval(categories),
          tickInterval:Math.ceil(categories.length / 15)
        },
        yAxis: {
          title: {
            text: ' '
          }
        },
        tooltip: {
          formatter:function(){
            return Highcharts.numberFormat(this.y,2);
          }
        },
        legend: {
          layout: 'vertical',
          align: 'right',
          verticalAlign: 'middle',
          borderWidth: 0
        },
        series: [
          {
            name:'玩家粘度(DAU/MAU)',
            data:eval(dauMauData)
          }
        ]
      });
    }

    function showAVGChart(categories, avgData, avg){
      $('#avg').highcharts({
        title: {
          text: '玩家在线平均时长:'+Highcharts.numberFormat(avg,0)+'分钟',
          x: -20 //center
        },
        xAxis: {
          categories:eval(categories),
          tickInterval:Math.ceil(categories.length / 15)
        },
        yAxis: {
          title: {
            text: ' '
          }
        },
        tooltip: {
          valueSuffix: '分钟'
        },
        legend: {
          layout: 'vertical',
          align: 'right',
          verticalAlign: 'middle',
          borderWidth: 0
        },
        series: [
          {
            name:'平均在线时长(分钟)',
            data:eval(avgData)
          }
        ]
      });
    }


    $("#games").combobox({
      url:'<%=basePath%>/admin/games/getAllGamesSimple',
      valueField:'appID',
      textField:'name',
      onSelect:function(rec){
        reqChartData();
      },
      onLoadSuccess:function(){

        $("#beginTime").datebox("setValue", lastWeekFormatTime());
        $("#endTime").datebox("setValue", currFormatTime());

        var data = $('#games').combobox('getData');
        $("#games").combobox('select',data[0].appID);


        //reqChartData();

      }
    });

  });



</script>

</body>
</html>

