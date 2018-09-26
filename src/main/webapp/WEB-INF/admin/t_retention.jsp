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

<div class="easyui-panel infoBar2" style="height: 12%;">

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

<div style="margin-top: 20px;width: 100%;">
  <div style="width: 24.8%;float: left;padding-left: 2px;overflow: hidden;">
    <div class="easyui-panel"  style="height: 60px;" title="次日留存平均值">
      <span class="u8_float_left_txt">AVG</span>
      <span id="dayAvg" class="u8_float_right_txt">0.0%</span>
    </div>
  </div>
  <div style="width: 24%;float:left;padding-left: 16px;overflow: hidden;">
    <div class="easyui-panel"  style="height: 60px;" title="3日留存平均值">
      <span class="u8_float_left_txt">AVG</span>
      <span id="tdayAvg" class="u8_float_right_txt">0.0%</span>
    </div>
  </div>
  <div style="width:24%;float: left;padding-left: 16px;overflow: hidden;">
    <div class="easyui-panel"  style="height: 60px;" title="7日留存平均值">
      <span class="u8_float_left_txt">AVG</span>
      <span id="wdayAvg" class="u8_float_right_txt">0.0%</span>
    </div>
  </div>

  <div style="width: 24%;float:left;padding-left: 16px;overflow: hidden;">
    <div class="easyui-panel" style="height: 60px;" title="30日留存平均值">
      <span class="u8_float_left_txt">AVG</span>
      <span id="mdayAvg" class="u8_float_right_txt">0.0%</span>
    </div>
  </div>

</div>

<div style="padding-top: 20px;clear:both;">
  <div id="retention" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="玩家留存"></div>
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

      $.post('<%=basePath%>/analytics/retentionData', {appID:appID, beginTime:beginTime,endTime:endTime}, function(result){
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

      showDayChart(data.keyCategory, data.dayArray, data.tdayArray, data.wdayArray, data.mdayArray);

      $("#dayAvg").html(Highcharts.numberFormat(data.dayAvg,2)+"%");
      $("#tdayAvg").html(Highcharts.numberFormat(data.tdayAvg,2)+"%");
      $("#wdayAvg").html(Highcharts.numberFormat(data.wdayAvg,2)+"%");
      $("#mdayAvg").html(Highcharts.numberFormat(data.mdayAvg,2)+"%");

    }

    function showDayChart(categories, dayArray, tdayArray,wdayArray,mdayArray){
      $('#retention').highcharts({
        title: {
          text: '玩家留存',
          x: -20 //center
        },
        xAxis: {
          categories:eval(categories),
          tickInterval:Math.ceil(categories.length / 15)
        },
        yAxis: {
          title: {
            text: ' '
          },
          labels:{
            formatter:function(){
              return this.value + "%";
            }
          }
        },
        tooltip: {
          shared: true,
          formatter:function(){

            var html = '' + this.points[0].x + ":";
            var index;
            for(index=0;index<this.points.length;index += 1){
              if(this.points[index].series.name == '次日留存'){
                html +=  '<br/>次日留存: ' + Highcharts.numberFormat(this.points[index].y,2) + ' %';
              }else if(this.points[index].series.name == '3日留存'){
                html +=  '<br/>3日留存: ' + Highcharts.numberFormat(this.points[index].y,2) + ' %';
              }
              else if(this.points[index].series.name == '7日留存') {
                html += '<br/>7日留存: ' + Highcharts.numberFormat(this.points[index].y,2) + ' %';
              }else{
                html += '<br/>30日留存: ' + Highcharts.numberFormat(this.points[index].y,2) + ' %';
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
            name:'次日留存',
            data:eval(dayArray)
          },
          {
            name:'3日留存',
            data:eval(tdayArray)
          },
          {
            name:'7日留存',
            data:eval(wdayArray)
          },
          {
            name:'30日留存',
            data:eval(mdayArray)
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

