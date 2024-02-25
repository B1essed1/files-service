package uz.edumed.fileservice.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ExceptionWithStatusCode extends  BaseException {
    private Integer httpStatusCode;
    private String messageKey;
    private Objects[] args;

    public ExceptionWithStatusCode(Integer status, String messageKey){
        this.httpStatusCode =status;
        this.messageKey = messageKey;
    }

    public ExceptionWithStatusCode(Integer status, String messageKey,Throwable cause){
        super(cause);
        this.httpStatusCode =status;
        this.messageKey = messageKey;
    }



    public ExceptionWithStatusCode(Integer httpStatusCode, String messageKey, Objects[] args) {
        this.httpStatusCode = httpStatusCode;
        this.messageKey = messageKey;
        this.args = args;
    }

    public ExceptionWithStatusCode(Throwable cause, Integer httpStatusCode, String messageKey, Objects[] args) {
        super(cause);
        this.httpStatusCode = httpStatusCode;
        this.messageKey = messageKey;
        this.args = args;
    }
}
