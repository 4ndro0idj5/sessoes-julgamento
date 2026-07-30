const API_URL = "/api/sessoes";
let sessoes = [];

if (!localStorage.getItem("usuarioLogado") || !localStorage.getItem("authToken")) {
  window.location.href = "login.html";
}

if (localStorage.getItem("perfilUsuario") === "ADMIN") {
  document.getElementById("usuariosLink").classList.remove("hidden");
}

function getAuthHeaders(headers = {}) {
  return {
    ...headers,
    Authorization: `Bearer ${localStorage.getItem("authToken")}`
  };
}

async function fetchAutenticado(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: getAuthHeaders(options.headers || {})
  });

  if (response.status === 401) {
    localStorage.removeItem("usuarioLogado");
    localStorage.removeItem("authToken");
    window.location.href = "login.html";
    throw new Error("Sess\u00e3o expirada. Fa\u00e7a login novamente.");
  }

  if (response.status === 403) {
    throw new Error("Acesso nao autorizado para este perfil.");
  }

  return response;
}

async function carregarSessoes() {
  const lista = document.getElementById("listaSessoes");
  const status = document.getElementById("status");
  const busca = document.getElementById("busca").value.trim();
  const dataInicial = document.getElementById("dataInicial").value;
  const dataFinal = document.getElementById("dataFinal").value;
  const tipo = document.getElementById("tipoFiltro").value;

  lista.innerHTML = "";
  status.textContent = "Carregando sess\u00f5es...";

  const params = new URLSearchParams();
  if (busca) params.append("busca", busca);
  if (dataInicial) params.append("dataInicial", dataInicial);
  if (dataFinal) params.append("dataFinal", dataFinal);
  if (tipo !== "TODAS") params.append("tipo", tipo);

  try {
    const response = await fetchAutenticado(`${API_URL}?${params.toString()}`);

    if (!response.ok) {
      throw new Error("Falha ao carregar sess\u00f5es.");
    }

    sessoes = await response.json();
    status.textContent = "";

    if (sessoes.length === 0) {
      status.textContent = "Nenhuma sess\u00e3o encontrada.";
      return;
    }

    sessoes.forEach(sessao => {
      const card = document.createElement("article");
      card.className = `session-card ${sessao.tipo === "VIRTUAL" ? "virtual" : ""} ${sessao.status === "CANCELADA" ? "cancelled" : ""}`;

      card.innerHTML = gerarConteudoCard(sessao);

      lista.appendChild(card);
    });
  } catch (error) {
    status.textContent = error.message;
  }
}

function buscarSessoes(event) {
  event.preventDefault();

  const dataInicial = document.getElementById("dataInicial").value;
  const dataFinal = document.getElementById("dataFinal").value;

  if (dataInicial && dataFinal && dataInicial > dataFinal) {
    alert("A data inicial n\u00e3o pode ser posterior \u00e0 data final.");
    return;
  }

  carregarSessoes();
}

function limparFiltros() {
  document.getElementById("busca").value = "";
  document.getElementById("dataInicial").value = "";
  document.getElementById("dataFinal").value = "";
  document.getElementById("tipoFiltro").value = "TODAS";
  carregarSessoes();
}

function gerarChipDocumento(nome, valor) {
  if (valor) {
    return `<span class="doc-chip ok">&check; ${nome}</span>`;
  }

  return `<span class="doc-chip">- ${nome}</span>`;
}

function abrirNovaSessao() {
  document.getElementById("modalTitulo").textContent = "Nova sess\u00e3o";
  document.getElementById("sessaoId").value = "";
  document.getElementById("tipo").value = "PRESENCIAL";
  document.getElementById("turma").value = "";
  document.getElementById("data").value = "";
  document.getElementById("horario").value = "";
  document.getElementById("sala").value = "";
  document.getElementById("procurador").value = "";
  document.getElementById("dataInicialVirtual").value = "";
  document.getElementById("dataFinalVirtual").value = "";
  document.getElementById("tipo").disabled = false;
  atualizarCamposModalidade();
  document.getElementById("modalSessao").classList.remove("hidden");
}

function normalizarSala(sala) {
  const valor = (sala || "").trim();
  const texto = valor
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[ª°]/g, "º")
    .replace(/\s+/g, " ")
    .toLowerCase();

  const equivalencias = new Map([
    ["3º andar", "3º andar"],
    ["3º andar, sala 1", "3º andar"],
    ["5º andar, sala 1", "5º andar, sala 1"],
    ["5º andar, sala 2", "5º andar, sala 2"],
    ["7º andar, sala 1", "7º andar, sala 1"],
    ["7º andar, sala 2", "7º andar, sala 2"],
    ["9º andar, sala 1", "9º andar, sala 1"],
    ["9º andar, sala 2", "9º andar, sala 2"],
  ]);

  return equivalencias.get(texto) || valor;
}

