'use strict';
const state = { usuarios: [], itens: [], movimentos: [] };
const $ = (s) => document.querySelector(s);
const esc = (value) => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const texto = { LIVRO:'Livro didático', APOSTILA:'Apostila', EQUIPAMENTO:'Equipamento', EMPRESTIMO:'Empréstimo', DOACAO:'Doação', EMPRESTADO:'Emprestado', DEVOLVIDO:'Devolvido', ATRASADO:'Atrasado', DOADO:'Doado' };
function aviso(text, erro = false) { const el = $('#mensagem'); el.textContent = text; el.className = erro ? 'error' : ''; el.hidden = false; }
async function api(path, options = {}) {
  const response = await fetch('/api' + path, { ...options, headers: { 'Content-Type': 'application/json', ...options.headers } });
  if (!response.ok) { const body = await response.json().catch(() => ({})); throw new Error(body.erro || 'Não foi possível concluir a operação.'); }
  return response.status === 204 ? null : response.json();
}
function selecionar(select, options, placeholder) {
  const anterior = select.value;
  select.innerHTML = `<option value="">${esc(placeholder)}</option>` + options.map(o => `<option value="${o.id}">${esc(o.nome)}</option>`).join('');
  select.value = anterior;
}
function render() {
  const nomes = Object.fromEntries(state.usuarios.map(u => [u.id,u.nome]));
  const itens = Object.fromEntries(state.itens.map(i => [i.id,i.titulo]));
  document.querySelectorAll('select[name="usuarioId"]').forEach(s => selecionar(s, state.usuarios, 'Selecione um estudante'));
  selecionar($('#form-movimento select[name="itemId"]'), state.itens.filter(i => i.disponivel).map(i => ({id:i.id,nome:i.titulo})), 'Selecione um material');
  const filtro = new FormData($('#filtro'));
  const filtrados = state.itens.filter(i => (!filtro.get('tipo') || i.tipo === filtro.get('tipo')) && (!filtro.get('disciplina') || i.disciplina.toLocaleLowerCase().includes(filtro.get('disciplina').toLocaleLowerCase())) && (!filtro.get('disponivel') || String(i.disponivel) === filtro.get('disponivel')));
  $('#lista-itens').innerHTML = filtrados.map(i => `<article class="card"><span class="badge ${i.disponivel ? '' : 'unavailable'}">${esc(texto[i.tipo])} · ${i.disponivel ? 'Disponível' : 'Indisponível'}</span><h4>${esc(i.titulo)}</h4><p>Disciplina: ${esc(i.disciplina)}</p><p>Responsável: ${esc(nomes[i.usuarioId] || i.usuarioId)}</p><div class="actions"><button class="secondary" data-edit-item="${i.id}">Editar</button><button class="secondary" data-delete-item="${i.id}">Excluir</button></div></article>`).join('') || '<p class="empty">Nenhum material encontrado. Cadastre um material ou ajuste os filtros.</p>';
  $('#lista-usuarios').innerHTML = state.usuarios.map(u => `<article class="card"><span class="badge">Estudante #${u.id}</span><h4>${esc(u.nome)}</h4><p>${esc(u.email)}</p><p>${esc(u.curso)} · ${esc(u.telefone)}</p><div class="actions"><button class="secondary" data-edit-user="${u.id}">Editar</button><button class="secondary" data-delete-user="${u.id}">Excluir</button></div></article>`).join('') || '<p class="empty">Ainda não há estudantes cadastrados. Cadastre o primeiro para começar.</p>';
  $('#lista-movimentos').innerHTML = state.movimentos.map(m => `<article class="card"><span class="badge">${esc(texto[m.tipo])} · ${esc(texto[m.status])}</span><h4>${esc(itens[m.itemId] || 'Item #' + m.itemId)}</h4><p>Estudante: ${esc(nomes[m.usuarioId] || m.usuarioId)}</p><p>Solicitação: ${esc(m.dataSolicitacao)}${m.prazoDevolucao ? ' · Prazo: ' + esc(m.prazoDevolucao) : ''}</p><details><summary>Histórico</summary><ul class="history">${m.historico.map(h => `<li>${esc(texto[h.status])} — ${esc(h.registradoEm.replace('T',' ').slice(0,16))}</li>`).join('')}</ul></details>${m.tipo === 'EMPRESTIMO' && ['EMPRESTADO','ATRASADO'].includes(m.status) ? `<div class="actions">${m.status === 'EMPRESTADO' ? `<button class="secondary" data-status="ATRASADO" data-id="${m.id}">Marcar atraso</button>` : ''}<button data-status="DEVOLVIDO" data-id="${m.id}">Registrar devolução</button></div>` : ''}</article>`).join('') || '<p class="empty">Nenhuma movimentação registrada.</p>';
}
async function atualizar() { [state.usuarios,state.itens,state.movimentos] = await Promise.all([api('/usuarios'),api('/itens'),api('/movimentacoes')]); render(); }
function camposItem() { const form = $('#form-item'); form.querySelectorAll('[data-item]').forEach(el => { el.hidden = el.dataset.item !== form.elements.tipo.value; el.querySelector('input').required = !el.hidden; }); }
function preencher(form, dados) { Object.entries(dados).forEach(([key,value]) => { if (form.elements[key]) form.elements[key].value = value ?? ''; }); form.closest('details').open = true; form.scrollIntoView({behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth',block:'center'}); }
document.querySelectorAll('.tab').forEach(button => button.addEventListener('click', () => { $('#mensagem').hidden = true; document.querySelectorAll('.tab').forEach(b => { const active = b === button; b.classList.toggle('active',active); if (active) b.setAttribute('aria-current','page'); else b.removeAttribute('aria-current'); $('#' + b.dataset.tab).hidden = !active; }); }));
document.querySelector('.brand').addEventListener('click', () => document.querySelector('[data-tab="itens"]').click());
document.querySelectorAll('[data-open-editor]').forEach(link => link.addEventListener('click', () => { document.getElementById(link.dataset.openEditor).open = true; }));
$('#form-item').elements.tipo.addEventListener('change', camposItem);
$('#filtro').addEventListener('submit', e => { e.preventDefault(); render(); });
document.querySelectorAll('[data-reset]').forEach(button => button.addEventListener('click', () => { const form = $('#' + button.dataset.reset); form.reset(); form.elements.id.value = ''; camposItem(); }));
async function enviar(e, tipo) {
  e.preventDefault();
  const form = e.currentTarget, dados = Object.fromEntries(new FormData(form)), id = dados.id;
  delete dados.id;
  if (tipo === 'itens') { dados.usuarioId = Number(dados.usuarioId); dados.numeroPaginas = dados.tipo === 'APOSTILA' ? Number(dados.numeroPaginas) : null; for (const k of ['numeracao','edicao']) if (dados.tipo !== 'LIVRO') dados[k] = null; for (const k of ['tipoEquipamento','estadoConservacao']) if (dados.tipo !== 'EQUIPAMENTO') dados[k] = null; }
  if (tipo === 'movimentacoes') { dados.usuarioId = Number(dados.usuarioId); dados.itemId = Number(dados.itemId); }
  try { await api('/' + tipo + (id ? '/' + id : ''), {method:id ? 'PUT' : 'POST',body:JSON.stringify(dados)}); form.reset(); if (form.elements.id) form.elements.id.value = ''; camposItem(); await atualizar(); aviso('Operação realizada com sucesso.'); } catch (error) { aviso(error.message,true); }
}
$('#form-usuario').addEventListener('submit', e => enviar(e,'usuarios'));
$('#form-item').addEventListener('submit', e => enviar(e,'itens'));
$('#form-movimento').addEventListener('submit', e => enviar(e,'movimentacoes'));
document.addEventListener('click', async e => {
  const b = e.target.closest('button'); if (!b) return;
  try {
    if (b.dataset.editUser) preencher($('#form-usuario'), state.usuarios.find(u => u.id === Number(b.dataset.editUser)));
    if (b.dataset.editItem) { preencher($('#form-item'), state.itens.find(i => i.id === Number(b.dataset.editItem))); camposItem(); }
    if (b.dataset.deleteUser && confirm('Excluir este estudante?')) { await api('/usuarios/' + b.dataset.deleteUser,{method:'DELETE'}); await atualizar(); aviso('Estudante excluído.'); }
    if (b.dataset.deleteItem && confirm('Excluir este material?')) { await api('/itens/' + b.dataset.deleteItem,{method:'DELETE'}); await atualizar(); aviso('Material excluído.'); }
    if (b.dataset.status) { await api('/movimentacoes/' + b.dataset.id + '/status',{method:'PATCH',body:JSON.stringify({status:b.dataset.status})}); await atualizar(); aviso('Status atualizado.'); }
  } catch (error) { aviso(error.message,true); }
});
camposItem(); atualizar().catch(error => { document.querySelectorAll('.cards').forEach(lista => { lista.innerHTML = '<p class="empty">Não foi possível carregar os dados. Verifique a conexão e atualize a página.</p>'; }); aviso('Não foi possível carregar os dados. Verifique a aplicação e o banco. ' + error.message,true); });
