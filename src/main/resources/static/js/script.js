const API_URL = "/api/sessoes";

function formatarDataIso(data) {
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, "0");
  const dia = String(data.getDate()).padStart(2, "0");
  return `${ano}-${mes}-${dia}`;
}

function obterInicioDoMesAtual() {
  const hoje = new Date();
  return formatarDataIso(new Date(hoje.getFullYear(), hoje.getMonth(), 1));
}

function obterFimDoMesAtual() {
  const hoje = new Date();
  return formatarDataIso(new Date(hoje.getFullYear(), hoje.getMonth() + 1, 0));
}

function gerarLinksDocumentos(documentos = {}) {
  documentos = documentos || {};
  const links = [];
  if (documentos.pautaOrdinaria) links.push(`<a class="download" href="${documentos.pautaOrdinaria}" target="_blank">Pauta ordin&aacute;ria</a>`);
  if (documentos.aditamentos) links.push(`<a class="download" href="${documentos.aditamentos}" target="_blank">Aditamentos</a>`);
  if (documentos.pautaMesa) links.push(`<a class="download" href="${documentos.pautaMesa}" target="_blank">Pauta de mesa</a>`);
  if (documentos.preferencias) links.push(`<a class="download" href="${documentos.preferencias}" target="_blank">Prefer&ecirc;ncias</a>`);
  return links.length ? links.join("") : `<p class="sem-documentos">Nenhum documento dispon&iacute;vel.</p>`;
}

function cabecalhoSessao(sessao) {
  const modalidade = sessao.tipo === "VIRTUAL" ? "Virtual" : "Presencial";
  return `<span class="badge">${modalidade}</span>`;
}

function informacoesSessao(sessao) {
  if (sessao.tipo === "VIRTUAL") {
    return `<p><strong>Per&iacute;odo:</strong> ${formatarPeriodo(sessao.dataInicial, sessao.dataFinal)}</p>`;
  }
  return `<p><strong>Procurador:</strong> ${sessao.procurador}</p>
          <p><strong>Local:</strong> ${sessao.local}</p>
          <p><strong>Data:</strong> ${formatarData(sessao.data)}</p>
          <p><strong>Hor&aacute;rio:</strong> ${formatarHorario(sessao.horario)}</p>`;
}

async function carregarSessoes() {
  const container = document.getElementById("sessoesContainer");
  const status = document.getElementById("status");
  const busca = document.getElementById("searchInput").value;
  const tipo = document.getElementById("tipoFiltro").value;
  let dataInicial = document.getElementById("dataInicial").value;
  let dataFinal = document.getElementById("dataFinal").value;

  if (!dataInicial && !dataFinal) {
    dataInicial = obterInicioDoMesAtual();
    dataFinal = obterFimDoMesAtual();
  }

  container.innerHTML = "";
  status.textContent = "Carregando sess\u00f5es...";
  const params = new URLSearchParams();
  if (busca) params.append("busca", busca);
  if (dataInicial) params.append("dataInicial", dataInicial);
  if (dataFinal) params.append("dataFinal", dataFinal);
  if (tipo !== "TODAS") params.append("tipo", tipo);

  try {
    const response = await fetch(`${API_URL}?${params.toString()}`);
    if (!response.ok) throw new Error("Falha ao carregar sess\u00f5es.");
    const sessoes = await response.json();
    status.textContent = "";
    if (!sessoes.length) {
      status.textContent = "Nenhuma sess\u00e3o encontrada.";
      return;
    }

    sessoes.forEach(sessao => {
      const card = document.createElement("article");
      card.className = `card ${sessao.tipo === "VIRTUAL" ? "virtual" : ""}`;
      card.innerHTML = `
        <div class="card-header"><h2>${sessao.turma}</h2>${cabecalhoSessao(sessao)}</div>
        <div class="info">${informacoesSessao(sessao)}</div>
        ${sessao.status === "CANCELADA" ? `<p class="sem-documentos"><strong>Sess&atilde;o cancelada:</strong> ${sessao.motivoCancelamento}</p>` : ""}
        <div class="documentos">${gerarLinksDocumentos(sessao.documentos)}</div>`;
      container.appendChild(card);
    });
  } catch (error) {
    status.textContent = error.message;
  }
}

function limparFiltros() {
  document.getElementById("searchInput").value = "";
  document.getElementById("dataInicial").value = "";
  document.getElementById("dataFinal").value = "";
  document.getElementById("tipoFiltro").value = "TODAS";
  carregarSessoes();
}

function formatarData(data) {
  return new Date(data + "T00:00:00").toLocaleDateString("pt-BR");
}

function formatarPeriodo(inicio, fim) {
  if (inicio === fim) return formatarData(inicio);
  return `${formatarData(inicio)} a ${formatarData(fim)}`;
}

function formatarHorario(horario) {
  return horario ? horario.slice(0, 5) : "";
}

carregarSessoes();