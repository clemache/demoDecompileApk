package com.easyflow.demodecompileapk.service;

import brut.androlib.ApkBuilder;
import brut.androlib.ApkDecoder;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;

import com.easyflow.demodecompileapk.configuration.Log;
import com.easyflow.demodecompileapk.configuration.Result;
import com.easyflow.demodecompileapk.configuration.mq.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ApkService {

    @Autowired
    private Publisher _notifications;
    @Autowired
    private Log log;

    private  final Config config = Config.getDefaultConfig();
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static final String RESULTS_DIR = Paths.get("results").toAbsolutePath().toString();
    private static final String OUTPUT_PATCH_DECOMPILATION =  Paths.get(RESULTS_DIR,"decompilation").toString();
    private static final String OUTPUT_DIR_NEW_APK = Paths.get(RESULTS_DIR,"recompiledApk").toString();
    private static final String OUTPUT_DIR_ZIPALIGN = Paths.get(RESULTS_DIR,"zipalign").toString();
    private static final File OUTPUT_DIR_SIGNED = new File(System.getProperty("user.dir"),"apkResult");
    private static final String KEY_STORE = "asistecom2024";

    //Windows
    private static final String TOOLS_DIR = Paths.get("tools").toAbsolutePath().toString();
    private static final String KEY_STORE_FILE_PATH = Paths.get(TOOLS_DIR, "keyStore","AsistecomApp.jks").toString();

    //Linux tools
    private static final String TOOLS_DIR_LNX = "/app/tools";
    private static final String KEY_STORE_FILE_PATH_LNX = Paths.get(TOOLS_DIR_LNX, "keyStore","AsistecomApp.jks").toString();


    public Result decompileApk(String apkPath, String userName,String device,String program) throws AndrolibException{

        String className = this.getClass().getSimpleName();
        String nameofCurrMethod = new Throwable() .getStackTrace()[0].getMethodName();
        Result response = new Result();

        File apkFile = new File(apkPath);
        File outPutPath = new File(OUTPUT_PATCH_DECOMPILATION);
        ExtFile extFile = new ExtFile(apkFile);
        ApkDecoder apkDecoder = new ApkDecoder(config,extFile);

        if (outPutPath.exists()) {
            deleteDirectory(outPutPath);
        }
        _notifications.sendProcessInfo("Starting APK decompilation for file: "+apkFile.getName(),"system");
        try {
            apkDecoder.decode(outPutPath);
            response.setStatus(Result.Status.SUCCESSFUL);
            response.setHttp(Result.Http.OK);
            response.setId(0);
            response.setCodError(0);
            response.setMessage("Decompiled APK at: " + outPutPath.getAbsolutePath());
            response.setObject(outPutPath.getAbsolutePath());
            log.registerLog("0", className, nameofCurrMethod, response.getMessage());
            _notifications.sendProcessInfo(log.toString(), userName);
        }catch (AndrolibException | IOException | DirectoryException  e) {
            response.setMessage("Error to decompile APK: "+e.getMessage());
            log.registerErrorLog("99", className, nameofCurrMethod, response.getMessage());
            _notifications.sendProcessError(log.toString(), userName);
        }
        return response;
    }

    //Main Method Moodify
    public Result modifyValueFile(String directoryPathDecompilation, String nameFile, String oldValue, String newValue, String username, String device,String program) throws IOException {

        String className = this.getClass().getSimpleName();
        String nameofCurrMethod = new Throwable() .getStackTrace()[0].getMethodName();
        Result response = new Result();

        List<File> files = searchFiles(directoryPathDecompilation,nameFile);

        if(!files.isEmpty()) {
            for (File file : files) {
                if(isFileReadable(file)){
               // response = updateValue(file,oldValue,newValue,username,device);
                    response = updateValueWhitoutOldValue(file,newValue,username,device);
                }else {
                    response.setStatus(Result.Status.FAIL);
                    response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
                    response.setId(99);
                    response.setCodError(99);
                    response.setMessage("The file cannot be edited, it does not contain valid characters.");
                    log.registerErrorLog("99", className, nameofCurrMethod, response.getMessage());
                    _notifications.sendProcessInfo(log.toString(), username);
                }
            }
        }else{
            response.setStatus(Result.Status.FAIL);
            response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
            response.setId(99);
            response.setCodError(99);
            response.setMessage("No files found with the name "+nameFile);
            log.registerErrorLog("99", className, nameofCurrMethod, response.getMessage());
            _notifications.sendProcessInfo(log.toString(), username);
        }
        return response;
    }

    public Result compileApk(String pathDecompilation, String username,String device,String program) throws AndrolibException {

        String className = this.getClass().getSimpleName();
        String nameofCurrMethod = new Throwable() .getStackTrace()[0].getMethodName();
        Result response = new Result();

        File apkFileDecompiled = new File(pathDecompilation);
        File outPutPathNewApk = new File(OUTPUT_DIR_NEW_APK,"ASISTEAPP_RC.apk");

        if (!outPutPathNewApk.exists()) {
            if (!outPutPathNewApk.mkdirs()) {
                throw new AndrolibException("Failed to create output directory.");
            }
        }
        ExtFile extFile = new ExtFile(apkFileDecompiled);
        if (!apkFileDecompiled.exists() || !apkFileDecompiled.isDirectory()) {
            throw new AndrolibException("Source directory does not exist or is not a directory.");
        }

        try {
            ApkBuilder apkBuilder = new ApkBuilder(config,extFile);
            apkBuilder.build(outPutPathNewApk);
            response.setStatus(Result.Status.SUCCESSFUL);
            response.setHttp(Result.Http.OK);
            response.setId(0);
            response.setCodError(0);
            response.setMessage("Compiled APK at: " + outPutPathNewApk.getAbsolutePath());
            response.setObject(outPutPathNewApk.getAbsolutePath());
            log.registerLog("0", className, nameofCurrMethod, response.getMessage());
            _notifications.sendProcessInfo(log.toString(), username);

        } catch (Exception e) {
            response.setStatus(Result.Status.FAIL);
            response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
            response.setId(99);
            response.setCodError(99);
            response.setMessage("Error during APK compilation: " + e.getMessage());
            log.registerErrorLog("99", className, nameofCurrMethod, response.getMessage());
        }
        return response;
    }

    public Result zipalignApk(String apkPathZipalign, String username, String device, String program) throws InterruptedException, IOException {

        String className = this.getClass().getSimpleName();
        String nameofCurrMethod = new Throwable() .getStackTrace()[0].getMethodName();
        Result response = new Result();

        File outPutDirZipalign = new File(OUTPUT_DIR_ZIPALIGN);
        if (!outPutDirZipalign.mkdirs()) throw new IOException("Failed to create output directory.");
        File outPutPathApkZipalign = new File(outPutDirZipalign.getPath(),"ASISTEAPP_ZI.apk");

        try {

            String zipalignComand;
            if(OS.contains("win")){
                 zipalignComand = String.format("%s\\zipalign.exe -v 4 \"%s\" \"%s\"", TOOLS_DIR, apkPathZipalign, outPutPathApkZipalign.getAbsolutePath());
            }else{
                 zipalignComand = String.format("%s/zipalign -v 4 %s %s", TOOLS_DIR_LNX, apkPathZipalign, outPutPathApkZipalign.getAbsolutePath());
            }

            Process zipalignProcess = Runtime.getRuntime().exec(zipalignComand);
            // Lee las salidas estándar y de error del proceso
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(zipalignProcess.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(zipalignProcess.getErrorStream()));

            String s;
            StringBuilder output = new StringBuilder();
            while ((s = stdInput.readLine()) != null) {
                output.append(s).append("\n");
            }

            StringBuilder errorOutput = new StringBuilder();
            while ((s = stdError.readLine()) != null) {
                errorOutput.append(s).append("\n");
            }

            int zipalignExitCode = zipalignProcess.waitFor();
            if (zipalignExitCode == 0) {
                response.setStatus(Result.Status.SUCCESSFUL);
                response.setCodError(0);
                response.setId(0);
                response.setMessage("Apk aligned at: " + outPutPathApkZipalign.getAbsolutePath());
                response.setObject(outPutPathApkZipalign.getAbsolutePath());
                log.registerLog("0", className, nameofCurrMethod,response.getMessage());
                _notifications.sendProcessInfo(log.toString(),username);
            } else {
                response.setStatus(Result.Status.FAIL);
                response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
                response.setId(99);
                response.setCodError(99);
                response.setMessage("Failed to zip-align the APK. Exit code: " + zipalignExitCode + "\n" + errorOutput);
                log.registerErrorLog("99", className, nameofCurrMethod, response.getMessage());
                _notifications.sendProcessInfo(log.toString(),username);
            }
        } catch (IOException | InterruptedException e) {
            response.setStatus(Result.Status.FAIL);
            response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
            response.setId(99);
            response.setCodError(99);
            response.setMessage("Failed to zip-align the APK: " + e.getMessage());
            log.registerErrorLog("99", className, nameofCurrMethod, response.getMessage());
            _notifications.sendProcessInfo(log.toString(),username);
        }
        return response;
    }

    public Result signApk(String zipalignedApkPath, String username, String device, String program) throws IOException, InterruptedException {

        String className = this.getClass().getSimpleName();
        String nameofCurrMethod = new Throwable() .getStackTrace()[0].getMethodName();
        Result response = new Result();

        if(!OUTPUT_DIR_SIGNED.exists()) {
            if(!OUTPUT_DIR_SIGNED.mkdirs()) throw new IOException("Failed to create output directory.");
        }
        File outFileApkSigned = new File(OUTPUT_DIR_SIGNED.getPath(),"ASISTEAPP_SG.apk");

        try{
            String apkSignerCommand;
            if(OS.contains("win")){
                apkSignerCommand = String.format("%s\\apksigner.bat sign --ks \"%s\" --ks-pass pass:%s --v1-signing-enabled true --v2-signing-enabled true --out \"%s\" \"%s\"",
                        TOOLS_DIR, KEY_STORE_FILE_PATH, KEY_STORE,outFileApkSigned ,zipalignedApkPath);
            }else{
                apkSignerCommand = String.format("%s/apksigner sign --ks %s --ks-pass pass:%s --v1-signing-enabled true --v2-signing-enabled true --out %s %s",
                        TOOLS_DIR_LNX, KEY_STORE_FILE_PATH_LNX, KEY_STORE, outFileApkSigned, zipalignedApkPath);
            }

            Process apkSignerProcess = Runtime.getRuntime().exec(apkSignerCommand);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(apkSignerProcess.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(apkSignerProcess.getErrorStream()));

            String s;
            StringBuilder output = new StringBuilder();
            while ((s = stdInput.readLine()) != null) {
                output.append(s).append("\n");
            }

            StringBuilder errorOutput = new StringBuilder();
            while ((s = stdError.readLine()) != null) {
                errorOutput.append(s).append("\n");
            }

            int signExitCode = apkSignerProcess.waitFor();
            if (signExitCode == 0) {
                response.setStatus(Result.Status.SUCCESSFUL);
                response.setHttp(Result.Http.OK);
                response.setId(0);
                response.setCodError(0);
                response.setMessage("Apk signed successfully, at : "+ outFileApkSigned.getAbsolutePath());
                response.setObject(outFileApkSigned);
                log.registerLog("0", className, nameofCurrMethod, response.getMessage());
                _notifications.sendProcessInfo(log.toString(),username);
            }else {
                response.setStatus(Result.Status.FAIL);
                response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
                response.setId(99);
                response.setCodError(99);
                response.setMessage("Failed to sign the APK. Exit code: " + signExitCode + "\n" + errorOutput);
                log.registerErrorLog("99", className, nameofCurrMethod, response.getMessage());
                _notifications.sendProcessInfo(log.toString(),username);
            }
        }catch (IOException | InterruptedException e) {
            response.setStatus(Result.Status.FAIL);
            response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
            response.setId(99);
            response.setCodError(99);
            response.setMessage("Failed to sign the APK: " + e.getMessage());
            log.registerErrorLog("99", className, nameofCurrMethod, response.getMessage());
            _notifications.sendProcessInfo(log.toString(),username);
        }
        return response;
    }

    //Functiosn extras
    public void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    public Result updateValue(File file, String oldValue, String newValue, String username, String device) throws IOException {
        String className = this.getClass().getSimpleName();
        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        Result response = new Result();

        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid file.");
        }

        List<String> allowedExtensions = Arrays.asList(".smali", ".xml");
        boolean isValidExtension = allowedExtensions.stream()
                .anyMatch(extension -> file.getName().endsWith(extension));

        if (!isValidExtension) {
            throw new IllegalArgumentException("The file is not a .smali or .xml file.");
        }

        String content = new String(Files.readAllBytes(file.toPath()));
        if(content.contains(oldValue)){
            String updatedContent = content.replace(oldValue, newValue);
            Files.write(file.toPath(), updatedContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            response.setStatus(Result.Status.SUCCESSFUL);
            response.setId(0);
            response.setCodError(0);
            response.setMessage("Value changed successfully.");
            log.registerLog("0", className, nameofCurrMethod, response.getMessage());
            _notifications.sendProcessInfo(log.toString(),username);
        }else{
            response.setStatus(Result.Status.FAIL);
            response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
            response.setId(99);
            response.setCodError(99);
            response.setMessage("Old value not found in file.");
            log.registerErrorLog("99",className,nameofCurrMethod,response.getMessage());
            _notifications.sendProcessInfo(log.toString(),username);
        }
        return response;
    }

    //Modify without oldValue
    public Result updateValueWhitoutOldValue(File file, String newValue, String username, String device) throws IOException {
        String className = this.getClass().getSimpleName();
        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
        Result response = new Result();
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid file.");
        }
        List<String> allowedExtensions = Arrays.asList(".smali", ".xml");
        boolean isValidExtension = allowedExtensions.stream()
                .anyMatch(extension -> file.getName().endsWith(extension));

        if (!isValidExtension) {
            throw new IllegalArgumentException("The file is not a .smali or .xml file.");
        }
        String content = new String(Files.readAllBytes(file.toPath()));
        String keyword = "sput-object v0, Lcom/example/asisteapp/Parametros;->dirServer:Ljava/lang/String;";
        String updateContent = content;
        String[] lines = null;
        if (content.contains(keyword)) {
            lines = content.split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].contains(keyword)) {
                    String prefix = "const-string v0,";
                    String newLine = prefix + "\"" + newValue + "\"";
                    lines[i - 1] = newLine;
                    break;
                }
            }
            updateContent = String.join(System.lineSeparator(), lines);
            Files.write(file.toPath(), updateContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            response.setStatus(Result.Status.SUCCESSFUL);
            response.setId(0);
            response.setCodError(0);
            response.setMessage("Value changed successfully. without old value");
            log.registerLog("0", className, nameofCurrMethod, response.getMessage());
            _notifications.sendProcessInfo(log.toString(), username);
        }else{
            response.setStatus(Result.Status.FAIL);
            response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
            response.setId(99);
            response.setCodError(99);
            response.setMessage("Target field not found in file without old value.");
            log.registerErrorLog("99", className, nameofCurrMethod, response.getMessage());
            _notifications.sendProcessInfo(log.toString(), username);
        }
        return response;
    }

    public List<File> searchFiles(String directoryPath,String nameFile) throws IOException {
        List<File> files = new ArrayList<>();
        Path dirPath = Paths.get(directoryPath);
        try(Stream<Path> paths = Files.walk(dirPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(nameFile))
                    .forEach(p -> {
                        files.add(p.toFile());
                    });
        }catch ( IOException e){
            throw new IllegalArgumentException("Error walking the directory "+e.getMessage());
        }
        return files;
    }

    public boolean isFileReadable(File file) {
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            while (reader.readLine() != null) {
                for (char c : reader.readLine().toCharArray()) {
                    if(Character.isISOControl(c) && !Character.isWhitespace(c)) {
                        return false;
                    }
                }
                return true;
            }
        } catch (IOException e){
            return false;
        }
        return false;
    }

}
