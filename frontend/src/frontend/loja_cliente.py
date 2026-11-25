from __future__ import annotations

import json
import urllib.error
import urllib.request
from dataclasses import dataclass, field


@dataclass
class Resposta:
    """Resultado de uma requisição à API."""

    status: int
    corpo: dict = field(default_factory=dict)

    @property
    def sucesso(self) -> bool:
        return 200 <= self.status <= 299

    def mensagem_de_erro(self) -> str:
        return self.corpo.get("erro", f"Erro HTTP {self.status}")


class LojaCliente:
    """Envia e recebe JSON da API da loja."""

    def __init__(self, url_base: str):
        self.url_base = url_base

    def get(self, caminho: str) -> Resposta:
        return self._requisitar("GET", caminho)

    def post(self, caminho: str, corpo: dict) -> Resposta:
        return self._requisitar("POST", caminho, corpo)

    def put(self, caminho: str, corpo: dict) -> Resposta:
        return self._requisitar("PUT", caminho, corpo)

    def _requisitar(self, metodo: str, caminho: str, corpo: dict | None = None) -> Resposta:
        dados = None
        cabecalhos = {"Accept": "application/json"}
        if corpo is not None:
            dados = json.dumps(corpo).encode("utf-8")
            cabecalhos["Content-Type"] = "application/json"

        requisicao = urllib.request.Request(
            self.url_base + caminho, data=dados, headers=cabecalhos, method=metodo
        )
        try:
            with urllib.request.urlopen(requisicao) as resposta:
                return Resposta(resposta.status, _ler_json(resposta.read()))
        except urllib.error.HTTPError as erro:
            # Respostas 4xx/5xx chegam como exceção, mas ainda trazem status e corpo.
            return Resposta(erro.code, _ler_json(erro.read()))


def _ler_json(conteudo: bytes) -> dict:
    """Converte o corpo da resposta em dict; devolve {} se vier vazio ou não for um objeto JSON."""
    if not conteudo:
        return {}
    try:
        dados = json.loads(conteudo.decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError):
        return {}
    return dados if isinstance(dados, dict) else {}
