package br.ufrn.imd.loja.console;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Cliente HTTP da API REST da loja.
 */
public class LojaCliente
{

	private final HttpClient http = HttpClient.newHttpClient();
	private final JsonMapper json = JsonMapper.builder()
		.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
		.build();
	private final String urlBase;

	public LojaCliente(String urlBase)
	{
		this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
	}

	public Resposta cadastrarProduto(int codigo, BigDecimal preco) throws IOException, InterruptedException
	{
		ObjectNode corpo = json.createObjectNode().put("codigo", codigo).put("preco", preco);
		return enviar("POST", "/loja/produto", corpo);
	}

	public Resposta consultarEstoque(int codigo) throws IOException, InterruptedException
	{
		return enviar("GET", "/loja/produto/" + codigo + "/estoque", null);
	}

	public Resposta entradaEstoque(int codigo, int quantidade) throws IOException, InterruptedException
	{
		return enviar("PUT", "/loja/produto/" + codigo + "/entrada", json.createObjectNode().put("quantidade", quantidade));
	}

	public Resposta saidaEstoque(int codigo, int quantidade) throws IOException, InterruptedException
	{
		return enviar("PUT", "/loja/produto/" + codigo + "/saida", json.createObjectNode().put("quantidade", quantidade));
	}

	/**
	 * @param itens quantidade vendida por código de produto
	 */
	public Resposta realizarVenda(Map<Integer, Integer> itens) throws IOException, InterruptedException
	{
		ObjectNode corpo = json.createObjectNode();
		ArrayNode lista = corpo.putArray("itens");
		itens.forEach((codigo, quantidade) -> lista.addObject().put("codigo", codigo).put("quantidade", quantidade));
		return enviar("POST", "/loja/venda", corpo);
	}

	private Resposta enviar(String metodo, String caminho, JsonNode corpo) throws IOException, InterruptedException
	{
		HttpRequest.BodyPublisher publicador = corpo == null
			? HttpRequest.BodyPublishers.noBody()
			: HttpRequest.BodyPublishers.ofString(json.writeValueAsString(corpo));
		HttpRequest requisicao = HttpRequest.newBuilder(URI.create(urlBase + caminho))
			.header("Content-Type", "application/json")
			.method(metodo, publicador)
			.build();

		HttpResponse<String> resposta = http.send(requisicao, HttpResponse.BodyHandlers.ofString());
		JsonNode corpoResposta = resposta.body().isBlank()
			? json.createObjectNode()
			: json.readTree(resposta.body());
		return new Resposta(resposta.statusCode(), corpoResposta);
	}

}
