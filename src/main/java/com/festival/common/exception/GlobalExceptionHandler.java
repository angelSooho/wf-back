package com.festival.common.exception;

import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.slack.api.webhook.WebhookPayloads.payload;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    private final Slack slackClient = Slack.getInstance();

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    /**
     * 서비스 로직 도중 발생하는 에러들을 커스텀하여 응답값을 내려줍니다.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest request){
        ErrorCode errorCode = e.getErrorCode();
        sendSlackAlertErrorLog(e, request);
        return ResponseEntity.status(errorCode.getStatus()).body( new ErrorResponse(errorCode.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException() {
        String errorMessage = maxFileSize + " 이내 용량의 파일들만 업로드 할 수 있습니다.";
        log.error(errorMessage);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(errorMessage));
    }

    /**
     * 바인딩 에러 json형식으로 에러가 있는 파라미터 값에 메시지를 담아 넘겨줍니다.
     *  itemPrice : 아이템 가격을 100원 이상 1,000,000,000원 이하여야 합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors()
                .forEach(c -> errors.put(((FieldError) c).getField(), c.getDefaultMessage()));
        log.error(ex.getMessage());
        return ResponseEntity.badRequest().body(errors);
    }

    private void sendSlackAlertErrorLog(CustomException e, HttpServletRequest request) {
        try {
            slackClient.send(webhookUrl, payload(p -> p
                    .text("\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8" +
                            " 서버에 에러가 감지되었습니다. 즉시 확인이 필요합니다. " +
                            "\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8")
                    // attachment는 list 형태여야 합니다.
                    .attachments(
                            List.of(generateSlackAttachment(e, request))
                    )
            ));
        } catch (IOException slackError) {
            // slack 통신 시 발생한 예외에서 Exception을 던져준다면 재귀적인 예외가 발생합니다.
            // 따라서 로깅으로 처리하였고, 모카콩 서버 에러는 아니므로 `error` 레벨보다 낮은 레벨로 설정했습니다.
            log.debug("Slack 통신과의 예외 발생");
        }
    }

    // attachment 생성 메서드
    private Attachment generateSlackAttachment(CustomException e, HttpServletRequest request) {
        String requestTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(LocalDateTime.now());
        String xffHeader = request.getHeader("X-FORWARDED-FOR");  // 프록시 서버일 경우 client IP는 여기에 담길 수 있습니다.
        return Attachment.builder()
                .color("ff0000")  // 붉은 색으로 보이도록
                .title(requestTime + " 발생 에러 로그")
// Field도 List 형태로 담아주어야 합니다.
                .fields(List.of(
                                generateSlackField("Request IP", xffHeader == null ? request.getRemoteAddr() : xffHeader),
                                generateSlackField("Request URL", request.getRequestURL() + " " + request.getMethod()),
                                generateSlackField("Error Code", e.getErrorCode().getStatus().toString()),
                                generateSlackField("Error Message", e.getErrorCode().getMessage())
                        )
                )
                .build();
    }

    // Field 생성 메서드
    private Field generateSlackField(String title, String value) {
        return Field.builder()
                .title(title)
                .value(value)
                .valueShortEnough(false)
                .build();
    }

}
