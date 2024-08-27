package com.easyflow.demodecompileapk.controller;

import brut.androlib.exceptions.AndrolibException;
import com.easyflow.demodecompileapk.configuration.Result;
import com.easyflow.demodecompileapk.service.ApkService;
import com.easyflow.demodecompileapk.util.ModificationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/apk")
public class ApkToolController {

    private final ApkService _apkService;

    public ApkToolController(ApkService _apkService) {
        this._apkService = _apkService;
    }


    @GetMapping("/modifyapk")
    public ResponseEntity<?> modifyValueApk(@RequestParam("apk") MultipartFile apkFile,@RequestParam("nameFile") String nameFile,
                                            @RequestParam("oldValue") String oldValue,@RequestParam("newValue") String newValue,
                                            @RequestParam("username") String username,@RequestParam("device") String device,@RequestParam("program") String program) {
        File tempDirectoryResults = null;
        Result response = new Result();

        if (apkFile == null || apkFile.isEmpty() ||
                nameFile == null || nameFile.trim().isEmpty() ||
                oldValue == null || oldValue.trim().isEmpty() ||
                newValue == null || newValue.trim().isEmpty()) {
            response.setStatus(Result.Status.FAIL);
            response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
            response.setId(99);
            response.setCodError(99);
            response.setMessage("Todos los parámetros son obligatorios y no pueden estar vacíos.");
            return ResponseEntity
                    .created(null)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(response);
        }else {

            try {
                tempDirectoryResults = new File(System.getProperty("user.dir"), "results");
                if (!tempDirectoryResults.exists() && !tempDirectoryResults.mkdirs()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }
                //save apk at temp directory
                File apkOnDirectory = new File(tempDirectoryResults, Objects.requireNonNull(apkFile.getOriginalFilename()));
                apkFile.transferTo(apkOnDirectory);
                //DECOMPILE APK
                response = _apkService.decompileApk(apkOnDirectory.getAbsolutePath(),username,device,program);
                if(response.getCodError()==0){
                    //Test to modify a file
                    String pathDecompileApk=(String)response.getObject();
                    response = _apkService.modifyValueFile(pathDecompileApk,nameFile,oldValue,newValue,username,device,program);
                    if(response.getCodError()==0){
                        //compilar de nuevo
                        response = _apkService.compileApk(pathDecompileApk,username,device,program);
                        if (response.getCodError()==0) {
                            //comílado correcto
                            //Align apk
                            response = _apkService.zipalignApk((String) response.getObject(),username,device,program);
                            if(response.getCodError()==0){
                                //Sign apk
                                response = _apkService.signApk((String)response.getObject(),username,device,program);
                                if(response.getCodError()==0){
                                    /*
                                    // ENVIO DE APK FILE DESCARGABLE
                                    File signedApkFile = (File)response.getObject();
                                    InputStreamResource resource = new InputStreamResource(new FileInputStream(signedApkFile));
                                    HttpHeaders headers = new HttpHeaders();
                                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                                    headers.setContentDisposition(ContentDisposition.builder("attachment")
                                            .filename(signedApkFile.getName())
                                            .build());
                                    headers.setContentLength(signedApkFile.length());
                                    return new ResponseEntity<>(resource, headers, HttpStatus.OK);
                                    */
                                    //Envio respuesta de la ruta donde esta la apk final

                                    return ResponseEntity
                                            .created(null)
                                            .contentType(MediaType.APPLICATION_XML)
                                            .body(response);

                                }else{
                                    // error al firmar la apk
                                    return ResponseEntity
                                            .created(null)
                                            .contentType(MediaType.APPLICATION_XML)
                                            .body(response);
                                }
                            }else{
                                // error al alinear el apk
                                return ResponseEntity
                                        .created(null)
                                        .contentType(MediaType.APPLICATION_XML)
                                        .body(response);
                            }
                        }else {
                            //error al compilar
                            return ResponseEntity
                                    .created(null)
                                    .contentType(MediaType.APPLICATION_XML)
                                    .body(response);
                        }
                    }else{
                        // error al modificar
                        return ResponseEntity
                                .created(null)
                                .contentType(MediaType.APPLICATION_XML)
                                .body(response);
                    }
                }else{
                    // error al decompilar la apk
                    return ResponseEntity
                            .created(null)
                            .contentType(MediaType.APPLICATION_XML)
                            .body(response);
                }
            } catch (IOException | AndrolibException | InterruptedException e) {
                // error during process
                response.setStatus(Result.Status.FAIL);
                response.setHttp(Result.Http.INTERNAL_SERVER_ERROR);
                response.setId(99);
                response.setCodError(99);
                response.setMessage(e.getMessage());
                return ResponseEntity
                        .created(null)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(response);
            }finally {
                if(tempDirectoryResults != null) {
                   // _apkService.deleteDirectory(tempDirectoryResults);
                }
            }
        }
    }


    @PostMapping("/test")
    public ResponseEntity<String> searchApk(@RequestParam("modifications") String modificationsJS) {
        File tempDirectoryResults = null;
        Result response = new Result();

        //Lista para modificaciones
        List<ModificationRequest> modificationRequestList = parseModifications(modificationsJS);
        ModificationRequest modificationRequest1 = modificationRequestList.get(0);

        return ResponseEntity.ok("Test = "+ modificationRequest1.getTypeFile());
    }

    private List<ModificationRequest> parseModifications(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, new TypeReference<List<ModificationRequest>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al parsear el JSON de modificaciones", e);
        }
    }

}
