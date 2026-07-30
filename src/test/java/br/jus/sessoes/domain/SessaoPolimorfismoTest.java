package br.jus.sessoes.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessaoPolimorfismoTest {

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void deveConverterJsonVirtualParaSubclasseCorreta() throws Exception {
        String json = """
                {"tipo":"VIRTUAL","turma":"5ª TESP","dataInicial":"2026-08-13","dataFinal":"2026-08-20"}
                """;

        Sessao sessao = mapper.readValue(json, Sessao.class);

        assertThat(sessao).isInstanceOf(SessaoVirtual.class);
        assertThat(sessao.getDataInicial()).hasToString("2026-08-13");
        assertThat(mapper.writeValueAsString(sessao)).contains("\"tipo\":\"VIRTUAL\"");
    }

    @Test
    void deveConverterJsonPresencialParaSubclasseCorreta() throws Exception {
        String json = """
                {"tipo":"PRESENCIAL","turma":"5ª TESP","data":"2026-08-13","horario":"14:00","local":"7º andar, sala 1","procurador":"PROCURADOR"}
                """;

        Sessao sessao = mapper.readValue(json, Sessao.class);

        assertThat(sessao).isInstanceOf(SessaoPresencial.class);
        assertThat(((SessaoPresencial) sessao).getLocal()).isEqualTo("7º andar, sala 1");
    }
}