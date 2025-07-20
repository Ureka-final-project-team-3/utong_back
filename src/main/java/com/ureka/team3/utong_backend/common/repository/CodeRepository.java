package com.ureka.team3.utong_backend.common.repository;

import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.common.entity.CodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeRepository extends JpaRepository<Code, CodeId> {
    List<Code> findByGroupCode(String groupCode);
}
