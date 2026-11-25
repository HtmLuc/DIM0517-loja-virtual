from decimal import Decimal
from textual.app import App, ComposeResult
from textual.containers import Container, Horizontal, VerticalScroll
from textual.screen import ModalScreen
from textual.widgets import Button, Header, Input, Label, Static

from .loja_cliente import LojaCliente

class ModalGenerico(ModalScreen):
    """Tela modal de diálogo base para formulários do sistema."""

    def __init__(self, titulo: str):
        super().__init__()
        self.titulo = titulo

    def compose(self) -> ComposeResult:
        pass


class ModalCadastrarProduto(ModalScreen[None]):
    def __init__(self, cliente: LojaCliente):
        super().__init__()
        self.cliente = cliente

    def compose(self) -> ComposeResult:
        yield Container(
            Label("Cadastrar Produto", id="modal-title"),
            Input(placeholder="Código (número inteiro)", id="input-codigo", type="integer"),
            Input(placeholder="Preço (ex: 49.90)", id="input-preco", type="number"),
            Label("", id="msg-erro"),
            Horizontal(
                Button("Salvar", variant="success", id="btn-salvar"),
                Button("Cancelar", variant="error", id="btn-cancelar"),
                classes="button-row"
            ),
            id="modal-dialog"
        )

    def on_button_pressed(self, event: Button.Pressed) -> None:
        if event.button.id == "btn-cancelar":
            self.dismiss()
            return

        codigo_raw = self.query_one("#input-codigo", Input).value.strip()
        preco_raw = self.query_one("#input-preco", Input).value.strip()
        lbl_erro = self.query_one("#msg-erro", Label)

        if not codigo_raw or not preco_raw:
            lbl_erro.update("[red]Preencha todos os campos![/red]")
            return

        try:
            codigo = int(codigo_raw)
            preco = float(preco_raw)
        except ValueError:
            lbl_erro.update("[red]Código e preço devem ser números válidos![/red]")
            return

        resp = self.cliente.post("/produto", {"codigo": codigo, "preco": preco, "estoque": 0})
        if resp.sucesso:
            self.app.notify("Produto cadastrado com sucesso!", title="Sucesso")
            self.dismiss()
        else:
            lbl_erro.update(f"[red]Erro: {resp.mensagem_de_erro()}[/red]")


class ModalConsultarEstoque(ModalScreen[None]):
    def __init__(self, cliente: LojaCliente):
        super().__init__()
        self.cliente = cliente

    def compose(self) -> ComposeResult:
        yield Container(
            Label("Consultar Estoque", id="modal-title"),
            Input(placeholder="Código do Produto", id="input-codigo", type="integer"),
            Static("", id="resultado-estoque"),
            Horizontal(
                Button("Buscar", variant="primary", id="btn-buscar"),
                Button("Fechar", id="btn-fechar"),
                classes="button-row"
            ),
            id="modal-dialog"
        )

    def on_button_pressed(self, event: Button.Pressed) -> None:
        if event.button.id == "btn-fechar":
            self.dismiss()
            return

        codigo_raw = self.query_one("#input-codigo", Input).value.strip()
        res_box = self.query_one("#resultado-estoque", Static)

        if not codigo_raw:
            res_box.update("[red]Informe um código valido.[/red]")
            return

        resp = self.cliente.get(f"/produto/{codigo_raw}")
        if resp.sucesso:
            estoque = resp.corpo.get("estoque", 0)
            res_box.update(f"[green]Estoque Atual:[/green] [bold]{estoque}[/bold] unidades")
        else:
            res_box.update(f"[red]Erro: {resp.mensagem_de_erro()}[/red]")


class ModalAjustarEstoque(ModalScreen[None]):
    def __init__(self, cliente: LojaCliente, tipo: str):
        super().__init__()
        self.cliente = cliente
        self.tipo = tipo  

    def compose(self) -> ComposeResult:
        titulo = "Entrada de Estoque" if self.tipo == "entrada" else "Saída de Estoque"
        yield Container(
            Label(titulo, id="modal-title"),
            Input(placeholder="Código do Produto", id="input-codigo", type="integer"),
            Input(placeholder="Quantidade (inteiro)", id="input-qtd", type="integer"),
            Label("", id="msg-erro"),
            Horizontal(
                Button("Confirmar", variant="success", id="btn-salvar"),
                Button("Cancelar", variant="error", id="btn-cancelar"),
                classes="button-row"
            ),
            id="modal-dialog"
        )

    def on_button_pressed(self, event: Button.Pressed) -> None:
        if event.button.id == "btn-cancelar":
            self.dismiss()
            return

        codigo_raw = self.query_one("#input-codigo", Input).value.strip()
        qtd_raw = self.query_one("#input-qtd", Input).value.strip()
        lbl_erro = self.query_one("#msg-erro", Label)

        try:
            codigo = int(codigo_raw)
            qtd = int(qtd_raw)
            if qtd <= 0:
                raise ValueError()
        except ValueError:
            lbl_erro.update("[red]Informe código e quantidade válidos (> 0).[/red]")
            return

        caminho = f"/produto/{codigo}/{self.tipo}"
        resp = self.cliente.put(caminho, {"quantidade": qtd})

        if resp.sucesso:
            self.app.notify(f"{self.tipo.capitalize()} realizada com sucesso!", title="Sucesso")
            self.dismiss()
        else:
            lbl_erro.update(f"[red]Erro: {resp.mensagem_de_erro()}[/red]")


