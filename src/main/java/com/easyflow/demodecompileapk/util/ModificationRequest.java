package com.easyflow.demodecompileapk.util;

public class ModificationRequest {

    private String nameFile;
    private modificationType type;
    private String oldValueOrVariable;
    private String newValue;
    private typeFile typeFile;

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public modificationType getType() {
        return type;
    }

    public void setType(modificationType type) {
        this.type = type;
    }

    public String getOldValueOrVariable() {
        return oldValueOrVariable;
    }

    public void setOldValueOrVariable(String oldValueOrVariable) {
        this.oldValueOrVariable = oldValueOrVariable;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public ModificationRequest.typeFile getTypeFile() {
        return typeFile;
    }

    public void setTypeFile(ModificationRequest.typeFile typeFile) {
        this.typeFile = typeFile;
    }

    @Override
    public String toString() {
        return "ModificationRequest{" +
                "nameFile='" + nameFile + '\'' +
                ", type=" + type +
                ", oldValueOrVariable='" + oldValueOrVariable + '\'' +
                ", newValue='" + newValue + '\'' +
                ", typeFile=" + typeFile +
                '}';
    }

    private enum modificationType {
        REPLACEVALUE,
        ADDLINE,
        REPLACELINE,
        REMOVELINEA
    }
    private enum typeFile {
        XML,
        SMALI
    }
}
