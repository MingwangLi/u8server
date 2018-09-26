package com.u8.server.common;

import com.opensymphony.xwork2.conversion.impl.DefaultTypeConverter;
import com.u8.server.utils.StringUtils;
import com.u8.server.utils.TimeUtils;

import java.sql.Time;
import java.util.Date;
import java.util.Map;

/**
 * Struts默认对Date类型的日期解析为 Y/m/d
 * 这里自定义日期类型解析，格式为Y-m-d HH:mm:ss
 * Created by ant on 2016/8/4.
 */
public class DateTimeConverter extends DefaultTypeConverter {

    public Object convertValue(Map<String, Object> context, Object value, Class toType) {

        try{

            if(toType == Date.class){
                String[] params = (String[]) value;

                String timeStr = params[0];

                if(StringUtils.isEmpty(timeStr)){
                    return null;
                }

                if(timeStr.length() >= 19){
                    return TimeUtils.parse_default(timeStr);
                }

                return TimeUtils.FORMATER_7.parseObject(timeStr);

            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return convertValue(value, toType);
    }

}
