package br.ufrn.imd.loja.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.ufrn.imd.loja.dto.LojaDTO;
import br.ufrn.imd.loja.model.Produto;
import br.ufrn.imd.loja.repository.ProdutoRepository;
import br.ufrn.imd.loja.service.exception.DadosInvalidosException;
import br.ufrn.imd.loja.service.exception.EstoqueInsuficienteException;
import br.ufrn.imd.loja.service.exception.ProdutoJaExisteException;
import br.ufrn.imd.loja.service.exception.ProdutoNaoEncontradoException;

@Service 
public class LojaService
{
	private static final Logger log = LoggerFactory.getLogger(LojaService.class);
	private final ProdutoRepository repositorio;

	public LojaService(ProdutoRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	public LojaDTO.ProdutoDTO cadastrarProduto(final LojaDTO.CriarProdutoDTO criarProdutoDTO) 
	{
		log.info("Criando produto com DTO: {}", criarProdutoDTO.toString());
		criarProdutoDTO.validar();
		if (this.repositorio.existe(criarProdutoDTO.codigo())) 
		{
			throw new ProdutoJaExisteException(criarProdutoDTO.codigo());
		}

		final var produto = this.repositorio.salvar(criarProdutoDTO.toEntity());
	
		final var resposta = LojaDTO.ProdutoDTO.fromEntity(produto);

		log.info("Produto criado com sucesso: {}", resposta.toString());

		return resposta;
	}

	public LojaDTO.ProdutoEstoqueDTO consultarEstoque(final int codigo) 
	{
		log.info("Consultando estoque do produto com código {}", codigo);

		final Produto produto = this.repositorio.buscar(codigo)
			.orElseThrow(() -> new ProdutoNaoEncontradoException(codigo));

		final var resposta = LojaDTO.ProdutoEstoqueDTO.fromEntity(produto);

		log.info("Estoque consultado com sucesso: {}", resposta);

		return resposta;
	}

	public LojaDTO.ProdutoEstoqueDTO retirarDoEstoque(
		final int codigo, final LojaDTO.RetirarEstoqueDTO retirarEstoqueDTO
	) 
	{
		retirarEstoqueDTO.validar();

		log.info("Saída de estoque de {} unidades para o produto com código {}", 
			retirarEstoqueDTO.quantidade(), codigo);

		final Produto produto = this.repositorio.buscar(codigo)
			.orElseThrow(() -> new ProdutoNaoEncontradoException(codigo));

		validarSaidaDoEstoque(produto, retirarEstoqueDTO.quantidade());

		int novoEstoque = produto.getEstoque() - retirarEstoqueDTO.quantidade();
		produto.setEstoque(novoEstoque);

		final Produto atualizado = this.repositorio.salvar(produto);
		final var resposta = LojaDTO.ProdutoEstoqueDTO.fromEntity(atualizado);

		log.info("Produto atualizado com sucesso: {}", resposta);

		return resposta;
	}

	public LojaDTO.ProdutoEstoqueDTO adicionarAoEstoque(
		final int codigo, final LojaDTO.RetirarEstoqueDTO entradaEstoqueDTO
	)
	{
		validarEntradaNoEstoque(entradaEstoqueDTO);

		log.info("Entrada de estoque de {} unidades para o produto com código {}",
			entradaEstoqueDTO.quantidade(), codigo);

		final Produto produto = this.repositorio.buscar(codigo)
			.orElseThrow(() -> new ProdutoNaoEncontradoException(codigo));

		int novoEstoque = produto.getEstoque() + entradaEstoqueDTO.quantidade();
		produto.setEstoque(novoEstoque);

		final Produto atualizado = this.repositorio.salvar(produto);
		final var resposta = LojaDTO.ProdutoEstoqueDTO.fromEntity(atualizado);

		log.info("Produto atualizado com sucesso: {}", resposta);

		return resposta;
	}

	public LojaDTO.VendaRespostaDTO realizarVenda(final LojaDTO.VendaDTO vendaDTO) 
	{
		log.info("Realizando venda: {}", vendaDTO);
		vendaDTO.validar();

		// Soma as quantidades por produto (o mesmo produto pode aparecer mais de uma vez)
		final Map<Integer, Integer> quantidadePorCodigo = new HashMap<>();
		for (final LojaDTO.ItemVendaDTO item : vendaDTO.itens()) 
		{
			quantidadePorCodigo.merge(item.codigo(), item.quantidade(), Integer::sum);
		}

		// Valida todos os itens antes de alterar qualquer estoque (venda atômica)
		final Map<Integer, Produto> produtos = new HashMap<>();
		for (final int codigo : quantidadePorCodigo.keySet()) 
		{
			final Produto produto = this.repositorio.buscar(codigo)
				.orElseThrow(() -> new ProdutoNaoEncontradoException(codigo));
			produtos.put(codigo, produto);
		}
		for (final Map.Entry<Integer, Integer> entrada : quantidadePorCodigo.entrySet()) 
		{
			validarSaidaDoEstoque(produtos.get(entrada.getKey()), entrada.getValue());
		}

		// Todos os itens são válidos: agora altera o estoque
		for (final Map.Entry<Integer, Integer> entrada : quantidadePorCodigo.entrySet()) 
		{
			final Produto produto = produtos.get(entrada.getKey());
			produto.setEstoque(produto.getEstoque() - entrada.getValue());
			this.repositorio.salvar(produto);
		}

		BigDecimal total = BigDecimal.ZERO;
		final List<LojaDTO.ItemVendidoDTO> itensVendidos = new ArrayList<>();
		for (final LojaDTO.ItemVendaDTO item : vendaDTO.itens()) 
		{
			final Produto produto = produtos.get(item.codigo());
			final BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.quantidade()));
			total = total.add(subtotal);
			itensVendidos.add(new LojaDTO.ItemVendidoDTO(
				produto.getCodigo(), item.quantidade(), subtotal, produto.getEstoque()
			));
		}

		final var resposta = new LojaDTO.VendaRespostaDTO(total, itensVendidos);

		log.info("Venda realizada com sucesso: {}", resposta);

		return resposta;
	}

	private void validarSaidaDoEstoque(final Produto produto, final int quantidade)
	{
    if (quantidade < 0) 
    {
      throw new DadosInvalidosException("Quantidade deve ser positiva.");
    }

		if (produto.getEstoque() < quantidade) 
		{
			throw new EstoqueInsuficienteException("Não foi possível realizar a saída de estoque. " 
				+ "A quantidade a ser retirada supera a armazenada."
			);
		}
	}

	private void validarEntradaNoEstoque(final LojaDTO.RetirarEstoqueDTO entradaEstoqueDTO)
	{
		if (entradaEstoqueDTO == null)
		{
			throw new DadosInvalidosException("Quantidade é obrigatória.");
		}

		if (entradaEstoqueDTO.quantidade() <= 0)
		{
			throw new DadosInvalidosException("Quantidade deve ser positiva.");
		}
	}

}