function selecionarValorOuAdicionarOpcao(select, valor) {
  select.value = valor;

  if (!valor || select.value === valor) {
    return;
  }

  const option = document.createElement("option");
  option.value = valor;
  option.textContent = valor;
  option.dataset.valorLegado = "true";
  select.appendChild(option);
  select.value = valor;
}

function editarSessao(id) {
  const sessao = sessoes.find(item => item.id === id);
  if (!sessao) return;

  document.getElementById("modalTitulo").textContent = "Editar sess\u00e3o";
  document.getElementById("sessaoId").value = sessao.id;
  document.getElementById("tipo").value = sessao.tipo;
  document.getElementById("tipo").disabled = true;
  document.getElementById("turma").value = sessao.turma;
  if (sessao.tipo === "VIRTUAL") {
    document.getElementById("dataInicialVirtual").value = sessao.dataInicial;
    document.getElementById("dataFinalVirtual").value = sessao.dataFinal;
  } else {
    document.getElementById("data").value = sessao.data;
    document.getElementById("horario").value = formatarHorario(sessao.horario);
    selecionarValorOuAdicionarOpcao(document.getElementById("sala"), normalizarSala(sessao.local));
    document.getElementById("procurador").value = sessao.procurador;
  }
  atualizarCamposModalidade();
  document.getElementById("modalSessao").classList.remove("hidden");
}

async function salvarSessao(event) {
  event.preventDefault();
  const id = document.getElementById("sessaoId").value;
  const tipo = document.getElementById("tipo").value;
  const dados = { tipo, turma: document.getElementById("turma").value };

  if (tipo === "VIRTUAL") {
    dados.dataInicial = document.getElementById("dataInicialVirtual").value;
    dados.dataFinal = document.getElementById("dataFinalVirtual").value;
    if (dados.dataFinal < dados.dataInicial) {
      alert("A data final n\u00e3o pode ser anterior \u00e0 data inicial.");
      return;
    }
  } else {
    dados.data = document.getElementById("data").value;
    dados.horario = document.getElementById("horario").value;
    dados.local = document.getElementById("sala").value;
    dados.procurador = document.getElementById("procurador").value;
  }

  try {
    const response = await fetchAutenticado(id ? `${API_URL}/${id}` : API_URL, {
      method: id ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dados)
    });
    if (!response.ok) throw new Error("N\u00e3o foi poss\u00edvel salvar a sess\u00e3o.");
    fecharModalSessao();
    carregarSessoes();
  } catch (error) {
    alert(error.message);
  }
}

function abrirCancelamento(id) {
  document.getElementById("cancelamentoSessaoId").value = id;
  document.getElementById("motivoCancelamento").value = "";
  document.getElementById("modalCancelamento").classList.remove("hidden");
  document.getElementById("motivoCancelamento").focus();
}

async function confirmarCancelamento() {
  const id = document.getElementById("cancelamentoSessaoId").value;
  const motivo = document.getElementById("motivoCancelamento").value.trim();

  try {
    const response = await fetchAutenticado(`${API_URL}/${id}/cancelar`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ motivo })
    });

    if (!response.ok) {
      throw new Error("N\u00e3o foi poss\u00edvel cancelar a sess\u00e3o.");
    }

    fecharModalCancelamento();
    carregarSessoes();
  } catch (error) {
    alert(error.message);
  }
}

async function reativarSessao(id) {
  try {
    const response = await fetchAutenticado(`${API_URL}/${id}/reativar`, { method: "PATCH" });

    if (!response.ok) {
      throw new Error("N\u00e3o foi poss\u00edvel reativar a sess\u00e3o.");
    }

    carregarSessoes();
  } catch (error) {
    alert(error.message);
  }
}

function abrirDocumentos(id) {
  const sessao = sessoes.find(item => item.id === id);

  if (!sessao) return;

  document.getElementById("documentoSessaoId").value = sessao.id;
  document.getElementById("docPautaOrdinaria").value = "";
  document.getElementById("docAditamentos").value = "";
  document.getElementById("docPautaMesa").value = "";
  document.getElementById("docPreferencias").value = "";
  document.getElementById("modalDocumentos").classList.remove("hidden");
}

