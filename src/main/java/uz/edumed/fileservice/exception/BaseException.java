package uz.edumed.fileservice.exception;

public abstract class BaseException extends RuntimeException{

    BaseException(){}

    BaseException(Throwable cause){
        super(cause);
    }
}
