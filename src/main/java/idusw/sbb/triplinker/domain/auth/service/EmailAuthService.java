package idusw.sbb.triplinker.domain.auth.service;

import idusw.sbb.triplinker.domain.auth.entity.EmailAuth;
import idusw.sbb.triplinker.domain.auth.repository.EmailAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import idusw.sbb.triplinker.global.exception.TooManyRequestsException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


//인증번호 생성, DB 저장, 실제 메일 발송

@Service
@RequiredArgsConstructor
public class EmailAuthService {
    // 구글 계정을 통해 실제로 메일을 발송해 주는 스프링 내장 도구
    private final JavaMailSender mailSender;

    // 발송한 인증번호를 DB에 저장하기 위한 Repository
    private final EmailAuthRepository emailAuthRepository;

    // ── 인증번호 남용 막기 (서버 한 대라 메모리에 둔다. 재시작하면 비워지고, 그때는 번호를 다시 받으면 됨) ──
    static final int MAX_WRONG_TRIES = 5;          // 한 번호당 틀릴 수 있는 횟수
    static final int MAX_SENDS = 5;                // 같은 이메일로 보낼 수 있는 횟수 ...
    static final long SEND_WINDOW_MS = 10 * 60 * 1000L; // ... 10분 동안
    static final long VERIFIED_TTL_MINUTES = 30;   // 인증을 마친 뒤 가입 · 재설정을 끝내야 하는 시간

    private static final SecureRandom RANDOM = new SecureRandom();
    private final Map<String, Integer> wrongTries = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> verifiedUntil = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> sendLog = new ConcurrentHashMap<>();

    //프론트엔드에서 이메일을 넘겨주면, 6자리 난수를 생성하여 발송하고 DB에 기록
    @Transactional
    public void sendEmailAuthCode(String email, String type) {
        // 같은 주소로 10분에 5번까지만 보낸다 (남의 메일함에 반복 발송 · 발송 한도 소진 막기)
        rememberSend(email);
        wrongTries.remove(key(email));
        // 6자리 랜덤 숫자(인증번호) 생성
        String authCode = generateRandomCode();
        String subject;
        String text;

        if ("reset".equals(type)) {
            subject = "[TripLinker] 비밀번호 재설정 인증번호입니다.";
            text = "비밀번호 재설정을 위한 인증번호입니다.\n\n인증번호: " + authCode;
        } else {
            subject = "[TripLinker] 회원가입 이메일 인증번호입니다.";
            text = "TripLinker 회원가입을 환영합니다!\n\n인증번호: " + authCode;
        }

        // DB 임시 창고에 저장하기 위한 엔티티
        // 인증 만료 시간은 현재 시간에 3분 플러스
        EmailAuth emailAuth = EmailAuth.builder()
                .email(email)
                .authCode(authCode)
                .expiryDate(LocalDateTime.now().plusMinutes(3))
                .build();

        // DB에 방금 만든 인증 정보 저장
        emailAuthRepository.save(emailAuth);

        // 구글을 통해 실제 메일 발송 설정
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        // 메일 최종 발송
        mailSender.send(message);
    }


    //사용자가 화면에 입력한 인증번호가 맞는지, 3분이 지나지 않았는지 확인

    @Transactional(readOnly = true) // 데이터베이스를 읽기만 할 때 성능을 높여주는 옵션
    public boolean verifyEmailCode(String email, String userInputCode) {

        // 해당 이메일로 발송된 가장 최근의 인증 정보를 DB 창고에서 꺼내옵니다
        EmailAuth emailAuth = emailAuthRepository.findTopByEmailOrderByExpiryDateDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 인증 번호를 요청한 이력이 없습니다."));

        // 만료 시간(3분)이 지났는지 확인합니다. (현재 시간과 비교)
        if (emailAuth.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 번호 유효시간(3분)이 초과되었습니다. 다시 요청해 주세요.");
        }

        // 한 번호를 5번 틀리면 맞는 번호를 넣어도 막는다 — 6자리를 차례로 넣어 보는 공격 방지
        if (wrongTries.getOrDefault(key(email), 0) >= MAX_WRONG_TRIES) {
            throw new IllegalArgumentException("인증 번호를 " + MAX_WRONG_TRIES + "번 틀렸습니다. 인증번호를 다시 받아 주세요.");
        }

        // 사용자가 입력한 번호와 DB에 저장된 진짜 번호가 똑같은지 비교합니다.
        if (!emailAuth.getAuthCode().equals(userInputCode)) {
            wrongTries.merge(key(email), 1, Integer::sum);
            throw new IllegalArgumentException("인증 번호가 일치하지 않습니다.");
        }

        // 에러 없이 통과했다면 인증 성공 — 가입 · 재설정을 서버에서도 확인할 수 있게 기록
        verifiedUntil.put(key(email), LocalDateTime.now().plusMinutes(VERIFIED_TTL_MINUTES));
        return true;
    }

    //100000 ~ 999999 사이의 6자리 랜덤 숫자를 생성하는 내부 헬퍼 메서드

    private String generateRandomCode() {
        // 예측 가능한 java.util.Random 대신 SecureRandom (FindSecBugs PREDICTABLE_RANDOM)
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    /** 이 이메일이 최근 30분 안에 인증번호 확인을 통과했는지 (회원가입에서 서버 쪽으로 다시 확인). */
    public boolean isVerified(String email) {
        LocalDateTime until = verifiedUntil.get(key(email));
        return until != null && until.isAfter(LocalDateTime.now());
    }

    /** 가입 · 비밀번호 변경이 끝나면 그 번호를 지워 다시 쓰지 못하게 한다. */
    @Transactional
    public void consume(String email) {
        emailAuthRepository.findTopByEmailOrderByExpiryDateDesc(email).ifPresent(emailAuthRepository::delete);
        verifiedUntil.remove(key(email));
        wrongTries.remove(key(email));
    }

    private void rememberSend(String email) {
        long now = System.currentTimeMillis();
        Deque<Long> log = sendLog.computeIfAbsent(key(email), k -> new ArrayDeque<>());
        synchronized (log) {
            while (!log.isEmpty() && now - log.peekFirst() > SEND_WINDOW_MS) log.pollFirst();
            if (log.size() >= MAX_SENDS) {
                throw new TooManyRequestsException("인증 메일을 너무 자주 요청했습니다. 10분 뒤에 다시 시도해 주세요.");
            }
            log.addLast(now);
        }
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}