package idusw.sbb.triplinker.domain.chat.service;

public interface ChatbotService {
    Long createSession(Long userId, Long planId); // 세션 생성 (그 여행의 주인 · 편집자만)
    String processMessage(Long userId, Long sessionId, String message); // 메시지 전송 및 답변 반환
    void saveSystemMessage(Long userId, Long sessionId, String message);
    java.util.List<java.util.Map<String, Object>> listSessions(Long userId, Long planId); // 그 여행의 대화방 (주인 · 멤버만)
    java.util.Map<String, Object> getSession(Long userId, Long sessionId);              // 대화 내용 (USER · AI 만, 숨은 SYSTEM 은 뺌)
}
