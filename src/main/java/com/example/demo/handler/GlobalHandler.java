package com.example.demo.handler;

import com.example.demo.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalHandler.class);

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ResponseError> userAlreadyExistsExcHandler(UserAlreadyExistsException exc) {
        LOGGER.error("[User Already Exists Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.CONFLICT);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseError);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseError> userNotFoundExcHandler(UserNotFoundException exc) {
        LOGGER.error("[User Not Found Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> methodArgumentNotValidExcHandler(MethodArgumentNotValidException exc) {
        List<String> errorMessages = new ArrayList<>();

        for (FieldError fieldError : exc.getFieldErrors()) {
            errorMessages.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }

        LOGGER.error("[Validation Error]: {}", errorMessages);

        ResponseError responseError = buildResponseError(
                String.join(" ", errorMessages),
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
    }

    @ExceptionHandler(OpenIdValidationException.class)
    public ResponseEntity<ResponseError> openIdValidationExcHandler(OpenIdValidationException exc) {
        LOGGER.error("[Open Id Validation Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
    }

    @ExceptionHandler(OpenIdServiceException.class)
    public ResponseEntity<ResponseError> openIdServiceExcHandler(OpenIdServiceException exc) {
        LOGGER.error("[Open Id Service Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
    }

    @ExceptionHandler(MinIoException.class)
    public ResponseEntity<ResponseError> minIoExcHandler(MinIoException exc) {
        LOGGER.error("[MinIo Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
    }

    @ExceptionHandler(ManufacturerNotFoundException.class)
    public ResponseEntity<ResponseError> manufacturerNotFoundExcHandler(ManufacturerNotFoundException exc) {
        LOGGER.error("[Manufacturer Not Found Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseError);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ResponseError> categoryNotFoundExcHandler(CategoryNotFoundException exc) {
        LOGGER.error("[Category Not Found Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseError);
    }

    @ExceptionHandler(ToolNotFoundException.class)
    public ResponseEntity<ResponseError> toolNotFoundExcHandler(ToolNotFoundException exc) {
        LOGGER.error("[Tool Not Found Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseError);
    }

    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<ResponseError> fileValidationExcHandler(FileValidationException exc) {
        LOGGER.error("[File Validation Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseError);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ResponseError> fileUploadExcHandler(FileUploadException exc) {
        LOGGER.error("[File Upload Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseError> authenticationExcHandler(AuthenticationException exc) {
        LOGGER.error("[Authentication Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.UNAUTHORIZED);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseError);
    }

    @ExceptionHandler(UserDontHavePermissionException.class)
    public ResponseEntity<ResponseError> userDontHavePermissionExcHandler(UserDontHavePermissionException exc) {
        LOGGER.error("[User Dont Have Permission Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.FORBIDDEN);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseError);
    }

    @ExceptionHandler(ElasticsearchException.class)
    public ResponseEntity<ResponseError> elasticsearchExcHandler(ElasticsearchException exc) {
        LOGGER.error("[Elastic Search Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseError);
    }

    @ExceptionHandler(DealNotFoundException.class)
    public ResponseEntity<ResponseError> dealNotFoundExcHandler(DealNotFoundException exc) {
        LOGGER.error("[Deal Not Found Exception]: {}", exc.getMessage());
        ResponseError responseError = buildResponseError(exc.getMessage(), HttpStatus.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseError);
    }


    private ResponseError buildResponseError(String message, HttpStatus status) {
        return ResponseError.builder()
                .message(message)
                .time(LocalDateTime.now())
                .status(status.value())
                .build();
    }



}
