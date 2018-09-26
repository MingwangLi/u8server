<%--
  Created by IntelliJ IDEA.
  User: 123
  Date: 2018/6/13
  Time: 15:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path;
%>
<base href="<%=basePath%>>">
<html>
<head>
    <title>Title</title>
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
    </ul>

</div>

<div style="margin-top: 20px;width: 100%;">
    <div style="width: 24.8%;float: left;padding-left: 2px;overflow: hidden;">
        <div class="easyui-panel"  style="height: 60px;" title="当日LTV平均值">
            <span class="u8_float_left_txt">AVG</span>
            <span id="todayAVG" class="u8_float_right_txt">0.0</span>
        </div>
    </div>
    <div style="width: 24%;float:left;padding-left: 16px;overflow: hidden;">
        <div class="easyui-panel"  style="height: 60px;" title="3日LTV平均值">
            <span class="u8_float_left_txt">AVG</span>
            <span id="threeDayAVG" class="u8_float_right_txt">0.0</span>
        </div>
    </div>
    <div style="width:24%;float: left;padding-left: 16px;overflow: hidden;">
        <div class="easyui-panel"  style="height: 60px;" title="7日LTV平均值">
            <span class="u8_float_left_txt">AVG</span>
            <span id="weekAVG" class="u8_float_right_txt">0.0</span>
        </div>
    </div>

    <div style="width: 24%;float:left;padding-left: 16px;overflow: hidden;">
        <div class="easyui-panel" style="height: 60px;" title="30日LTV平均值">
            <span class="u8_float_left_txt">AVG</span>
            <span id="monthAVG" class="u8_float_right_txt">0.0</span>
        </div>
    </div>

</div>

<div style="padding-top: 20px;clear:both;">
    <div id="ltv" class="easyui-panel" style="min-width: 310px; height: 300px; margin: 0 auto" title="玩家LTV"></div>
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

            $.post('<%=basePath%>/admin/getLTVData', {appID:appID, beginTime:beginTime,endTime:endTime}, function(result){
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
            $("#todayAVG").html(Highcharts.numberFormat(data.todayAVG,2)+"元");
            $("#threeDayAVG").html(Highcharts.numberFormat(data.threeDayAVG,2)+"元");
            $("#weekAVG").html(Highcharts.numberFormat(data.weekAVG,2)+"元");
            $("#monthAVG").html(Highcharts.numberFormat(data.monthAVG,2)+"元");
            showLTVChart( data.date, data.today, data.threeDay,data.week,data.month);

        }


        function showLTVChart(date, today, threeDay,week,month){
            $("#ltv").highcharts({
                title: {
                    text: 'LTV(元)',
                    x: -20 //center
                },
                xAxis: {
                    categories:eval(date),
                    tickInterval:Math.ceil(date.length / 15)
                },
                yAxis: [
                    {
                        title: {
                            text: ' '
                        }

                    }
                ],
                tooltip: {
                    shared: true,
                    formatter:function(){
                        var html = '' + this.points[0].x + ":";
                        var index;
                        for(index=0;index<this.points.length;index += 1){
                            //alert(payUserNum[index]);
                            if(this.points[index].series.name == '当日LTV'){
                                html += '<br/>当日LTV: ' + this.points[index].y + ' 元';
                            }else if (this.points[index].series.name == '3日LTV'){
                                html +=  '<br/>3日LTV: '+ this.points[index].y + ' 元';
                            }else if (this.points[index].series.name == '7日LTV') {
                                html +=  '<br/>7日LTV: ' + this.points[index].y+ ' 元' ;
                            }else {
                                html +=  '<br/>30日LTV: ' + this.points[index].y+ ' 元' ;
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
                        name:'当日LTV',
                        type:'column',
                        yAxis:0,
                        data:eval(today)
                    },
                    {
                        name:'3日LTV',
                        type:'column',
                        yAxis:0,
                        data:eval(threeDay)
                    },
                    {
                        name:'7日LTV',
                        type:'column',
                        yAxis:0,
                        data:eval(week)
                    },
                    {
                        name:'30日LTV',
                        type:'column',
                        yAxis:0,
                        data:eval(month)
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
            }
        });

    });
</script>
</body>
</html>
