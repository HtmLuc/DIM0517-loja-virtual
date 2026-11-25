package br.ufrn.imd.loja.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.ufrn.imd.loja.model.Produto;

public class ProdutoRepositoryImpl implements ProdutoRepository
{

	private final Map<Integer, Produto> produtos = new HashMap<>();

	@Override
	public Optional<Produto> buscar(int codigo)
	{
		return Optional.ofNullable(produtos.get(codigo));
	}

	@Override
	public boolean existe(int codigo)
	{
		return produtos.containsKey(codigo);
	}

	@Override
	public Produto salvar(Produto produto)
	{
		produtos.put(produto.getCodigo(), produto);
		return produtos.get(produto.getCodigo());
	}

}
