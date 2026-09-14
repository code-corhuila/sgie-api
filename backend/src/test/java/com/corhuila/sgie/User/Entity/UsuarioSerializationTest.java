package com.corhuila.sgie.User.Entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void passwordNuncaSeSerializaEnJson() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("demo@mail.com");
        usuario.setPassword("$2a$12$hashSecreto");

        String json = objectMapper.writeValueAsString(usuario);

        assertThat(json).doesNotContain("hashSecreto");
        assertThat(json).doesNotContain("\"password\"");
        assertThat(json).contains("demo@mail.com");
    }

    @Test
    void personaNuncaExponeElUsuarioAsociado() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setEmail("demo@mail.com");
        usuario.setPassword("$2a$12$hashSecreto");

        Persona persona = new Persona();
        persona.setId(5L);
        persona.setNombres("Demo");
        persona.setUsuario(usuario);

        String json = objectMapper.writeValueAsString(persona);

        assertThat(json).doesNotContain("\"usuario\"");
        assertThat(json).doesNotContain("hashSecreto");
    }
}
