package idusw.sbb.triplinker.security;

/** 다른 패키지 시험에서 TripFixture.loginAs 를 쓰기 위한 다리. */
public final class TripFixtureAccess {
    private TripFixtureAccess() {}

    public static void loginAs(Long userId) {
        TripFixture.loginAs(userId);
    }
}
