package br.ufrn.imd.loja.service;

import br.ufrn.imd.loja.repository.ProdutoRepository;

public class LojaService
{
	private final ProdutoRepository repositorio;

	public LojaService(ProdutoRepository repositorio)
	{
		this.repositorio = repositorio;
	}

}
