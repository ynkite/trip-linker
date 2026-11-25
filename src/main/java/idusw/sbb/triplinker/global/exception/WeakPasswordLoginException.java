package idusw.sbb.triplinker.global.exception;

import idusw.sbb.triplinker.global.common.PasswordPolicy;

/**
 * 비밀번호는 맞지만 지금 규칙(PasswordPolicy)에 맞지 않아 로그인을 막을 때.
 * 규칙이 생기기 전에 만든 계정(관리자 · 테스트용 포함)이 쉬운 비밀번호를 계속 쓰지 못하게 한다.
 */
public class WeakPasswordLoginException extends RuntimeException {

    public WeakPasswordLoginException() {
        super("비밀번호가 지금 규칙에 맞지 않아 로그인할 수 없습니다. 「비밀번호 재설정」 으로 새 비밀번호를 정해 주세요. ("
                + PasswordPolicy.MESSAGE + ")");
    }
}
