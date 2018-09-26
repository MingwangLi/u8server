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

  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.easyui.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>

  <script type="text/javascript" src="<%=basePath%>/js/highcharts.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/dark-unica.js"></script>


</head>
<body>

<div class="easyui-panel infoBar">

  <ul>
    <li>
      <span class="u8_item_combobox" style="margin-top: -3px;"><input id="games" type="text" class="easyui-combobox" style="width: 120px;height: 25px;" prompt="选择游戏" name="allgames"  /></span>
      <span class="u8_item_combobox" style="margin-top: 5px;"><input id="times" type="text" class="easyui-combobox" style="width: 120px;height: 25px;" prompt="选择时间段" name="times" data-options="
                  valueField:'value',
                  textField:'label',
                  data:[
                    {label:'近一周',value:'2'},
                    {label:'昨日',value:'1'},
                    {label:'近一月',value:'3'},
                    {label:'全部',value:'4'}
                  ]
                "/></span>
    </li>
    <li class="infoBar1"><span>设备激活</span><strong id="totalDeviceInstallNum">0</strong></li>
    <li class="infoBar5"><span>新增玩家</span><strong id="totalNewPlayerNum">0</strong></li>
    <li class="infoBar3"><span>付费玩家</span><strong id="totalChargePlayerNum">0</strong></li>
    <li class="infoBar4"><span id="moneyTitle">收入（元）</span><strong id="totalIncomeNum"><small class="USD"></small>0</strong></li>

  </ul>

</div>

<div style="margin-top: 20px;">
  <div id="keyData" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="新增和转化"></div>
</div>

<div style="margin-top: 20px;">
  <div id="dau" class="easyui-panel" style="height: 300px;" title="玩家活跃度"></div>
</div>

<div style="margin-top: 20px;">
  <div id="payUser" class="easyui-panel" style="height: 300px;" title="付费玩家"></div>
</div>

<div style="margin-top: 20px;">
  <div id="money" class="easyui-panel" style="height: 300px;" title="收 入"></div>
</div>

<script type="text/javascript">

  $(function(){

    //showKeyChart("['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun','Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']", "[{name: 'Tokyo',data: [7.0, 6.9, 9.5, 14.5, 18.2, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6]}, {name: 'New York',data: [-0.2, 0.8, 5.7, 11.3, 17.0, 22.0, 24.8, 24.1, 20.1, 14.1, 8.6, 2.5]}, {name: 'Berlin',data: [-0.9, 0.6, 3.5, 8.4, 13.5, 17.0, 18.6, 17.9, 14.3, 9.0, 3.9, 1.0]}, {name: 'London',data: [3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8]}]");


    function reqChartData(){

      var appID = $("#games").combobox("getValue");
      var timeType = $("#times").combobox("getValue");

      if(!appID) {
//        $.messager.show({
//          title: '操作提示',
//          msg: '请先选择一个游戏'
//        });
        return;
      }

      $.post('<%=basePath%>/analytics/summaryData', {appID:appID, timeType:timeType}, function(result){
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

      $("#totalDeviceInstallNum").html(data.deviceNum);
      $("#totalNewPlayerNum").html(data.userNum);
      $("#totalChargePlayerNum").html(data.payUserNum);
      $("#moneyTitle").html(formatMoneyTitle(data.payMoney));
      $("#totalIncomeNum").html("￥"+formatMoney(data.payMoney));

      showKeyChart(data.keyCategory, data.deviceData, data.newUserData);
      showDAUChart(data.keyCategory, data.allDaudata, data.newDaudata);
      showPayUserChart(data.keyCategory, data.allPayData, data.newPayData);
      showMoneyChart(data.keyCategory, data.moneyData);

    }

    function showKeyChart(categories, deviceData, userData){
      $('#keyData').highcharts({
        title: {
          text: '新增激活和账户',
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
              if(this.points[index].series.name == '新增设备数'){
                html +=  '<br/>新增设备数: ' + this.points[index].y + ' 个';

              }else{
                html += '<br/>新增账户数: ' + this.points[index].y + ' 个';
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
            name:'新增设备数',
            data:eval(deviceData)
          },
          {
            name:'新增账户数',
            data:eval(userData)
          }
        ]
      });
    }

    function showDAUChart(categories, dauData, newdauData){
      $('#dau').highcharts({
        title: {
          text: '玩家活跃度(新用户独立显示)',
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
                html +=  '<br/>活跃玩家: ' + this.points[index].y + ' 个';

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
            data:eval(newdauData)
          }
        ]
      });
    }


    function showPayUserChart(categories, payData, newPayData){
      $('#payUser').highcharts({
        title: {
          text: '付费用户(新用户付费独立显示)',
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
                html +=  '<br/>付费玩家: ' + this.points[index].y + ' 个';

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
            data:eval(payData)
          },
          {
            name:'新玩家',
            data:eval(newPayData)
          }
        ]
      });
    }

    function showMoneyChart(categories, moneyData){
      $('#money').highcharts({
        title: {
          text: '游戏收入',
          x: -20 //center
        },
        xAxis: {
          categories:eval(categories),
          tickInterval:Math.ceil(categories.length / 15)
        },
        yAxis: {
          title: {
            text: '金额(元)'
          }
        },
        tooltip: {
          valueSuffix: '元'
        },
        legend: {
          layout: 'vertical',
          align: 'right',
          verticalAlign: 'middle',
          borderWidth: 0
        },
        series: [
          {
            name:'收入',
            data:eval(moneyData)
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
        var data = $('#games').combobox('getData');
        $("#games").combobox('select',data[0].appID);

        reqChartData();

      }
    });

      /**
       * 将分转换 元/万/亿
       */
      function formatMoneyTitle(money) {
          var yuan = money / 100;
          if (yuan >= 100000000) {
              return "收入(亿)"
          } else if (yuan >= 1000000) {
              return "收入(万)"
          } else {
              return "收入(元)"
          }
      }


      /**
       * 转换数值
       */
      function formatMoney(money) {
          var yuan = money / 100;
          if (yuan >= 1000000000) {
              return Highcharts.numberFormat(yuan / 100000000, 2)
          } else if (yuan >= 1000000) {
              return Highcharts.numberFormat(yuan / 10000, 2)
          } else {
              return Highcharts.numberFormat(yuan, 0)
          }
      }


  });



</script>

</body>
</html>
