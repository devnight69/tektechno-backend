package com.tektechno.payout.repository;

import com.tektechno.payout.model.Beneficiary;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

  Optional<Beneficiary> findByBeneficiaryId(String beneficiaryId);

  boolean existsByBeneficiaryBankAccountNumber(String beneficiaryBankAccountNumber);

}
