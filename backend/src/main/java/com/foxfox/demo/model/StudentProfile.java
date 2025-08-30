// 学生扩展档案：与 User 1:1，存放学号、班级、专业、年级等信息。
package com.foxfox.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_profiles_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_student_profiles_student_id", columnNames = "student_id")
        })
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Integer id; // 主键

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "student_profiles_ibfk_1"))
    private User user; // 关联用户

    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId; // 学号

    @Column(name = "class", length = 50)
    private String className; // 班级

    @Column(length = 50)
    private String major; // 专业

    @Column(length = 20)
    private String grade; // 年级

    // getter & setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}