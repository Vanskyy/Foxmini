//教师的详细信息
package com.foxfox.demo.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_teacher_profiles_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_teacher_profiles_teacher_id", columnNames = "teacher_id")
        })
public class TeacherProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Integer id; // 主键

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "teacher_profiles_ibfk_1"))
    private User user; // 关联用户

    @Column(name = "teacher_id", nullable = false, length = 20)
    private String teacherId; // 教师工号

    @Column(length = 50)
    private String department; // 院系

    @Column(length = 50)
    private String title; // 职称/头衔

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 档案创建时间

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 档案更新时间

    // getter & setter

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}