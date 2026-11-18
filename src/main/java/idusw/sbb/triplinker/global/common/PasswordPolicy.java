package idusw.sbb.triplinker.global.common;

/**
 * 비밀번호 규칙. 가입(SignUpRequestDTO) · 재설정(PasswordResetDTO) 의 @Pattern 과 같은 식이다.
 * 마이페이지 비밀번호 변경은 DTO 없이 Map 으로 받아서 이 규칙을 서비스에서 직접 확인한다.
 */
public final class PasswordPolicy {

    public static final String REGEX = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}";
    public static final String MESSAGE = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.";

    private PasswordPolicy() {
    }

    public static boolean isValid(String raw) {
        return raw != null && raw.matches(REGEX);
    }
}
