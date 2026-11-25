package br.ufrn.imd.loja.service;

import org.springframework.stereotype.Service;

import br.ufrn.imd.loja.dto.LojaDTO;
import br.ufrn.imd.loja.model.Produto;
import br.ufrn.imd.loja.repository.ProdutoRepository;
import br.ufrn.imd.loja.service.exception.ProdutoJaExisteException;

@Service 
public class LojaService
{
	private final ProdutoRepository repositorio;

	public LojaService(ProdutoRepository repositorio)
	{
		this.repositorio = repositorio;
	}

	public LojaDTO.ProdutoDTO cadastrarProduto(final LojaDTO.CriarProdutoDTO criarProdutoDTO) 
	{
		criarProdutoDTO.validar();
		if (this.repositorio.existe(criarProdutoDTO.codigo())) 
		{
			throw new ProdutoJaExisteException(criarProdutoDTO.codigo());
		}

		Produto p = criarProdutoDTO.toEntity();
		p = this.repositorio.salvar(p);
	
		return LojaDTO.ProdutoDTO.fromEntity(p);
	}

}
