package com.easyflow.demodecompileapk.configuration.mq;

import java.io.Serializable;

public class Message implements Serializable {

    private Integer id;
    private String topic;
    private String messageContent;
    private Object object;
    private String process;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", messageContent='" + messageContent + '\'' +
                ", object=" + object +
                ", process='" + process + '\'' +
                '}';
    }

}
