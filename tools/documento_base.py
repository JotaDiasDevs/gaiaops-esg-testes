# -*- coding: utf-8 -*-
"""
GaiaOps ESG - motor de formatacao dos documentos de entrega (python-docx).

Blocos prontos para montar um documento: capa, titulos, paragrafos com
**negrito**, *italico* e `codigo`, listas, tabelas, listagens de codigo, notas
e prints (com quadro de "print pendente" quando a imagem ainda nao existe).
O conteudo de cada documento fica em tools/gerar_documento.py.
"""

import os
import sys

try:
    from docx import Document
    from docx.enum.table import WD_TABLE_ALIGNMENT
    from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
    from docx.oxml import OxmlElement
    from docx.oxml.ns import qn
    from docx.shared import Cm, Pt, RGBColor
except ImportError:
    print("FALTA A BIBLIOTECA python-docx. Instale com:  py -m pip install python-docx")
    sys.exit(1)

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DIR_PRINTS = os.path.join(RAIZ, "docs", "prints")

# Paleta (a mesma das entregas anteriores)
VERDE_ESCURO = RGBColor(0x1B, 0x4D, 0x3E)
VERDE_MEDIO = RGBColor(0x2D, 0x6A, 0x4F)
CINZA_TEXTO = RGBColor(0x33, 0x33, 0x33)
CINZA_CODIGO = "F5F5F5"
VERDE_CLARO = "E8F3EE"
LARANJA_FUNDO = "FFF4E5"
LARANJA_BORDA = "E67E22"


def caminho_print(nome):
    for ext in (".png", ".jpg", ".jpeg"):
        caminho = os.path.join(DIR_PRINTS, nome + ext)
        if os.path.exists(caminho):
            return caminho
    return None


def existe_print(nome):
    return caminho_print(nome) is not None


# =============================================================================
# HELPERS DE FORMATACAO
# =============================================================================

def _sombrear(elemento_pr, cor_hex):
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), cor_hex)
    elemento_pr.append(shd)


def _borda_paragrafo(paragrafo, cor, tamanho=6):
    pPr = paragrafo._p.get_or_add_pPr()
    pBdr = OxmlElement("w:pBdr")
    for lado in ("top", "bottom", "left", "right"):
        el = OxmlElement("w:" + lado)
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), str(tamanho))
        el.set(qn("w:space"), "6")
        el.set(qn("w:color"), cor)
        pBdr.append(el)
    pPr.append(pBdr)


