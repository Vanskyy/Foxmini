// 系统用户：支持多角色（STUDENT / TEACHER / ADMIN）。扩展信息放在 StudentProfile / TeacherProfile。
// 密码应存加密后的摘要；avatar 可为相对路径或完整URL。
package com.foxfox.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id; // 主键

    @Column(nullable = false, length = 50)
    private String username; // 登录用户名

    @Column(nullable = false, length = 255)
    private String password; // 加密密码

    @Column(nullable = false, length = 100)
    private String email; // 邮箱

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName; // 真实姓名

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role; // 角色

    @Column(length = 255)
    private String avatar; // 头像地址

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 注册时间

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 最近更新时间

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private StudentProfile studentProfile; // 学生档案

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private TeacherProfile teacherProfile; // 教师档案

    // getter & setter
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public StudentProfile getStudentProfile() { return studentProfile; }
    public void setStudentProfile(StudentProfile studentProfile) { this.studentProfile = studentProfile; }
    public TeacherProfile getTeacherProfile() { return teacherProfile; }
    public void setTeacherProfile(TeacherProfile teacherProfile) { this.teacherProfile = teacherProfile; }
}