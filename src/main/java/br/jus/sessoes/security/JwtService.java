package br.jus.sessoes.security;

import br.jus.sessoes.domain.Usuario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtService {

    private final byte[] secret;
    private final long expirationMs;
    private final ObjectMapper objectMapper;

    public JwtService(
            @Value("${sessoes.jwt.secret}") String secret,
            @Value("${sessoes.jwt.expiration-ms}") long expirationMs,
            ObjectMapper objectMapper
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
        this.objectMapper = objectMapper;
    }

    public String gerarToken(Usuario usuario) {
        if (usuario.getId() == null || usuario.getLogin() == null || usuario.getPerfil() == null) {
            throw new IllegalArgumentException("Usuario sem identidade completa para emissao do JWT.");
        }

        long agora = Instant.now().getEpochSecond();
        long expiracao = agora + (expirationMs / 1000);

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", usuario.getId().toString());
        payload.put("login", usuario.getLogin());
        payload.put("perfil", usuario.getPerfil().name());
        payload.put("iat", agora);
        payload.put("exp", expiracao);

        String conteudo = codificarJson(header) + "." + codificarJson(payload);
        return conteudo + "." + assinar(conteudo);
    }

    public Optional<UsuarioAutenticado> validarToken(String token) {
        try {
            String[] partes = token == null ? new String[0] : token.split("\\.");

            if (partes.length != 3) {
                return Optional.empty();
            }

            String conteudo = partes[0] + "." + partes[1];
            byte[] assinaturaRecebida = Base64.getUrlDecoder().decode(partes[2]);
            byte[] assinaturaEsperada = assinarBytes(conteudo);

            if (!MessageDigest.isEqual(assinaturaEsperada, assinaturaRecebida)) {
                return Optional.empty();
            }

            JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(partes[1]));
            long expiracao = payload.path("exp").asLong(0);
            String subject = payload.path("sub").asText(null);
            String login = payload.path("login").asText(null);
            String perfil = payload.path("perfil").asText(null);

            if (expiracao < Instant.now().getEpochSecond()
                    || subject == null
                    || login == null || login.isBlank()
                    || perfil == null || perfil.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new UsuarioAutenticado(Long.valueOf(subject), login, br.jus.sessoes.domain.PerfilUsuario.valueOf(perfil)));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private String assinar(String conteudo) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(assinarBytes(conteudo));
    }

    private byte[] assinarBytes(String conteudo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(conteudo.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel assinar o token JWT.", exception);
        }
    }

    private String codificarJson(Object valor) {
        try {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(valor));
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel criar o token JWT.", exception);
        }
    }
}
