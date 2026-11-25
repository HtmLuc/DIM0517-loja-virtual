package br.ufrn.imd.loja.repository;

import java.util.Optional;

import br.ufrn.imd.loja.model.Produto;

public interface ProdutoRepository
{
	Optional<Produto> buscar(int codigo);

	boolean existe(int codigo);

	void salvar(Produto produto);

}
