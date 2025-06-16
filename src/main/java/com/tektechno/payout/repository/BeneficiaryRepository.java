package com.tektechno.payout.repository;

import com.tektechno.payout.model.Beneficiary;
import com.tektechno.payout.projection.BeneficiaryIdNameProjection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

  Optional<Beneficiary> findByBeneficiaryId(String beneficiaryId);

  boolean existsByBeneficiaryBankAccountNumber(String beneficiaryBankAccountNumber);

  @Query("SELECT b.beneficiaryName FROM Beneficiary b WHERE b.beneficiaryId = :beneficiaryId")
  String fetchBeneficiaryNameByBeneficiaryId(String beneficiaryId);

  @Query("SELECT b.beneficiaryId AS id, b.beneficiaryName AS name FROM Beneficiary "
      + "b WHERE b.beneficiaryId IN :beneficiaryIds")
  List<BeneficiaryIdNameProjection> findAllByBeneficiaryIdIn(@Param("beneficiaryIds") Set<String> beneficiaryIds);


}
