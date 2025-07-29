package com.ureka.team3.utong_backend.auth.service;

import com.ureka.team3.utong_backend.auth.entity.Account;

public interface AccountService {
    Account findById(String id);
}
