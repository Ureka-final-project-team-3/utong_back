package com.ureka.team3.utong_backend.auth.entity;


import com.ureka.team3.utong_backend.common.exception.business.InsufficientPointException;
import com.ureka.team3.utong_backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements UserDetails {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "mileage")
    private Long mileage;

    @Column(name = "default_line")
    private String defaultLine;
    @Column(name = "is_mail", nullable = false)
    @Builder.Default
    private Boolean isMail = true;
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    public void updateMailSetting(Boolean isMail) {
        this.isMail = isMail;
    }
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // 포인트 충전 후 account 의 마일리지 컬럼 업데이트 메서드
    public void addMileage(Long amount) {
        if (this.mileage == null) this.mileage = 0L;
        this.mileage += amount;
    }

    public void increasePoint(Long salePrice) {
        this.mileage += salePrice;
    }

    public void decreasePoint(Long purchasePrice) {
        if (mileage < purchasePrice)
            throw new InsufficientPointException();
        this.mileage -= purchasePrice;
    }

    public boolean isMyId(String id){
        return this.id.equals(id);
    }

    public boolean isPayAble(Long amount){
        return this.mileage >= amount;
    }
}
