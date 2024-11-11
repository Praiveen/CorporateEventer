package com.example.CorporateEventer.entities;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import jakarta.persistence.*;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;


@NoArgsConstructor
@Data
@Accessors(chain = true)
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String fullName;
    private String name;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String password;
    @Transient
    private String passwordConfirm;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "subdepartment_id", nullable = true)
    private SubDepartment subDepartment;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;

    @ManyToMany
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles; 

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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

    @Override
    public String toString() {
        return "User{";
    }

    @Override
    public String getUsername() {
        return email;
    }

}

// @NoArgsConstructor
// @Data
// @Accessors(chain = true)
// @Table(name = "users")
// @Entity
// public class User implements UserDetails {
//     @Id
//     @GeneratedValue(strategy = GenerationType.AUTO)
//     private Integer id;

//     private String fullName;
//     private String email;
//     private String test;

//     private String password;

//     @CreationTimestamp
//     @Column(updatable = false, name = "created_at")
//     private Date createdAt;

//     @UpdateTimestamp
//     @Column(name = "updated_at")
//     private Date updatedAt;

//     @Override
//     public Collection<? extends GrantedAuthority> getAuthorities() {
//         return List.of();
//     }

//     // public String getPassword() {
//     //     return password;
//     // }

//     @Override
//     public String getUsername() {
//         return email;
//     }

//     // @Override
//     // public boolean isAccountNonExpired() {
//     //     return true;
//     // }

//     // @Override
//     // public boolean isAccountNonLocked() {
//     //     return true;
//     // }

//     // @Override
//     // public boolean isCredentialsNonExpired() {
//     //     return true;
//     // }

//     // @Override
//     // public boolean isEnabled() {
//     //     return true;
//     // }

//     // public Integer getId() {
//     //     return id;
//     // }

//     // public User setId(Integer id) {
//     //     this.id = id;
//     //     return this;
//     // }

//     // public String getFullName() {
//     //     return fullName;
//     // }

//     // public User setFullName(String fullName) {
//     //     this.fullName = fullName;
//     //     return this;
//     // }

//     // public String getEmail() {
//     //     return email;
//     // }

//     // public User setEmail(String email) {
//     //     this.email = email;
//     //     return this;
//     // }

//     // public User setPassword(String password) {
//     //     this.password = password;
//     //     return this;
//     // }

//     // public Date getCreatedAt() {
//     //     return createdAt;
//     // }

//     // public User setCreatedAt(Date createdAt) {
//     //     this.createdAt = createdAt;
//     //     return this;
//     // }

//     // public Date getUpdatedAt() {
//     //     return updatedAt;
//     // }

//     // public User setUpdatedAt(Date updatedAt) {
//     //     this.updatedAt = updatedAt;
//     //     return this;
//     // }

//     // @Override
//     // public String toString() {
//     //     return "User{" +
//     //             "id=" + id +
//     //             ", fullName='" + fullName + '\'' +
//     //             ", email='" + email + '\'' +
//     //             ", password='" + password + '\'' +
//     //             ", createdAt=" + createdAt +
//     //             ", updatedAt=" + updatedAt +
//     //             '}';
//     // }
// }

