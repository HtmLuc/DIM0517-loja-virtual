package br.ufrn.imd.loja.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.ufrn.imd.loja.service.exception.DadosInvalidosException;
import br.ufrn.imd.loja.service.exception.EstoqueInsuficienteException;
import br.ufrn.imd.loja.service.exception.ProdutoJaExisteException;
import br.ufrn.imd.loja.service.exception.ProdutoNaoEncontradoException;

@RestControllerAdvice
public class TratadorDeErros
{

	public record ErroResponse(int status, String erro)
	{
	}

	@ExceptionHandler(DadosInvalidosException.class)
	public ResponseEntity<ErroResponse> dadosInvalidos(DadosInvalidosException e)
	{
		return erro(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException e)
	{
		return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou com campo de tipo inválido");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException e)
	{
		return erro(HttpStatus.BAD_REQUEST, "Valor inválido para '" + e.getName() + "'");
	}

	@ExceptionHandler(ProdutoNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> produtoNaoEncontrado(ProdutoNaoEncontradoException e)
	{
		return erro(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler(ProdutoJaExisteException.class)
	public ResponseEntity<ErroResponse> produtoJaExiste(ProdutoJaExisteException e)
	{
		return erro(HttpStatus.CONFLICT, e.getMessage());
	}

	@ExceptionHandler(EstoqueInsuficienteException.class)
	public ResponseEntity<ErroResponse> estoqueInsuficiente(final EstoqueInsuficienteException e) 
	{
		return erro(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	private static ResponseEntity<ErroResponse> erro(HttpStatus status, String mensagem)
	{
		return ResponseEntity.status(status).body(new ErroResponse(status.value(), mensagem));
	}

}
