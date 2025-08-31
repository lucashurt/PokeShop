package com.example.ecommercefull;

import com.example.ecommercefull.auth.models.Role;
import com.example.ecommercefull.auth.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ECommerceJsonTests {

    @Autowired
    private JacksonTester<User> json;

    @Test
    void userSerializationTest() throws IOException {
        Role role = new Role("CUSTOMER");
        User user = new User("lucas", "password", "lucas hurtado", role);
        assertThat(json.write(user)).isEqualTo("single.json");
    }

    @Test
    void userDeserializationTest() throws IOException {
        String expectedJson = """
                {
                  "username": "lucas",
                  "password": "password",
                  "fullName": "lucas hurtado",
                  "role": {
                    "name": "CUSTOMER"
                  }
                }
                """;
        assertThat(json.parse(expectedJson).getObject().getUsername()).isEqualTo("lucas");
        assertThat(json.parse(expectedJson).getObject().getPassword()).isEqualTo("password");
        assertThat(json.parse(expectedJson).getObject().getFullName()).isEqualTo("lucas hurtado");
        assertThat(json.parse(expectedJson).getObject().getRole().getName()).isEqualTo("CUSTOMER");
    }
}
