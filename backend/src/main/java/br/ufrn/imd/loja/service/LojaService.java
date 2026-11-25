package br.ufrn.imd.loja.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import br.ufrn.imd.loja.dto.LojaDTO;
import br.ufrn.imd.loja.model.Produto;
import br.ufrn.imd.loja.repository.ProdutoRepository;
import br.ufrn.imd.loja.service.exception.ProdutoJaExisteException;

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

}
