package br.ufrn.imd.loja.service.exception;

public class ProdutoNaoEncontradoException extends RuntimeException
{

	public ProdutoNaoEncontradoException(int codigo)
	{
		super("Produto " + codigo + " não encontrado");
	}

}
