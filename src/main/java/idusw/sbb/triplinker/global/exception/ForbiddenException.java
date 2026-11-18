package idusw.sbb.triplinker.global.exception;

/** 로그인은 했지만 그 데이터의 주인 · 멤버가 아닐 때. GlobalExceptionHandler 가 403 으로 바꾼다. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("접근 권한이 없습니다.");
    }
}
