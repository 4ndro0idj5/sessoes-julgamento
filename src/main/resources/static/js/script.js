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

  if (documentos.pautaOrdinaria) {
    links.push(`<a class="download" href="${documentos.pautaOrdinaria}" target="_blank">Pauta ordin&aacute;ria</a>`);
  }

  if (documentos.aditamentos) {
    links.push(`<a class="download" href="${documentos.aditamentos}" target="_blank">Aditamentos</a>`);
  }

  if (documentos.pautaMesa) {
    links.push(`<a class="download" href="${documentos.pautaMesa}" target="_blank">Pauta de mesa</a>`);
  }

  if (documentos.preferencias) {
    links.push(`<a class="download" href="${documentos.preferencias}" target="_blank">Prefer&ecirc;ncias</a>`);
  }

  if (links.length === 0) {
    return `<p class="sem-documentos">Nenhum documento dispon&iacute;vel.</p>`;
  }

  return links.join("");
}

async function carregarSessoes() {
  const container = document.getElementById("sessoesContainer");
  const status = document.getElementById("status");
  const busca = document.getElementById("searchInput").value;

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

  try {
    const response = await fetch(`${API_URL}?${params.toString()}`);

    if (!response.ok) {
      throw new Error("Falha ao carregar sess\u00f5es.");
    }

    const sessoes = await response.json();
    status.textContent = "";

    if (sessoes.length === 0) {
      status.textContent = "Nenhuma sess\u00e3o encontrada.";
      return;
    }

    sessoes.forEach(sessao => {
      const card = document.createElement("article");
      card.className = "card";

      card.innerHTML = `
        <div class="card-header">
          <h2>${sessao.turma}</h2>
          <span class="badge">${formatarData(sessao.data)} &agrave;s ${formatarHorario(sessao.horario)}</span>
        </div>

        <div class="info">
          <p><strong>Procurador:</strong> ${sessao.procurador}</p>
          <p><strong>Local:</strong> ${sessao.sala}</p>
          <p><strong>Data:</strong> ${formatarData(sessao.data)}</p>
          <p><strong>Hor&aacute;rio:</strong> ${formatarHorario(sessao.horario)}</p>
        </div>

        ${sessao.status === "CANCELADA" ? `
          <p class="sem-documentos"><strong>Sess&atilde;o cancelada:</strong> ${sessao.motivoCancelamento}</p>
        ` : ""}

        <div class="documentos">
          ${gerarLinksDocumentos(sessao.documentos)}
        </div>
      `;

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
  carregarSessoes();
}

function formatarData(data) {
  return new Date(data + "T00:00:00").toLocaleDateString("pt-BR");
}

function formatarHorario(horario) {
  return horario ? horario.slice(0, 5) : "";
}

carregarSessoes();



