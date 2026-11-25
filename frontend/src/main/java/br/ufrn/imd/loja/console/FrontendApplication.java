package br.ufrn.imd.loja.console;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import tools.jackson.databind.JsonNode;

/**
 * Frontend de console da loja. A URL da API pode ser passada como argumento
 * (padrão: http://localhost:8080).
 */
public class FrontendApplication
{

	private final Scanner entrada = new Scanner(System.in);
	private final LojaCliente cliente;

	public FrontendApplication(LojaCliente cliente)
	{
		this.cliente = cliente;
	}

	public static void main(String[] args)
	{
		String urlBase = args.length > 0 ? args[0] : "http://localhost:8080";
		new FrontendApplication(new LojaCliente(urlBase)).executar();
	}

	public void executar()
	{
		while (true)
		{
			System.out.println();
			System.out.println("===== Loja Virtual =====");
			System.out.println("1 - Cadastrar produto");
			System.out.println("2 - Consultar estoque");
			System.out.println("3 - Entrada de estoque");
			System.out.println("4 - Saída de estoque");
			System.out.println("5 - Venda");
			System.out.println("0 - Sair");

			String opcao = lerLinha("Opção: ");
			if (opcao == null || opcao.equals("0"))
			{
				return;
			}

			try
			{
				switch (opcao)
				{
					case "1" -> cadastrarProduto();
					case "2" -> consultarEstoque();
					case "3" -> entradaEstoque();
					case "4" -> saidaEstoque();
					case "5" -> realizarVenda();
					default -> System.out.println("Opção inválida.");
				}
			}
			catch (IOException e)
			{
				System.out.println("Não foi possível conectar à API: " + e.getMessage());
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private void cadastrarProduto() throws IOException, InterruptedException
	{
		int codigo = lerInteiro("Código: ");
		BigDecimal preco = lerDecimal("Preço: ");

		Resposta resposta = cliente.cadastrarProduto(codigo, preco);
		if (resposta.sucesso())
		{
			JsonNode produto = resposta.corpo();
			System.out.println("Produto " + produto.get("codigo").asInt() + " cadastrado com preço "
				+ produto.get("preco").decimalValue() + " e estoque " + produto.get("estoque").asInt() + ".");
		}
		else
		{
			exibirErro(resposta);
		}
	}

	private void consultarEstoque() throws IOException, InterruptedException
	{
		exibirEstoque(cliente.consultarEstoque(lerInteiro("Código: ")));
	}

	private void entradaEstoque() throws IOException, InterruptedException
	{
		int codigo = lerInteiro("Código: ");
		exibirEstoque(cliente.entradaEstoque(codigo, lerInteiro("Quantidade: ")));
	}

	private void saidaEstoque() throws IOException, InterruptedException
	{
		int codigo = lerInteiro("Código: ");
		exibirEstoque(cliente.saidaEstoque(codigo, lerInteiro("Quantidade: ")));
	}

	private void realizarVenda() throws IOException, InterruptedException
	{
		Map<Integer, Integer> itens = new LinkedHashMap<>();
		System.out.println("Informe os itens da venda (código vazio para finalizar).");
		while (true)
		{
			String codigo = lerLinha("Código: ");
			if (codigo == null || codigo.isBlank())
			{
				break;
			}
			try
			{
				int quantidade = lerInteiro("Quantidade: ");
				itens.merge(Integer.parseInt(codigo.trim()), quantidade, Integer::sum);
			}
			catch (NumberFormatException e)
			{
				System.out.println("Código inválido: informe um número inteiro.");
			}
		}
		if (itens.isEmpty())
		{
			System.out.println("Venda cancelada: nenhum item informado.");
			return;
		}

		Resposta resposta = cliente.realizarVenda(itens);
		if (!resposta.sucesso())
		{
			exibirErro(resposta);
			return;
		}
		for (JsonNode item : resposta.corpo().get("itens"))
		{
			System.out.println("Produto " + item.get("codigo").asInt() + ": " + item.get("quantidade").asInt()
				+ " un. | subtotal " + item.get("subtotal").decimalValue()
				+ " | estoque restante " + item.get("estoque").asInt());
		}
		System.out.println("Total da venda: " + resposta.corpo().get("total").decimalValue());
	}

	private void exibirEstoque(Resposta resposta)
	{
		if (resposta.sucesso())
		{
			System.out.println("Produto " + resposta.corpo().get("codigo").asInt() + " - estoque: "
				+ resposta.corpo().get("estoque").asInt());
		}
		else
		{
			exibirErro(resposta);
		}
	}

	private void exibirErro(Resposta resposta)
	{
		System.out.println("Erro (" + resposta.status() + "): " + resposta.mensagemDeErro());
	}

	private String lerLinha(String rotulo)
	{
		System.out.print(rotulo);
		return entrada.hasNextLine() ? entrada.nextLine().trim() : null;
	}

	private int lerInteiro(String rotulo)
	{
		while (true)
		{
			String valor = lerLinha(rotulo);
			if (valor == null)
			{
				throw new IllegalStateException("Entrada encerrada");
			}
			try
			{
				return Integer.parseInt(valor);
			}
			catch (NumberFormatException e)
			{
				System.out.println("Valor inválido: informe um número inteiro.");
			}
		}
	}

	private BigDecimal lerDecimal(String rotulo)
	{
		while (true)
		{
			String valor = lerLinha(rotulo);
			if (valor == null)
			{
				throw new IllegalStateException("Entrada encerrada");
			}
			try
			{
				return new BigDecimal(valor.replace(',', '.'));
			}
			catch (NumberFormatException e)
			{
				System.out.println("Valor inválido: informe um número decimal (ex.: 59.90).");
			}
		}
	}

}
