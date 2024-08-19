package com.easyflow.demodecompileapk.service;

import brut.androlib.ApkBuilder;
import brut.androlib.ApkDecoder;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.common.BrutException;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;
import com.easyflow.demodecompileapk.configuration.Log;
import com.easyflow.demodecompileapk.configuration.mq.Message;
import com.easyflow.demodecompileapk.configuration.mq.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Config config = Config.getDefaultConfig();

    private static final String RESULTS_DIR = Paths.get("results").toAbsolutePath().toString();
    private static final String TOOLS_DIR = Paths.get("tools").toAbsolutePath().toString();
    private static final String OUTPUT_PATCH_DECOMPILATION =  Paths.get(RESULTS_DIR,"decompilation").toString();
    private static final String OUTPUT_DIR_NEW_APK = Paths.get(RESULTS_DIR,"recompiledApk").toString();
    private static final String OUTPUT_DIR_ZIPALIGN = Paths.get(RESULTS_DIR,"zipalign").toString();
    private static final File OUTPUT_DIR_SIGNED = new File(System.getProperty("user.dir"),"apkResult");
    private static final String KEY_STORE_FILE_PATH = Paths.get(TOOLS_DIR, "keyStore","AsistecomApp.jks").toString();
    private static final String KEY_STORE = "asistecom2024";

    public String decompileApk(String apkPath) throws AndrolibException{

        File apkFile = new File(apkPath);
        File outPutPath = new File(OUTPUT_PATCH_DECOMPILATION);
        ExtFile extFile = new ExtFile(apkFile);
        ApkDecoder apkDecoder = new ApkDecoder(config,extFile);

        if (outPutPath.exists()) {
            deleteDirectory(outPutPath);
        }

        try {
            apkDecoder.decode(outPutPath);
           // return "Decompile app successfully.";
            return outPutPath.getAbsolutePath();
        }catch (AndrolibException | IOException | DirectoryException  e) {
            return  "Error to decompile APK: " + e.getMessage();
        }

    }

    //Main Method Moodify
    public String modifyOnFileValue(String directoryPathDecompilation, String nameFile, String oldValue, String newValue, String username, String device) throws IOException {
        List<File> files = searchFiles(directoryPathDecompilation,nameFile);
        String response = "";
        if(!files.isEmpty()) {
            for (File file : files) {
                if(isFileReadable(file)){
                    updateValueInFile(file,oldValue,newValue,username,device);
                }else {
                    response = "Los archivos no se pueden editar.";
                }
            }
        }else{
            response = "No se encontrarn archivos con el nombre "+nameFile;
        }
        return response;
    }

    public void updateValueInFile(File file, String oldValue, String newValue, String username, String device) throws IOException {
        String className = this.getClass().getName();
        String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();

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
        if (!content.contains(oldValue)) {
            log.registerLog("0",className,nameofCurrMethod,"Old value not found in file.");
            Message message = new Message();
            message.setId(0);
            message.setTopic("Error");
            message.setMessageContent("Old value not found in file.");
            message.setObject(null);
            message.setProcess("Update File");
            _notifications.sendMessageError(message);
            //throw new IllegalArgumentException("Old value not found in file.");
        }

        String updatedContent = content.replace(oldValue, newValue);
        if (!content.equals(updatedContent)) {
            Files.write(file.toPath(), updatedContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);


        }
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

    public String compileApk(String pathDecompilation) throws AndrolibException {
        File apkFileDecompiled = new File(pathDecompilation);
        File outPutPathNewApk = new File(OUTPUT_DIR_NEW_APK,"ASISTEAPP_RC.apk");

        if (!outPutPathNewApk.exists()) {
            if (!outPutPathNewApk.mkdirs()) {
                return "Failed to create output directory.";
            }
        }
        //Verifica si existe la ruta de los archivos decompilados de la apk
        ExtFile extFile = new ExtFile(apkFileDecompiled);
        if (!apkFileDecompiled.exists() || !apkFileDecompiled.isDirectory()) {
            return "Source directory does not exist or is not a directory.";
        }

        try {
            ApkBuilder apkBuilder = new ApkBuilder(config,extFile);
            // Recompilar APK
            apkBuilder.build(outPutPathNewApk);
            return outPutPathNewApk.getAbsolutePath();
            //return "Recompile successfull new apk";

        } catch (AndrolibException e) {
            return "Error during APK compilation: " + e.getMessage();
        } catch (BrutException e) {
            throw new RuntimeException(e);
        }
    }

    public String zipalignApk(String apkPathZipalign) throws InterruptedException, IOException {

        File outPutDirZipalign = new File(OUTPUT_DIR_ZIPALIGN);
        if (!outPutDirZipalign.mkdirs()) return "Failed to create output directory.";
        File outPutPathApkZipalign = new File(outPutDirZipalign.getPath(),"ASISTEAPP_ZI.apk");

        try {
            String ZipalignCommand = String.format("%s\\zipalign.exe -v 4 \"%s\" \"%s\"", TOOLS_DIR, apkPathZipalign, outPutPathApkZipalign);
            Process zipalignProcess = Runtime.getRuntime().exec(ZipalignCommand);
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
                return outPutPathApkZipalign.getAbsolutePath();
                //return "APK zip-aligned successfully!\n";
            } else {
                return "Failed to zip-align the APK. Exit code: " + zipalignExitCode + "\n" + errorOutput.toString();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public File signApk(String zipalignedApkPath) throws IOException, InterruptedException {

        if(!OUTPUT_DIR_SIGNED.exists()) {
            if(!OUTPUT_DIR_SIGNED.mkdirs()) throw new IOException("Failed to create output directory.");
        }
        //if (!OUTPUT_DIR_SIGNED.mkdirs()) throw  new IOException("Failed to create output directory.");
        File outPutPathApkSigned = new File(OUTPUT_DIR_SIGNED.getPath(),"ASISTEAPP_SG.apk");

        try{
            String apksignerCommand = String.format("%s\\apksigner.bat sign --ks \"%s\" --ks-pass pass:%s --v1-signing-enabled true --v2-signing-enabled true --out \"%s\" \"%s\"",
                    TOOLS_DIR, KEY_STORE_FILE_PATH, KEY_STORE,outPutPathApkSigned ,zipalignedApkPath);
            Process apkSignerProcess = Runtime.getRuntime().exec(apksignerCommand);

            //lectura de las salidas estandar y errores
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
                 return outPutPathApkSigned;
                //return "APK signature successfully!\n";
            }else {
                throw new IOException("Failed to sign the APK. Exit code: " + signExitCode + "\n" + errorOutput.toString());
            }
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
    }

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
