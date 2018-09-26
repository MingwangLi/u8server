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

<div class="easyui-panel infoBar2" style="height: 7%;">

  <ul>
    <li>
      <span><input id="games" type="text" class="easyui-combobox" style="width: 120px;height: 25px;" prompt="选择游戏" name="allgames"  /></span>
    </li>
    <li style="padding:5px 5px 0 10px;"><span><input id="beginTime" class="easyui-datebox" style="height: 25px" name="beginCreateTime" prompt="开始时间" data-options="showSeconds:true" style="width:140px"></span><span style="margin-left: 5px;">~</span></li>
    <li style="padding:5px 10px 0 0px;"><span><input id="endTime" class="easyui-datebox" style="height: 25px" name="beginCreateTime" prompt="结束时间" data-options="showSeconds:true" style="width:140px"></span></li>
    <li style="padding:5px 10px 0 10px;">
      <a id="btnUpdate" href="javascript:void(0)" class="easyui-linkbutton c6" style="width:80px">刷新数据</a>
    </li>
    <li style="padding:5px 10px 0 10px;float: right;font-weight: bold;font-size: 16px;">
      <span id="total">总付费用户数：0</span>
    </li>
  </ul>

</div>

<div style="padding-top: 20px;clear:both;">
  <div id="pay" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="日付费率"></div>
</div>

<div style="padding-top: 20px;clear:both;">
  <div id="arpu" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="ARPU(日)"></div>
</div>

<div style="padding-top: 20px;clear:both;">
  <div id="arppu" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="ARPPU(日)"></div>
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

      $.post('<%=basePath%>/analytics/payData', {appID:appID, beginTime:beginTime,endTime:endTime}, function(result){
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

      showPayChart(data.keyCategory, data.payCountData, data.payRatioData, data.payRatioAvg, data.payCountAvg);
      showARPUChart( data.keyCategory, data.arpuData, data.arpuAvg);
      showARPPUChart( data.keyCategory, data.arppuData, data.arppuAvg);
      $("#total").html("总付费用户数："+data.totalCount);
    }

    function showPayChart(categories, payData, payRatio, payRatioAvg, payCountAvg){
      $('#pay').highcharts({
        title: {
          text: '日付费率 AVG:'+Highcharts.numberFormat(payRatioAvg,2)+" %  日付费用户数 AVG:"+Highcharts.numberFormat(payCountAvg,0)+" 个",
          x: -20 //center
        },
        xAxis: {
          categories:eval(categories),
          tickInterval:Math.ceil(categories.length / 15)
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
              if(this.points[index].series.name == '付费用户数'){
                html +=  '<br/>付费用户数: ' + this.points[index].y + ' 个';

              }else{
                html += '<br/>付费率: ' + Highcharts.numberFormat(this.points[index].y,2) + ' %';
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
            name:'付费用户数',
            type:'column',
            yAxis:0,
            data:eval(payData)
          },
          {
            name:'付费率',
            type:'spline',
            yAxis:1,
            data:eval(payRatio)
          }
        ]
      });
    }

    function showARPUChart(categories, moneyData, avg){
      $("#arpu").highcharts({
        title: {
          text: 'ARPU(日)      AVG:'+ Highcharts.numberFormat(avg,2) + ' 元',
          x: -20 //center
        },
        xAxis: {
          categories:eval(categories),
          tickInterval:Math.ceil(categories.length / 15)
        },
        yAxis: [
          {
            title: {
              text: ' '
            }

          }
        ],
        tooltip: {
          formatter:function(){
            return '' + this.x + ': ' + Highcharts.numberFormat(this.y,2) + ' 元';

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
            name:'ARPU(日)',
            yAxis:0,
            data:eval(moneyData)
          }
        ]
      });
    }

    function showARPPUChart(categories, moneyData, avg){
      $("#arppu").highcharts({
        title: {
          text: 'ARPPU(日)     AVG:'+ Highcharts.numberFormat(avg,2) + ' 元',
          x: -20 //center
        },
        xAxis: {
          categories:eval(categories),
          tickInterval:Math.ceil(categories.length / 15)
        },
        yAxis: [
          {
            title: {
              text: ' '
            }

          }
        ],
        tooltip: {
          formatter:function(){
            return '' + this.x + ': ' + Highcharts.numberFormat(this.y,2) + ' 元';

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
            name:'ARPPU(日)',
            yAxis:0,
            data:eval(moneyData)
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

