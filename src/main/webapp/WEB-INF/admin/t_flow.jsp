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

<div class="easyui-panel infoBar2" style="height: 10%;">

  <ul>
    <li>
      <span><input id="games" type="text" class="easyui-combobox" style="width: 120px;height: 25px;" prompt="选择游戏" name="allgames"  /></span>
    </li>
    <li style="padding:5px 5px 0 10px;width:200px;">
      <span style="margin-top: 5px;">连续<input id="times" type="text" class="easyui-combobox" style="width: 60px;height: 25px;float:left;" prompt="流失条件" name="times" data-options="
                  valueField:'value',
                  textField:'label',
                  data:[
                    {label:'7天',value:'1'},
                    {label:'14天',value:'2'},
                    {label:'30天',value:'3'}
                  ]
                "/>未登录为流失</span>
    </li>
    <li style="padding:5px 5px 0 10px;"><span><input id="beginTime" class="easyui-datebox" style="height: 25px" name="beginCreateTime" prompt="开始时间" data-options="showSeconds:true" style="width:140px"></span><span style="margin-left: 5px;">~</span></li>
    <li style="padding:5px 10px 0 0px;"><span><input id="endTime" class="easyui-datebox" style="height: 25px" name="beginCreateTime" prompt="结束时间" data-options="showSeconds:true" style="width:140px"></span></li>
    <li style="padding:5px 10px 0 10px;">
      <a id="btnUpdate" href="javascript:void(0)" class="easyui-linkbutton c6" style="width:80px">刷新数据</a>
    </li>
  </ul>

</div>

<%--<div style="margin-top: 20px;width: 100%;">--%>
  <%--<div style="width: 33%;float: left;padding-left: 2px;overflow: hidden;">--%>
    <%--<div class="easyui-panel"  style="height: 60px;" title="7日平均流失率">--%>
      <%--<span class="u8_float_left_txt">AVG</span>--%>
      <%--<span id="wdayAvg" class="u8_float_right_txt">0.0%</span>--%>
    <%--</div>--%>
  <%--</div>--%>
  <%--<div style="width: 31%;float:left;padding-left: 15px;overflow: hidden;">--%>
    <%--<div class="easyui-panel"  style="height: 60px;" title="14日平均流失率">--%>
      <%--<span class="u8_float_left_txt">AVG</span>--%>
      <%--<span id="wwdayAvg" class="u8_float_right_txt">0.0%</span>--%>
    <%--</div>--%>
  <%--</div>--%>
  <%--<div style="width:32%;float: left;padding-left: 15px;overflow: hidden;">--%>
    <%--<div class="easyui-panel"  style="height: 60px;" title="30日平均流失率">--%>
      <%--<span class="u8_float_left_txt">AVG</span>--%>
      <%--<span id="mdayAvg" class="u8_float_right_txt">0.0%</span>--%>
    <%--</div>--%>
  <%--</div>--%>

<%--</div>--%>

<div style="padding-top: 20px;clear:both;">
  <div id="flow" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="玩家流失率"></div>
</div>

<div style="padding-top: 20px;clear:both;">
  <div id="back" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="玩家回归率"></div>
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
      var timeType = $("#times").combobox("getValue");
      if(!appID) {
        return;
      }

      $.post('<%=basePath%>/analytics/flowData', {appID:appID, beginTime:beginTime,endTime:endTime,timeType:timeType}, function(result){
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


      showFlowChart( data);

      showBackChart( data);

//      $("#wdayAvg").html(Highcharts.numberFormat(data.wdayAvg,2)+"%");
//      $("#wwdayAvg").html(Highcharts.numberFormat(data.wwdayAvg,2)+"%");
//      $("#mdayAvg").html(Highcharts.numberFormat(data.mdayAvg,2)+"%");

    }

    function showFlowChart(data){
      $("#flow").highcharts({
        title: {
          text: '平均流失率:'+Highcharts.numberFormat(data.wdayAvg,2) + '% 平均流失人数:'+data.wdayCountAvg+'个 总流失用户人数:'+data.wdayCountTotal + '个',
          x: -20 //center
        },
        xAxis: {
          categories:eval(data.keyCategory),
          tickInterval:Math.ceil(data.keyCategory.length / 15)
        },
        yAxis: [
          {
            title: {
              text: ' '
            }

          },
          {
            gridLineWidth:0,
            title:{
              text: ' '
            },
            labels:{
              formatter:function(){
                return this.value + "%";
              }
            },
            opposite: true  //显示在Y轴右侧，通常为false时，左边显示Y轴，下边显示X轴
          }
        ],
        tooltip: {
          shared: true,
          formatter:function(){

            var html = '' + this.points[0].x + ":";
            var index;
            for(index=0;index<this.points.length;index += 1){
              if(this.points[index].series.name == '流失用户数量'){
                html +=  '<br/>流失用户数量: ' + this.points[index].y + ' 个';

              }else{
                html += '<br/>流失率: ' + Highcharts.numberFormat(this.points[index].y,2) + ' %';
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
        series:[
          {
            name:'流失用户数量',
            type:'column',
            yAxis:0,
            data:eval(data.wdayCountData)
          },
          {
            name:'流失率',
            type:'spline',
            yAxis:1,
            data:eval(data.wdayData)
          }
        ]
      });
    }

    function showBackChart( data){
      $("#back").highcharts({
        title: {
          text: '平均回归率:'+Highcharts.numberFormat(data.backAvg,2) + '% 平均回归人数:'+data.backCountAvg+'个 总回归用户人数:'+data.backCountTotal + '个',
          x: -20 //center
        },
        xAxis: {
          categories:eval(data.keyCategory),
          tickInterval:Math.ceil(data.keyCategory.length / 15)
        },
        yAxis: [
          {
            title: {
              text: ' '
            }

          },
          {
            gridLineWidth:0,
            title:{
              text: ' '
            },
            labels:{
              formatter:function(){
                return this.value + "%";
              }
            },
            opposite: true  //显示在Y轴右侧，通常为false时，左边显示Y轴，下边显示X轴
          }
        ],
        tooltip: {
          shared: true,
          formatter:function(){

            var html = '' + this.points[0].x + ":";
            var index;
            for(index=0;index<this.points.length;index += 1){
              if(this.points[index].series.name == '回归用户数量'){
                html +=  '<br/>回归用户数量: ' + this.points[index].y + ' 个';

              }else{
                html += '<br/>回归率: ' + Highcharts.numberFormat(this.points[index].y,2) + ' %';
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
        series:[
          {
            name:'回归用户数量',
            type:'column',
            yAxis:0,
            data:eval(data.backCountData)
          },
          {
            name:'回归率',
            type:'spline',
            yAxis:1,
            data:eval(data.backData)
          }
        ]
      });
    }

    $("#times").combobox({
      onLoadSuccess:function(){
        var data = $('#times').combobox('getData');
        $("#times").combobox('select',data[0].value);
      },
      onSelect:function(rec){
        reqChartData();
      }
    });

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

