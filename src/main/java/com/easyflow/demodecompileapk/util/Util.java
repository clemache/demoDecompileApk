package com.easyflow.demodecompileapk.util;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {


    public static boolean saveFile(String pathFile, String stringToSave)
            throws IOException {


        FileWriter archivo;//nuestro archivo log
        //Pregunta el archivo existe, caso contrario crea uno con el nombre log.txt
        if (new File(pathFile).exists()==false)
            archivo=new FileWriter(new File(pathFile),false);
        archivo = new FileWriter(new File(pathFile), true);
        try
        {
            //Empieza a escribir en el archivo
            archivo.write(stringToSave);
            archivo.close(); //Se cierra el archivo
        }
        catch(Exception ar)
        {
            if(archivo!=null)
                archivo.close();
        }

        return true;
    }


    public static boolean copyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) {
                    break;
                }
                os.write(bytes, 0, count);
            }
            os.close();
            is.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static String zerofillLeft(String str, int size) {
        StringBuilder sb = new StringBuilder();

        for (int toPrepend=size-str.length(); toPrepend>0; toPrepend--) {
            sb.append('0');
        }

        sb.append(str);
        String result = sb.toString();
        return result;
    }

    public static boolean isNumeric(String chain){
        try {
            Integer.parseInt(chain);
            return true;
        } catch (NumberFormatException nfe){
            return false;
        }
    }

    public static Date dateFormat(String stringDate)
    {
        Date fechaformateada = null;

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");//dd/MM/yyyy
        try {

            fechaformateada = sdfDate.parse(stringDate);
        }
        catch (ParseException e)
        {
            // Error, la cadena de texto no se puede convertir en fecha.
        }


        return fechaformateada;

    }

    public static Date stringDateYearMonthDayHourMinuteSecondToDate(String stringToDate)
    {
        Date formatDate = null;

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMddHHmmss");//dd/MM/yyyy
        try {

            formatDate = sdfDate.parse(stringToDate);
        }
        catch (ParseException e)
        {
            // Error, la cadena de texto no se puede convertir en fecha.
        }


        return formatDate;

    }


    public static Date customDateFormat(String stringDate, String format) throws ParseException
    {
        Date fechaformateada = null;

        SimpleDateFormat sdfDate = new SimpleDateFormat(format);//dd/MM/yyyy
        try {

            fechaformateada = sdfDate.parse(stringDate);
        }
        catch (ParseException e)
        {
            // Error, la cadena de texto no se puede convertir en fecha.
        }
        return fechaformateada;

    }

    public static String customDateFormatToString(Date date, String format) throws ParseException
    {
        String fechaformateada = "";

        SimpleDateFormat sdfDate = new SimpleDateFormat(format);//dd/MM/yyyy
        fechaformateada = sdfDate.format(date);
        return fechaformateada;

    }

    public static Date stringHourToDate(String HHmmss)
    {
        Date fechaformateada = null;

        SimpleDateFormat sdfDate = new SimpleDateFormat("HHmmss");//dd/MM/yyyy
        try {

            fechaformateada = sdfDate.parse(HHmmss);
        }
        catch (ParseException e)
        {
            // Error, la cadena de texto no se puede convertir en fecha.
        }


        return fechaformateada;

    }



    public static String dateToString(Date dateInput) throws ParseException
    {
        Date date = dateInput;

        SimpleDateFormat fecha = new SimpleDateFormat("yyyyMMdd");
        String convertido = fecha.format(date);

        return convertido;

    }

    public static String dateToStringFormat(Date inputDate, String format) throws ParseException
    {
        Date date = inputDate;

        SimpleDateFormat fecha = new SimpleDateFormat(format);
        String convertido = fecha.format(date);

        return convertido;

    }

    public static String dateGregoriantToString(XMLGregorianCalendar date) throws ParseException
    {

        Date dateformat = date.toGregorianCalendar().getTime();

        SimpleDateFormat aux = new SimpleDateFormat("yyyyMMdd");
        String convertido = aux.format(dateformat);


        return convertido;

    }



    public static String hourToString(Date inputDate) throws ParseException
    {
        Date date = inputDate;
        // Tambien se puede obtener solo la hora
        SimpleDateFormat hora = new SimpleDateFormat("HHmmss");
        String convertido = hora.format(date);
        //System.out.println(convertido);

        return convertido;

    }
    public static String currentDateToString()
    {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static Date gregorianDateToString(XMLGregorianCalendar inputGregorianDate)
    {

        Date dateformat = inputGregorianDate.toGregorianCalendar().getTime();


        return dateformat;
    }

    public static XMLGregorianCalendar getGregorianDate(Date inputDate) throws DatatypeConfigurationException
    {

        GregorianCalendar dateformat = new GregorianCalendar();
        dateformat.setTime(inputDate);
        XMLGregorianCalendar dateformat2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateformat);

        return dateformat2;
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    //Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\\\"(?:[\\\\x01-\\\\x08\\\\x0b\\\\x0c\\\\x0e-\\\\x1f\\\\x21\\\\x23-\\\\x5b\\\\x5d-\\\\x7f]|\\\\\\\\[\\\\x01-\\\\x09\\\\x0b\\\\x0c\\\\x0e-\\\\x7f])*\\\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\\\x01-\\\\x08\\\\x0b\\\\x0c\\\\x0e-\\\\x1f\\\\x21-\\\\x5a\\\\x53-\\\\x7f]|\\\\\\\\[\\\\x01-\\\\x09\\\\x0b\\\\x0c\\\\x0e-\\\\x7f])+)\\\\])"
    //		, Pattern.CASE_INSENSITIVE);


    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }

    public static boolean validaIdEcuador(String cedula) {

        int suma=0;

        if(cedula.length()==9){

            return false;

        }else{
            int a[]=new int [cedula.length()/2];
            int b[]=new int [(cedula.length()/2)];
            int c=0;
            int d=1;
            for (int i = 0; i < cedula.length()/2; i++) {
                a[i]=Integer.parseInt(String.valueOf(cedula.charAt(c)));
                c=c+2;
                if (i < (cedula.length()/2)-1) {
                    b[i]=Integer.parseInt(String.valueOf(cedula.charAt(d)));
                    d=d+2;
                }
            }

            for (int i = 0; i < a.length; i++) {
                a[i]=a[i]*2;
                if (a[i] >9){
                    a[i]=a[i]-9;
                }
                suma=suma+a[i]+b[i];
            }
            int aux=suma/10;
            int dec=(aux+1)*10;
            if ((dec - suma) == Integer.parseInt(String.valueOf(cedula.charAt(cedula.length()-1))))
                return true;
            else
            if(suma%10==0 && cedula.charAt(cedula.length()-1)=='0'){
                return true;
            }else{
                return false;
            }

        }
    }
}
