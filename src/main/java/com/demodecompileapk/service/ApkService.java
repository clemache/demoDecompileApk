package com.demodecompileapk.service;

import brut.androlib.ApkBuilder;
import brut.androlib.ApkDecoder;
import brut.androlib.Config;
import brut.androlib.exceptions.AndrolibException;
import brut.common.BrutException;
import brut.directory.DirectoryException;
import brut.directory.ExtFile;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;


@Service
public class ApkService {

    private static final String RESULTS_DIR = Paths.get("results").toAbsolutePath().toString();
    private static final String TOOLS_DIR = Paths.get("tools").toAbsolutePath().toString();
    private static final String OUTPUT_PATCH_DECOMPILATION =  Paths.get(RESULTS_DIR,"decompilation").toString();
    private static final String OUTPUT_DIR_NEW_APK = Paths.get(RESULTS_DIR,"recompiledApk").toString();
    private static final String OUTPUT_DIR_ZIPALIGN = Paths.get(RESULTS_DIR,"zipalign").toString();
    private static final File OUTPUT_DIR_SIGNED = new File(System.getProperty("user.dir"),"apkResult");
    private static final String KEY_STORE_FILE_PATH = Paths.get(TOOLS_DIR, "keyStore","AsistecomApp.jks").toString();
    private static final String KEY_STORE = "asistecom2024";

    private Config config = Config.getDefaultConfig();

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

    public String modifyFile(String filePath, String oldValue, String newValue) throws IOException {
        File file = new File(filePath);
        String errorMessage = "0";

        if (!file.exists() || !file.isFile()) {
            errorMessage = "Invalid file.";
        }

        // Verifica si el archivo es .smali o .xml
        if (!(file.getName().endsWith(".smali") || file.getName().endsWith(".xml"))) {
            errorMessage = "The file is not a .smali or .xml file.";
        }

        // lee el contenido del archivo
        String content = new String(Files.readAllBytes(file.toPath()));

        // Verifica si el valor viejo está presente
        if (!content.contains(oldValue)) {
            errorMessage = "Old value not found in file.";
        }

        // Remplaza el valor antiguo por el nuevo
        content = content.replace(oldValue, newValue);
        // vuelve a escribir en el archivo con el contenido modificado
        Files.write(file.toPath(), content.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        return errorMessage;

    }

    public String modifyFilesInDirectory(String directoryPath, String fileName, String oldValue, String newValue) throws IOException {
        Path dirPath = Paths.get(directoryPath);
        StringBuilder errorMessages = new StringBuilder();
        try (Stream<Path> paths = Files.walk(dirPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(fileName))
                    .forEach(p -> {
                        try {
                            String result = modifyFile(p.toString(), oldValue, newValue);
                            if (!result.equals("0")) {
                                errorMessages.append("Error in file ").append(p).append(": ").append(result).append("\n");
                            }
                        } catch (IOException e) {
                            errorMessages.append("Error modifying file ").append(p).append(": ").append(e.getMessage()).append("\n");
                        }
                    });
        }catch (IOException e) {
            errorMessages.append("Error walking the directory: ").append(e.getMessage()).append("\n");
        }
        if (errorMessages.length() > 0) {
            return "Modification completed with errors:\n" + errorMessages.toString();
        } else {
            return "Modification complete.";
        }
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

}
