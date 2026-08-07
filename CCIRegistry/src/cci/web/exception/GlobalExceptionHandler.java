package cci.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundCertificateException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(NotFoundCertificateException ex) {
        ErrorResponse error = new ErrorResponse("404", "Not Found", ex.getMessage() );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BadRequestNumberException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(BadRequestNumberException ex) {
        ErrorResponse error = new ErrorResponse("400", "Bad Request", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(CertificateGetFileException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CertificateGetFileException ex) {
        ErrorResponse error = new ErrorResponse("500", "Internal Server Error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(EnterpriseGetFileException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(EnterpriseGetFileException ex) {
        ErrorResponse error = new ErrorResponse("404", "Not Found", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(EnterpriseReadFileException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(EnterpriseReadFileException ex) {
        ErrorResponse error = new ErrorResponse("500", "Internal Server Error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse error = new ErrorResponse("500", "Internal Server Error","An unexpected error occurred: " + ex.getMessage() );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
        
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        ErrorResponse error = new ErrorResponse("400", "Bad Endpoint", "Метод не определен или отсутствует номер сертификата/акта экспертиз/УНП организации");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    
}