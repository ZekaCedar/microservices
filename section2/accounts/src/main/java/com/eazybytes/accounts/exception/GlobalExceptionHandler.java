package com.eazybytes.accounts.exception;

import com.eazybytes.accounts.dto.ErrorResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     *
     * Requires extends ResponseEntityExceptionHandler
     * get all validation errors failed in the input data ~ validationErrorList
     * from each list ~ iterate 1 by 1 ~ the fieldName and the validationMsg ~ populate into the hashMap
     *  @Email(message = "Email address should be a valid value")
     *  !! Send the validationErrors as part of the ResponseEntity !!
     *
     *  sample result from PostMan
     *  {
     *     "mobileNumber": "Mobile number must be 10 digits",
     *     "name": "The length of the customer name should be between 5 and 30",
     *     "email": "Email address should be a valid value"
     * }
     *
     * Triggered from @Validated
     * public class AccountsController {........
     *
     * To communicate with Spring Boot to perform the validations define in dto
     * public ResponseEntity<ResponseDto> createAccount(@Valid @RequestBody CustomerDto customerDto){
     *
     * Exception thrown when validation annotated @Valid or Validation related annotations fails
     * Validation defined from the dto like @NotEmpty, @Email, @Pattern...
     *
     * Customize the handling of {@link MethodArgumentNotValidException}.
     * <p>This method delegates to {@link #handleExceptionInternal}.
     *
     * @param ex      the exception to handle
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} for the response to use, possibly
     * {@code null} when the response is already committed
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        List<ObjectError> validationErrorList = ex.getBindingResult().getAllErrors();

        validationErrorList.forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String validationMsg = error.getDefaultMessage();
            validationErrors.put(fieldName, validationMsg);
        });
        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
    }

    /**
     * For Runtime error, can be enhance to trigger an email to operation or
     * make an entry into one of the table
     *
     * How to Test RunTime Exception? Remove @AllArgsConstructor from AccountsController
     * causing no Autowiring for AccountsService resulting a NullPointException
     *
     * @param exception
     * @param webRequest
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception exception, WebRequest webRequest){
        ErrorResponseDto errorResponseDTO = new ErrorResponseDto(
                webRequest.getDescription(false), // what is the api path client is trying to invoke
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest webRequest){
        ErrorResponseDto errorResponseDTO = new ErrorResponseDto(
                webRequest.getDescription(false), // what is the api path client is trying to invoke
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomerAlreadyExistException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomerAlreadyExistsException(CustomerAlreadyExistException exception, WebRequest webRequest){
        ErrorResponseDto errorResponseDTO = new ErrorResponseDto(
                webRequest.getDescription(false), // what is the api path client is trying to invoke
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponseDTO, HttpStatus.BAD_REQUEST);
    }

}
