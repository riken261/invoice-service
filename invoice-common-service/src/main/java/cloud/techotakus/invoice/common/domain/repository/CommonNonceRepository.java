package cloud.techotakus.invoice.common.domain.repository;

import cloud.techotakus.invoice.common.domain.entity.CommonNonceEntity;

public interface CommonNonceRepository {

    void save(CommonNonceEntity nonce);

    CommonNonceEntity consume(String nonce);
}
