package idusw.sbb.triplinker.domain.chat.repository;

import idusw.sbb.triplinker.domain.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    // 여행 하나의 대화방들(오래된 순). 플래너를 다시 열 때 대화를 되살리는 데 쓴다
    java.util.List<ChatSession> findByPlanIdOrderByIdAsc(Long planId);
}