class ModalVenda(ModalScreen[None]):
    def __init__(self, cliente: LojaCliente):
        super().__init__()
        self.cliente = cliente
        self.itens: list[dict] = []

    def compose(self) -> ComposeResult:
        yield Container(
            Label("Registrar Venda (Atômica)", id="modal-title"),
            Horizontal(
                Input(placeholder="Código", id="input-item-codigo", type="integer"),
                Input(placeholder="Qtd", id="input-item-qtd", type="integer"),
                Button("Adicionar", id="btn-add-item", variant="primary")
            ),
            Static("Itens da venda:\nNenhum item adicionado.", id="lista-itens"),
            Label("", id="msg-erro"),
            Horizontal(
                Button("Finalizar Venda", variant="success", id="btn-finalizar"),
                Button("Cancelar", variant="error", id="btn-cancelar"),
                classes="button-row"
            ),
            id="modal-dialog"
        )

    def on_button_pressed(self, event: Button.Pressed) -> None:
        lbl_erro = self.query_one("#msg-erro", Label)

        if event.button.id == "btn-cancelar":
            self.dismiss()
            return

        if event.button.id == "btn-add-item":
            cod_raw = self.query_one("#input-item-codigo", Input).value.strip()
            qtd_raw = self.query_one("#input-item-qtd", Input).value.strip()

            try:
                codigo = int(cod_raw)
                qtd = int(qtd_raw)
                if qtd <= 0:
                    raise ValueError()
                self.itens.append({"codigo": codigo, "quantidade": qtd})
                self._atualizar_lista()
                self.query_one("#input-item-codigo", Input).value = ""
                self.query_one("#input-item-qtd", Input).value = ""
                lbl_erro.update("")
            except ValueError:
                lbl_erro.update("[red]Valores inválidos para o item.[/red]")

        elif event.button.id == "btn-finalizar":
            if not self.itens:
                lbl_erro.update("[red]Adicione pelo menos um item à venda.[/red]")
                return

            resp = self.cliente.post("/venda", {"itens": self.itens})
            if resp.sucesso:
                total = resp.corpo.get("total", "0.00")
                self.app.notify(f"Venda efetuada! Total: R$ {total}", title="Sucesso", timeout=5)
                self.dismiss()
            else:
                lbl_erro.update(f"[red]Erro na Venda: {resp.mensagem_de_erro()}[/red]")

    def _atualizar_lista(self) -> None:
        box = self.query_one("#lista-itens", Static)
        texto = "Itens da venda:\n" + "\n".join(
            [f"• Produto {i['codigo']} — Quantidade: {i['quantidade']}" for i in self.itens]
        )
        box.update(texto)


class LojaTUIApp(App):
    """Aplicação Principal TUI para Gerenciamento de Loja."""

    CSS = """
    Screen {
        align: center middle;
    }
    
    #main-container {
        width: 60;
        height: 22;
        border: heavy $accent;
        padding: 1 2;
        background: $surface;
    }

    .menu-btn {
        width: 100%;
        margin-bottom: 1;
    }

    #modal-dialog {
        width: 60;
        height: auto;
        border: thick $primary;
        background: $surface;
        padding: 1 2;
    }

    #modal-title {
        text-align: center;
        text-style: bold;
        margin-bottom: 1;
    }

    .button-row {
        margin-top: 1;
        align: center middle;
    }

    .button-row Button {
        margin: 0 1;
    }

    #msg-erro {
        margin-top: 1;
    }
    """

    def __init__(self, url_base: str = "http://localhost:8080/api/v1/loja"):
        super().__init__()
        self.cliente = LojaCliente(url_base)

    def compose(self) -> ComposeResult:
        yield Header(show_clock=True)
        yield Container(
            Label("=== PAINEL DE CONTROLE DA LOJA ===", id="modal-title"),
            Button("1. Cadastrar Produto", id="btn-cadastrar", classes="menu-btn"),
            Button("2. Consultar Estoque", id="btn-consultar", classes="menu-btn"),
            Button("3. Entrada de Estoque", id="btn-entrada", classes="menu-btn"),
            Button("4. Saída de Estoque", id="btn-saida", classes="menu-btn"),
            Button("5. Realizar Venda", id="btn-venda", classes="menu-btn"),
            Button("Sair", id="btn-sair", variant="error", classes="menu-btn"),
            id="main-container"
        )

    def on_button_pressed(self, event: Button.Pressed) -> None:
        btn_id = event.button.id
        if btn_id == "btn-cadastrar":
            self.push_screen(ModalCadastrarProduto(self.cliente))
        elif btn_id == "btn-consultar":
            self.push_screen(ModalConsultarEstoque(self.cliente))
        elif btn_id == "btn-entrada":
            self.push_screen(ModalAjustarEstoque(self.cliente, "entrada"))
        elif btn_id == "btn-saida":
            self.push_screen(ModalAjustarEstoque(self.cliente, "saida"))
        elif btn_id == "btn-venda":
            self.push_screen(ModalVenda(self.cliente))
        elif btn_id == "btn-sair":
            self.exit()

def main():
    import sys
    url = "http://localhost:8080/api/v1/loja"
    app = LojaTUIApp(url_base=url)
    app.run()

if __name__ == "__main__":
    main()
