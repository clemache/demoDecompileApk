package com.easyflow.demodecompileapk.controller;

import brut.androlib.exceptions.AndrolibException;
import com.easyflow.demodecompileapk.configuration.mq.Publisher;
import com.easyflow.demodecompileapk.service.ApkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/apk")
public class ApkToolController {

    private final ApkService apkService;
   // @Autowired
   // private Publisher _notifications;

    public ApkToolController(ApkService apkService) {
        this.apkService = apkService;
    }

    @PostMapping("/modify")
    public ResponseEntity<Resource> modifyApk(@RequestParam("file") MultipartFile apkFile) {

        //En postamn recibe la opcion de "ENVIAR Y DESCARGAR"
        //String PATH_DECOMPILATION = Paths.get("results").toString();
        String pathDecompile;
        String pathRecompile;
        String pathZipalign;
        File signedApkFile;
        File tempDirectoryResults = null;

        try {

            tempDirectoryResults = new File(System.getProperty("user.dir"), "results");
            if (!tempDirectoryResults.exists() && !tempDirectoryResults.mkdirs()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            //guarda el apk en el directorio temporal
            File tempFile = new File(tempDirectoryResults, apkFile.getOriginalFilename());
            apkFile.transferTo(tempFile);

            // DECOMPILA LA APK
             pathDecompile = apkService.decompileApk(tempFile.getAbsolutePath());

            // PRUEBA PARA MODIFICAR UN ARCHIVO ENVIANDO EL NOMBRE DEL ARCHIVO, EL VALOR A REMPLAZAR Y EL NUEVO VALOR
            try {
                String fileName = "Parametros.smali";
                String oldValue = "192.168.1.1";
                String newValue = "192.168.1.3";
                 String result = apkService.modifyOnFileValue(pathDecompile,fileName,oldValue,newValue,"user001","device");

            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            //COMPILA LA APK DE NUEVO CON LOS CAMBIOS
            pathRecompile = apkService.compileApk(pathDecompile);
            //_notifications.sendProcessInfo("Decompilando apk - "+tempFile.getName() ,"user001");

            //ZIPALIGN optimiza archivos APK para mejorar el rendimiento
            pathZipalign = apkService.zipalignApk(pathRecompile);

            //APKSIGNER | FIRMA LA APK
            signedApkFile = apkService.signApk(pathZipalign);

            // Preparar el archivo para ser descargado
            InputStreamResource resource = new InputStreamResource(new FileInputStream(signedApkFile));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(signedApkFile.getName())
                    .build());
            headers.setContentLength(signedApkFile.length());

           // _notifications.sendProcessInfo("APK generado de "+ tempFile.getName(),"user001");

            // Retornar el archivo firmado como respuesta
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);


        } catch (IOException | AndrolibException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            if(tempDirectoryResults != null) {
                apkService.deleteDirectory(tempDirectoryResults);
            }
        }
    }

    @PostMapping("/searchFile")
    public ResponseEntity<String> searchApk(@RequestParam("file") String test) {

        return ResponseEntity.ok("Test"+ test);
    }
}
