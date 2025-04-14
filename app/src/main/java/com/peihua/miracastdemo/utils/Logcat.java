package com.peihua.miracastdemo.utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.peihua.miracastdemo.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Logcat {
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String NULL_TIPS = "Log with null object";
    private static final int MAX_LENGTH = 2000;
    private static final String DEFAULT_MESSAGE = "execute";
    private static final String PARAM = "Param";
    private static final String NULL = "null";
    private static final String TAG_DEFAULT = "LogCat";
    private static final String SUFFIX = ".java";

    public static final int JSON_INDENT = 4;

    public static final int V = 0x1;
    public static final int D = 0x2;
    public static final int I = 0x3;
    public static final int W = 0x4;
    public static final int E = 0x5;
    public static final int A = 0x6;

    public static final int JSON = 0x7;
    public static final int XML = 0x8;

    private static final int STACK_TRACE_INDEX_6 = 6;
    private static final int STACK_TRACE_INDEX_4 = 4;

    private static String mGlobalTag;
    private static boolean mIsGlobalTagEmpty = true;
    private static boolean isShowLog = true;
    private static boolean isWriteLogFile = true;

    public static void init(boolean isShowLog) {
        init(isShowLog, isShowLog);
    }

    public static void init(boolean isShowLog, boolean isWriteLogFile) {
        init(isShowLog, isWriteLogFile, "");
    }

    public static void init(boolean isShowLog, boolean isWriteLogFile, String tag) {
        Logcat.isShowLog = isShowLog;
        Logcat.isWriteLogFile = isWriteLogFile;
        mGlobalTag = tag;
        mIsGlobalTagEmpty = TextUtils.isEmpty(mGlobalTag);
    }

    public static boolean isShowLog() {
        return isShowLog;
    }

    public static boolean isWriteLogFile() {
        return isWriteLogFile;
    }

    private static String format(String format, Object... args) {
        return args == null || args.length == 0 ? format : String.format(Locale.US, format, args);
    }

    public static void v() {
        printLog(V, null, DEFAULT_MESSAGE);
    }

    public static void v(Object msg) {
        printLog(V, null, msg);
    }

    public static void v(String msg) {
        printLog(V, null, msg);
    }

    public static void v(String tag, Object... args) {
        printLog(V, tag, args);
    }

    public static void v(String tag, String format, Object... args) {
        printLog(V, tag, format(format, args));
    }

    public static void d() {
        printLog(D, null, DEFAULT_MESSAGE);
    }

    public static void d(Object msg) {
        printLog(D, null, msg);
    }

    public static void d(String msg) {
        printLog(D, null, msg);
    }

    public static void d(String tag, Object... args) {
        printLog(D, tag, args);
    }

    public static void d(String tag, String format, Object... args) {
        printLog(D, tag, format(format, args));
    }

    public static void i() {
        printLog(I, null, DEFAULT_MESSAGE);
    }

    public static void i(Object msg) {
        printLog(I, null, msg);
    }

    public static void i(String msg) {
        printLog(I, null, msg);
    }

    public static void i(String tag, Object... args) {
        printLog(I, tag, args);
    }

    public static void i(String tag, String format, Object... args) {
        printLog(I, tag, format(format, args));
    }

    public static void w() {
        printLog(W, null, DEFAULT_MESSAGE);
    }

    public static void w(Object msg) {
        printLog(W, null, msg);
    }

    public static void w(String msg) {
        printLog(W, null, msg);
    }

    public static void w(String tag, Object... args) {
        printLog(W, tag, args);
    }

    public static void w(String tag, String format, Object... args) {
        printLog(W, tag, format(format, args));
    }

    public static void e() {
        printLog(E, null, DEFAULT_MESSAGE);
    }

    public static void e(Object msg) {
        printLog(E, null, msg);
    }

    public static void e(String msg) {
        printLog(E, null, msg);
    }

    public static void e(String tag, Object... args) {
        printLog(E, tag, args);
    }

    public static void e(String tag, String format, Object... args) {
        printLog(E, tag, format(format, args));
    }

    public static void a() {
        printLog(A, null, DEFAULT_MESSAGE);
    }

    public static void a(Object msg) {
        printLog(A, null, msg);
    }

    public static void a(String msg) {
        printLog(A, null, msg);
    }

    public static void a(String tag, Object... args) {
        printLog(A, tag, args);
    }

    public static void a(String tag, String format, Object... args) {
        printLog(A, tag, format(format, args));
    }

    public static void json(String jsonFormat) {
        printLog(JSON, null, jsonFormat);
    }

    public static void json(String tag, String jsonFormat) {
        printLog(JSON, tag, jsonFormat);
    }

    public static void xml(String xml) {
        printLog(XML, null, xml);
    }

    public static void xml(String tag, String xml) {
        printLog(XML, tag, xml);
    }

    public static void debug() {
        printDebug(null, DEFAULT_MESSAGE);
    }

    public static void debug(Object msg) {
        printDebug(null, msg);
    }

    public static void debug(String tag, Object... objects) {
        printDebug(tag, objects);
    }

    public static void trace() {
        printStackTrace();
    }

    private static void printStackTrace() {

        if (!isShowLog()) {
            return;
        }

        Throwable tr = new Throwable();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        String message = sw.toString();

        String traceString[] = message.split("\\n\\t");
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (String trace : traceString) {
            sb.append(trace).append("\n");
        }
        String[] contents = wrapperContent(STACK_TRACE_INDEX_4, null, sb.toString());
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        printDefault(D, tag, headString + msg);
    }

    public static void printLog(int type, String tagStr, Object... objects) {
        printLog(STACK_TRACE_INDEX_6, type, tagStr, objects);
    }

    public static void printLog(int stackTraceIndex, int type, String tagStr, Object... objects) {
        if (!isShowLog()) {
            return;
        }

        String[] contents = wrapperContent(stackTraceIndex, tagStr, objects);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];

        switch (type) {
            case V:
            case D:
            case I:
            case W:
            case E:
            case A:
                printDefault(type, tag, headString + msg);
                break;
            case JSON:
                printJson(tag, msg, headString);
                break;
            case XML:
                printXml(tag, msg, headString);
                break;
            default:
                break;
        }
    }

    private static void printDebug(String tagStr, Object... objects) {
        printDebug(STACK_TRACE_INDEX_6, tagStr, objects);
    }

    public static void printDebug(int stackTraceIndex, String tagStr, Object... objects) {
        String[] contents = wrapperContent(stackTraceIndex, tagStr, objects);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        printDefault(D, tag, headString + msg);
    }

    public static String[] wrapperContent(int stackTraceIndex, String tagStr, Object... objects) {

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement targetElement = stackTrace[stackTraceIndex];
        String fileName = targetElement.getFileName();
        String className = fileName;
        if (TextUtils.isEmpty(fileName)) {
            className = targetElement.getClassName();
            String[] classNameInfo = className.split("\\.");
            if (classNameInfo.length > 0) {
                className = classNameInfo[classNameInfo.length - 1] + SUFFIX;
            }
            if (className.contains("$")) {
                className = className.split("\\$")[0] + SUFFIX;
            }
        }

        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();

        if (lineNumber < 0) {
            lineNumber = 0;
        }

        String tag = (tagStr == null ? className : tagStr);

        if (mIsGlobalTagEmpty && TextUtils.isEmpty(tag)) {
            tag = TAG_DEFAULT;
        } else if (!mIsGlobalTagEmpty) {
            tag = mGlobalTag;
        }

        String msg = (objects == null || objects.length == 0) ? NULL_TIPS : getObjectsString(objects);
        String headString = "[ (" + className + ":" + lineNumber + ")#" + methodName + " ] ";

        return new String[]{tag, msg, headString};
    }

    private static String getObjectsString(Object... objects) {

        if (objects.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (object == null) {
                    stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ").append(NULL).append("\n");
                } else {
                    stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ").append(object.toString()).append("\n");
                }
            }
            return stringBuilder.toString();
        } else {
            Object object = objects[0];
            return object == null ? NULL : object.toString();
        }
    }

    public static void printDefault(int type, String tag, String msg) {

        int index = 0;
        int length = msg.length();
        int countOfSub = length / MAX_LENGTH;

        if (countOfSub > 0) {
            for (int i = 0; i < countOfSub; i++) {
                String sub = msg.substring(index, index + MAX_LENGTH);
                printSub(type, tag, sub);
                index += MAX_LENGTH;
            }
            printSub(type, tag, msg.substring(index, length));
        } else {
            printSub(type, tag, msg);
        }
    }

    private static void printSub(int type, String tag, String sub) {
        switch (type) {
            case V:
                Log.v(tag, sub);
                break;
            case D:
                Log.d(tag, sub);
                break;
            case I:
                Log.i(tag, sub);
                break;
            case W:
                Log.w(tag, sub);
                break;
            case E:
                Log.e(tag, sub);
                break;
            case A:
                Log.wtf(tag, sub);
                break;
            default:
                break;
        }
    }

    public static void printXml(String tag, String xml, String headString) {

        if (xml != null) {
            xml = formatXML(xml);
            xml = headString + "\n" + xml;
        } else {
            xml = headString + NULL_TIPS;
        }

        printLine(tag, true);
        String[] lines = xml.split(LINE_SEPARATOR);
        for (String line : lines) {
            if (!isEmpty(line)) {
                Log.d(tag, "║ " + line);
            }
        }
        printLine(tag, false);
    }

    private static String formatXML(String inputXML) {
        try {
            Source xmlInput = new StreamSource(new StringReader(inputXML));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString().replaceFirst(">", ">\n");
        } catch (Exception e) {
            e.printStackTrace();
            return inputXML;
        }
    }

    public static void printJson(String tag, String msg, String headString) {

        String message;

        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(JSON_INDENT);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(JSON_INDENT);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }

        printLine(tag, true);
        message = headString + LINE_SEPARATOR + message;
        String[] lines = message.split(LINE_SEPARATOR);
        for (String line : lines) {
            Log.d(tag, "║" + line);
        }
        printLine(tag, false);
    }

    public static boolean isEmpty(String line) {
        return TextUtils.isEmpty(line) || line.equals("\n") || line.equals("\t") || TextUtils.isEmpty(line.trim());
    }

    public static void printLine(String tag, boolean isTop) {
        if (isTop) {
            Log.d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        } else {
            Log.d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
        }
    }

    /**
     * log 写入文件
     *
     * @param tagStr
     * @param log
     */
    public static void writeLog(String tagStr, String log) {
        String[] contents = wrapperContent(STACK_TRACE_INDEX_4, tagStr, log);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        String logMsg = headString + msg;
        Logcat.printLog(Logcat.D, tag, logMsg);
        if (!isWriteLogFile()) return;
        AsyncTask.execute(() -> FileLog.writeLog(tag + " " + logMsg));
    }

    /**
     * 日志写入文件
     */
    static class FileLog {
        private static final String DIR = "Logcat";
        /**
         * 一天
         */
        public static final int ONE_DAY = 60 * 1000 * 60 * 24;
        /**
         * 5天
         */
        public static final int FRI_DAY = ONE_DAY * 5;

        static void writeLog(String logString) {
            try {
                File rootFile = Environment.getExternalStorageDirectory();
//                File parentFile = ComposeDemoApp.context.getExternalCacheDir();
                File parentFile = new File(rootFile, "/Android/data/" + BuildConfig.APPLICATION_ID + "/cache");
                File fileParentDir = new File(parentFile, DIR);//判断log目录是否存在
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs();
                }

                try {
                    File[] files = fileParentDir.listFiles();
                    if (files != null && files.length >= 5) {
                        for (File file : files) {
                            if (isRemoveFile(file)) {
                                file.delete();
                                Log.d("FileLog", "delete file =" + file.getName());
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                File logFile = new File(fileParentDir, "Logcat-" + getCurData() + ".log");//日志文件名
                PrintWriter printWriter = new PrintWriter(new FileOutputStream(logFile, true));//紧接文件尾写入日志字符串
                String time = "[" + getCurTime() + "] ";
                printWriter.println(time + logString);
                printWriter.flush();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        public static String getCurData() {
            Calendar cd = Calendar.getInstance();//日志文件时间
            int year = cd.get(Calendar.YEAR);
            String month = addZero(cd.get(Calendar.MONTH) + 1);
            String day = addZero(cd.get(Calendar.DAY_OF_MONTH));
            return year + "-" + month + "-" + day;
        }

        public static String getCurTime() {
            Calendar cd = Calendar.getInstance();//日志文件时间
            int year = cd.get(Calendar.YEAR);
            String month = addZero(cd.get(Calendar.MONTH) + 1);
            String day = addZero(cd.get(Calendar.DAY_OF_MONTH));
            String hour = addZero(cd.get(Calendar.HOUR_OF_DAY));
            String min = addZero(cd.get(Calendar.MINUTE));
            String sec = addZero(cd.get(Calendar.SECOND));
            return year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
        }

        private static String addZero(int i) {
            if (i < 10) {
                String tmpString = "0" + i;
                return tmpString;
            } else {
                return String.valueOf(i);
            }
        }

        private static boolean isRemoveFile(File file) {
            //log-2024-04-29.log
            String fileName = file.getName();
            String dateStr = fileName.substring(4, fileName.indexOf("."));
            String[] dates = dateStr.split("-");
            Calendar cd = Calendar.getInstance();//日志文件时间
            long curTime = cd.getTimeInMillis();
            cd.add(Calendar.YEAR, toInt(dates[0]));
            cd.add(Calendar.MONTH, toInt(dates[1]) - 1);
            cd.add(Calendar.DAY_OF_MONTH, toInt(dates[2]));
            long fileTime = cd.getTimeInMillis();
            Log.d("FileLog", "delete file =" + file.getName() + ",dateStr:" + dateStr + ",dates:" + Arrays.toString(dates));
            return (curTime - FRI_DAY) > fileTime;
        }
    }

    /**
     * 将Object对象转成Integer类型
     *
     * @param value
     * @return 如果value不能转成Integer，则默认0
     */
    static int toInt(Object value) {
        return toInt(value, 0);
    }

    /**
     * 将Object对象转成Integer类型
     *
     * @param value
     * @return 如果value不能转成Integer，则默认0
     */
    static int toInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (int) value;
        }
        if (value instanceof Number) {
            return (int) value;
        }
        if (value instanceof String) {
            try {
                return (int) Double.parseDouble(value.toString());
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
