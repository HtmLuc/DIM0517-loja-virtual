package br.ufrn.imd.loja.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ufrn.imd.loja.dto.LojaDTO;
import br.ufrn.imd.loja.repository.ProdutoRepositoryImpl;
import br.ufrn.imd.loja.service.exception.ProdutoJaExisteException;
import br.ufrn.imd.loja.service.exception.ProdutoNaoEncontradoException;

class LojaServiceTest
{
	private LojaService servico;

	@BeforeEach
	void setUp()
	{
		this.servico = new LojaService(new ProdutoRepositoryImpl());
	}

	private void cadastrarComEstoque(final int codigo, final String preco, final int estoque)
	{
		this.servico.cadastrarProduto(new LojaDTO.CriarProdutoDTO(codigo, new BigDecimal(preco)));
		this.servico.adicionarAoEstoque(codigo, new LojaDTO.RetirarEstoqueDTO(estoque));
	}

	@Test
	void cadastrarProdutoCriaComCodigoPrecoEEstoqueZero()
	{
		final var produto = this.servico.cadastrarProduto(
			new LojaDTO.CriarProdutoDTO(123, new BigDecimal("59.90"))
		);

		assertEquals(123, produto.codigo());
		assertEquals(0, new BigDecimal("59.90").compareTo(produto.preco()));
		assertEquals(0, produto.estoque());
	}

	@Test
	void cadastrarProdutoRejeitaCodigoJaCadastrado()
	{
		final var dto = new LojaDTO.CriarProdutoDTO(123, new BigDecimal("59.90"));
		this.servico.cadastrarProduto(dto);

		assertThrows(ProdutoJaExisteException.class, () -> this.servico.cadastrarProduto(dto));
	}

	@Test
	void consultarEstoqueRetornaEstoqueCorreto()
	{
		cadastrarComEstoque(123, "59.90", 15);

		assertEquals(15, this.servico.consultarEstoque(123).estoque());
	}

	@Test
	void consultarEstoqueDeProdutoInexistenteLancaErro()
	{
		assertThrows(ProdutoNaoEncontradoException.class, () -> this.servico.consultarEstoque(999));
	}

	@Test
	void entradaDeEstoqueAcrescentaQuantidade()
	{
		cadastrarComEstoque(123, "59.90", 15);

		final var resposta = this.servico.adicionarAoEstoque(123, new LojaDTO.RetirarEstoqueDTO(10));

		assertEquals(25, resposta.estoque());
	}

	@Test
	void entradaDeEstoqueDeProdutoInexistenteLancaErro()
	{
		assertThrows(ProdutoNaoEncontradoException.class,
			() -> this.servico.adicionarAoEstoque(999, new LojaDTO.RetirarEstoqueDTO(10)));
	}

	@Test
	void saidaDeEstoqueSubtraiQuantidade()
	{
		cadastrarComEstoque(123, "59.90", 15);

		final var resposta = this.servico.retirarDoEstoque(123, new LojaDTO.RetirarEstoqueDTO(10));

		assertEquals(5, resposta.estoque());
	}

	@Test
	void saidaDeEstoqueDeProdutoInexistenteLancaErro()
	{
		assertThrows(ProdutoNaoEncontradoException.class,
			() -> this.servico.retirarDoEstoque(999, new LojaDTO.RetirarEstoqueDTO(10)));
	}

	@Test
	void saidaMaiorQueEstoqueDeixaEstoqueNegativo()
	{
		cadastrarComEstoque(123, "59.90", 15);

		final var resposta = this.servico.retirarDoEstoque(123, new LojaDTO.RetirarEstoqueDTO(20));

		assertEquals(-5, resposta.estoque());
	}

	@Test
	void vendaBaixaEstoqueDeTodosOsItensECalculaTotal()
	{
		cadastrarComEstoque(123, "59.90", 15);
		cadastrarComEstoque(456, "69.90", 8);

		final var resposta = this.servico.realizarVenda(new LojaDTO.VendaDTO(List.of(
			new LojaDTO.ItemVendaDTO(123, 2), new LojaDTO.ItemVendaDTO(456, 1)
		)));

		assertEquals(0, new BigDecimal("189.70").compareTo(resposta.total()));
		assertEquals(13, this.servico.consultarEstoque(123).estoque());
		assertEquals(7, this.servico.consultarEstoque(456).estoque());
	}

	@Test
	void vendaComProdutoInexistenteNaoAlteraEstoqueDeNenhumItem()
	{
		cadastrarComEstoque(123, "59.90", 15);
		cadastrarComEstoque(456, "69.90", 8);

		assertThrows(ProdutoNaoEncontradoException.class,
			() -> this.servico.realizarVenda(new LojaDTO.VendaDTO(List.of(
				new LojaDTO.ItemVendaDTO(123, 2), new LojaDTO.ItemVendaDTO(456, 1),
				new LojaDTO.ItemVendaDTO(999, 1)
			))));

		assertEquals(15, this.servico.consultarEstoque(123).estoque());
		assertEquals(8, this.servico.consultarEstoque(456).estoque());
	}

}
