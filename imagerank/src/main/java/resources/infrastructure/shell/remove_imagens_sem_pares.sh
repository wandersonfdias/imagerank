#!/bin/bash
diretorioBase="$PWD/base_completa"
diretorioImagens="imagens"
arquivoPares="$PWD/pares_originais/tag-classes-reduced.dat"
java -Dfile.encoding=UTF-8 -cp imagerank-1.0.0-SNAPSHOT.jar br.ufmg.dcc.imagerank.pairs.util.NonPairImageDelete $diretorioBase $diretorioImagens $arquivoPares