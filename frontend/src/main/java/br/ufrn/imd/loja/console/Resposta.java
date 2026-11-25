package br.ufrn.imd.loja.console;

import tools.jackson.databind.JsonNode;

public record Resposta(int status, JsonNode corpo)
{

	public boolean sucesso()
	{
		return status >= 200 && status < 300;
	}

	public String mensagemDeErro()
	{
		JsonNode erro = corpo.get("erro");
		return erro != null ? erro.asString() : "Erro HTTP " + status;
	}

}