async function salvarDocumentos() {
  const id = Number(document.getElementById("documentoSessaoId").value);
  const formData = new FormData();

  adicionarArquivo(formData, "pautaOrdinaria", "docPautaOrdinaria");
  adicionarArquivo(formData, "aditamentos", "docAditamentos");
  adicionarArquivo(formData, "pautaMesa", "docPautaMesa");
  adicionarArquivo(formData, "preferencias", "docPreferencias");

  try {
    const response = await fetchAutenticado(`${API_URL}/${id}/documentos`, {
      method: "POST",
      body: formData
    });

    if (!response.ok) {
      throw new Error("N\u00e3o foi poss\u00edvel salvar os documentos.");
    }

    fecharModalDocumentos();
    carregarSessoes();
  } catch (error) {
    alert(error.message);
  }
}

function adicionarArquivo(formData, campo, inputId) {
  const arquivo = document.getElementById(inputId).files[0];

  if (arquivo) {
    formData.append(campo, arquivo);
  }
}

function fecharModalSessao() {
  document.getElementById("modalSessao").classList.add("hidden");
}

function fecharModalDocumentos() {
  document.getElementById("modalDocumentos").classList.add("hidden");
}

function fecharModalCancelamento() {
  document.getElementById("modalCancelamento").classList.add("hidden");
}

function logout() {
  localStorage.removeItem("usuarioLogado");
  localStorage.removeItem("perfilUsuario");
  localStorage.removeItem("authToken");
  window.location.href = "login.html";
}


function atualizarCamposModalidade() {
  const virtual = document.getElementById("tipo").value === "VIRTUAL";
  document.getElementById("camposPresencial").classList.toggle("hidden", virtual);
  document.getElementById("camposVirtual").classList.toggle("hidden", !virtual);
  ["data", "horario", "sala", "procurador"].forEach(id => document.getElementById(id).required = !virtual);
  ["dataInicialVirtual", "dataFinalVirtual"].forEach(id => document.getElementById(id).required = virtual);
}

function gerarConteudoCard(sessao) {
  const virtual = sessao.tipo === "VIRTUAL";
  const badge = virtual
    ? formatarPeriodo(sessao.dataInicial, sessao.dataFinal)
    : `${formatarData(sessao.data)} &agrave;s ${formatarHorario(sessao.horario)}`;
  const info = virtual
    ? `<p><strong>Modalidade:</strong> Virtual</p><p><strong>Per&iacute;odo:</strong> ${formatarPeriodo(sessao.dataInicial, sessao.dataFinal)}</p>`
    : `<p><strong>Modalidade:</strong> Presencial</p><p><strong>Procurador:</strong> ${sessao.procurador}</p><p><strong>Local:</strong> ${sessao.local}</p><p><strong>Data:</strong> ${formatarData(sessao.data)}</p><p><strong>Hor&aacute;rio:</strong> ${formatarHorario(sessao.horario)}</p>`;

  return `<div class="session-card-header"><h3>${sessao.turma}</h3><span class="badge">${badge}</span></div>
    ${sessao.status === "CANCELADA" ? `<div class="cancelled-banner">Sess&atilde;o cancelada</div>` : ""}
    <div class="session-info">${info}</div>
    <div class="documents-status">
      ${gerarChipDocumento("Ordin&aacute;ria", sessao.documentos?.pautaOrdinaria)}
      ${gerarChipDocumento("Aditamentos", sessao.documentos?.aditamentos)}
      ${gerarChipDocumento("Mesa", sessao.documentos?.pautaMesa)}
      ${gerarChipDocumento("Prefer&ecirc;ncias", sessao.documentos?.preferencias)}
    </div>
    ${sessao.status === "CANCELADA" && sessao.motivoCancelamento ? `<div class="cancelled-reason"><strong>Motivo:</strong> ${sessao.motivoCancelamento}</div>` : ""}
    <div class="card-actions">
      <button class="document-button" onclick="abrirDocumentos(${sessao.id})">Documentos</button>
      <button class="edit-button" onclick="editarSessao(${sessao.id})">Editar</button>
      ${sessao.status === "CANCELADA" ? `<button class="reactivate-button" onclick="reativarSessao(${sessao.id})">Reativar sess&atilde;o</button>` : `<button class="cancel-button" onclick="abrirCancelamento(${sessao.id})">Cancelar sess&atilde;o</button>`}
    </div>`;
}

function formatarPeriodo(inicio, fim) {
  return inicio === fim ? formatarData(inicio) : `${formatarData(inicio)} a ${formatarData(fim)}`;
}
function formatarData(data) {
  return new Date(data + "T00:00:00").toLocaleDateString("pt-BR");
}

function formatarHorario(horario) {
  return horario ? horario.slice(0, 5) : "";
}

carregarSessoes();



