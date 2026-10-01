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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

	@Test
	@DisplayName("executa onboarding em 2 etapas com campos e aliases do frontend (/cadastro -> /estabelecimento)")
	void deveExecutarOnboardingEmDuasEtapasComCamposDoFrontend() throws Exception {
		String email = uniqueEmail();
		String cnpj = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 14);

		String etapa1Payload = """
				{
				  "email": "%s",
				  "senha": "SenhaForte2024!",
				  "estabelecimento": {
				    "name": "Mercearia do Ver-o-Peso",
				    "cnpj": "%s",
				    "neighborhood": "Campina"
				  }
				}
				""".formatted(email, cnpj);

		MvcResult etapa1Result = mockMvc.perform(post("/auth/registro")
				.contentType(MediaType.APPLICATION_JSON)
				.content(etapa1Payload))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.estabelecimentoId").isNumber())
				.andReturn();

		String token = com.jayway.jsonpath.JsonPath.read(etapa1Result.getResponse().getContentAsString(), "$.token");
		Number estabIdNum = com.jayway.jsonpath.JsonPath.read(etapa1Result.getResponse().getContentAsString(), "$.estabelecimentoId");
		Long estabId = estabIdNum.longValue();

		String etapa2Payload = """
				{
				  "owner": "Maria",
				  "street": "Travessa Visconde de Souza Franco (Doca)",
				  "number": "742",
				  "type": "Mercado",
				  "days": ["Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"],
				  "opening": "07:30",
				  "closing": "19:00",
				  "criticalItems": ["Freezer no chão", "Estoque de secos"]
				}
				""";

		mockMvc.perform(put("/estabelecimento/" + estabId)
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(etapa2Payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Mercearia do Ver-o-Peso"))
				.andExpect(jsonPath("$.responsavel").value("Maria"))
				.andExpect(jsonPath("$.owner").value("Maria"))
				.andExpect(jsonPath("$.logradouro").value("Travessa Visconde de Souza Franco (Doca)"))
				.andExpect(jsonPath("$.street").value("Travessa Visconde de Souza Franco (Doca)"))
				.andExpect(jsonPath("$.numero").value("742"))
				.andExpect(jsonPath("$.number").value("742"))
				.andExpect(jsonPath("$.tipoNegocio").value("MERCADO"))
				.andExpect(jsonPath("$.type").value("MERCADO"))
				.andExpect(jsonPath("$.horarioAbertura").value("07:30"))
				.andExpect(jsonPath("$.opening").value("07:30"))
				.andExpect(jsonPath("$.horarioFechamento").value("19:00"))
				.andExpect(jsonPath("$.closing").value("19:00"))
				.andExpect(jsonPath("$.dias").isArray())
				.andExpect(jsonPath("$.days").isArray())
				.andExpect(jsonPath("$.itensCriticos[0]").value("Freezer no chão"))
				.andExpect(jsonPath("$.criticalItems[1]").value("Estoque de secos"));

		mockMvc.perform(get("/estabelecimento/me")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(estabId))
				.andExpect(jsonPath("$.owner").value("Maria"))
				.andExpect(jsonPath("$.dias").isArray())
				.andExpect(jsonPath("$.itensCriticos").isArray());
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
