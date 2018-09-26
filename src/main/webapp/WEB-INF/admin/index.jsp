<%--
  Created by IntelliJ IDEA.
  User: xiaohei
  Date: 2015/8/22
  Time: 10:07
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
  <title>聚合SDK后台管理系统</title>
  <style type="text/css">
    #menus
    {
      width:200px;
      font-family:Arial;
    }
    #menus ul
    {
      list-style-type:none; /* 不显示项目符号*/
      margin:0px;
      padding:0px;
      text-decoration:none;
    }
    #menus li
    {
      border-bottom:1px solid #474746; /* 添加标签<li>的下划线*/
    }
    #menus li a
    {
      display:block; /* 区块显示*/
      padding:5px 5px 5px 0.5em;
      text-decoration:none;
      border-left:12px solid #6c6f71; /* 左边的粗红边*/
      border-right:1px solid #819191; /* 右侧阴影*/
    }
    #menus li a:link,#menus li a:visited /* 超链接正常状态、被访问过的状态*/
    {
      background-color: #666666; /* 设置背景色*/
      color: #F6FFFF; /* 设置文字颜色*/
    }
    #menus li a:hover /*鼠标经过时*/
    {
      background-color:#474746; /* 改变背景色*/
      color:#AAD790; /* 改变文字颜色*/
    }
    ::-webkit-scrollbar{width:14px;}
    ::-webkit-scrollbar-track{background-color: #acb7a3;}
    ::-webkit-scrollbar-thumb{background-color:#333333;}
    ::-webkit-scrollbar-thumb:hover {background-color:#819191}
    ::-webkit-scrollbar-thumb:active {background-color:#333333}
  </style>
  <link rel="icon" href="<%=basePath%>/images/favicon.ico">
  <link rel="stylesheet" type="text/css" href="<%=basePath%>/js/plugins/easyui/themes/black/easyui.css">
  <link rel="stylesheet" type="text/css" href="<%=basePath%>/js/plugins/easyui/themes/icon.css">
  <link rel="stylesheet" type="text/css" href="<%=basePath%>/js/plugins/easyui/themes/color.css">
  <link rel="stylesheet" type="text/css" href="<%=basePath%>/css/u8server.css">
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/jquery.easyui.min.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/plugins/easyui/locale/easyui-lang-zh_CN.js"></script>
  <script type="text/javascript" src="<%=basePath%>/js/jquery.md5.js"></script>

  <script type="text/javascript">

    /**
     * 创建新选项卡
     * @param tabId    选项卡id
     * @param title    选项卡标题
     * @param url      选项卡远程调用路径
     */
    function addTab(tabId,title,url){
      //如果当前id的tab不存在则创建一个tab

      if($("#"+tabId).html()!= null){
        $("#centerTab").tabs("close", title);
      }

      var name = 'iframe_'+tabId;
      $('#centerTab').tabs('add',{
        title: title,
        closable:true,
        cache : false,
        tools:[
          {
            iconCls:'icon-mini-refresh',
            handler:function(){

              var tab = $('#centerTab').tabs('getSelected');
              $('#centerTab').tabs('update', {
                tab: tab,
                options: {
                  title: title,
                  content: '<iframe name="'+name+'"id="'+tabId+'"src="'+url+'" width="100%" height="100%" frameborder="0" scrolling="auto">'+'</iframe>'
                }
              });
            }
          }
        ],
        //注：使用iframe即可防止同一个页面出现js和css冲突的问题
        content : '<iframe name="'+name+'"id="'+tabId+'"src="'+url+'" width="100%" height="100%" frameborder="0" scrolling="auto">'+'</iframe>'
      });

    }

    $(function(){
      $.ajax({
        type:'POST',
        dataType:'json',
        url:"<%=basePath%>/admin/getMyMenus",
        success:function(data){

          $.each(data, function(i, item){

            var xhtml = '<div title=\"' + item.name + '\"';
            if(i == 0){
              xhtml += ' selected=\"true\" '
            }

            xhtml += '>';

            if(item.childMenus){

              xhtml += '<ul>'
              $.each(item.childMenus, function(m, c){
                 xhtml += '<li><a  href=\'javascript:addTab(\"tabId_' + c.id + '\",\"'+ c.name + '\",\"<%=basePath%>/' + c.path + '\");\'>' + c.name + '</a></li>'
              });
              xhtml += '</ul>';
            }

            xhtml += '</div>';

            $("#menus").accordion('add', {
              title:item.name,
              content:xhtml
            });

          });
        }
      })
    });

  </script>

</head>
<body class="easyui-layout">
  <!-- 正上方panel -->
  <div region="north" style="height:84px" href="<%=basePath%>/header.jsp">
  </div>
  <!-- 正左边panel -->
  <div region="west" title="菜单栏" split="true" style="width:270px;padding1:1px;overflow:hidden;">
    <div id="menus" class="easyui-accordion" fit="true" border="false"></div>
  </div>
  <!-- 正中间panel -->
  <div region="center" title="功能区" >
    <div class="easyui-tabs" id="centerTab" fit="true" border="false">

      <div title="首页">
     <%--<iframe src="<%=basePath%>/analytics/money" width="100%" height="100%" frameborder="0" scrolling="no"></iframe>--%>
     <%--<iframe src="<%=basePath%>/analytics/currTime" width="100%" height="100%" frameborder="0" scrolling="no"></iframe>--%>

        <table border="0" width="40%" height="30%" style="margin-left: 50px">
          <tr style="text-align: left">
            <th>游戏</th>
            <th>上线时间</th>
          </tr>
          <tr>
            <td>灵域战姬</td>
            <td>2017/10/27</td>
          </tr>
          <tr>
            <td>神权永恒</td>
            <td>2017/12/1</td>
          </tr>
          <tr>
            <td>赤月征程</td>
            <td>2017/12/20</td>
          </tr>
          <tr>
            <td>苍天问道</td>
            <td>2018/01/12</td>
          </tr>
          <tr>
            <td>梦幻觉醒</td>
            <td>2018/03/13</td>
          </tr>
          <tr>
            <td>玛雅纪元</td>
            <td>2018/03/20</td>
          </tr>
          <tr>
            <td>游龙传说</td>
            <td>2018/04/02</td>
          </tr>
          <tr>
            <td>血染征途</td>
            <td>2018/05/18</td>
          </tr>
          <tr>
            <td colspan="2">后台数据以统计分析-实时数据为准</td>
          </tr>
        </table>
        <br><br>
      </div>
    </div>
  </div>
  <!-- 正下方panel -->
  <div region="south" style="height:50px;padding-top: 7px" align="center">
    <label>
      聚合SDK——让手游SDK接入更简单<br/>
      官方网站：<a href="http://www.jmsht.cn/">太极互娱</a>
    </label>
  </div>

</body>
</html>
