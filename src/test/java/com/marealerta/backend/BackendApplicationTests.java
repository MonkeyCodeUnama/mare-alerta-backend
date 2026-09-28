package com.marealerta.backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("carrega o contexto com migrations Flyway")
	void contextLoads() {
	}

	@Test
	void registraUsuarioERetornaToken() throws Exception {
		String email = uniqueEmail();

		mockMvc.perform(post("/auth/registro")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registrationPayload(email, "senha-segura")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void rejeitaEmailJaCadastrado() throws Exception {
		String email = uniqueEmail();
		String payload = registrationPayload(email, "senha-segura");

		mockMvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("e-mail já cadastrado"));
	}

	@Test
	void rejeitaCadastroComSenhaCurta() throws Exception {
		mockMvc.perform(post("/auth/registro")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registrationPayload(uniqueEmail(), "curta")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.path").value("/auth/registro"));
	}

	@Test
	void autenticaComCredenciaisValidas() throws Exception {
		String email = uniqueEmail();
		mockMvc.perform(post("/auth/registro")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registrationPayload(email, "senha-segura")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"senha\":\"senha-segura\"}".formatted(email)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void rejeitaCredenciaisInvalidas() throws Exception {
		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"inexistente@example.com\",\"senha\":\"senha-segura\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("credenciais inválidas"));
	}

	@Test
	void permiteRotasSemToken() throws Exception {
		mockMvc.perform(get("/alertas"))
				.andExpect(status().isOk());
	}

	@Test
	void aceitaTokenEmRotaProtegida() throws Exception {
		MvcResult registration = mockMvc.perform(post("/auth/registro")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registrationPayload(uniqueEmail(), "senha-segura")))
				.andExpect(status().isCreated())
				.andReturn();
		String token = com.jayway.jsonpath.JsonPath.read(registration.getResponse().getContentAsString(), "$.token");

		mockMvc.perform(get("/alertas").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	private String uniqueEmail() {
		return "user-" + UUID.randomUUID() + "@example.com";
	}

	private String registrationPayload(String email, String password) {
		return """
				{ "email": "%s", "senha": "%s", "estabelecimento": {
				  "nome": "Mercado Central", "documento": "%s", "tipoNegocio": "MERCADO",
				  "responsavel": "Maria Silva", "bairro": "MARCO",
				  "logradouro": "Rua das Flores", "numero": "10"
				} }
				""".formatted(email, password,
						UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 11));
	}

}
