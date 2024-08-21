package com.easyflow.demodecompileapk.configuration;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.springframework.stereotype.Component;
import java.io.Serializable;

@Component
@XmlRootElement()
@JacksonXmlRootElement(localName = "result")
public class Result implements Serializable{


    private static final long serialVersionUID = 1L;
    private Status status;
    private Http http;
    private String message;
    private int id;
    private Object object;
    private int codError;

    public Result()
    {
        status = Status.FAIL;
        message = "Error: The status of the Result has not been assigned correctly";
        id = 99;
        codError = 99;
        http = Http.INTERNAL_SERVER_ERROR;
    }

    public enum Status {
        SUCCESSFUL(1),
        FAIL(2),
        UNANSWERED(3),
        VALIDATION_ERROR (4),
        UNKNOWN(99);

        int code;

        Status(int p) {
            code = p;
        }

        public int getStatus() {
            return code;
        }

        public Status getStatus(int code) {
            if(code == 1)
                return SUCCESSFUL;
            else if(code == 2)
                return FAIL;
            else if(code == 3)
                return UNANSWERED;
            else if(code == 4)
                return VALIDATION_ERROR;
            return UNKNOWN;
        }
    }

    public enum Http {

        OK(200),
        ACCEPTED(202),
        AMBIGUOUS(300),
        BADGATEWAY(502),
        BADREQUEST (400),
        CREATED(201),
        CONFLICT(409),
        GATEWAY_TIMEOUT(504),
        HTTP_VERSION_NOT_SUPPORTED(505),
        INTERNAL_SERVER_ERROR(500),
        NOCONTENT(204),
        NOTFOUND(404),
        REQUEST_TIMEOUT(408),
        FORBIDDEN(403);

        int code;

        Http(int p) {
            code = p;
        }

        public static Http getHttp(int code) {
            Http  type = Http.OK;

            switch (code) {
                case 200:
                    type = OK;
                case 202:
                    type = ACCEPTED;
                case 300:
                    type = AMBIGUOUS;
                case 502:
                    type = BADGATEWAY;
                case 400:
                    type = BADREQUEST;
                case 201:
                    type = CREATED;
                case 409:
                    type = CONFLICT;
                case 504:
                    type = GATEWAY_TIMEOUT;
                case 505:
                    type = HTTP_VERSION_NOT_SUPPORTED;
                case 500:
                    type = INTERNAL_SERVER_ERROR;
                case 204:
                    type = NOCONTENT;
                case 404:
                    type = NOTFOUND;
                case 408:
                    type = REQUEST_TIMEOUT;
                case 403:
                    type = FORBIDDEN;
                default:
                    type = NOTFOUND;

                    return type;

            }

        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(Http http) {
        this.http = http;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public int getCodError() {
        return codError;
    }

    public void setCodError(int codError) {
        this.codError = codError;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + codError;
        result = prime * result + ((http == null) ? 0 : http.hashCode());
        result = prime * result + id;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Result other = (Result) obj;
        if (codError != other.codError)
            return false;
        if (http != other.http)
            return false;
        if (id != other.id)
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (object == null) {
            if (other.object != null)
                return false;
        } else if (!object.equals(other.object))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return id+" "+message;

    }


}