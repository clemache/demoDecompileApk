package com.easyflow.demodecompileapk.configuration;

import com.easyflow.demodecompileapk.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Component
public class Log implements Serializable {

    @Value("${easyflow.log.path}")
    private String logPath;
    private String message;
    private String codError;
    private String className;
    private String currentMethod;
    private String logFormat;

    public Log(){
        this.message = "";
        this.codError = "0";
        this.className = "apkDecompile";
        this.currentMethod = "apkDecompileMethod";
        this.logFormat = "";
    }

    public void registerLog(String codError, String classPath, String method, String message ) {
        this.codError = codError;
        this.className = classPath;
        this.currentMethod = method;
        this.message = formatMessage(message);
        System.out.println(logFormat);
        //saveLog();
    }

    public void registerLog(String codError, String classPath, String method,String project, String message ) {
        this.codError = codError;
        this.className = classPath;
        this.currentMethod = method;
        this.message = formatMessage(project+" : "+message);
        System.out.println(logFormat);
        //saveLog();
    }

    public void registerErrorLog(String codError, String classPath, String method, String message) {
        this.codError = codError;
        this.className = classPath;
        this.currentMethod = method;
        this.message = formatMessage("Error: "+message);
        System.err.println(logFormat);
        //saveLog();
    }

    public void registerErrorLog(String codError, String classPath, String method, String project, String message) {
        this.codError = codError;
        this.className = classPath;
        this.currentMethod = method;
        this.message = formatMessage(project+" : "+"Error: "+message);
        System.err.println(logFormat);
        //saveLog();
    }

    private String formatMessage(String message) {

        this.logFormat = "";

        char separator = ' ';

        String infoType= "";

        if (message.contains("Error:")) {
            infoType= "ERROR";

        }
        if (message.contains("Advertencia:")) {
            infoType= "WARN ";
        }
        else if (message.contains("Advertencia:") && message.contains("Error:")){
            infoType= "WARN ";
        }
        else if (!message.contains("Advertencia:") && !message.contains("Error:")){
            infoType= "INFO ";
        }

        Calendar fechaActual = Calendar.getInstance(); //Para poder utilizar el paquete calendar
        this.logFormat = (""
                + Util.zerofillLeft(String.valueOf(fechaActual.get(Calendar.DAY_OF_MONTH)),2)
                +"-"+Util.zerofillLeft(String.valueOf(fechaActual.get(Calendar.MONTH)+1),2)
                +"-"+String.valueOf(fechaActual.get(Calendar.YEAR))
                +" "+Util.zerofillLeft(String.valueOf(fechaActual.get(Calendar.HOUR_OF_DAY)),2)
                +":"+Util.zerofillLeft(String.valueOf(fechaActual.get(Calendar.MINUTE)),2)
                +":"+Util.zerofillLeft(String.valueOf(fechaActual.get(Calendar.SECOND)),2))
                +separator+"["+infoType+"]"
                +separator+"["+Util.zerofillLeft(String.valueOf(codError), 4)+"]"
                +separator+className+" "+currentMethod+":"
                +separator+message+"\r\n";

        return logFormat;

    }

    private void saveLog() {

        Date now = new Date();
        SimpleDateFormat formateador = new SimpleDateFormat("dd-MM-yyyy");
        String archivo = logPath;
        archivo = archivo.replace(".log", "_"+formateador.format(now)+".log");

        try {
            Util.saveFile(archivo, message);
        } catch (IOException e) {

        }

    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return this.logFormat;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getCodError() {
        return codError;
    }

    public void setCodError(String codError) {
        this.codError = codError;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCurrentMethod() {
        return currentMethod;
    }

    public void setCurrentMethod(String currentMethod) {
        this.currentMethod = currentMethod;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLogFormat() {
        return logFormat;
    }

    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Log log = (Log) o;

        if (!Objects.equals(logPath, log.logPath)) return false;
        if (!Objects.equals(message, log.message)) return false;
        if (!Objects.equals(codError, log.codError)) return false;
        if (!Objects.equals(className, log.className)) return false;
        if (!Objects.equals(currentMethod, log.currentMethod)) return false;
        return Objects.equals(logFormat, log.logFormat);
    }

    @Override
    public int hashCode() {
        int result = logPath != null ? logPath.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (codError != null ? codError.hashCode() : 0);
        result = 31 * result + (className != null ? className.hashCode() : 0);
        result = 31 * result + (currentMethod != null ? currentMethod.hashCode() : 0);
        result = 31 * result + (logFormat != null ? logFormat.hashCode() : 0);
        return result;
    }

}
