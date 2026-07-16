const USUARIOS_API = "/api/usuarios";
let usuarios = [];

if (!localStorage.getItem("authToken")) {
  window.location.href = "login.html";
} else if (localStorage.getItem("perfilUsuario") !== "ADMIN") {
  window.location.href = "admin.html";
}

function authHeaders(headers = {}) {
  return {
    ...headers,
    Authorization: `Bearer ${localStorage.getItem("authToken")}`
  };
}

async function fetchAdmin(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: authHeaders(options.headers || {})
  });

  if (response.status === 401) {
    logout();
    throw new Error("Sessão expirada.");
  }

  if (response.status === 403) {
    throw new Error("Acesso permitido somente para administradores.");
  }

  return response;
}

async function carregarUsuarios() {
  const status = document.getElementById("statusUsuarios");
  status.textContent = "Carregando usuários...";

  try {
    const response = await fetchAdmin(USUARIOS_API);
    if (!response.ok) throw new Error(await mensagemErro(response));

    usuarios = await response.json();
    renderizarUsuarios();
    status.textContent = "";
  } catch (error) {
    status.textContent = error.message;
  }
}

function renderizarUsuarios() {
  const corpo = document.getElementById("listaUsuarios");
  document.getElementById("usersCount").textContent = `(${usuarios.length})`;

  if (usuarios.length === 0) {
    corpo.innerHTML = '<tr><td colspan="5" class="empty-row">Nenhum usuário cadastrado.</td></tr>';
    return;
  }

  corpo.innerHTML = usuarios.map(usuario => `
    <tr>
      <td><strong>${escapar(usuario.nome)}</strong></td>
      <td>${escapar(usuario.login)}</td>
      <td><span class="role-badge ${usuario.perfil.toLowerCase()}">${rotuloPerfil(usuario.perfil)}</span></td>
      <td><span class="status-badge ${usuario.ativo ? "active" : "inactive"}">${usuario.ativo ? "Ativo" : "Inativo"}</span></td>
      <td class="table-actions">
        <button type="button" class="edit-button" onclick="abrirEdicaoUsuario(${usuario.id})">Editar</button>
      </td>
    </tr>
  `).join("");
}

function abrirNovoUsuario() {
  document.getElementById("modalUsuarioTitulo").textContent = "Novo usuário";
  document.getElementById("usuarioId").value = "";
  document.getElementById("usuarioNome").value = "";
  document.getElementById("usuarioLogin").value = "";
  document.getElementById("usuarioPerfil").value = "GESTOR";
  document.getElementById("usuarioAtivo").checked = true;
  document.getElementById("usuarioAtivo").disabled = true;
  document.getElementById("usuarioSenha").value = "";
  document.getElementById("usuarioSenha").required = true;
  document.getElementById("senhaAjuda").textContent = "Mínimo de 8 caracteres.";
  document.getElementById("modalUsuario").classList.remove("hidden");
}

function abrirEdicaoUsuario(id) {
  const usuario = usuarios.find(item => item.id === id);
  if (!usuario) return;

  document.getElementById("modalUsuarioTitulo").textContent = "Editar usuário";
  document.getElementById("usuarioId").value = usuario.id;
  document.getElementById("usuarioNome").value = usuario.nome;
  document.getElementById("usuarioLogin").value = usuario.login;
  document.getElementById("usuarioPerfil").value = usuario.perfil;
  document.getElementById("usuarioAtivo").checked = usuario.ativo;
  document.getElementById("usuarioAtivo").disabled = false;
  document.getElementById("usuarioSenha").value = "";
  document.getElementById("usuarioSenha").required = false;
  document.getElementById("senhaAjuda").textContent = "Deixe em branco para manter a senha atual.";
  document.getElementById("modalUsuario").classList.remove("hidden");
}

async function salvarUsuario(event) {
  event.preventDefault();

  const id = document.getElementById("usuarioId").value;
  const senha = document.getElementById("usuarioSenha").value;
  const dados = {
    nome: document.getElementById("usuarioNome").value.trim(),
    login: document.getElementById("usuarioLogin").value.trim(),
    perfil: document.getElementById("usuarioPerfil").value
  };

  if (id) {
    dados.ativo = document.getElementById("usuarioAtivo").checked;
    dados.senha = senha || null;
  } else {
    dados.senha = senha;
  }

  try {
    const response = await fetchAdmin(id ? `${USUARIOS_API}/${id}` : USUARIOS_API, {
      method: id ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dados)
    });

    if (!response.ok) throw new Error(await mensagemErro(response));

    fecharModalUsuario();
    await carregarUsuarios();
  } catch (error) {
    alert(error.message);
  }
}

async function mensagemErro(response) {
  try {
    const dados = await response.json();
    return dados.detail || dados.message || dados.error || "Não foi possível concluir a operação.";
  } catch {
    return "Não foi possível concluir a operação.";
  }
}

function fecharModalUsuario() {
  document.getElementById("modalUsuario").classList.add("hidden");
}

function rotuloPerfil(perfil) {
  return perfil === "ADMIN" ? "Administrador" : "Gestor";
}

function escapar(valor) {
  return String(valor)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function logout() {
  localStorage.removeItem("usuarioLogado");
  localStorage.removeItem("perfilUsuario");
  localStorage.removeItem("authToken");
  window.location.href = "login.html";
}

carregarUsuarios();
