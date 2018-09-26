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
    <li style="padding:5px 5px 0 10px;"><span><input id="beginTime" class="easyui-datebox" style="height: 25px" name="beginCreateTime" prompt="开始时间" data-options="showSeconds:true" style="width:140px"></span><span style="margin-left: 5px;">~</span></li>
    <li style="padding:5px 10px 0 0px;"><span><input id="endTime" class="easyui-datebox" style="height: 25px" name="beginCreateTime" prompt="结束时间" data-options="showSeconds:true" style="width:140px"></span></li>
    <li style="padding:5px 10px 0 10px;">
      <a id="btnUpdate" href="javascript:void(0)" class="easyui-linkbutton c6" style="width:80px">刷新数据</a>
    </li>
    <li style="padding:5px 10px 0 10px;float: right;font-weight: bold;font-size: 16px;">
      <span id="total">总收入：￥0.0</span>
    </li>
  </ul>

</div>

<div style="padding-top: 20px;clear:both;">
  <div id="money" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="游戏日收入"></div>
</div>

<div style="padding-top: 20px;clear:both;">
  <div id="channel" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="渠道收入分布"></div>
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

      $.post('<%=basePath%>/analytics/moneyData', {appID:appID, beginTime:beginTime,endTime:endTime}, function(result){
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

      $("#total").html("总收入：￥"+Highcharts.numberFormat(data.totalMoney,2));
      showMoneyChart(data.keyCategory, data.moneyData, data.avgMoney);
      showChannelChart( data.channelCategory, data.channelMoneyData, data.channelMoneyRatio,data.payUserNum);

    }

    function showMoneyChart(categories, moneyData, avg){
      $("#money").highcharts({
        title: {
          text: '平均日收入:'+Highcharts.numberFormat(avg,2)+"元",
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
            name:'收入(元)',
            type:'column',
            yAxis:0,
            data:eval(moneyData)
          }
        ]
      });
    }

    function showChannelChart(categories, moneyData, ratioData,payUserNum){
      $("#channel").highcharts({
        title: {
          text: '渠道收入分布(元)',
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
                //alert(payUserNum[index]);
              if(this.points[index].series.name == '占比'){
                html += '<br/>占比: ' + Highcharts.numberFormat(this.points[index].y,2) + ' %';
              }else if (this.points[index].series.name == '收入(元)'){
                html +=  '<br/>金额: '+ this.points[index].y + ' 元';
              }else {
                  html +=  '<br/>付费用户数: ' + this.points[index].y+ ' 个' ;
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
            name:'收入(元)',
            type:'column',
            yAxis:0,
            data:eval(moneyData)
          },
          {
            name:'占比',
            type:'spline',
            yAxis:1,
            data:eval(ratioData)
          },{
              name:'付费用户数',
                type:'column',
                yAxis:0,
                data:eval(payUserNum)
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

