document
  .getElementById("loginForm")
  .addEventListener("submit", async function(e) {
    e.preventDefault();

    const usuario = document.getElementById("usuario").value;
    const senha = document.getElementById("senha").value;

    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ usuario, senha })
      });

      if (!response.ok) {
        throw new Error("Usuário ou senha inválidos.");
      }

      const dados = await response.json();
      localStorage.setItem("usuarioLogado", dados.usuario);
      localStorage.setItem("perfilUsuario", dados.perfil);
      localStorage.setItem("authToken", dados.token);
      window.location.href = "admin.html";
    } catch (error) {
      alert(error.message);
    }
  });