class Documento(object):

    def __init__(self, rodape):
        self.doc = Document()
        secao = self.doc.sections[0]
        secao.page_height, secao.page_width = Cm(29.7), Cm(21.0)
        secao.left_margin = secao.right_margin = Cm(2.2)
        secao.top_margin = secao.bottom_margin = Cm(2.0)

        normal = self.doc.styles["Normal"]
        normal.font.name = "Calibri"
        normal.font.size = Pt(11)
        normal.font.color.rgb = CINZA_TEXTO
        normal.paragraph_format.space_after = Pt(6)
        normal.paragraph_format.line_spacing = 1.15

        for nivel, tamanho, cor in ((1, 18, VERDE_ESCURO), (2, 13.5, VERDE_MEDIO), (3, 12, VERDE_MEDIO)):
            estilo = self.doc.styles["Heading %d" % nivel]
            estilo.font.name = "Calibri"
            estilo.font.size = Pt(tamanho)
            estilo.font.bold = True
            estilo.font.color.rgb = cor
            estilo.paragraph_format.space_before = Pt(14 if nivel == 1 else 10)
            estilo.paragraph_format.space_after = Pt(6)

        self._rodape(rodape)

    def _rodape(self, texto):
        p = self.doc.sections[0].footer.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(texto + " · página ")
        r.font.size = Pt(8)
        r.font.color.rgb = RGBColor(0x88, 0x88, 0x88)
        for tipo, texto in (("begin", None), (None, "PAGE"), ("end", None)):
            run = p.add_run()
            run.font.size = Pt(8)
            if tipo:
                fld = OxmlElement("w:fldChar")
                fld.set(qn("w:fldCharType"), tipo)
                run._r.append(fld)
            else:
                instr = OxmlElement("w:instrText")
                instr.set(qn("xml:space"), "preserve")
                instr.text = texto
                run._r.append(instr)

    # --- blocos -----------------------------------------------------------------

    def quebra_pagina(self):
        self.doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)

    def h1(self, texto, nova_pagina=True):
        if nova_pagina:
            self.quebra_pagina()
        self.doc.add_heading(texto, level=1)

    def h2(self, texto):
        self.doc.add_heading(texto, level=2)

    def h3(self, texto):
        self.doc.add_heading(texto, level=3)

    def _texto_rico(self, paragrafo, texto):
        """Suporta **negrito**, *italico* e `codigo` inline (sem aninhamento)."""
        partes = []
        buffer, modo, i = "", None, 0
        while i < len(texto):
            if texto.startswith("**", i) and modo in (None, "b"):
                partes.append((buffer, modo))
                buffer, modo = "", (None if modo == "b" else "b")
                i += 2
                continue
            if texto[i] == "*" and modo in (None, "i"):
                partes.append((buffer, modo))
                buffer, modo = "", (None if modo == "i" else "i")
                i += 1
                continue
            if texto[i] == "`" and modo in (None, "c"):
                partes.append((buffer, modo))
                buffer, modo = "", (None if modo == "c" else "c")
                i += 1
                continue
            buffer += texto[i]
            i += 1
        partes.append((buffer, modo))
        for conteudo, estilo in partes:
            if not conteudo:
                continue
            run = paragrafo.add_run(conteudo)
            if estilo == "b":
                run.bold = True
            elif estilo == "i":
                run.italic = True
            elif estilo == "c":
                run.font.name = "Consolas"
                run.font.size = Pt(9.5)
                run.font.color.rgb = VERDE_ESCURO

    def par(self, texto):
        p = self.doc.add_paragraph()
        p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        self._texto_rico(p, texto)
        return p

    def bullets(self, itens):
        for item in itens:
            p = self.doc.add_paragraph(style="List Bullet")
            self._texto_rico(p, item)

    def numerados(self, itens):
        for item in itens:
            p = self.doc.add_paragraph(style="List Number")
            self._texto_rico(p, item)

    def codigo(self, texto, legenda=None):
        if legenda:
            p = self.doc.add_paragraph()
            r = p.add_run(legenda)
            r.italic = True
            r.font.size = Pt(9)
            p.paragraph_format.space_after = Pt(2)
        tabela = self.doc.add_table(rows=1, cols=1)
        tabela.alignment = WD_TABLE_ALIGNMENT.CENTER
        celula = tabela.rows[0].cells[0]
        _sombrear(celula._tc.get_or_add_tcPr(), CINZA_CODIGO)
        celula.paragraphs[0].text = ""
        for i, linha in enumerate(texto.splitlines()):
            p = celula.paragraphs[0] if i == 0 else celula.add_paragraph()
            p.paragraph_format.space_after = Pt(0)
            p.paragraph_format.line_spacing = 1.0
            r = p.add_run(linha if linha else " ")
            r.font.name = "Consolas"
            r.font.size = Pt(8.5)
        self.doc.add_paragraph().paragraph_format.space_after = Pt(2)

    def nota(self, texto):
        p = self.doc.add_paragraph()
        _sombrear(p._p.get_or_add_pPr(), VERDE_CLARO)
        _borda_paragrafo(p, "2D6A4F", 4)
        self._texto_rico(p, texto)

    def tabela(self, cabecalho, linhas, larguras=None):
        t = self.doc.add_table(rows=1, cols=len(cabecalho))
        t.style = "Table Grid"
        t.alignment = WD_TABLE_ALIGNMENT.CENTER
        for i, titulo in enumerate(cabecalho):
            celula = t.rows[0].cells[i]
            _sombrear(celula._tc.get_or_add_tcPr(), "1B4D3E")
            celula.paragraphs[0].text = ""
            r = celula.paragraphs[0].add_run(titulo)
            r.bold = True
            r.font.size = Pt(9.5)
            r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        for linha in linhas:
            celulas = t.add_row().cells
            for i, valor in enumerate(linha):
                celulas[i].paragraphs[0].text = ""
                self._texto_rico(celulas[i].paragraphs[0], str(valor))
                for r in celulas[i].paragraphs[0].runs:
                    r.font.size = Pt(9.5)
        if larguras:
            # O LibreOffice le a largura do <w:tblGrid> (colunas), o Word a da
            # celula: as duas sao definidas, e o ajuste automatico e desligado.
            t.autofit = False
            for i, largura in enumerate(larguras):
                t.columns[i].width = Cm(largura)
                for celula in t.columns[i].cells:
                    celula.width = Cm(largura)
        self.doc.add_paragraph().paragraph_format.space_after = Pt(2)

    def print_tela(self, nome, legenda, o_que_capturar):
        """Insere docs/prints/<nome>.png ou um quadro indicando o print pendente."""
        caminho = caminho_print(nome)
        if caminho:
            self.doc.add_picture(caminho, width=Cm(16.5))
            self.doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
        else:
            p = self.doc.add_paragraph()
            _sombrear(p._p.get_or_add_pPr(), LARANJA_FUNDO)
            _borda_paragrafo(p, LARANJA_BORDA, 12)
            r = p.add_run("PRINT PENDENTE: docs/prints/%s.png\n" % nome)
            r.bold = True
            r.font.color.rgb = RGBColor(0xB3, 0x5C, 0x00)
            p.add_run(o_que_capturar).font.size = Pt(10)
        p = self.doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(legenda)
        r.italic = True
        r.font.size = Pt(9)

    def salvar(self, caminho):
        self.doc.save(caminho)
