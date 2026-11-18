package idusw.sbb.triplinker.global.exception;

/** 같은 요청을 짧은 시간에 너무 많이 보냈을 때. GlobalExceptionHandler 가 429 로 바꾼다. */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
