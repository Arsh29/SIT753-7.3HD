package com.sit753.assignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sit753.assignment.model.Student;
import com.sit753.assignment.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
    }

    @Test
    void testCreateStudent() throws Exception {
        Student student = new Student(null, "John", "Doe", "john.doe@example.com", "Computer Science");

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(student)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.course", is("Computer Science")));
    }

    @Test
    void testGetAllStudents() throws Exception {
        Student s1 = new Student(null, "Alice", "Smith", "alice@example.com", "Software Engineering");
        Student s2 = new Student(null, "Bob", "Brown", "bob@example.com", "Cyber Security");
        studentRepository.save(s1);
        studentRepository.save(s2);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("Alice")))
                .andExpect(jsonPath("$[1].firstName", is("Bob")));
    }

    @Test
    void testGetStudentById_Found() throws Exception {
        Student student = studentRepository.save(
                new Student(null, "Charlie", "Green", "charlie@example.com", "Data Science")
        );

        mockMvc.perform(get("/api/students/{id}", student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(student.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("Charlie")))
                .andExpect(jsonPath("$.lastName", is("Green")))
                .andExpect(jsonPath("$.email", is("charlie@example.com")))
                .andExpect(jsonPath("$.course", is("Data Science")));
    }

    @Test
    void testGetStudentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/students/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateStudent_Success() throws Exception {
        Student existing = studentRepository.save(
                new Student(null, "David", "Miller", "david@example.com", "Information Technology")
        );

        Student updatedDetails = new Student(null, "David", "Miller", "david.new@example.com", "Cloud Computing");

        mockMvc.perform(put("/api/students/{id}", existing.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existing.getId().intValue())))
                .andExpect(jsonPath("$.email", is("david.new@example.com")))
                .andExpect(jsonPath("$.course", is("Cloud Computing")));
    }

    @Test
    void testUpdateStudent_NotFound() throws Exception {
        Student updatedDetails = new Student(null, "Non", "Existent", "non@example.com", "Math");

        mockMvc.perform(put("/api/students/{id}", 9999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteStudent_Success() throws Exception {
        Student student = studentRepository.save(
                new Student(null, "Emma", "Watson", "emma@example.com", "Artificial Intelligence")
        );

        mockMvc.perform(delete("/api/students/{id}", student.getId()))
                .andExpect(status().isNoContent());

        assertFalse(studentRepository.existsById(student.getId()));
    }

    @Test
    void testDeleteStudent_NotFound() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", 9999L))
                .andExpect(status().isNotFound());
    }
}
