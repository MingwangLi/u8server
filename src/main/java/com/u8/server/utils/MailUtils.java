// package com.u8.server.utils;
//
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import javax.mail.*;
// import javax.mail.internet.InternetAddress;
// import javax.mail.internet.MimeMessage;
// import javax.mail.internet.MimeUtility;
// import java.util.Properties;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.LinkedBlockingQueue;g
// import java.util.concurrent.ThreadPoolExecutor;
// import java.util.concurrent.TimeUnit;
//
//
// /**
//  * 邮件系统 用于发送通知支付回调失败订单信息
//  */
// //感觉是个鸡肋 挺消耗性能的  用处不大  当初只是觉得好玩
// public class MailUtils {
//     private Logger logger = LoggerFactory.getLogger(MailUtils.class);
//     private final String mailHost = "smtp.qq.com";
//     private final String mailPort = "465";   //阿里云服务器出于安全考虑 禁用了25端口号  需要通过465端口发送邮件
//     private final String smtpMailPort = "25";
//     private final String mailFrom = "351637351@qq.com";
//     private final String userName = "351637351";
//     private final String password = "lwtfvqsgfdhhcbah";
//     private MimeMessage message;
//     private Transport transport;
//     private Session session;
//     private static volatile MailUtils instance;
//     private ExecutorService executorService;
//     private MailUtils() {
//         executorService =  new ThreadPoolExecutor(
//                 2,
//                 9,
//                 60,
//                 TimeUnit.SECONDS,
//                 new LinkedBlockingQueue<Runnable>()
//         );
//         Properties properties = new Properties();
//         properties.setProperty("mail.transport.protocol", "smtp");// 设置传输协议
//         properties.setProperty("mail.smtp.host", mailHost);
//         properties.setProperty("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
//         properties.setProperty("mail.smtp.socketFactory.port", mailPort);
//         properties.setProperty("mail.smtp.port", smtpMailPort);
//         properties.setProperty("mail.smtp.auth", "true"); // 验证
//         properties.setProperty("mail.debug","true");
//         session = Session.getInstance(properties);
//         message = new MimeMessage(session);
//     }
//
//     public static MailUtils getInstance() {
//         if (null == instance) {
//             synchronized (MailUtils.class){
//                 if (null == instance) {
//                     instance = new MailUtils();
//                 }
//             }
//         }
//         return instance;
//     }
//
//     public void sendMail(final String receiveUser,final String subject,final String content) {
//         executorService.submit(new Runnable() {
//             @Override
//             public void run() {
//                 try {
//                     InternetAddress from = new InternetAddress(MimeUtility.encodeWord("u8Server发送邮件")+" <"+mailFrom+">");
//                     message.setFrom(from);
//                     // 收件人
//                     InternetAddress to = new InternetAddress(receiveUser);
//                     message.setRecipient(Message.RecipientType.TO, to);//还可以有CC、BCC
//                     // 邮件主题
//                     message.setSubject(subject);
//                     // 邮件内容,也可以使纯文本"text/plain"
//                     message.setContent(content, "text/html;charset=UTF-8");
//                     // 保存邮件
//                     message.saveChanges();
//                     transport = session.getTransport("smtp");
//                     // smtp验证，就是你用来发邮件的邮箱用户名密码
//                     transport.connect(mailHost, userName, password);
//                     // 发送
//                     transport.sendMessage(message, message.getAllRecipients());
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                     logger.error("邮件发送异常,发送信息:{},异常信息:{}",content,e.getMessage());
//                 }finally {
//                     if (null != transport) {
//                         try {
//                             transport.close();
//                         } catch (MessagingException e) {
//                             e.printStackTrace();
//                         }
//                     }
//                 }
//             }
//         });
//     }
//
//
//
//     public static void main(String[] args) throws Exception{
//         MailUtils instance = MailUtils.getInstance();
//         instance.sendMail("3462951792@qq.com","多线程测试邮件 使用465发送邮件","支付回调异常");
//     }
//
//
// }
