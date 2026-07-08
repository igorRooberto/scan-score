<p align="center">
  <img src="./assets/Logo.png" alt="ScanScore Logo" width="250"/>
</p>

# ScanScore

> **SCAN. GRADE. DELIVER RESULTS.**

### 📝 Sobre o Projeto

O **ScanScore** é uma aplicação baseada em visão computacional projetada para automatizar a correção de provas de múltipla escolha (gabaritos), reduzindo drasticamente o tempo de trabalho manual de educadores.

### 🎯 Motivação e Impacto Social

Professores, especialmente na rede pública de ensino, perdem dezenas de horas valiosas todos os meses corrigindo provas à mão. O ScanScore nasceu para devolver esse tempo aos educadores, utilizando o Reconhecimento Óptico de Marcas (OMR) para escanear e tabular notas em segundos, permitindo que o foco principal volte a ser o ensino e o desenvolvimento dos alunos.

### 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java
- **Framework Principal:** Spring Boot (Spring Web, Spring Boot DevTools)
- **Visão Computacional:** OpenCV
- **Utilitários:** Project Lombok

## 🧠 Como Funciona o Motor de Visão Computacional (OMR)

Para construir o núcleo de processamento de imagens do ScanScore, optei por não utilizar modelos pesados de Inteligência Artificial. Em vez disso, a escolha foi focar em **Análise Geométrica e Heurísticas** puras com a ajuda do OpenCV. Isso manteve o sistema extremamente rápido, previsível e leve.

A lógica desenvolvida para detectar as bolinhas funciona em três etapas principais:

### 1. Pré-processamento (A Binarização)
Primeiro, a foto original da prova é convertida para tons de cinza. Em seguida, aplica-se uma binarização invertida (utilizando `THRESH_BINARY_INV`). O objetivo aqui foi transformar o papel branco num fundo preto absoluto, deixando apenas a tinta da caneta brilhando em branco. Isso facilita a manipulação matemática da imagem nas etapas seguintes.

### 2. A Busca pelas Formas (`findContours`)
Com a imagem já em preto e branco, utiliza-se a função `findContours` do OpenCV. Ela varre a imagem procurando qualquer "ilha" contínua de pixels brancos. O desafio nesta etapa é que a biblioteca mapeia absolutamente tudo: as letras do cabeçalho, riscos tortos, a margem do papel e, no meio disso, as bolinhas que realmente precisamos.

### 3. A "Peneira" de Validação (Filtro Heurístico)
Aqui entra a lógica principal do motor. Para ensinar o sistema a separar o que é "ruído" do que é realmente uma marcação de gabarito, traça-se um retângulo imaginário (`boundingRect`) ao redor de cada forma encontrada e aplicam-se três regras lógicas bem rigorosas:

* 📐 **A Regra da Proporção (Aspect Ratio):** Dividindo a largura da forma pela sua altura, exige-se que o resultado fique entre `0.8` e `1.2`. Como um círculo perfeito cabe examente dentro de um quadrado, valores fora dessa margem (como letras esticadas ou linhas retas) são imediatamente descartados.
* 🔎 **Filtro Anti-Poeira:** Foi definido que a mancha precisa ter pelo menos `15x15` pixels. Assim, evita-se que o programa confunda um pingo acidental de tinta ou uma poeira no scanner com uma marcação feita pelo aluno.
* 🛑 **Filtro Anti-Gigantes:** O tamanho máximo foi limitado a `80x80` pixels. Se uma forma for maior que isso, significa que o sistema provavelmente esbarrou numa caixa preta do cabeçalho ou capturou a borda escura da mesa onde a foto foi tirada.

Se uma mancha passar nestes três testes simultaneamente, ela é classificada oficialmente como uma bolinha válida. Só então as suas coordenadas (X, Y) são guardadas para avançar para a próxima fase do sistema: olhar para dentro dessa coordenada, verificar se está preenchida com tinta e calcular a nota do aluno.

### 4. Estruturação do Gabarito (Organização em Grid)

As bolinhas validadas na etapa anterior são entregues de forma caótica pelo OpenCV. Para transformar essa "nuvem" de coordenadas em um gabarito estruturado, o motor executa um algoritmo de ordenação espacial:

* ⬇️ **Ordenação Vertical (Eixo Y):** Todas as bolinhas são ordenadas de cima para baixo. Isso garante que a leitura comece pela primeira questão da folha.

* 📏 **Agrupamento por Linha (Tolerância Y):** Como a foto do papel nunca é 100% reta, o algoritmo utiliza uma margem de segurança (ex: `15 pixels`). Se a altura (Y) da próxima bolinha for próxima à da anterior, ela é agrupada na mesma linha (mesma questão). Se a diferença for grande, o sistema entende que houve uma quebra de linha.

* ➡️ **Ordenação Horizontal (Eixo X):** Com as bolinhas agrupadas em uma questão, elas são ordenadas da esquerda para a direita. Assim, garante-se que o índice `0` será sempre a alternativa 'A', o `1` a 'B', e assim por diante.

O resultado final desta etapa é uma matriz perfeitamente estruturada (Um Mapa de Questões para Alternativas), pronta para a etapa final: analisar o preenchimento de tinta dentro de cada coordenada e calcular a nota.

### 5. Detecção de Tinta (Análise de Preenchimento)

Com o gabarito perfeitamente estruturado, o motor precisa descobrir quais bolinhas foram efetivamente pintadas pelo aluno. Para manter a performance sem usar redes neuronais lentas, o sistema aplica matemática de imagem rápida e direta:

* ✂️ **Recorte (Region of Interest):** Para cada bolinha, as suas coordenadas são usadas para extrair um "recorte" exato (`Rect`) daquela pequena região na imagem binarizada do Dia 1.
* 🧮 **Contagem de Pixels (Tinta):** Como a imagem está invertida (tinta = branco), uma função nativa (`Core.countNonZero`) conta exatamente quantos pixels de tinta existem dentro do recorte.
* ⚖️ **O Limiar de Aceitação (Threshold):** Calcula-se a percentagem de preenchimento dividindo os pixels de tinta pela área total do retângulo. Se a bolinha tiver **mais de 40%** de preenchimento, o sistema considera-a marcada.
* 👨‍🏫 **Regras de Negócio (O Juiz):** O motor previne fraudes e esquecimentos aplicando três regras finais a cada questão:
  * **Em Branco (`-`):** O aluno não atingiu o limiar de 40% em nenhuma das alternativas.
  * **Anulada/Rasurada (`*`):** O aluno atingiu o limiar em 2 ou mais alternativas (tentativa de marcar mais de uma ou erro ao apagar).
  * **Resposta Válida (`A`, `B`, `C`...):** Apenas uma única alternativa passou no limiar de tinta.

No final desta etapa, toda a complexidade visual do OpenCV é descartada, e o sistema entrega um dicionário de dados limpo e ordenado (ex: `{1='A', 2='C', 3='-', 4='*'}`), pronto para o Motor de Correção calcular a nota final do aluno.
