package cloud.techotakus.invoice.common.domain.repository;

import cloud.techotakus.invoice.common.domain.entity.CommonBffSessionEntity;

public interface CommonBffSessionRepository {

    CommonBffSessionEntity find(String sessionId);
}
