package br.ufrn.imd.loja.service;

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

}
